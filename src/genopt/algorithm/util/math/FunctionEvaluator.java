package genopt.algorithm.util.math;

import java.util.Stack;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.lang.NoSuchMethodException;
import java.lang.reflect.*;

/** Function parser.
  * This class parses a given <code>String</code> argument and computes the function value as
  * specified by the <code>String</code>.
  * To invoke the function, the class <code>genopt.algorithm.util.math.Fun</code> is parsed. If
  * the function cannot be found in this class, then the class <code>java.lang.StrictMath</code>
  * is parsed.
  *
  * A typical argment has the form <code>add(2, subtract(sin(3), 1))</code>.
  *
  *
  * <P><I>This project was carried out at:</I>
  * <UL><LI><A HREF="http://www.lbl.gov">
  * Lawrence Berkeley National Laboratory (LBNL)</A>,
  * <A HREF="http://simulationresearch.lbl.gov">
  * Simulation Research Group</A>,</UL></LI>
  * <I>and supported by</I><UL>
  * <LI>the <A HREF="http://www.energy.gov">
  * U.S. Department of Energy (DOE)</A>,
  * <LI>the <A HREF="http://www.satw.ch">
  * Swiss Academy of Engineering Sciences (SATW)</A>,
  * <LI>the Swiss National Energy Fund (NEFF), and
  * <LI>the <A HREF="http://www.snf.ch">
  * Swiss National Science Foundation (SNSF)</A></UL></LI><P>
  *
  * GenOpt Copyright (c) 1998-2010, The Regents of the University of
  * California, through Lawrence Berkeley National Laboratory (subject 
  * to receipt of any required approvals from the U.S. Dept. of Energy).  
  * All rights reserved.
  *
  * @author <A HREF="mailto:MWetter@lbl.gov">Michael Wetter</A>
  *
  * @version GenOpt(R) 3.0.3 (April 26, 2010)<P>
  */

/*
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions are met:
  * 
  * (1) Redistributions of source code must retain the above copyright notice, 
  * this list of conditions and the following disclaimer.
  * 
  * (2) Redistributions in binary form must reproduce the above copyright 
  * notice, this list of conditions and the following disclaimer in the 
  * documentation and/or other materials provided with the distribution.
  * 
  * (3) Neither the name of the University of California, Lawrence Berkeley 
  * National Laboratory, U.S. Dept. of Energy nor the names of its 
  * contributors may be used to endorse or promote products derived from 
  * this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR 
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT 
  * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED 
  * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * 
  * You are under no obligation whatsoever to provide any bug fixes, 
  * patches, or upgrades to the features, functionality or performance of 
  * the source code ("Enhancements") to anyone; however, if you choose to 
  * make your Enhancements available either publicly, or directly to 
  * Lawrence Berkeley National Laboratory, without imposing a separate 
  * written license agreement for such Enhancements, then you hereby grant 
  * the following license: a non-exclusive, royalty-free perpetual license 
  * to install, use, modify, prepare derivative works, incorporate into 
  * other computer software, distribute, and sublicense such enhancements 
  * or derivative works thereof, in binary and source code form. 
 */

public class FunctionEvaluator {

    /**
     * Line separator
     */
    private final static String LS = System.getProperty("line.separator");

    /**
     * Creates a new <code>FunctionEvaluator</code> instance.
     *
     * @param name name of the function
     * @param function function to be evaluated
     */
    public FunctionEvaluator(String name, String function){
	Name=name;
	Func=function;
    }

    /**
     * Evaluates the function.
     *
     * @return the function value
     * @exception NoSuchMethodException if the method could not be found
     * @exception IllegalAccessException if an error occurs
     * @exception InvocationTargetException if an error occurs
     * @exception IndexOutOfBoundsException if an error occurs
     */
    public double evaluate() 
	throws NoSuchMethodException, IllegalAccessException,
	       InvocationTargetException{
	_setArrayList();
	// get first element. If it is a word, it must be a function name
	if ( ((Element)arrLis.get(0)).ttype == Element.TT_WORD ){
	    final String fun = ((Element)arrLis.remove(0)).getString();
	    try{
		final Element ele = _parse(arrLis, fun);
		if (arrLis.size() != 0 ){
		    String em = "Premature termination of formula." + LS;
		    em += "Following Token are invalid: ";
		    for(int i = 0; i < arrLis.size(); i++){
			em += "'" + ((Element)arrLis.get(i)).getStringRepresentation() + "'";
			if ( i != (arrLis.size() - 1) )
			    em += ", ";
		    }
		    throw new IllegalArgumentException(em);
		}
		return ele.getNumber();
	    }
	    catch (IndexOutOfBoundsException e) {
		throw new IllegalArgumentException( // throw as illegal argument
						    "Invalid function: " + _getErrorMessagePrefix());
	    }
	    catch (IllegalArgumentException e) {
		throw new IllegalArgumentException( _getErrorMessagePrefix() + e.getMessage());
	    }
	    catch (NoSuchMethodException e) {
		throw new NoSuchMethodException( _getErrorMessagePrefix() + e.getMessage());
	    }
	    catch (IllegalAccessException e) {
		throw new IllegalAccessException( _getErrorMessagePrefix() + e.getMessage());
	    }
	    catch (InvocationTargetException e) {
		throw new InvocationTargetException(e.getTargetException(),
						    _getErrorMessagePrefix() + e.getMessage());
	    }

    	}
	// check whether we have only one entry, which must be a number
	else if (( arrLis.size() == 1) && 
		 (((Element)arrLis.get(0)).ttype == Element.TT_NUMBER ) ){
	    final Element ele = (Element)arrLis.get(0);
	    return ele.getNumber();

	}
	else{
	    final String em = _getErrorMessagePrefix() + 
		"Function must start with a function name, followed by '('.";
	    throw new IllegalArgumentException(em);
	}
    }

    /** Sets the array list that will be used to evaluate the function
     */
    private void _setArrayList(){
	final String delimiter = ",()";
	
	StringTokenizer str = new StringTokenizer(Func, delimiter, true);
	arrLis  = new ArrayList<Element>();
	while (str.hasMoreTokens()){
	    final String tok = str.nextToken().trim();
	    if ( tok.length() != 0 )
		arrLis.add( new Element( tok ));
	}
    }
    
    /**
     * Gets the prefix for the error message.
     *
     * @return the prefix for the error message
     */
    private String _getErrorMessagePrefix(){
	return LS + "Function '" + Name + " = " + Func + "'." + LS;
    }
 
    /**
     * Parses the function.
     *
     * @param lis the <code>ArrayList</code> with the function being evaluated.
     *           The function name must be passed in the <code>fun</code> argument.
     * @param fun name of the function that has to be invoked
     * @return an <code>Element</code> containing the function value
     * @exception NoSuchMethodException if an error occurs
     * @exception IllegalAccessException if an error occurs
     * @exception InvocationTargetException if an error occurs
     */
    private Element _parse(ArrayList<Element> lis, String fun) 
	throws NoSuchMethodException, IllegalAccessException, InvocationTargetException{
	assert fun != null : "Argument fun is 'null'.";
	//System.out.println("Enter _parse for function '" + fun + "'.");
	final Stack<Double> sta = new Stack<Double>();

	Element ele = (Element)(lis.remove(0));
	if ( ele.ttype != '(' )
	    throw new IllegalArgumentException("'(' expected after '" + fun + "'.");
	
	ele = (Element)(lis.remove(0));
	do{
	    switch (ele.ttype){
	    case Element.TT_WORD:
		//System.out.println("Word          '" + ele.getString() + "'.");
		lis.add( 0, _parse(lis, ele.getString()) );
		ele = null;
		break;
	    case Element.TT_NUMBER:
		//System.out.println("Number        " + ele.getNumber());
		sta.push( new Double( ele.getNumber() ) );
		ele = (Element)(lis.remove(0));
		if ( ! ( ele.ttype == ')' || ele.ttype == ',' ) )
		    throw new IllegalArgumentException("')' or ',' expected.");
		else if ( ele.ttype == ')' )
		    return _evaluate(fun, sta);
		break;
	    case '(':
		throw new IllegalArgumentException("Too many open brackets.");
	    case ')':
		throw new IllegalArgumentException("Too many closing brackets.");
	    case '+':
		throw new IllegalArgumentException("'+' is not allowed.");
	    case '*':
		throw new IllegalArgumentException("'*' is not allowed.");
	    case ',':
		throw new IllegalArgumentException("Too many ','.");
	    default:
		assert false : "Error in parsing function.";
	    }
	    ele = (Element)(lis.remove(0));
	}while ( lis.size() != 0 );
	throw new IllegalArgumentException("Syntax error in formula.");
    }

    /**
     * Evaluates the function <code>fun</code> using all elements in <code>sta</code> as the argument.
     *
     * @param fun the name of the function that will be invoked
     * @param sta a stack of <code>Double</code>s that are the arguments of
     *            <code>fun</code>
     * @return an <code>Element</code> that contains the function value
     * @exception NoSuchMethodException if an error occurs
     * @exception IllegalAccessException if an error occurs
     * @exception InvocationTargetException if an error occurs
     */
    private Element _evaluate(String fun, Stack<Double> sta)
	throws NoSuchMethodException, IllegalAccessException, InvocationTargetException{
	final int staSiz = sta.size();
	Class[]  parTyp = new Class[staSiz];
	Object[] arg    = new Object[staSiz];
	for(int i = 0; i < staSiz; i++){
	    arg[i] = sta.elementAt(i);
	    parTyp[i] = double.class;
	}
	// check first in class 'Fun' for function
	final Class<Fun> c0 = Fun.class;
	final Class<StrictMath> c1 = StrictMath.class;
	try{

	    final Method meth = c0.getMethod(fun, parTyp);
	    return new Element ( meth.invoke(fun, arg).toString() ) ;
	}
	// function not found, check in StrictMath
	catch(NoSuchMethodException e0){
	    try{
		final Method meth = c1.getMethod(fun, parTyp);
		return new Element ( meth.invoke(fun, arg).toString() ) ;
	    }
	    catch(NoSuchMethodException e1){
		String em = "No method '" + e1.getMessage() +
		    "' in '" + c0.getName() + "' and '" + c1.getName() + "'.";
		throw new NoSuchMethodException(em);
	    }
	}
    }

    public String getName() { return new String(Name); }
    public String getFunction() { return new String(Func); }
    
    /** Function name. */
    protected String Name;

    /** Function to be evaluated. */
    protected String Func;

    /** List with Elements that resulted from function parsing. */
    protected ArrayList<Element> arrLis;

   /////////////////////////////////////////////////
    
    /**
     * Internal class for an element that contains either a 
     * <code>String</code>, a <code>double</code>, or 
     * a <code>char</code> value.
     *
     */
    private class Element implements Cloneable{

	/**
	 * Creates a new <code>Element</code> instance.
	 *
	 * @param stringRepresentation a <code>String</code> value of the element being parsed.
	 *                             The argument must have the leading and terminating whitespace
	 *                             characters, if any, removed.
	 */
	public Element(String stringRepresentation){
	    if (stringRepresentation == null)
		throw new RuntimeException("Argument of Element constructor must not be 'null'.");
	    sval = stringRepresentation;
	    _setAttribute();
	}

	private void _setAttribute(){
	    if (sval.length() == 1){
		if      ( sval.charAt(0) == ',' ) { ttype = TT_COMMA; return; }
		else if ( sval.charAt(0) == '(' ) { ttype = TT_OBRA; return; }
		else if ( sval.charAt(0) == ')' ) { ttype = TT_CBRA; return; }
	    }
	    nval = 0;
	    try {
		nval = Double.parseDouble(sval);
		ttype = TT_NUMBER;
	    }
	    catch(NumberFormatException e){
		ttype = TT_WORD;
	    }
	}
	
	/**
	 * Gets the <code>double</code> value of the element.
	 *
	 * @return the <code>double</code> value of the element
	 * @exception RuntimeException if the element is not of 
	 * type <code>double</code>
	 */
	public double getNumber() throws RuntimeException { 
	    if ( ttype != TT_NUMBER )
		throw new RuntimeException("Element is not a number.");
	    return nval; 
	}

	/**
	 * Gets the <code>String</code> value of the element.
	 *
	 * @return the <code>String</code> value of the element
	 * @exception RuntimeException if the element is not of 
	 * type <code>String</code>
	 */
	public String getString() throws RuntimeException {
	    if ( ttype != TT_WORD )
		throw new RuntimeException("Element is not a string.");
	    return new String(sval);
	}
	
	/**
	 * Gets the <code>String</code> representation of the element.
	 * This method can be called on any element, also for elements
	 * representing a <code>double</code> number.
	 *
	 * @return the <code>String</code> representation of the element
	 */
	public String getStringRepresentation() {
	    switch (ttype){
	    case TT_NUMBER:
		return Double.toString(nval);
	    case TT_WORD:
		return new String(sval);
	    case TT_COMMA:
		return ",";
	    case TT_OBRA:
		return "(";
	    case TT_CBRA:
		return ")";
	    }
	    assert false : "Wrong type for ttype.";
	    return  "Wrong type for ttype.";
	}
	
	/**
	 * Returns a <code>String</code> representation of the object.
	 *
	 * @return the <code>String</code> representation of the object
	 */
	public String toString(){ 
	    switch (ttype){
	    case TT_NUMBER:
		return "Number             " + nval;
	    case TT_WORD:
		return "Word               " + sval;
	    case TT_COMMA:
		return "Comma              ,";
	    case TT_OBRA:
		return "Opening bracket    (";
	    case TT_CBRA:
		return "Closing bracket    )";
	    }
	    assert false : "Wrong type for ttype.";
	    return  "Wrong type for ttype.";
	}
	
	/**
	 * Clones the instance.
	 *
	 * @return a clone of the instance
	 */
	public Object clone(){
	    try{
		final Element el = (Element)super.clone();
		el.sval = new String(this.sval);
		return el;
	    }
	    catch(CloneNotSupportedException e){
		assert false : "Cloning failed.";
	    }
	    return this;
	}
	    
	private String sval;
	private double nval;
	public int ttype;
	public static final int TT_NUMBER = -2;
	public static final int TT_WORD = -3;
	public static final int TT_COMMA = ',';
	public static final int TT_OBRA =  '(';
	public static final int TT_CBRA =  ')';
	
    }
    ///////////////////////////////////////////////////////////

    /*
     * Main method.
     *
     * @param args a <code>String[]</code> value, 1st argument is a descriptive
     *  function name (used for error report only),
     *  2nd argument is a function of the form 'add(10, sin(1))'.

    public static void main(String[] args) {
        final boolean internalTest = true;
	
	if (internalTest){
	    final int nTest = 30;
	    String[] formula = new String[nTest];
	    double[] result = new double[nTest];
	    int i = 0;
	    formula[i] = "add(2,-10)"; result[i] = 2-10; i++;
	    formula[i] = "add(2, -10)"; result[i] = 2-10; i++;
	    formula[i] = "add(2,-10 )"; result[i] = 2-10; i++;
	    formula[i] = "add( 2,-10)"; result[i] = 2-10; i++;
	    formula[i] = "add(2 ,-10)"; result[i] = 2-10; i++;	    
	    formula[i] = "add( 2 , -10 )"; result[i] = 2-10; i++;
	    formula[i] = "subtract(2,log(10))"; result[i] = 2-StrictMath.log(10); i++;
	    formula[i] = "subtract(2, log(10))"; result[i] = 2-StrictMath.log(10); i++;
	    formula[i] = "subtract(2,log( 10))"; result[i] = 2-StrictMath.log(10); i++;
	    formula[i] = "subtract(2,log(10 ))"; result[i] = 2-StrictMath.log(10); i++;
	    formula[i] = "subtract(2,log(10) )"; result[i] = 2-StrictMath.log(10); i++;
	    formula[i] = "subtract(2,log( 10 ) )"; result[i] = 2-StrictMath.log(10); i++;
	    formula[i] = "subtract(2, log(10))"; result[i] = 2-StrictMath.log(10); i++;
	    formula[i] = "add(2, subtract(log(10),9))"; result[i] = 2+(StrictMath.log(10)-9); i++;
	    formula[i] = "add(2, subtract(log(10), 9))"; result[i] = 2+(StrictMath.log(10)-9); i++;
	    formula[i] = "add(2, subtract( log(10), 9))"; result[i] = 2+(StrictMath.log(10)-9); i++;
	    formula[i] = "add(2, subtract(log (10), 9))"; result[i] = 2+(StrictMath.log(10)-9); i++;
	    formula[i] = " add( 2 , subtract(log (10), 9)) "; result[i] = 2+(StrictMath.log(10)-9); i++;
	    formula[i] = " add( 2 , subtract(log (10), 9) ) "; result[i] = 2+(StrictMath.log(10)-9); i++;

	    formula[i] = "add(subtract(log(10),9), 2)"; result[i] = 2+(StrictMath.log(10)-9); i++;
	    formula[i] = "add( subtract(log(10),9), 2)"; result[i] = 2+(StrictMath.log(10)-9); i++;
	    formula[i] = "add(subtract (log(10),9), 2)"; result[i] = 2+(StrictMath.log(10)-9); i++;
	    formula[i] = "add( subtract( log( 10),9), 2)"; result[i] = 2+(StrictMath.log(10)-9); i++;
	    formula[i] = "2.3"; result[i] = 2.3; i++;
	    formula[i] = "-2.3"; result[i] = -2.3; i++;
	    formula[i] = "2.3E-7"; result[i] = 2.3E-7; i++;
	    formula[i] = "2.3E+17"; result[i] = 2.3E+17; i++;
	    formula[i] = "-2.3E-7"; result[i] = -2.3E-7; i++;
	    formula[i] = "2.3"; result[i] = 2.3; i++;
	    formula[i] = "0"; result[i] = 0; i++;


	    for(int iT=0; iT < nTest; iT++){
		FunctionEvaluator funEva = new FunctionEvaluator("Test number " + iT, formula[iT]);
		try{
		    final double res = funEva.evaluate();
		    System.out.println("Difference: " + (res-result[iT]) +"  Result: " + res);
		}
		catch(Exception e){
		    System.err.println(e.getMessage() );
		}
	    }
	}
	else{
	    if (args.length != 2)
		throw new RuntimeException("Need two string arguments.");
	    
	    final FunctionEvaluator funEva = new FunctionEvaluator(args[0], args[1]);
	    try{
		System.out.println("Result: " + funEva.evaluate() );
	    }
	    catch(Exception e){
		System.err.println(e.getMessage() );
	    }
	}
    }
*/
}
