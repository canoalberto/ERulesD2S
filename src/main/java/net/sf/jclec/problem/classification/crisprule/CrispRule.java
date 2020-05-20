package net.sf.jclec.problem.classification.crisprule;

import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.InstancesHeader;

import net.sf.jclec.exprtree.ExprTree;
import net.sf.jclec.problem.classification.base.Rule;
import net.sf.jclec.problem.classification.blocks.And;
import net.sf.jclec.problem.classification.blocks.AttributeValue;
import net.sf.jclec.problem.classification.blocks.Not;
import net.sf.jclec.problem.classification.blocks.Or;
import net.sf.jclec.problem.classification.blocks.RandomConstantOfContinuousValues;

/**
 * Implementation of a classification crisp rule.<p/>
 * 
 * The main method is classify() that checks if an instance can be classified by the rule.
 * If the rule covers an instance it returns the consequent as the class prediction.
 * Otherwise, it returns -1 indicating that the rule cannot classify the given instance.
 * 
 * The getConditions() method calculates the number of conditions of the rule as indicator of its length.
 * The toString() method shows a human-readable representation of the rule based on the metadata context (attributes names and values).
 * 
 * @author Sebastian Ventura
 * @author Amelia Zafra
 * @author Jose M. Luna 
 * @author Alberto Cano 
 * @author Juan Luis Olmo
 */

public class CrispRule extends Rule
{
	/////////////////////////////////////////////////////////////////
	// --------------------------------------- Serialization constant
	/////////////////////////////////////////////////////////////////

	/** Generated by Eclipse */
	
	private static final long serialVersionUID = -8174242256644010121L;

	/////////////////////////////////////////////////////////////////
	// ------------------------------------------------- Constructors
	/////////////////////////////////////////////////////////////////
	
	/**
	 * Empty constructor.
	 */
	
	public CrispRule()
	{
		super();
	}
	
	/**
	 * Constructor
	 * 
	 * @param antecedent the antecedent of the rule
	 */
	
	public CrispRule(ExprTree antecedent)
	{
		super(antecedent);
	}
	
	/////////////////////////////////////////////////////////////////
	// ----------------------------------------------- Public methods
	/////////////////////////////////////////////////////////////////
	
	/**
	 * Classify an instance
	 * 
	 * @param instance the instane
	 * @return the class predicted if the rule covers the instance, -1 otherwise
	 */
	
	@Override
	public double classify(Instance instance)
	{
		if((Boolean) covers(instance))
			return consequent;
		else
			return -1;
	}
	
	/**
	 * Implementation of copy()
	 * 
	 * {@inheritDoc}
	 */

	@Override
	public CrispRule copy()
	{
		CrispRule newRule = new CrispRule();
		
		newRule.setAntecedent(code.copy());
		newRule.setConsequent(consequent);
		if(fitness != null) newRule.setFitness(fitness);
		
		return newRule;
	}
	
	/**
     * Obtain the number of conditions of the rule
     * 
     * @return number of conditions
     */
	
	public int getConditions()
	{
		int count = 1;
		
		for(int j=0; j<code.size(); j++)
		{
			if(code.getBlock(j) instanceof And ||
			   code.getBlock(j) instanceof Or  )
				count++;
			
			if(code.getBlock(j) instanceof Not)
			{
				int nots = 1;
				
				for(int k = j+1; k < code.size() && code.getBlock(k) instanceof Not; k++)
					nots++;
				
				if(nots % 2 != 0)
					count++;
				
				j+=nots-1;
			}
		}
		
		return count;
	}
	
	/** 
	 *  Shows the complete rule antecedent and consequent
	 *  
	 *  @param metadata the metadata
	 *  @return the rule
	 */
	
	public String toString(InstancesHeader metadata)
	{
		StringBuffer sb = new StringBuffer("IF (");
		
		for(int j=0; j<code.size(); j++) 
		{
			if(code.getBlock(j) instanceof AttributeValue)
			{
				double value = Double.valueOf(code.getBlock(j+1).toString());
				sb.append(metadata.attribute(Integer.parseInt(code.getBlock(j).toString())).name() + " ");
				
				if(code.getBlock(j+1) instanceof RandomConstantOfContinuousValues)
					sb.append(value + " ");
				else
					sb.append(metadata.attribute(Integer.parseInt(code.getBlock(j).toString())).getAttributeValues().get((int) value) + " ");
				j++;
			}
			else
			{
				if(code.getBlock(j) instanceof Not)
				{
					int count = 1;
					
					for(int k = j+1; k < code.size() && code.getBlock(k) instanceof Not; k++)
						count++;
					
					if(count % 2 != 0)
						sb.append(code.getBlock(j).toString() + " ");
					
					j+=count-1;
				}
				else
					sb.append(code.getBlock(j).toString() + " ");
			}
		}
		sb.append(") THEN ("+metadata.classAttribute().name()+" = "+ metadata.classAttribute().getAttributeValues().get((int)consequent)+")");
		return sb.toString();
	}
}