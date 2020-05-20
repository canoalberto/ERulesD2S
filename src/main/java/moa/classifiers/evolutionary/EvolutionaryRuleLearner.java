package moa.classifiers.evolutionary;

import java.util.List;

import com.github.javacliparser.FloatOption;
import com.github.javacliparser.IntOption;
import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.Instances;
import com.yahoo.labs.samoa.instances.InstancesHeader;

import moa.classifiers.AbstractClassifier;
import moa.classifiers.MultiClassClassifier;
import moa.core.Measurement;
import net.sf.jclec.IIndividual;
import net.sf.jclec.exprtree.ExprTree;
import net.sf.jclec.fitness.SimpleValueFitness;
import net.sf.jclec.problem.classification.blocks.AttributeValue;
import net.sf.jclec.problem.classification.evolutionarylearner.EvolutionaryRuleLearnerAlgorithm;
import net.sf.jclec.problem.classification.syntaxtree.SyntaxTreeRuleIndividual;

public class EvolutionaryRuleLearner extends AbstractClassifier implements MultiClassClassifier {

	private static final long serialVersionUID = 1L;

	public IntOption seed = new IntOption("seed", 'r', "Seed", 123456, 1, Integer.MAX_VALUE);

	public IntOption populationSize = new IntOption("populationSize", 'p', "Population size", 25, 2, 1000);

	public IntOption numberGenerations = new IntOption("numberGenerations", 'g', "Number generations", 50, 1, 1000);

	public IntOption numberRulesClass = new IntOption("numberRulesClass", 'n', "Number rules per class", 5, 1, 100);

	public IntOption numberWindows = new IntOption("numberWindows", 'w', "Number windows", 5, 1, 100);

	public IntOption windowSize = new IntOption("windowSize", 's', "Window size", 1000, 1, Integer.MAX_VALUE);

	public FloatOption fadingFactor = new FloatOption("fadingFactor", 'f', "Fading factor", 0.5, 0.0, 1.0);

	protected EvolutionaryRuleLearnerAlgorithm algorithm;

	protected Instances window;

	protected boolean model;

	@Override
	public void setModelContext(InstancesHeader context) {
		algorithm = new EvolutionaryRuleLearnerAlgorithm();
		algorithm.contextualize(context, seed.getValue(), populationSize.getValue(), numberGenerations.getValue(), numberRulesClass.getValue(), numberWindows.getValue(), fadingFactor.getValue());
	}

	@Override
	public void resetLearningImpl() {
	}

	@Override
	public double[] getVotesForInstance(Instance instance) {

		double[] votes = new double[instance.numClasses()];

		if(model)
		{
			List<IIndividual>[] rules = algorithm.getSolutions();

			for(int i = 0; i < instance.numClasses(); i++)
			{
				for(IIndividual rule : rules[i])
				{
					if((Boolean) ((SyntaxTreeRuleIndividual) rule).getPhenotype().covers(instance))
						votes[i] += ((SimpleValueFitness) rule.getFitness()).getValue();
				}
			}
		}

		return votes;
	}

	@Override
	protected Measurement[] getModelMeasurementsImpl() {
		Measurement[] measurements = null;

		if (algorithm != null) {
			List<IIndividual>[] rules = algorithm.getSolutions();

			measurements = new Measurement[3];
			int numberRules = 0;
			int numberNodes = 0;
			int numberConditionalClauses = 0;

			for(int i = 0; i < rules.length; i++)
			{
				for(IIndividual rule : rules[i])
				{
					ExprTree expr = ((SyntaxTreeRuleIndividual) rule).getPhenotype().getAntecedent();

					for(int j = 0; j < expr.size(); j++)
						if(expr.getBlock(j) instanceof AttributeValue)
							numberConditionalClauses++;

					numberNodes += expr.size();
					numberRules++;
				}
			}

			measurements[0] = new Measurement("NumberRules", numberRules);
			measurements[1] = new Measurement("NumberConditions", numberConditionalClauses);
			measurements[2] = new Measurement("NumberNodes", numberNodes);
		}

		return measurements;
	}

	@Override
	public void getModelDescription(StringBuilder out, int indent) {
	}

	public boolean isRandomizable() {
		return true;
	}

	@Override
	public void trainOnInstanceImpl(Instance instance) {

		if(window == null)
			window = new Instances(instance.dataset(), 0);

		window.add(instance);

		if(window.size() == windowSize.getValue())
		{
			algorithm.addChunkData(window);
			algorithm.prepare();
			algorithm.execute();
			model = true;

			window.delete();
		}
	}
}