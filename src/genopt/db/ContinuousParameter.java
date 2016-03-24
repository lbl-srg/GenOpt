package genopt.db;

import genopt.db.IndependentParameter;
import java.lang.Math;

/** Object that represents a continuous parameter of
  * the optimization process.
  * Note the meaning of field <CODE>constraints</CODE>.
  * 
  * <h2> Meaning of field <CODE>constraints</CODE>: </h2>
  * <pre> 1: no under boundary, no upper boundary
  * 2: under boundary,    no upper boundary
  * 3: under boundary,    upper boundary
  * 4: no under boundary, upper boundary </pre>
  * <P>
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
  * GenOpt Copyright (c) 1998-2016, The Regents of the University of
  * California, through Lawrence Berkeley National Laboratory (subject 
  * to receipt of any required approvals from the U.S. Dept. of Energy).  
  * All rights reserved.
  *
  * @author <A HREF="mailto:MWetter@lbl.gov">Michael Wetter</A>
  *
  * @version GenOpt(R) 3.1.1 (March 24, 2016)<P>
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

public class ContinuousParameter extends IndependentParameter
{
    /** constructor. Does nothing
     */
    public ContinuousParameter() { super(); }
    
    /** Constructor for a continuous variable.
     * @param VariableName  Name of the free parameter as specified in the
     *                      command file
     * @param MinValue      Minimum of free parameter
     * @param OriginalValue Initial value of free parameter in orginal space
     * @param MaxValue      Maximum of free parameter
     * @param OriginalStepSize      Step size in original space
     * @param Constraint    Integer that specifies the kind of parameter
     *                      constraints<BR>
     *                      1: continuous, unconstrained,<BR>
     *                      2: continuous, lower bounded,<BR>
     *                      3: continuous, lower and upper bounded,<BR>
     *                      4: continuous, upper bounded
     */
    public ContinuousParameter(String VariableName, double MinValue, double OriginalValue, 
		   double MaxValue, double OriginalStepSize, int Constraint)
    {
	super(VariableName);
	minimum		 = MinValue;
	original	 = OriginalValue;
	maximum		 = MaxValue;
	originalStepSize = OriginalStepSize;
	constraint	 = Constraint;
    }

    /** Clone
     */
    protected Object clone(){
	if (name != null)
	    return new ContinuousParameter(new String(name), minimum, original, maximum,
					   originalStepSize, constraint);
	else
	    return new ContinuousParameter(null, minimum, original, maximum,
					   originalStepSize, constraint);
	
    }
    
    /** Gets the parameter value in the original space
     * @return parameter value in the original space
     */
    public final double getOriginalValue() { return original; }
    
    /** Gets the parameter value in the transformed space
     * @return parameter value in the transformed space
     */
    public final double getTransformedValue(){ 
	return transformValue(original, minimum, maximum, constraint, 0);
    }

    /** Gets the minimum restriction of the parameter
     * @return minimum restriction of the parameter
     */
    public final double getMinimum() { return minimum; }

    /** Gets the maximum restriction of the parameter
     * @return maximum restriction of the parameter
     */
    public final double getMaximum() { return maximum; }


    /** Gets the step size in the original space
     * @return step size in the original space
     */
    public final double getOriginalStepSize() { return originalStepSize; }

    /** Gets the kind of constraint
     * @return kind of constraint
     */
    public final int getKindOfConstraint() {return constraint;}

    /** Transforms a value from the original to the transformed space or 
     * vice-versa, depending on the value of <CODE>direction</CODE><BR>
     * @param x the value that has to be transformed
     * @param l its minimum value (or any dummy value if not applicable for this
     *                type of constraint
     * @param u its minimum value (or any dummy value if not applicable for this
     *                type of constraint
     * @param con kind of constraint
     * @param dir <CODE>0</CODE> if transformation has to be done
     *       from original to transformed space, <CODE>1</CODE> otherwise
     * @return the transformed value
     */
    public static double[] transformValue(double x[], 
					  double l[],
					  double u[],
					  int con[],
					  int dir){
	final int n = x.length;
	double[] r = new double[n];
	for(int i = 0; i < n; i++)
	    r[i] = transformValue(x[i], l[i], u[i], con[i], dir);
	return r;
    }

    /** Transforms a value from the original to the transformed space or 
     * vice-versa, depending on the value of <CODE>direction</CODE><BR>
     * @param x the value that has to be transformed
     * @param l its minimum value (or any dummy value if not applicable for this
     *                type of constraint
     * @param u its minimum value (or any dummy value if not applicable for this
     *                type of constraint
     * @param con kind of constraint
     * @param dir <CODE>0</CODE> if transformation has to be done
     *       from original to transformed space, <CODE>1</CODE> otherwise
     * @return the transformed value
     */
    public static double transformValue(double x, 
					double l,
					double u,
					int con,
					int dir)
    {
	if (dir == 0) 
	    {   //transform from original to transformed
		switch (con)
		    {
		    case 2:
			return Math.pow(x - l, 0.5);
		    case 3:
			return Math.asin(Math.pow((x-l)/(u-l), 0.5)) ;
		    case 4:
			return Math.pow(u-x, 0.5);
		    default:
			return x;

		    }
	    }
	else   
	    {	//transform from transformed to original
		switch (con)
		    {
		    case 2:
			return l + Math.pow(x, 2.);
		    case 3:
			return l + (u-l) * Math.pow(Math.sin(x),2.);
		    case 4:
			return u - Math.pow(x, 2.);
		    default:
			return x;
		    }
	    }
    }

    /** Gets the step size in the transformed space
     * @param x the parameter value in the original space
     * @param dx the step size in the original space
     * @param l its minimum value (or any dummy value if not applicable for this
     *                type of constraint
     * @param u its minimum value (or any dummy value if not applicable for this
     *                type of constraint
     * @param con kind of constraint
     * @param dir <CODE>0</CODE> if transformation has to be done
     *       from original to transformed space, <CODE>1</CODE> otherwise
     * @return step size in the transformed space  
	  */
    public static double getTransformedStepSize(double x, 
						 double dx,
						 double l,
						 double u,
						 int con,
						 int dir){
	if (con == 1) {   // no restrictions
	    return dx;
	}
	else{   // some restrictions
	    double HalSteSiz = dx / 2;
	    double LowVal = x - HalSteSiz;
	    double HigVal = x + HalSteSiz;
	    // ensure that constraints are not crossed 
	    if (LowVal < l){
		LowVal = l;
		HigVal = LowVal + 2 * HalSteSiz;
	    }
	    if (HigVal > u){
		HigVal = u;
		LowVal = HigVal - 2 * HalSteSiz;
	    }
	    final double r = Math.abs(transformValue(HigVal, l, u, con, dir) -
			    transformValue(LowVal, l, u, con, dir));
	    return r;
	}
    }

    /** value in the original space */
    protected double original;
    /** lower boundary */
    protected double minimum;
    /** upper boundary */
    protected double maximum;
    /** step size in the original space */
    protected double originalStepSize;
    /** kind of constraint<BR>
	   1: continuous, unconstrained,<BR>
	   2: continuous, lower bounded,<BR>
	   3: continuous, lower and upper bounded,<BR>
	   4: continuous, upper bounded */
    protected int constraint;
}







