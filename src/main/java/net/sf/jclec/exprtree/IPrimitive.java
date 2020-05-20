package net.sf.jclec.exprtree;

import java.io.Serializable;
import java.util.List;

/**
 * Primitive. This interface represents generically all possible primitive functions 
 * that be used in an expression tree node
 *  
 * @author Sebasti�n Ventura
 */

public interface IPrimitive extends Serializable
{ 
	// Functional information
	
	/**
	 * @return Return type of this primitive
	 */
	
	public Class<?> returnType();
	
	/**
	 * Argument types for this primitive. If this is a terminal, this
	 * method returns an empty Class<?> array.
	 * 
	 * @return Argument types for this primitive. 
	 */
	
	public Class<?> [] argumentTypes();

	// Execution method
	
	/**
	 * Evaluation method.
	 * 
	 * @param context Execution context
	 */
	
	public void evaluate(IContext context);
	
	// Primitive management
	
	/**
	 * Instance creation.
	 * 
	 * @return A new instance of this primitive class
	 */
	
	public IPrimitive instance();
	
	/**
	 * Copy method.
	 * 
	 * @return A copy of this primitive
	 */
	
	public IPrimitive copy();
	
	/**
	 * Returns the internal representation of the primitive for GPU computation
	 * 
	 * @return the internal float representation
	 */
	
	public List<Float> toGPUcode();
}
