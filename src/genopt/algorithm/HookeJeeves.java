package genopt.algorithm;
import java.io.IOException;
import genopt.GenOpt;
import genopt.simulation.SimulationInputException;
import genopt.algorithm.util.math.*;
import genopt.lang.OptimizerException;
import genopt.io.InputFormatException;

/** Class for minimizing a function using the Hooke-Jeeves
  * algorithm.<P>
  *
  * <B>Note</B>: This class has been replaced by {@link GPSHookeJeeves}
  *  and is here only for compatibility with old input files.
  * <p>
  * This algorithm has the following modifications to the original
  * Hooke-Jeeves algorithm implemented:
  * <ul><li>
  * Altering of exploration direction and program flow
  * according to Bell and Pike.<BR>
  * See: Bell, M., Pike, M.C.; Remark on Algorithm 178;
  * <I>Comm. ACM, Vol. 9</I> (Sept. 1966), p. 685-6.
  * <li>
  * Exit algorithm according to De Vogelaere.<BR>
  * See: De Vogelaere, R.; Remark on Algorithm 178;
  * <I>Comm. ACM, Vol. 11</I> (Jul. 1968), p. 498.
  * <li>
  * Different step sizes for each variable, similarly than
  * Smith's proposal.
  * In contrast to Smith, GenOpt's implementation
  * initializes each variable with its own step size and not
  * with DELTA * abs(psi(k)), where DELTA is the initial step length and
  * psi(1:K) the starting point of the search.<BR>
  * See: Smith Lyle B.; Remark on Algorithm 178;
  * <I>Comm. ACM, Vol. 12</I> (Nov. 1969), p. 638.
  * </ul>
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

public class HookeJeeves extends Optimizer
{
    private static final String SUCEXPMOV = "Exploration move successfull at ";
    private static final String FAIEXPMOV = "Exploration move failed      at ";
    private static final String POSDIR    = "+d";
    private static final String NEGDIR    = "-d";
    private static final String PATMOV    = "Pattern move.";
    private static final String INCSTENUM = "Increased step number.";

    /** Constructor
     * @param genOptData a reference to the GenOpt object.<BR>
     * <B>Note:</B> the object is used as a reference.
     *              Hence, the data of GenOpt are modified
     *              by this Class.
     * @exception OptimizerException if algorithm is used for problems
     *            with less than 2 independent variables
     * @exception Exception
     *@exception IOException if an I/O exception occurs
     * @exception InputFormatException
     */
    public HookeJeeves(GenOpt genOptData)
        throws OptimizerException, IOException, Exception, InputFormatException {
        super(genOptData, 0);
	ensureOnlyContinuousParameters();		
	
        dimX = getDimensionContinuous();
	dimF = getDimensionF();

        // retrieve settings for Hooke Jeeves algorithm
        steRed = getInputValueDouble("StepReduction");
        if (steRed < Double.MIN_VALUE || steRed >= 1)
            throwInputError("value bigger than 0 and less than 1");

	nMaxSteRed = getInputValueInteger("NumberOfStepReduction");
	if (nMaxSteRed < 1)
	    throwInputError("integer bigger than 0");

        if (dimX < 2){
	    final String s = "HookeJeeves algorithm can only be used for" + LS +
		"problems with at least 2 continuous independent variables.";
	    throw new OptimizerException(s);
	}

	// check initial point for feasibility
	String errMes = "";
	for (int i=0; i < dimX; i++)
	    if (getX(i) < getL(i))
		errMes += getVariableNameContinuous(i) + "=" + 
		    getX(i) + ": Lower bound " + getL(i) + LS;
	    else if (getX(i) > getU(i))
		errMes += getVariableNameContinuous(i) + "=" + 
		    getX(i) + ": Upper bound " + getU(i) + LS;
	if (errMes.length() > 0)
	    throw new OptimizerException("Initial point not feasible:" + 
					 LS + errMes);

        sr = new double[dimX];
	LinAlg.initialize(sr, 1.);
	
        rb = new double[dimX];
	
	nSteRed = 0;
	absSr = 1; // (step not reduced yet)
    }

    /** Runs the optimization process until a termination criteria
     * is satisfied
     * @return <CODE>-1</CODE> if the maximum number of iteration
     *                         is exceeded
     *     <BR><CODE>+1</CODE> if the required accuracy is reached
     * @exception Exception
     * @exception OptimizerException
     */
    public int run() throws OptimizerException, Exception{
        boolean iterate = true;
        int i, step;
        int retFla = 0;
        double[] pb = new double[dimX]; // previous base point
        double[] cb = new double[dimX]; // current base point
        double[] fc; // function value at the current base point
	Point temPoi; // Point object, for report used only
		
        // evaluate function at base point
        for (i=0; i < dimX; i++) cb[i] = getX(i);
        fc = getF(cb);
	fr = new double[fc.length];
	fp = new double[fc.length];
	temPoi = new Point(cb, fc, getStepNumber(), "Initial point.");
        report(temPoi, SUBITERATION);
        report(temPoi, MAINITERATION);
        step = 1;
	
        // optimization loop
	iteration: do{
	    switch (step){
	    case 1: // initial exploration
		System.arraycopy(fc, 0, fp, 0, fc.length);
		System.arraycopy(cb, 0, rb, 0, dimX);
		if (explore() == -1) break iteration;
		step = (fp[0] < fc[0]) ? 2 : 3;
		break;
	    case 2: // basic iteration
                    // update sign
		for (i = 0; i < dimX; i++) {
		    if ( (rb[i] > cb[i] && sr[i] < 0) ||
			 (rb[i] < cb[i] && sr[i] > 0) )
			sr[i] = -sr[i];
		}
		// update basis and function values
		System.arraycopy(cb, 0, pb, 0, dimX);
		System.arraycopy(rb, 0, cb, 0, dimX);
		System.arraycopy(fp, 0, fc, 0, fp.length);
		// make step
		rb = LinAlg.subtract(LinAlg.multiply(2., rb), pb);
		fp = getF(rb); // note that fp=f(rb) and not fr=f(rb)
		temPoi = new Point(rb, fp, getStepNumber(), PATMOV);
		report(temPoi, SUBITERATION);
		report(temPoi, MAINITERATION);
		if (checkMaxIteration()) break iteration;
		
		checkObjectiveFunctionValue();
		
		if (explore() == -1) break iteration;
		
		// modif. of Bell and Pike
		if (fp[0] >= fc[0])
		    step = 1;
		else{
		    step = 3;
		    for (i = 0; i < dimX; i++)
			if (StrictMath.abs(rb[i]-cb[i]) > 0.5 * absSr * getDx(i)) {
			    step = 2;
			    i = dimX;
			}
		}
		break;
	    case 3: // reduction of step size
		absSr *= steRed;
		if (nSteRed == nMaxSteRed) {
		    iterate = false;
		    reportMinimum();
		    retFla = 1;
		}
		else{
		    for (i = 0; i < dimX; i++)
			sr[i] = (sr[i] > 0) ? absSr : -absSr;
		    nSteRed++;
		    println("Relative step size reduced to " + absSr);
		    step = 1;
		    // increase step number
		    // (used if user implements penalty function, barrier function
		    // or slack variables )
		    increaseStepNumber();
		    fp = getF(rb); // note that fp=f(rb) and not fr=f(rb)
		    temPoi = new Point(rb, fp, getStepNumber(), INCSTENUM);
		    report(temPoi, SUBITERATION);
		    report(temPoi, MAINITERATION);
		}
	    }
        } while (iterate);
	
        if (iterate){ // maximum number of simulation exceeded
	    reportCurrentLowestPoint();
	    retFla = -1;
	}
        return retFla;
    }
    
    /** Makes exploration step around the base <CODE>rb</CODE><BR>
     * <B>Note:</B> If return value is 0, then <CODE>rb</CODE> and
     *              <CODE>fp</CODE> are modified if and only if
     *              a step has been successful, i.e., led to a reduction of
     *              the function value
     * @return <CODE>-1</CODE> if the maximum number of iteration
     *                         is exceeded
     *     <BR><CODE>0</CODE> otherwise
     */
    protected int explore()
        throws Exception, OptimizerException {
        // function value resulting from the current move
        LinAlg.initialize(fr,0);
        double cen;
        String dir = "";

        for (int i = 0; i < dimX; i++) {
	    cen = rb[i]; // center of test
	    // check first direction
	    rb[i] = cen + sr[i] * getDx(i);

	    fr = getF(rb);
	    dir = (sr[i] > 0) ? POSDIR : NEGDIR;
	    
	    if (fr[0] >= fp[0]){   // check second direction
		report(new Point(rb, fr, getStepNumber(),
				 FAIEXPMOV + 
				 getVariableNameContinuous(i) +
				 dir + getVariableNameContinuous(i) + '.'), 
		       SUBITERATION);
		if (checkMaxIteration()) return -1;
		checkObjectiveFunctionValue();
		
		sr[i] = -sr[i];
		rb[i] = cen + sr[i] * getDx(i);
		fr = getF(rb);
		dir = (sr[i] > 0) ? POSDIR : NEGDIR;
		
		if (fr[0] >= fp[0]){
		    // reset checked coordinate value of base since we could
		    // not make any success in pos. and neg. direction
		    report(new Point(rb, fr, getStepNumber(),
				     FAIEXPMOV + 
				     getVariableNameContinuous(i) +
				     dir + 
				     getVariableNameContinuous(i)  + '.'),
			   SUBITERATION);
		    rb[i] = cen;
		}
	    }
	    
	    if (fr[0] < fp[0]){
		report(new Point(rb, fr, getStepNumber(),
				 SUCEXPMOV + 
				 getVariableNameContinuous(i) + dir + 
				 getVariableNameContinuous(i) + '.'), 
		       SUBITERATION);
		System.arraycopy(fr, 0, fp, 0, fr.length);
	    }
	    checkObjectiveFunctionValue();
	    if (checkMaxIteration()) return -1;
	    
	}
        return 0;
    }
    
    /** Checks whether the last objective function value has already been
     * obtained previously.<BR>
     * If it has been obtained previously, an information message is reported.
     * If the maximum number of matching function value is obtained, an exception
     * is thrown.
     *@exception OptimizerException thrown if the maximum number of matching
     *           function value is obtained
     */
    public void checkObjectiveFunctionValue()
	throws OptimizerException {
	if (checkObjFun) super.checkObjectiveFunctionValue();
    }
    
    
    /** Reports the new trial
     *@param x the point to be reported
     *@param MainIteration <CODE>true</CODE> if step was a main iteration or
     *       <CODE>false</CODE> if it was a sub iteration
     *@exception IOException if an I/O error in the optimization output files
     *               occurs
     */
    public void report(Point x, boolean MainIteration)
	throws IOException{
	boolean report = false;
	for (int i = 0; i < dimF; i++)
	    if ( x.getF(i) != Double.MAX_VALUE){
		report = true;
		i = dimF;
	    }
	if (report) super.report(x, MainIteration);
    }


    /** Gets the objective function value and registers it into
     * the data base
     *@exception OptimizerException if an OptimizerException occurs or
     *           if the user required to stop GenOpt
     *@exception SimulationInputException if an error in writing the
     *           simulation input file occurs
     *@exception Exception if an I/O error in the simulation input file occurs
     * @return an array with the values of the objective function, in the same order
     *           as specified in the input file
     */
    public double[] getF(double[] x)
	throws SimulationInputException, OptimizerException, Exception {
	// first, check for feasibility
	if (! isFeasiblePoint(x) ){ // unfeasible
	    checkObjFun = false;
	    double[] r = new double[dimF];
	    for (int i = 0; i < dimF; i++)
		r[i] = Double.MAX_VALUE;
	    return r;
	}
	else{
	    Point p = new Point(dimX, 0, dimF);
	    p.setX(x);
	    final int iSim = getSimulationNumber();
	    p = super.getF(p);
	    checkObjFun = ( iSim < getSimulationNumber() );
	    
	    return p.getF();
	}
    }
    
    /** Checks whether a point is feasible
     *@param x the point to be checked
     *@return <CODE>true</CODE> if point is feasible,
     *        <CODE>false</CODE> otherwise
     */
    private boolean isFeasiblePoint(double[] x) {
	for(int i = 0; i < dimX; i++){
	    float xi = (float)(x[i]);
	    if (xi > (float)(getU(i)) || xi < (float)(getL(i)) )
		return false;
	}
	return true;
    }
    
    /** Checks if the maximum number of iteration is exceeded
     * @return <CODE>true</CODE> if the number of simulation is
     *         equal or bigger than the maximum number of iteration,
     *         <CODE>false</CODE> otherwise
     */
    protected boolean checkMaxIteration() {
	return (getSimulationNumber() >= getMaxIterationNumber());
    }

    /** The base point resulting from the current move */
    protected double[] rb;
    /** The function value at the previous base point */
    protected double[] fp;
    /** The function value at the resulting base point */
    protected double[] fr;
    /** The number of independent variables */
    protected int dimX;
    /** The number of function values */
    protected int dimF;	
    /** The current relative step reduction, compared to original step size,
     * including its sign.<BR>
     * All elements are the same in magnitude */
    protected double[] sr;
    /** The current magnitude of relative step reduction, compared to original step size */
    protected double absSr;
    /** The relative step reduction for each reduction */
    protected double steRed;
    /** The maximal number of step reduction before the algorithm terminates */
    protected int    nMaxSteRed;
    /** The number of step reduction */
    protected int    nSteRed;
    /** A flag whether the objective function value has to be checked for previous
     * matching results.
     */
    protected boolean checkObjFun;
}







