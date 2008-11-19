package genopt.algorithm.util.linesearch;

import genopt.GenOpt;
import genopt.lang.OptimizerException;
import genopt.algorithm.Optimizer;
import genopt.algorithm.util.linesearch.IntervalDivider;
import genopt.algorithm.util.math.Point;
import genopt.io.InputFormatException;

import java.io.*;

/** Abstract class for doing a <I>one-dimensional minimization</I>
  * in a finite interval.<BR>
  * To do the optimization, a unimodal interval of the
  * parameter being varied must be specified.
  * The interval is given by the lower and upper bound
  * of the parameter specified in the command file.
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
  * @version GenOpt(R) 3.0.0 alpha 2 (November 18, 2008)<P>
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

public abstract class ModelFiniteIntervalSearcher extends Optimizer
{
    /** Constructor
     * @param genOptData The GenOpt object.<BR> 
     * <B>Note:</B> the object is used as a reference. 
     *              Hence, the datas of GenOpt are modified
     *              by this Class.
     * @exception OptimizerException if algorithm is used for problems
     *            with more than 1 independent variable, or if
     *            independent variable does not have a lower and upper bound
     * @exception IOException if an I/O exception occurs
     * @exception Exception if an exception occurs
     */
    public ModelFiniteIntervalSearcher(GenOpt genOptData)
        throws OptimizerException, IOException, Exception {
        super(genOptData, 0);
	ensureOnlyContinuousParameters();
		
        if (getDimensionX() > 1)
	    {
		String s = "The selected optimization algorithm cannot be used for" + LS + 
		    "problems with more than 1 independent variable.";
		throw new OptimizerException(s);
	    }
		
	if (getKindOfConstraint(0) != 3)
	    {
		String s = "Min and Max of parameter must be specified for using" + LS +
		    "the selected optimization algorithm.";
		throw new OptimizerException(s);
	    }
    }
	
    /** Initializes the IntervalDivider instance
      * @exception OptimizerException
      * @exception Exception
      * @exception InputFormatException	  
	  * @exception OptimizerException
	  */
    protected void initialize()
	throws Exception, InputFormatException, OptimizerException
    {
	// parse parameter
	String[] par = {"AbsDiffFunction", "IntervalReduction", "}"};
	int iPar = 0;
	if (isNextToken(par[iPar]))
	    {	// Absolut difference of objective function values
		double diffFun = getInputValueDouble(par[iPar]);
		if (diffFun < Double.MIN_VALUE)
		    {
			String s = "value greater than " + Double.MIN_VALUE;
			throwInputError(s);
		    }
		id.setAbsDFMin(diffFun, getMaxIterationNumber());
	    }
	else if (isNextToken(par[++iPar]))
	    {	// Interval Reduction (normalized)
		double dx = getInputValueDouble(par[iPar]);
		if (dx < Double.MIN_VALUE || dx >= 1)
		    {
			String s = "value greater than " +
			    Double.MIN_VALUE + " and less than 1";
			throwInputError(s);
		    }
		id.setUncertaintyInterval(dx);
	    }
	else if (isNextToken(par[++iPar])) // no keyword specified
	    id.setMaxIntRed(getMaxIterationNumber());
	else if (!isNextToken(par[iPar]))
	    throwInputError(par[0] + "', '" + par[1] + "', or '" + par[2]);
    }

    /** Runs the optimization process until a termination criteria
      * is satisfied
      * @param xIni initial point
      * @return <CODE>-1</CODE> if the maximum number of iteration 
      *                         is exceeded
      *     <BR><CODE>+1</CODE> if the required accuracy is reached
      * @exception Exception    
      * @exception OptimizerException
      */
    public int run(Point xIni)
	throws OptimizerException, Exception
    {
	Point x0 = (Point)xIni.clone();
	Point x3 = (Point)xIni.clone();
	double[] temp = new double[1];
        temp[0] = getL(0);
        x0.setX(temp);
	temp[0] = getU(0);
        x3.setX(temp);
	int r = id.run(x0, x3);
	if (r==1)
	    reportMinimum();
	else
	    reportCurrentLowestPoint();

	Point xLow = id.getXLower();
	Point xUpp = id.getXUpper();


	String s;

	if (r == -2)
	    {
		s = "Optimization terminated with error.";
		appendToOutputListing(LS + s);
	    }

	s = LS + "Result overview" + LS + "***************" + LS ;
	s += "Lower border of uncertainty interval        : " + xLow.getX(0);
	println(s);  appendToOutputListing(LS + s);
	s =  "Upper border of uncertainty interval        : " + xUpp.getX(0);
	println(s);  appendToOutputListing(LS + s);
	s =  "Mid point of uncertainty interval           : " + (xUpp.getX(0)+xLow.getX(0))/2;
	println(s);  appendToOutputListing(LS + s);
	s =  "Length of uncertainty interval              : " + (xUpp.getX(0) - xLow.getX(0));
	println(s);  appendToOutputListing(LS + s);
	s =  "Normalized reduction of uncertainty interval: " +
            (xUpp.getX(0) - xLow.getX(0))/(x3.getX(0)-x0.getX(0)) + LS;
	println(s);  appendToOutputListing(LS + s);
		
	if (r == -2)
	    {
		throw new OptimizerException(
					     "Nullspace in line search. Linesearch terminated.");
	    }			
	return r;
    }
    /** The IntervalDivider that performs the line search */
    protected IntervalDivider id;
}
