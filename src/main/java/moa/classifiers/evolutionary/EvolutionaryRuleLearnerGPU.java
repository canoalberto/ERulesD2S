package moa.classifiers.evolutionary;

import com.yahoo.labs.samoa.instances.InstancesHeader;

import net.sf.jclec.problem.classification.evolutionarylearner.EvolutionaryRuleLearnerAlgorithmGPU;
import net.sf.jclec.problem.classification.evolutionarylearner.RuleEvaluatorGPU;

public class EvolutionaryRuleLearnerGPU extends EvolutionaryRuleLearner {

	private static final long serialVersionUID = 1L;
	
	@Override
	public void setModelContext(InstancesHeader context) {
		algorithm = new EvolutionaryRuleLearnerAlgorithmGPU();
		algorithm.contextualize(context, seed.getValue(), populationSize.getValue(), numberGenerations.getValue(), numberRulesClass.getValue(), numberWindows.getValue(), fadingFactor.getValue());
	}
	
	public void releaseGPU() {
		((RuleEvaluatorGPU) algorithm.getEvaluator()).releaseGPU();
	}
}