package genopt.db;
import genopt.*;
import genopt.db.IndependentParameter;
import java.lang.Math;
import java.util.Vector;

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
  * @version GenOpt(R) 2.1.0 (May 29, 2008)<P>
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
	transformed      = transformValue(OriginalValue, 0);
	setTransformedStepSize();
    }

    /** Clone
     */
    protected Object clone()
    {
	if (name != null)
	    return new ContinuousParameter(new String(name), minimum, original, maximum,
					   originalStepSize, constraint);
	else
	    return new ContinuousParameter(null, minimum, original, maximum,
					   originalStepSize, constraint);
	    
    }

    /** Set the parameter value in the original space and
     * update the parameter value in the transformed space
     * and the step size in the transformed space.<BR>
     * @param OriginalValue parameter value in the original space
     */
    public final void setOriginalValue(double OriginalValue)
    {
	original    = OriginalValue;
	transformed = transformValue(OriginalValue, 0);
	setTransformedStepSize();
    }

    /** Set the parameter value in the transformed space and
     * update the parameter value in the original space
     * and the step size in the transformed space.<BR>
     * @param TransfomedValue parameter value in the transformed space
     */
    public final void setTransformedValue(double TransfomedValue)
    {
	transformed = TransfomedValue;
	original    = transformValue(TransfomedValue, 1);
	setTransformedStepSize();
    }

    /** Get the parameter value in the original space
     * @return parameter value in the original space
     */
    public final double getOriginalValue() { return original; }
 
    
    /** Get the parameter value in the transformed space
     * @return parameter value in the transformed space
     */
    public final double getTransformedValue(){ return transformed;  }

    /** Get the minimum restriction of the parameter
     * @return minimum restriction of the parameter
     */
    public final double getMinimum() { return minimum; }

    /** Get the maximum restriction of the parameter
     * @return maximum restriction of the parameter
     */
    public final double getMaximum() { return maximum; }


    /** Get the step size in the original space
     * @return step size in the original space
     */
    public final double getOriginalStepSize() { return originalStepSize; }

    /** Get the step size in the transformed space
     * @return step size in the transformed space
     */
    public final double getTransformedStepSize() { return transformedStepSize; }

    /** Get the kind of constraint
     * @return kind of constraint
     */
    public final int getKindOfConstraint() {return constraint;}

    /** transforms a value from the original to the transformed space or 
     * vice-versa, depending on the value of <CODE>direction</CODE><BR>
     * @param value the value that has to be transformed
     * @param direction <CODE>0</CODE> if transformation has to be done
     *       from original to transformed space, <CODE>1</CODE> otherwise
     * @return the transformed value
     */
    private double transformValue(double value, int direction)
    {
	if (direction == 0) 
	    {   //transform from original to transformed
		switch (constraint)
		    {
		    case 2:
			return Math.pow(value - minimum, 0.5);
		    case 3:
			return Math.asin(Math.pow((value - minimum) /
						  (maximum-minimum), 0.5)) ;
		    case 4:
			return Math.pow(maximum-value, 0.5);
		    default:
			return value;

		    }
	    }
	else   
	    {	//transform from transformed to original
		switch (constraint)
		    {
		    case 2:
			return minimum + Math.pow(value, 2.);
		    case 3:
			return minimum + (maximum-minimum) * 
			    Math.pow(Math.sin(value),2.);
		    case 4:
			return maximum - Math.pow(value, 2.);
		    default:
			return value;
		    }
	    }
    }

    /** Set the step size in the transformed space
	  */
    private void setTransformedStepSize()
    {
	if (constraint == 1)
	    {	// no restrictions
		transformedStepSize = originalStepSize;
	    }
	else
	    {	// some restrictions
		double HalSteSiz = originalStepSize / 2;
		double LowVal = original - HalSteSiz;
		double HigVal = original + HalSteSiz;
		// ensure that constraints are not crossed 
		if (LowVal < minimum)
		    {
			LowVal = minimum;
			HigVal = LowVal + 2 * HalSteSiz;
		    }
		if (HigVal > maximum)
		    {
			HigVal = maximum;
			LowVal = HigVal - 2 * HalSteSiz;
		    }
		transformedStepSize = Math.abs(transformValue(HigVal, 0) -
					       transformValue(LowVal, 0));
	    }
    }

    /** value in the original space */
    protected double original;
    /** value in the transformed space */
    protected double transformed;
    /** lower boundary */
    protected double minimum;
    /** upper boundary */
    protected double maximum;
    /** step size in the original space */
    protected double originalStepSize;
    /** step size in the transformed space */
    protected double transformedStepSize;
    /** kind of constraint<BR>
	   1: continuous, unconstrained,<BR>
	   2: continuous, lower bounded,<BR>
	   3: continuous, lower and upper bounded,<BR>
	   4: continuous, upper bounded */
    protected int constraint;
}







