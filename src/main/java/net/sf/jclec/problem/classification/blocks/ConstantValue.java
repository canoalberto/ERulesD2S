package net.sf.jclec.problem.classification.blocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sf.jclec.exprtree.fun.AbstractPrimitive;
import net.sf.jclec.exprtree.fun.ExprTreeFunction;
import net.sf.jclec.exprtree.IPrimitive;

/**
 * Static constant value
 * 
 * @author Alberto Cano 
 * @author Amelia Zafra
 * @author Sebastian Ventura
 * @author Jose M. Luna 
 * @author Juan Luis Olmo
 */

public class ConstantValue extends AbstractPrimitive
{
	/////////////////////////////////////////////////////////////////
	// --------------------------------------------------- Properties
	/////////////////////////////////////////////////////////////////

	private static final long serialVersionUID = 4000968954325988985L;
	
	/** Constant value */
	
	private double value;
	
	/////////////////////////////////////////////////////////////////
	// ----------------------------------------------- Public methods
	/////////////////////////////////////////////////////////////////
	
	public ConstantValue(Class<?>[] atypes, Class<?> rtype) {
		super(new Class<?> [] {}, rtype);
	}

	/**
	 * Gets value
	 * 
	 * @return value constant value
	 * 
	 */
	
	public double getValue() 
	{
		return value;
	}

	/**
	 * Sets value
	 * 
	 * @param value constant value
	 * 
	 */
	
	public void setValue(double value) 
	{
		this.value = value;
	}
	
	/**
	 * Execute this operation over the stack and context
	 * 
	 * @param context the ExprTreeFunction context
	 */

	public void evaluate(ExprTreeFunction context) 
	{
		super.push(context,value);
	}
	
	/**
	 * Default implementation of copy() return this.
	 * 
	 * {@inheritDoc}
	 */
	
	public IPrimitive copy() 
	{
		return this;
	}

	/**
	 * Default implementation of instance() return this.
	 * 
	 * {@inheritDoc}
	 */
	
	public IPrimitive instance() 
	{
		return this;
	}
	
	/////////////////////////////////////////////////////////////////
	// ------------------------- Overwriting java.lang.Object methods
	/////////////////////////////////////////////////////////////////

	/**
	 * Compare two objects
	 * 
	 * @param other object to compare
	 * 
	 * @return result of the comparison
	 * 
	 */
	
	public boolean equals(Object other)
	{
		if(!(other instanceof ConstantValue))
			return false;
		if(((ConstantValue) other).value != value)
			return false;
		return true;
	}	

	/**
	 * Shows this operation identification
	 * 
	 * @return value
	 */
	
	public String toString()
	{
		return String.valueOf(value);
	}
	
	public List<Float> toGPUcode()
	{
		return new ArrayList<Float>(Arrays.asList((float) value));
	}
}
