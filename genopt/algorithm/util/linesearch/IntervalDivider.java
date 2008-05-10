package genopt.algorithm.util.linesearch;

import genopt.lang.OptimizerException;
import genopt.algorithm.Optimizer;
import genopt.algorithm.util.math.LinAlg;
import genopt.algorithm.util.math.Point;

import java.io.IOException;

/** Abstract class for doing a line search using
  * an interval division method.
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
  * @version GenOpt(R) 2.0.0 (Jan. 5, 2004)<P>
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

public abstract class IntervalDivider
{
    /** System dependent line separator */
    protected final static String LS = System.getProperty("line.separator");

    /** The default value for number of interval reductions */
    protected static final int NINTREDMAXDEF = 10;

    /** Constructor
     * @param o a reference to the Optimizer object
     */
    public IntervalDivider(Optimizer o)
    {
        opt = o;
        dimX = opt.getDimensionX();
        // initial value
        x0   = new Point(dimX, 0, dimF);
        dx   = new double[dimX];
		// minimum value
        xMin = new Point(dimX, 0, dimF);
	x1   = new Point(dimX, 0, dimF);
	x2   = new Point(dimX, 0, dimF);
	xLow = new Point(dimX, 0, dimF);
	xUpp = new Point(dimX, 0, dimF);
        setMaxIntRed(NINTREDMAXDEF);  // default stopping mode, the maximum number of iterations
    }

    /** Gets the point with the lowest function value
      *@return the point with the lowest function value
      */
    public Point getXMin()
    {
        return (Point)xMin.clone();
    }

    /** Gets the reduction factor q = I(n+1)/I(n)
      *@return the reduction factor q = I(n+1)/I(n)
      */
    protected abstract double getReductionFactor();

    /** Runs a line search in the interval from <CODE>xS</CODE> to
      * <CODE>xE</CODE>.
      * @param xS the start point of the interval
      * @param xE the end point of the interval
      * @return <CODE>-2</CODE> if the objective function has a null space, and
      *                         the stopping criteria is not equal to <CODE>1</CODE>
      *     <BR><CODE>-1</CODE> if the maximum number of iteration 
      *                         is exceeded
      *     <BR><CODE>+1</CODE> if the required accuracy is reached
      * @exception OptimizerException
      * @exception OptimizerException if an OptimizerException occurs
      * @exception Exception if an Exception occurs
      */
    public int run(Point xS, Point xE)
        throws OptimizerException, Exception
    {
        boolean terminate = false;
        boolean f1eqf2    = false;
        x0 = (Point)xS.clone();
        x3 = (Point)xE.clone();
        
        dx = LinAlg.subtract(x3.getX(), x0.getX());
        double I = 1.; // interval length (in terms of alpha, which is normalized)
        nIntRed = 0;   // zero-based step of interval division
        I *= getReductionFactor();
        x2.setX( LinAlg.add(x0.getX(), LinAlg.multiply(I, dx)) );
        nIntRed++;
        I *= getReductionFactor();
        x1.setX( LinAlg.add(x0.getX(), LinAlg.multiply(I, dx)) );
        // initial function evaluation
        
	x1 = getF(x1);
        x2 = getF(x2);

        do
        {
            nIntRed++;
            I *=getReductionFactor();

            if (x2.getF(0) < x1.getF(0))
            {   // data management
                fLowBor = x1.getF(0); // we need that for one of the stopping criteria
                x0 = (Point)x1.clone();
                x1 = (Point)x2.clone();
                // new point
                x2.setX( LinAlg.subtract(x3.getX(), LinAlg.multiply(I, dx)) );
                x2 = getF(x2);
            }
            else
            {   // data management
                fLowBor = x2.getF(0); // we need that for one of the stopping criteria
                x3 = (Point)x2.clone();
                x2 = (Point)x1.clone();
                // new point
                x1.setX( LinAlg.add(x0.getX(), LinAlg.multiply(I, dx)) );
                x1 = getF(x1);
            }

            // check for null space of objective function, unless
            // stoCri is equal to one
            if (stoCri != 1 && x1.getF(0) == x2.getF(0))
            {
                if (f1eqf2)            // the last two were also equal, so the
                    terminate = true;  // current three are equal
                f1eqf2 = true;
            }
            else // reset flag
                f1eqf2 = false;
        } while (iterate() && !terminate);

        // tolerance achieved or maximum number of iteration exceeded
        // store minimum value
        if (x1.getF(0) < x2.getF(0))
        {
            xLow = (Point)x0.clone();
            xMin = (Point)x1.clone();
            xUpp = (Point)x2.clone();            
        }
        else
        {
            xLow = (Point)x1.clone();
            xMin = (Point)x2.clone();
            xUpp = (Point)x3.clone();            
        }
        // if we got a null space
        if (terminate) return -2;
        // check whether the maximum number of iteration is exceeded
        if (stoCri == 1)
            if (!isDFltdFMin()) return -1;
        // if search has been successful
        return +1;
    }

    /** Sets the fraction of the desired uncertainty interval (0..1)
      *@param dx the normalized fraction of the uncertainty interval
      */
    public abstract void setUncertaintyInterval(double dx);

    /** Sets the minimal absolut difference between the lowest
      * function values as the stopping criteria
      *@param dFMinimal the minimal difference between the lowest
      *       function values that has to be obtained before the search stops
      *@param nMax the maximum number of iteration before the
      *       search stops (in case that <CODE>dFMinimal</CODE>
      *       cannot be obtained within a reasonable number of trials)
      *@exception OptimizerException
      */
    public void setAbsDFMin(double dFMinimal, int nMax) throws OptimizerException
    {
        setMaxIntRed(nMax); // "emergency break"
        dFMin = dFMinimal;
        stoCri = 1;
    }   
   
    /** Sets the maximum number of interval reductions
      *@param n the maximum number of interval reductions
      */
    public void setMaxIntRed(int n)
    {
        // since nIntRedMax = {0, 1, 2, ... ,(n-1)},
        // n is the number of reduction, we have to subtract 1 from n
        nIntRedMax = (n > 1) ? (n-1) : (NINTREDMAXDEF-1);
        stoCri = 0; // the stopping criteria
    }
    
    /** gets the lower bound of the uncertainty interval
      *@return the lower bound of the uncertainty interval
      */
    public Point getXLower()
    {
        return (Point)xLow.clone();
    }

        /** gets the upper bound of the uncertainty interval
      *@return the upper bound of the uncertainty interval
      */
    public Point getXUpper()
    {
        return (Point)xUpp.clone();
    }

    /** Checks whether the iteration has to be continued.
      *@return <CODE>true</CODE> if iteration has to be continued,
      *        <CODE>false</CODE> if tolerance has been achieved, or
      *        the required number of interval reductions has been achieved.
      */
    protected boolean iterate()
    {
        switch (stoCri)
        {
            case 1:
                    if (isDFltdFMin()) return false;
                    // continue to check the maximum number of iteration
            default:
                return (nIntRed == nIntRedMax) ? false : true;
        }
    }

    /** Checks whether the difference between the lower of either <CODE>f1</CODE>
      * or <CODE>f2</CODE> and the lower of either <CODE>f0</CODE> or <CODE>f3</CODE>
      * is smaller than the prescribed <CODE>dFMin</CODE>
      *@return <CODE>true</CODE> if difference is smaller, <CODE>false</CODE> otherwise
      */
    private boolean isDFltdFMin()
    {
        double fLow = (x2.getF(0) < x1.getF(0)) ? x2.getF(0) : x1.getF(0);
        double dF = Math.abs(fLowBor-fLow);
        return (dF < dFMin) ? true : false;
    }

    /** Evaluates the objective function, reports the results, and checks for
      * a null space of the objective function
      *@param x the point being evaluated
      *@exception OptimizerException if an OptimizerException occurs
      *@exception Exception if an Exception occurs
      */
    private Point getF(Point x) throws OptimizerException, Exception
    {
        Point r = opt.getF(x);
        r.setComment("Linesearch.");
        opt.report(r, opt.SUBITERATION);
        return r;
    }



   /** The reference to the Optimizer object */
    protected Optimizer opt;
    /** The number of independent variables */
    protected int dimX;
    /** The number of function values */
    protected int dimF;
    /** The maximal width of the interval (<CODE>dx=xEnd-x0</CODE>) */
    double[] dx;
    /** The lowest value on abscissa (start of interval)*/
    protected Point x0;
    /** The point with the lowest obtained function value */
    protected Point xMin;

    /** The 2nd lowest value on abscissa */
    protected Point x1;
    /** The 3rd lowest value on abscissa */
    protected Point x2;
    /** The highest value on abscissa */
    protected Point x3;
    /** The lower border of the uncertainty interval after stop of algorithm */
    protected Point xLow;
    /** The upper border of the uncertainty interval after stop of algorithm */
    protected Point xUpp;
 
    /** The lower function value of the 2 border
      * (<CODE>x0<CODE> or <CODE>x3</CODE>) that limit the interval
      * of uncertainty */
    private double fLowBor;

    /** The counter for number of interval reductions */
    protected int nIntRed;
    /** The maximum number of interval reductions */
    protected int nIntRedMax;
    
    /** The stopping criteria<PRE>
      0: number of interval reduction
      1: maximum difference of the best 3 function values</PRE> */
    private int stoCri;
    
    /** The minimal difference between the lowest
      * 3 function values that has to be obtained before the search stops
      * (only used if <CODE>stoCri=1</CODE>  */
    private double dFMin;
}







