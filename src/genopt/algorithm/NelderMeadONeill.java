package genopt.algorithm;

import genopt.algorithm.util.optimality.Perturber;
import genopt.algorithm.util.math.*;
import genopt.GenOpt;
import genopt.lang.OptimizerException;
import genopt.io.InputFormatException;
import genopt.simulation.SimulationInputException;
import genopt.db.ContinuousParameter;
import java.io.*;

/** Class for minimizing a function using the Simplex algorithm 
  * of Nelder and Mead with an extension by O'Neill.
  * <BR>
  * The restart criterion can be modified (optimality condition is then
  * only checked if we have just a partial inside contraction or a 
  * total contraction done and if <CODE>d(i)^T*d(i-1) &lt; 0</CODE>
  * holds where <CODE>d(i) = (x(i)-x(i-1))</CODE>.
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
  * @version GenOpt(R) 3.0.0 beta 2 (February 23, 2009)<P>
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

public class NelderMeadONeill extends Optimizer
{
    /** Constructor
     * @param genOptData a reference to the GenOpt object.<BR> 
     * <B>Note:</B> the object is used as a reference. 
     *              Hence, the datas of GenOpt are modified
     *              by this Class.
     * @exception OptimizerException if algorithm is used for problems
     *            with less than 2 independent variables
     * @exception Exception if an exception occurs
     * @exception IOException if an I/O exception occurs
     * @exception InputFormatException if an input format is wrong
     */
    public NelderMeadONeill(GenOpt genOptData)
	throws OptimizerException, IOException, Exception, InputFormatException
    {
	super(genOptData, 1);
	ensureOnlyContinuousParameters();
	// retrieve settings for Nelder Mead algorithm
	sqEps = getInputValueDouble("Accuracy");
	if (sqEps < Double.MIN_VALUE)
	    throwInputError("value bigger than zero");
	sqEps *= sqEps;

	cFactor = getInputValueDouble("StepSizeFactor");
	if (cFactor <= Double.MIN_VALUE) 
	    throwInputError("value bigger than zero");

	konvge = getInputValueInteger("BlockRestartCheck");

	if (konvge < 0) 
	    throwInputError("value equal or bigger than zero");
		
	modStoCri = getInputValueBoolean("ModifyStoppingCriterion");

	dimX = getDimensionX();
	dimXP1 = dimX + 1;

	if (dimX < 2)
	    {
		String s = new String();
		s = "NelderMeadONeill algorithm cannot be used for" + LS + 
		    "problems with less than 2 dimensions.";
		throw new OptimizerException(s);
	    }
	x = new Point[dimXP1];
	for (int i = 0; i < dimXP1; i++)
	    x[i] = new Point(getDimensionX(), 0, getDimensionF());
	
	// Constraints
	low = new double[dimX];
	upp = new double[dimX];
	con = new int[dimX];
	for (int i = 0; i < dimX; i++){
	    low[i] = getL(i);
	    upp[i] = getU(i);
	    con[i] = getKindOfConstraint(i);
	}
    }

    /** Runs the optimization process until a termination criteria
	  * is satisfied
	  * @param x0 initial point
	  * @return <CODE>-1</CODE> if the maximum number of iteration 
	  *                         is exceeded
	  *     <BR><CODE>+1</CODE> if the required accuracy is reached
	  * @exception Exception	  
	  * @exception OptimizerException
	  */
    public int run(Point x0) throws OptimizerException, Exception
    {
	boolean restart = false;
	int retFla = 0;
	int i, j, nue;
	int w = 0;
	int b = 0;
	double ssf = 1;
	int dimF = getDimensionF();
	Point[] xkP1  = new Point[dimXP1]; // xkP1 = x(k+1)
	for (i = 0; i < dimXP1; i++)
	    xkP1[i] = new Point(dimX, 0, dimF);
	Point xN      = new Point(dimX, 0, dimF);
	Point xNN     = new Point(dimX, 0, dimF);
	double[] xC      = new double[dimX];
	double[] xTemp   = new double[dimX];

	String[] com = new String[11];
	com[1] = "Establish initial simplex (1).";
	com[2] = "Reflection (2).";
	com[4] = "Expansion (4).";
	com[5] = "Partial outside contraction (5).";
	com[6] = "Partial inside contraction (6).";
	com[7] = "Total contraction to best known point (7).";
	com[10] = "Invoke simplex reconstruction (10).";

	final String comBesNowPoi = "Best known point in total contraction (7).";
	final String comNewSte    = "New step.";
	final String comBasPoi    = "Base point for optimality check.";

	double[] dNew   = new double[dimX];    // new movement direction of changed point
	double[] dOld   = new double[dimX];    // old movement direction of changed point
	double dNewL2;                        // L2norm of dNew
	int stepOld     = 0;                  // number of last step section
	double cosMov = -1;                   // inner product of the last two directions
	boolean directionTestReady   = false; // flag if direction test can be done or not
	boolean insideContraction    = false; // flag whether we have just a inside contraction
	// beyond us
	int step = 0;                             // number of step
	boolean tryRes;
	boolean iterate = true;
	int nexAllRes = konvge; // number of main iteration when next restart might
	// be enabled
								
	// Perturber for check of the optimaltity condition
	Perturber per = new Perturber(this);

	LinAlg.initialize(xC, 0);

	// optimization loop
	do
	    {
		switch (step)
		    {
		    case 0: // Initalization
			for (i = 0; i < dimX; i++) // starting point
			    x[0].setX(i, getX0(i));
			step = 1;
			break;
		    case 1: // Etablish the initial simplex
			for (i = 1; i < dimXP1; i++)
			    {
				for (j = 0; j < dimX; j++)
				    {   // getX is used on x[0] since x[0] may be reassigned after a failed optimality check
					if (i==j+1)
					    x[i].setX(j, x[0].getX(j) +
						      ssf * getDx( j, x[0].getX(j) ));
					else
					    x[i].setX(j, x[0].getX(j));
				    }
			    }
				// get the objective function values for the initial simplex
			if (restart)
			    {
				i = 1; // first point is already known from step 10
				restart = false;
			    }
			else
			    i = 0;

			for ( ; i < dimXP1; i++){
				x[i].setComment(com[1]);
				x[i] = getF(x[i]);
				report(x[i], SUBITERATION);
				if ( i == 0)
				    report(x[i], MAINITERATION);
				checkObjectiveFunctionValue( x[i] );	
			    }
			step = 2;
			break;
		    case 2: // determine worst and best point for the normal reflection
			w = getWorst();
			b = getBest();
			xC = getXCenter(w);
			xN.setComment(com[2]);
			xN.setX( reflect(xC, x[w].getX()) );
			xN = getF(xN);
			report(xN, SUBITERATION);
			stepOld = step;
			step = (xN.getF(0) < x[b].getF(0)) ? 4 : 3;
			break;
		    case 3: // compare trial with other vertices
			nue = dimXP1 - rank(xN.getF(0));    // if nue is 0, we got the worst point

			if (nue == 0) // we got even a worser point than we had
			    step = 6;
			else if (nue == 1) // we got a worse point but not that worse as the last was
			    step = 5;
			else // we got a good point
			    {
				xkP1[w] = (Point)xN.clone();
				step = 8;
			    }
			break;
		    case 4: //Expansion (try if further expansion of point is successfull)
			xNN.setComment(com[4]);
			xNN.setX( LinAlg.subtract(LinAlg.multiply(2., 
								  xN.getX()), xC) );
			xNN = getF(xNN);
			report(xNN, SUBITERATION);

			if (xNN.getF(0) < x[b].getF(0))
			    xkP1[w] = (Point)xNN.clone();  // expansion was successful
			else
			    xkP1[w] = (Point)xN.clone();
			stepOld = step;
			step = 8;
			break;
		    case 5: // Partial outside contraction
			xNN.setComment(com[5]);
			xNN.setX( LinAlg.multiply(0.5, LinAlg.add(xC, xN.getX())) );
			xNN = getF(xNN);
			report(xNN, SUBITERATION);
			if (xNN.getF(0) <= xN.getF(0))
			    {
				xkP1[w] = (Point)xNN.clone();
				stepOld = step;
				step = 8;
			    }
			else
			    step = 7;
			break;
		    case 6: // Partial inside contraction
			xNN.setComment(com[6]);
			xNN.setX( LinAlg.multiply(0.5, LinAlg.add(xC, x[w].getX())) );
			xNN = getF(xNN);
			report(xNN, SUBITERATION);

				/* the inequality (fNN >= fX[w]) is changed so that we 
				   get a total contraction
				   if some vertices are in a null space.
				   Nelder Mead have in their paper (1965) the strict
				   inequality (fNN > fX[w])    02/16/99 wm
				*/
			if (xNN.getF(0) >= x[w].getF(0)) 
			    step = 7;
			else
			    {
				xkP1[w] = (Point)xNN.clone();
				stepOld = step;
				step = 8;
				insideContraction = true;
			    }
			break;
		    case 7: // Total contraction to best point
				// construct new simplex
			xkP1[b] = (Point)x[b].clone();
			xkP1[b].setComment(comBesNowPoi);
			report(xkP1[b], SUBITERATION);
			report(xkP1[b], MAINITERATION);				
				
			xTemp = x[b].getX();

			for (i = 0; i < dimXP1; i++)
			    {
				if ( i != b)
				    {
					xkP1[i].setComment(com[7]);
					xkP1[i].setX( LinAlg.multiply(0.5, 
								      LinAlg.add(xTemp, 
										 x[i].getX())) );
					xkP1[i] = getF(xkP1[i]);
					report(xkP1[i], SUBITERATION);
					report(xkP1[i], MAINITERATION);
					checkObjectiveFunctionValue( xkP1[i] );
				    }
			    }
			insideContraction = true;
			step = 9;
			break;
		    case 8: // normal iteration loop
			for (i = 0; i < dimXP1; i++)
			    {
				if ( i != w)
				    xkP1[i] = (Point)x[i].clone();
				else
				    {
					xkP1[w].setComment( com[stepOld] );
					report(xkP1[w], MAINITERATION);
					checkObjectiveFunctionValue( xkP1[w] );
				    }
			    }
			step = 9;
			break;
		    case 9: // Termination criterion
			// increase k <- k+1
			// Note: 9 is only entered from 7 or 8
			
			// determine movement direction of simplex
			if (modStoCri) {
			    System.arraycopy(dNew, 0, dOld, 0, dimX);
			    // new (current) search direction
			    double[][] temp1 = new double[dimXP1][dimX];
			    double[][] temp2 = new double[dimXP1][dimX];
			    for (i = 0; i < dimXP1; i++)
				{
				    temp1[i] = xkP1[i].getX();
				    temp2[i] = x[i].getX();
				}
			    dNew = LinAlg.subtract(
						   LinAlg.getCenter(temp1), LinAlg.getCenter(temp2));
			    dNewL2 = LinAlg.twoNorm(dNew);
			    if (dNewL2 > Double.MIN_VALUE)
				dNew = LinAlg.multiply(1/dNewL2, dNew);
			    cosMov = LinAlg.innerProduct(dOld, dNew);
			}
			
			///////////////////////////////////////////////////////////
			// update points
			for (i = 0; i < dimXP1; i++)
			    x[i] = (Point)xkP1[i].clone();
			
			if (checkMaxIteration()) {
			    retFla = -1;
			    iterate = false;
			    break;
			}
			
			// check for restart
			if (modStoCri)
			    tryRes = (insideContraction && (cosMov <= 0) &&
				      (nexAllRes <= getMainIterationNumber()) &&
				      restartCriterion());
			else
			    tryRes = (nexAllRes <= getMainIterationNumber()) &&
				restartCriterion();
			
			if (tryRes) {
			    // try a restart
			    step = 10;
			    nexAllRes = getMainIterationNumber() + konvge + 1;
			}
			else
			    {
				// do a usual iteration loop
				if (writeStepNumber()) {
				    // get the best point
				    b = getBest();
				    x[b].setComment(comNewSte);
				    x[b] = increaseStepNumber(x[b]);
				    report(x[b], MAINITERATION);
				    report(x[b], SUBITERATION);
				}
				step = 2;
			    }
			// reset contraction flag
			insideContraction = false;
			break;
		    case 10: // restart test (note that w points to the new vertex)
			ssf = cFactor; // factor for step size
			b   = getBest();
			x[b].setComment(comBasPoi);
			report(x[b], SUBITERATION);
			per.perturb(x[b], ssf);
			if (per.gotOptimum()) {	// we are at the minimum
			    retFla = 1;
			    reportMinimum();
			    iterate = false;
			}
			else {
			    restart = true; // to prevent a recalculation of the 0-th pt.
			    x[0] = per.getOptimalPoint();
			    x[0].setComment(com[10]);
			    report(x[0], MAINITERATION);
			    if (writeStepNumber())
				{
				    x[0].setComment(comNewSte);
				    x[0] = increaseStepNumber(x[0]);
				    report(x[0], MAINITERATION);
				}
			    step = 1;
			}
			break;
		    }
	    }
	while (iterate);
	return retFla;
    }


    /** Determines whether a restart with a smaller simplex should be tried or not
	  * @return <CODE>true</CODE> if restart can be tried, <CODE>false</CODE> otherwise
	  */
    protected boolean restartCriterion()
    {
	double f, S1, S2;
	double[] fX = new double[dimXP1];
	for (int i = 0; i < dimXP1; i++)
	    fX[i] = x[i].getF(0);
	S1 = LinAlg.twoNorm(fX);
	S1 *= S1;
	S2 = LinAlg.oneNorm(fX);
	S2 *= S2;
	S2 /= (double)(dimXP1);
	f = (S1 - S2) / (double)dimX;
	if ( f < (sqEps))
	    return true;
	else
	    return false;
    }


    /** Gets the number of the worst point
	  * @return the index w such that f(x(k,w)) = max(f(x(k,i)), i=0..dimX(x))
	 */
    protected int getWorst()
    {
	int w = 0;
	for(int i = 1; i < dimXP1; i++)
	    {
		if (x[i].getF(0) >= x[w].getF(0))
		    w = i;
	    }
	return w;
    }

    /** Gets the number of the best point
	  * @return the index w such that f(x(k,w)) = min(f(x(k,i)), i=0..dimX(x))
	 */
    protected int getBest()
    {
	int b = 0;
	for(int i = 1; i < dimXP1; i++)
	    {
		if (x[i].getF(0) < x[b].getF(0))
		    b = i;
	    }
	return b;
    }

    /** Gets the center point for the reflection
	  * @param worst the index of the worst point
	  * @return the center point for the reflection, such that
	  * <CODE>x = 1 / dimX(x) * sum(x(k,i), i = 0..dimX(x), i != worst)</CODE>
	  */
    protected double[] getXCenter(int worst)
    {
	double[] xCenter = new double[dimX];
		
	LinAlg.initialize(xCenter, 0);
		
	for(int i = 0; i < dimXP1; i++)
	    {
		if ( i != worst) xCenter = LinAlg.add(xCenter, x[i].getX());
	    }
	return LinAlg.multiply( 1/((double)dimX), xCenter);
    }
	
    /** Reflects a point
	  * @param xCenter the center point for normal reflection
	  * @param xWorst the point that has to be reflected
	  * @return the new point, such that <CODE>xNew = 2 * xCenter - xWorst</CODE>
	  */
    protected double[] reflect(double[] xCenter, double[] xWorst)
    {
	return LinAlg.subtract( LinAlg.multiply(2, xCenter), xWorst) ;
    }

    /** Compares a trial with the other vertices
	  * @param f the trial to be compared
	  * @return the number of ranking where 0 stands for the best point
	 */
    protected int rank(double f)
    {
	int r = 0;
	for(int i = 0; i < dimXP1; i++)
	    {
		if (f >= x[i].getF(0))
		    r++;
	    }
	return r;
    }

    /** The points */
    protected Point[] x;
    /** The lower bounds */
    protected double[] low;
    /** The upper bounds */
    protected double[] upp;
    /** The kind of constraints */
    protected int[] con;
    /** The dimension of the problem */
    protected int dimX;
    /** The dimension of the problem plus 1 */
    protected int dimXP1;
    /** The required accuracy of the variance (eps*eps) */
    protected double sqEps;
    /** A flag whether the convergence check is only allowed after 'konvge' 
	  * main iterations */
    protected int konvge;
    /** The step size factor for perturbation and reconstruction*/
    protected double cFactor;
    /** A flag whether the stopping criterion has to be modified or not */
    protected boolean modStoCri;
}
