package net.sf.jclec.problem.classification.blocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sf.jclec.exprtree.fun.AbstractPrimitive;
import net.sf.jclec.exprtree.fun.ExprTreeFunction;
import net.sf.jclec.problem.classification.base.Rule;
import net.sf.jclec.exprtree.IPrimitive;

/**
 * Get an attribute for actual instance 
 * 
 * @author Alberto Cano 
 * @author Amelia Zafra
 * @author Sebastian Ventura
 * @author Jose M. Luna 
 * @author Juan Luis Olmo
 */

public class AttributeValue extends AbstractPrimitive
{
	/////////////////////////////////////////////////////////////////
	// --------------------------------------------------- Properties
	/////////////////////////////////////////////////////////////////

	private static final long serialVersionUID = 8407032123271160647L;

	/** Target attribute index */
	
	private int attributeIndex;
	
	/////////////////////////////////////////////////////////////////
	// ----------------------------------------------- Argument types
	/////////////////////////////////////////////////////////////////

	/** Argument types */
	
	private static final Class<?> [] ARG_TYPES = new Class<?> [0];
	
	/////////////////////////////////////////////////////////////////
	// -------------------------------------------------- Constructor
	/////////////////////////////////////////////////////////////////

	/**
	 * Empty constructor
	 */
	
	public AttributeValue()
	{
		super(ARG_TYPES,Double.class);
	}

	/////////////////////////////////////////////////////////////////
	// ----------------------------------------------- Public methods
	/////////////////////////////////////////////////////////////////

	/**
	 * Gets attribute index
	 * 
	 * @return attributeIndex attribute index
	 */
	
	public final int getAttributeIndex() 
	{
		return attributeIndex;
	}

	/**
	 * Sets attribute index
	 * 
	 * @param attributeIndex attribute index
	 */
	
	public final void setAttributeIndex(int attributeIndex) 
	{
		this.attributeIndex = attributeIndex;
	}
	
	/**
	 * Execute this operation over the stack and context
	 * 
	 * @param context the ExprTreeFunction context
	 */

	public void evaluate(ExprTreeFunction context) 
	{
		// Get attribute value
		Object attributeValue = ((Rule) context).getActualInstance().value(attributeIndex);
		
		// Push attribute value on stack
		super.push(context,attributeValue);
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
	
	/**
	 * Default implementation of copy() return this.
	 * 
	 * {@inheritDoc}
	 */
	
	public IPrimitive copy() 
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
	 */
	
	public boolean equals(Object other)
	{
		if(!(other instanceof AttributeValue))
			return false;
		if(((AttributeValue) other).getAttributeIndex() != attributeIndex)
			return false;
		return true;
	}	

	/**
	 * Shows this operation identification
	 * 
	 * @return attribute index
	 */
	
	public String toString()
	{
		return String.valueOf(attributeIndex);
	}
	
	public List<Float> toGPUcode()
	{
		return new ArrayList<Float>(Arrays.asList((float) attributeIndex));
	}
}
