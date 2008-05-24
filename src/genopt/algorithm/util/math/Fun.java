package genopt.algorithm.util.math;

/** Collection of mathematical functions. This package is scanned by <code>FunctionEvaluator</code>.<P>
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
  * GenOpt Copyright (c) 1998-2008, The Regents of the University of
  * California, through Lawrence Berkeley National Laboratory (subject 
  * to receipt of any required approvals from the U.S. Dept. of Energy).  
  * All rights reserved.
  *
  * @author <A HREF="mailto:MWetter@lbl.gov">Michael Wetter</A>
  *
  * @version GenOpt(R) 2.1.0 (May 23, 2008)<P>
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

public class Fun
{
    /** Natural logarithm of 10. */
    private static final double LN10 = StrictMath.log(10.);

    /** Gets an array containing spacing between <CODE>x0</CODE> and <CODE>x1</CODE>.
     *@param nStep number of intervals. If negative, spacing will be 
     *             logarithmic, otherwise it will be linear
     *@param x0 first point of spacing
     *@param x1 last point of spacing
     *@return array with all coordinate values
     */
    final public static double[] getSpacing(final int nStep, 
					    final double x0, 
					    final double x1){
	final int nS = StrictMath.abs(nStep);
	double[] r = new double[nS+1];
	r[0] = x0;

	if (nStep > 0){ // linear spacing
	    double dx = (x1 - x0) / nStep;
	    for(int i=1; i <= nS; i++)
		r[i] = x0 + (double)(i) * dx;
	}
	if (nStep < 0){ // logarithmic spacing
	    double p = StrictMath.log(x1 / x0) / nS;
	    for(int i=1; i <= nS; i++)
		r[i] = (double)(float)(x0 * StrictMath.pow( StrictMath.E, p * i));
	}
	return r;
    }
    
    /** Returns the logarithm (base 10) of a double  value. <BR>
     * Special cases:<UL>
     * <LI> If the argument is NaN or less than zero, then the result is NaN.
     * <LI> If the argument is positive infinity, then the 
     *      result is positive infinity.
     * <LI> If the argument is positive zero or negative zero, 
     *      then the result is negative infinity.</UL>
     *
     *@param x0 - a number greater than 0
     *@return the logarithm of x.
     */
    final public static double log10(final double x0){ return StrictMath.log(x0) / LN10; }


    /**
     * Adds two numbers.
     * This method is typically being used by <code>FunctionEvaluator</code>
     *
     * @param x0 1st argument
     * @param x1 2nd argument
     * @return the sum of the arguments
     */
    final public static double add(final double x0, 
				   final double x1) { return x0 + x1; }

    /**
     * Adds three numbers.
     * This method is typically being used by <code>FunctionEvaluator</code>
     *
     * @param x0 1st argument
     * @param x1 2nd argument
     * @param x2 3rd argument
     * @return the sum of the arguments
     */
    final public static double add(final double x0, 
				   final double x1, 
				   final double x2) { 
	return x0 + x1 + x2; 
    }

    /**
     * Adds four numbers.
     * This method is typically being used by <code>FunctionEvaluator</code>
     *
     * @param x0 1st argument
     * @param x1 2nd argument
     * @param x2 3rd argument
     * @param x3 4th argument
     * @return the sum of the arguments
     */
    final public static double add(final double x0, 
				   final double x1, 
				   final double x2, 
				   final double x3) { 
	return x0 + x1 + x2 + x3; 
    }

    /**
     * Adds five numbers.
     * This method is typically being used by <code>FunctionEvaluator</code>
     *
     * @param x0 1st argument
     * @param x1 2nd argument
     * @param x2 3rd argument
     * @param x3 4th argument
     * @param x4 5th argument
     * @return the sum of the arguments
     */
    final public static double add(final double x0, 
				   final double x1, 
				   final double x2, 
				   final double x3, 
				   final double x4) { 
	return x0 + x1 + x2 + x3 + x4; 
    }

    /**
     * Adds six numbers.
     * This method is typically being used by <code>FunctionEvaluator</code>
     *
     * @param x0 1st argument
     * @param x1 2nd argument
     * @param x2 3rd argument
     * @param x3 4th argument
     * @param x4 5th argument
     * @param x5 6th argument
     * @return the sum of the arguments
     */
    final public static double add(final double x0, 
				   final double x1,  
				   final double x2, 
				   final double x3, 
				   final double x4, 
				   final double x5) { 
	return x0 + x1 + x2 + x3 + x4 + x5; 
    }

    /**
     * Subtracts two numbers.
     * This method is typically being used by <code>FunctionEvaluator</code>
     *
     * @param x0 1st argument
     * @param x1 2nd argument
     * @return the difference <code>x0 - x1</code>
     */
    final public static double subtract(final double x0, 
					final double x1) { return x0 - x1; }

    /**
     * Multiplies two numbers.
     * This method is typically being used by <code>FunctionEvaluator</code>
     *
     * @param x0 1st argument
     * @param x1 2nd argument
     * @return the product of the arguments
     */
    final public static double multiply(final double x0,
					final double x1) { return x0 * x1; }

    /**
     * Multiplies three numbers.
     * This method is typically being used by <code>FunctionEvaluator</code>
     *
     * @param x0 1st argument
     * @param x1 2nd argument
     * @param x2 3rd argument
     * @return the product of the arguments
     */
    final public static double multiply(final double x0, 
					final double x1, 
					final double x2) { 
	return x0 * x1 * x2; 
    }

    /**
     * Divides two numbers.
     * This method is typically being used by <code>FunctionEvaluator</code>
     *
     * @param x 1st argument
     * @param y 2nd argument
     * @return the ratio <code>x/y</code>
     */
    final public static double divide(final double x, 
				      final double y) { return x / y; }
}
