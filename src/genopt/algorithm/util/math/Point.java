package genopt.algorithm.util.math;

/** Abstract class for a point with continuous and discrete coordinates.<P>
  *
  * The discrete parameters are stored as zero-based indices.
  * This class has methods to get for each discrete parameter its value 
  * as a Gray coded binary <CODE>int[]</CODE> arrays.
  *
  * <P><I>This project was carried out at:</I>
  * <UL><LI><A HREF="http://www.lbl.gov">
  * Lawrence Berkeley National Laboratory (LBNL)</A>,
  * <A HREF="http://simulationresearch.lbl.gov">
  * Simulation Research Group</A>,</LI></UL>
  * <I>and supported by</I><UL>
  * <LI>the <A HREF="http://www.energy.gov">
  * U.S. Department of Energy (DOE)</A>,
  * <LI>the <A HREF="http://www.satw.ch">
  * Swiss Academy of Engineering Sciences (SATW)</A>,
  * <LI>the Swiss National Energy Fund (NEFF), and
  * <LI>the <A HREF="http://www.snf.ch">
  * Swiss National Science Foundation (SNSF)</A></LI></UL><P>
  *
  * GenOpt Copyright (c) 1998-2011, The Regents of the University of
  * California, through Lawrence Berkeley National Laboratory (subject 
  * to receipt of any required approvals from the U.S. Dept. of Energy).  
  * All rights reserved.
  *
  * @author <A HREF="mailto:MWetter@lbl.gov">Michael Wetter</A>
  *
  * @version GenOpt(R) 3.1.0 (December 8, 2011)<P>
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

public class Point implements Comparable<Point>
{
    /** Accuracy for testing of rounding errors */
    public static double EPSILON = 1E-12;
    public static double ONEMINUSEPSILON = 1-EPSILON;
    public static double ONEPLUSEPSILON = 1+EPSILON;

    /** Constructor
     *
     *@param nCon number of continuous variables
     *@param nDis number of discrete variables
     *@param nF   number of function values
     */
    public Point(final int nCon, final int nDis, final int nF){
	_initialize(nCon, nDis, nF);
    }

    /** Allocates all arrays.
     *
     *@param nCon number of continuous variables
     *@param nDis number of discrete variables
     *@param nF   number of function values
     */
    private void _initialize(final int nCon, final int nDis, final int nF){
	dimCon = nCon;
	dimDis = nDis;
	dimF   = nF;
	cooCon = new double[nCon];
	cooDis = new int[nDis];
	fun = new double[nF];
	cooDisMax = new int[nDis];
	cooDisBitLength = new int[nDis];
	com = "";
	steNum = 0;
	simNum = 0; // the simulation number starts with 1 in GenOpt. So, 0 means it is not set
	for(int i = 0; i < dimDis; i++){
	    cooDisMax[i]       = Integer.MAX_VALUE;
	    cooDisBitLength[i] = -1; // not initialized yet
	}
    }
    
    /** Constructor for a point with only discrete independent variables
     *@param xDis the point's discrete coordinates
     *@param f its function values
     *@param stepNumber the step number
     *@param comment a comment
     */
    public Point(int[] xDis, double[] f, int stepNumber, String comment){
	final int nDis = ( xDis == null ) ? 0 : xDis.length;
	final int nCon = 0;
	final int nFun = ( f    == null ) ? 0 : f.length;
	_initialize(nCon, nDis, nFun);
	set(xDis, f, stepNumber, comment);
    }		

    /** Constructor for a point with only continuous independent variables
     *@param xCon the point's continuous coordinates
     *@param f its function values
     *@param stepNumber the step number
     *@param comment a comment
     */
    public Point(double[] xCon, double[] f, int stepNumber, String comment){ 
	final int nCon = ( xCon == null ) ? 0 : xCon.length;
	final int nDis = 0;
	final int nFun = ( f    == null ) ? 0 : f.length;
	_initialize(nCon, nDis, nFun);
	set(xCon, f, stepNumber, comment);
    }

    /** Constructor for a point with continuous and discrete 
     *  independent variables
     *@param xCon the point's continuous coordinates
     *@param xDis the point's discrete coordinates
     *@param f its function values
     *@param stepNumber the step number
     *@param comment a comment
     */
    public Point(double[] xCon, int[] xDis, double[] f, 
                 int stepNumber, String comment){
	final int nCon = ( xCon == null ) ? 0 : xCon.length;
	final int nDis = ( xDis == null ) ? 0 : xDis.length;
	final int nFun = ( f    == null ) ? 0 : f.length;
	_initialize(nCon, nDis, nFun);
	set(xCon, xDis, f, stepNumber, comment); 
    }

    /** Constructor for a point with continuous and discrete 
     *  independent variables
     *@param xCon the point's continuous coordinates
     *@param xDis the point's discrete coordinates
     *@param xDisMax the point's maximum value of the discrete coordinates
     *@param f its function values
     *@param stepNumber the step number
     *@param comment a comment
     *@param simulationNumber the number of the simulation
     *@exception IllegalArgumentException if <code>xDis</code>
     *           and <code>xDisMax</code> are not <code>null</code>
     *           but of different length
     */
    public Point(double[] xCon, int[] xDis, int[] xDisMax,
		 double[] f, 
                 int stepNumber, String comment, int simulationNumber){
	if ( xDis != null && xDisMax != null )
	    if ( xDis.length != xDisMax.length ) 
		throw new IllegalArgumentException("xDis.length = " +
						   xDis.length + 
						   ", xDisMax.length = " +
						   xDisMax.length + 
						   ", must be same length.");
	final int nCon = ( xCon == null ) ? 0 : xCon.length;
	final int nDis = ( xDis == null ) ? 0 : xDis.length;
	final int nFun = ( f    == null ) ? 0 : f.length;
	_initialize(nCon, nDis, nFun);
	set(xCon, xDis, xDisMax, f, stepNumber, comment);
	simNum = simulationNumber;
    }
    
    /** Clones the object.
     *@return a copy of the object
     */
    public Object clone(){
	return new Point(cooCon, cooDis, cooDisMax,
			 fun, steNum, com, simNum);
    }

    /** Returns a string representation of the object.
     *@return a string representation of the object
     */
    public String toString(){
	final String LS = System.getProperty("line.separator");
	String r = getClass().getName();
	r += LS + "Continuous coordinates: ";
	for (int i=0; i < dimCon; i++)
	    r += cooCon[i] + "; ";
	r += LS + "Discrete coordinates  : ";
	for (int i=0; i < dimDis; i++)
	    r += cooDis[i] + "; ";
	r += LS + "Maximum coordinates   : ";
	for (int i=0; i < dimDis; i++)
	    r += cooDisMax[i] + "; ";
	r += LS + "Step number           : " + steNum + ";";
	r += LS + "Function values       : ";
	for (int i=0; i < dimF; i++)
	    r += fun[i] + "; ";
	r += LS + "Comment               : " + com + LS;
	r += LS + "Simulation number     : " + simNum + ";";
	return r;
    }

    /** Gets the point's function values
      *@return the point's function values
      */
    public double[] getF(){
    	if (dimF == 0) return null;
        double[] r = new double[dimF];
        System.arraycopy(fun, 0, r, 0, dimF);
        return r;
    }
		
    /** Gets the point's function values
     * @param i the index of the function value
     *@return the point's function values
     */
    public double getF(int i) { return fun[i]; }

    /** Gets the point's continuous independent variables
     *@return the point's continuous independent variables
    */		
    public double[] getX(){
	if (dimCon == 0) return null;
	double[] r = new double[dimCon];
	System.arraycopy(cooCon, 0, r, 0, dimCon);
	return r;
    }

    /** Gets the indices of the discrete point's
     *@return the indices of the discrete point's
    */		
    public int[] getIndex(){
	if (dimDis == 0) return null;
	int[] r = new int[dimDis];
	System.arraycopy(cooDis, 0, r, 0, dimDis);
	return r;
    }

    /** Gets the maximum allowed value of the indices of the discrete point's
     *@return the maximum allowed value of the indices of the discrete point's
    */		
    public int[] getMaximumIndex(){
	if (dimDis == 0) return null;
	int[] r = new int[dimDis];
	System.arraycopy(cooDisMax, 0, r, 0, dimDis);
	return r;
    }

    /** Gets the point's i-th continuous coordinate
     *@param i the index of the continuous coordinate
     *@return the point's i-th continuous coordinate
     */
    public double getX(int i) { return cooCon[i]; }

    /** Gets the i-th point's index
     *@param i the number of the discrete variable
     *@return the i-th point's index
     */
    public int getIndex(int i) { return cooDis[i]; }
    
    /** Gets the number of continuous coordinates
     *@return the number of continuous coordinates
     */
    public int getDimensionContinuous() {return dimCon;}

    /** Gets the number of discrete coordinates
     *@return the number of discrete coordinates
     */
    public int getDimensionDiscrete() {return dimDis;}
		
    /** Gets the dimension of the points function value vector
     *@return the dimension of the points function value vector
     */
    public int getDimensionF() {return dimF;}

    /** Gets the point's step number
     *@return the point's step number
     */
    public int getStepNumber() { return steNum; }

    /** Gets the point's comment
     *@return the point's comment
     */
    public String getComment() { return new String(com); }        
        
    /** Sets the point's continuous coordinates
     *@param x the point's continuous coordinates
     */
    public void setX(double[] x){
	if (x == null){
	    dimCon = 0;
	    cooCon = null;
	}
	else{
	    dimCon = x.length;
	    cooCon = new double[dimCon];
	    System.arraycopy(x, 0, cooCon, 0, dimCon);
	}
    }

    /** Sets the indices of the point's discrete variables
     *@param x the indices of the point's discrete variables
     *@exception IllegalArgumentException if an element is negative, or
     *       an element is larger than the maximum allowed value, or
     *       the length of the argument is different from the one allocated
     *       by this object
     */
    public void setIndex(int[] x)
    throws IllegalArgumentException{
	if (x == null){
	    dimDis = 0;
	    cooDis = null;
	}
	else{
	    for(int i = 0; i < x.length; i++){
		if ( x[i] < 0 )
		    throw new IllegalArgumentException("Received negative index.");
		if ( dimDis > 0 && x[i] > cooDisMax[i] )
		    throw new IllegalArgumentException("Received index larger than maximum allowed value.");
	    }
	    if ( dimDis != x.length )
		throw new IllegalArgumentException("Array length mismatch.");
	    System.arraycopy(x, 0, cooDis, 0, dimDis);
        }
    }

    /** Sets the indices of the point's discrete variables and its maximum allowed values
     *@param xDis the indices of the point's discrete variables
     *@param xDisMax the maximum allowed value for the indices of the point's discrete variables
     *@exception IllegalArgumentException if an element is negative, or
     *       an element is larger than the maximum allowed value, or
     *       the array lengths are different
     */
    public void setIndex(int[] xDis, int[] xDisMax){
	if ( xDis == null ){
	    if ( dimDis > 0)
		throw new IllegalArgumentException("Received 'xDis = null' but have discrete parameters.");
	    else
		return;
	}
	
	if (xDis.length != xDisMax.length)
	    throw new IllegalArgumentException("Array have different length.");
	else
	    for(int i = 0; i < xDis.length; i++){
		setMaximumIndex(i, xDisMax[i]);
		setIndex(i, xDis[i]);
	    }
    }


    /** Sets the index of the point's i-th discrete variables
     *@param i number of discrete variable
     *@param ind index of the point's i-th discrete variables
     *@exception IllegalArgumentException if the argument is negative or
     *       larger than the maximum allowed value
     */
    public void setIndex(int i, int ind)
    throws IllegalArgumentException{ 
	if ( ind < 0 )
	    throw new IllegalArgumentException("Received negative index.");
	if ( ind > cooDisMax[i] )
	    throw new IllegalArgumentException("Received index larger than maximum allowed value.");
	cooDis[i] = ind;
    }

    
    /** Sets the point's discrete and continuous coordinates
     *@param xCon the point's continuous coordinates
     *@param xDis the point's discrete coordinates
     *@exception IllegalArgumentException if an element is negative, or
     *       an element is larger than the maximum allowed value, or
     *       the length of the argument is different from the one allocated
     *       by this object
     */
    public void setXIndex(double[] xCon, int[] xDis)
    throws IllegalArgumentException{
	setX(xCon);
	setIndex(xDis);
    }

    /** Sets the i-th continuous independent variable
     *@param i the zero-based index of the continuous independent variable
     *@param x value to be set
     */
    public void setX(int i, double x) { cooCon[i] = x; }

    /** Sets the index of the point's i-th discrete independent variable
     *@param variableNumber the zero-based index of the discrete independent 
     *             variable
     *@param index the index of the variable
     *@exception IllegalArgumentException if the argument is negative or
     *       larger than the maximum allowed value
     *@deprecated replaced by <CODE>setIndex(int, int)</CODE>
     */
    public void setX(int variableNumber, int index) {
	setIndex(variableNumber, index);
    }

    /** Sets a point's function value
     *@param i the index of the function value
     *@param f points function value
     */
    public void setF(int i, double f){
	assert ( i >= 0 && i < fun.length ) : "Wrong value of i.";
	fun[i] = f;
    }

    /** Sets a point's function value
     *@param f a points function value
     */
    public void setF(double[] f){
	if (f == null){
	    dimF = 0;
	    fun = null;
	}
	else{
	    dimF = f.length;
	    fun = new double[dimF];
	    System.arraycopy(f, 0, fun, 0, dimF);
	}
    }		
		
    /** Sets a the step number
     *@param stepNumber the step number
     */
    public void setStepNumber(int stepNumber) { steNum = stepNumber;  }

    /** Sets a the simulation number
     *@param simulationNumber the number of the simulation
     */
    public void setSimulationNumber(int simulationNumber) { simNum = simulationNumber;  }

    /** Gets the simulation number
     *@return simulationNumber the number of the simulation
     */
    public int getSimulationNumber() { return simNum;  }
        
    /** Sets a comment
     *@param comment the comment
     */
    public void setComment(String comment){
	com = (comment == null) ? null : new String(comment);
    }
        
    /** Sets a point with only continuous independent variables
     *@param x the point's continuous coordinates
     *@param f its function values
     *@param stepNumber its step number
     *@param comment its comment
     *@exception IllegalArgumentException if an element of <CODE>x</CODE>
     *       is negative, or is larger than the maximum allowed value, or
     *       the length of the argument is different from the one allocated
     *       by this object
     */
    public void set(double[] x, double[] f, int stepNumber, String comment)
    throws IllegalArgumentException{
	set(x, stepNumber, comment);
	setF(f);
    }

    /** Sets a point with only discrete independent variables
     *@param x the point's discrete coordinates
     *@param f its function values
     *@param stepNumber its step number
     *@param comment its comment
     *@exception IllegalArgumentException if an element of <CODE>x</CODE>
     *       is negative, or is larger than the maximum allowed value, or
     *       the length of the argument is different from the one allocated
     *       by this object
     */
    public void set(int[] x, double[] f, int stepNumber, String comment)
	throws IllegalArgumentException{
	set(x, stepNumber, comment);
	setF(f);
    }

    /** Sets a point with continuous and discrete independent variables
     *@param xCon the point's continuous coordinates
     *@param xDis the point's discrete coordinates
     *@param f its function values
     *@param stepNumber its step number
     *@param comment its comment
     *@exception IllegalArgumentException if an element of <CODE>xDis</CODE>
     *       is negative, or is larger than the maximum allowed value, or
     *       the length of the argument is different from the one allocated
     *       by this object
     */
    public void set(double[] xCon, int[] xDis, double[] f, 
		    int stepNumber, String comment)
	throws IllegalArgumentException{
	set(xCon, f, stepNumber, comment);
	setIndex(xDis);
    }

    /** Sets a point with continuous and discrete independent variables
     *@param xCon the point's continuous coordinates
     *@param xDis the point's discrete coordinates
     *@param xDisMax the point's maximum value for the discrete coordinates
     *@param f its function values
     *@param stepNumber its step number
     *@param comment its comment
     *@exception IllegalArgumentException if an element of <CODE>xDis</CODE>
     *       is negative, or is larger than the maximum allowed value, or
     *       the length of the argument is different from the one allocated
     *       by this object
     */
    public void set(double[] xCon, int[] xDis, 
		    int[] xDisMax, double[] f, 
		    int stepNumber, String comment)
	throws IllegalArgumentException{
	set(xCon, f, stepNumber, comment);
	setIndex(xDis, xDisMax);
    }


    /** Sets a point with only continuous independent variables
     *@param x the point's continuous coordinates
     *@param stepNumber its step number
     *@param comment its comment
     */
    public void set(double[] x, int stepNumber, String comment){
	setX(x);
	steNum = stepNumber;
	setComment(comment);
    }

    /** Sets a point with only discrete independent variables
     *@param x the point's discrete coordinates
     *@param stepNumber its step number
     *@param comment its comment
     *@exception IllegalArgumentException if an element of <CODE>x</CODE>
     *       is negative, or is larger than the maximum allowed value, or
     *       the length of the argument is different from the one allocated
     *       by this object
     */
    public void set(int[] x, int stepNumber, String comment)
    throws IllegalArgumentException{
	setIndex(x);
	steNum = stepNumber;
	setComment(comment);
    }

    /** Compares this object with the specified object for equality, 
      * whereas only the x-coordinates in (<CODE>float</CODE> precision),
      * the index of the discrete variables,
      * and the step number are compared.
      *@return <CODE>0</CODE> if the objects are equal,
      *        <CODE>-1</CODE> if the received object is the smaller one
      *        <CODE>+1</CODE> if the received object is the larger one
      */
    public int compareTo(Point pt)
    {
	//final Point pt = (Point)o;
	// check step number first
	int st = pt.getStepNumber();
	if (steNum > st) return -1;
	if (steNum < st) return +1;
	
	// check continuous variables
	if (dimCon > 0){
	    final double[] argCon = pt.getX();
	    for (int i = 0; i < dimCon; i++){

		if ( ! areEqual(cooCon[i], argCon[i]) ){
		    if (cooCon[i] > argCon[i])
			return -1;
		    else
			return +1;
		}
	    }
	}
	// check discrete variables
	if (dimDis > 0){
	    final int[] argDis = pt.getIndex();
	    for (int i = 0; i < dimDis; i++){
		if (cooDis[i] > argDis[i]) return -1;
		if (cooDis[i] < argDis[i]) return +1;
	    }
	    final int[] argDisMax = pt.getMaximumIndex();
	    for (int i = 0; i < dimDis; i++){
		if (cooDisMax[i] > argDisMax[i]) return -1;
		if (cooDisMax[i] < argDisMax[i]) return +1;
	    }
	}
	return 0;
    }

    /** Compares two numbers for equality. Rounding errors are neglected.
     *@param x1 first number
     *@param x2 second number
     *@return <code>true</code> if the numbers are equal except 
     *         for rounding errors
     */
    protected static boolean areEqual(double x1, double x2){
	if ( x1 > 0 )
	    return ( (ONEPLUSEPSILON) * x1 >= x2 &&  (ONEMINUSEPSILON) * x1 <= x2 );
	else
	    return ( (ONEMINUSEPSILON) * x1 >= x2 &&  (ONEPLUSEPSILON) * x1 <= x2 );
    }

    /** Compares this object with the specified object for equality,
      * whereas only the x-coordinates in (<CODE>float</CODE> precision),
      * the index of the discrete variables,
      * and the step number are compared.
     *@param o Object to be compared
     *@return <CODE>true</CODE> if the objects are equal 
     *        in the sense of the <CODE>compareTo(Object)</CODE> method,
     *        <CODE>false</CODE> otherwise
     */
    public boolean equals(Point o){
	return (compareTo(o) == 0) ? true : false;
    }


    /** Compares two objects for equality,
      * whereas only the x-coordinates in (<CODE>float</CODE> precision),
      * the index of the discrete variables,
      * and the step number are compared.
     *@param o1 Object to be compared
     *@param o2 Object to be compared
     *@return <CODE>0</CODE> if the objects are equal,
     *        <CODE>-1</CODE> if o1 > o2,
     *        <CODE>+1</CODE> if o1 < o2,
     *        in the sense of the <CODE>compareTo(Object)</CODE> method
     */
    public int compare(Point o1, Point o2){
	//final Point p = (Point)(o1);
	return o1.compareTo(o2);
    }

    /** Sets the maximum value for the index of the 
     *  point's i-th discrete variables.
     *
     *  This function must be called before the value
     *  of the i-th discrete variable is requested as a binary string.
     *
     *@param i index of discrete variable
     *@param max maximum value of the point's i-th discrete variables
     *@exception IllegalArgumentException if <CODE>max < 0</CODE>
     */
    public void setMaximumIndex(int i, int max)
    throws IllegalArgumentException{ 
	if ( max < 0 )
	    throw new IllegalArgumentException("Argument must be non-negative.");
	cooDisMax[i] = max;
	cooDisBitLength[i] = Binary.getStringLength( Binary.getGrayCode( max ) );
    }

    /** Gets the string length used to represent the maximum value of the parameter
     *  as a Gray coded binary string.
     *
     *@param index index of the discrete variable
     *@return the string length used to represent the maximum value of the parameter
     *@exception IllegalArgumentException if the maximum value has not been set for
     *        this variable
     */
    public int getGrayBinaryStringLength(final int index)
	throws IllegalArgumentException{
	if ( cooDisBitLength[index] < 0 )
	    throw new IllegalArgumentException("No maximum value is set for variable number '"
					       + index + "'.");
	return cooDisBitLength[index];
    }

    /** Gets the value of a discrete variable encoded as a binary string
     *  using Gray encoding.
     *
     *@param index index of the discrete variable
     *@return the value of the discrete variable, Gray encoded, as a binary string
     */
    public int[] getGrayBinaryString(int index){
	if ( cooDisBitLength[index] < 0 )
	    throw new IllegalArgumentException("No maximum value is set for variable number '"
					       + index + "'.");
	long cooGra = Binary.getGrayCode( cooDis[index] );
	int[] r = Binary.toBinaryInt(cooGra, cooDisBitLength[index] );
	return r;
    }

    /** Sets the value of a discrete variable as a Gray encoded binary string.
     *
     * If <CODE>graBinStr</CODE> represent a larger value than the maximum
     * allowed value for this variable, then the variable value will be set
     * to its maximum value.
     *
     *@param index index of the discrete variable
     *@param graBinStr the value of the discrete variable as a Gray encoded binary string
     *@exception IllegalArgumentException if no maximum is set for this variable
     */
    public void setGrayBinaryString(int index, int[] graBinStr){
	if ( cooDisBitLength[index] < 0 )
	    throw new IllegalArgumentException("No maximum value is set for variable number '"
					       + index + "'.");
	final long lVal = Binary.getInverseGrayCode(graBinStr);
	final int iVal = (new Long(lVal)).intValue();
	assert ( iVal == lVal );
	// reduce value to maximum allowed value
	cooDis[index] = ( iVal > cooDisMax[index] ) ? cooDisMax[index] : iVal;
    }

    /** the point's continuous coordinates */
    protected double[] cooCon;
    /** the point's discrete coordinates */
    protected int[] cooDis;
    /** the maximum allowed value for the point's discrete coordinates.
     *  This field is used to compute the length of the binary string
     *  representation. */
    protected int[] cooDisMax;
    /** the length of the binary string representation */
    protected int[] cooDisBitLength;
    /** the point's function values */
    protected double[] fun;
    /** the point's comment */
    protected String com;
    /** the point's step number */
    protected int steNum;
    /** the number of continuous independent variables */
    protected int dimCon;
    /** the number of discrete independent variables */
    protected int dimDis;
    /** the dimension of the function value vector */
    protected int dimF;		
    /** the number of the simulation corresponding to this point */
    protected int simNum;		

    public static void main(String[] args){
	double x1 = new Double(args[0]).doubleValue();
	double x2 = new Double(args[1]).doubleValue();
	System.out.println( x1 + " == "+ x2+ " :" + areEqual(x1,x2) + " " + areEqual(x2, x1));
	System.out.println(ONEMINUSEPSILON + " " +ONEPLUSEPSILON);
    }
}
