package genopt.algorithm;

import genopt.GenOpt;
import genopt.io.InputFormatException;
import genopt.lang.OptimizerException;
import genopt.simulation.SimulationInputException;
import genopt.algorithm.util.math.Point;
import java.io.IOException;
import java.util.Vector;

/** Class for doing a parametric run where the parameters are
  * the nodes of an equidistant grid.<BR>
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
  * @version GenOpt(R) 3.0.0 alpha 3 (November 20, 2008)<P>
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

public class EquMesh extends Optimizer{
    /** Constructor
     * @param genOptData a reference to the GenOpt object.<BR> 
     * <B>Note:</B> the object is used as a reference. 
     *              Hence, the datas of GenOpt are modified
     *              by this Class.
     * @exception OptimizerException
     * @exception IOException if an I/O exception occurs
     * @exception Exception
     * @exception InputFormatException
     */
    public EquMesh(GenOpt genOptData)
	throws OptimizerException, IOException, Exception, InputFormatException{
	super(genOptData, 0);
	ensureOnlyContinuousParameters();
	dimX = getDimensionX();
	dimCon = getDimensionContinuous();
	dimDis = getDimensionDiscrete();
	dimF = getDimensionF();
	
	// get additional input
	stopAtError = getInputValueBoolean("StopAtError");

	// check whether all lower and upper bounds are set
	String em = "";
	for (int i = 0; i < dimCon; i++){
	    if (getKindOfConstraint(i) != 3)
		em += "Parameter '" + getVariableNameContinuous(i) + 
		    "' does not have lower and upper bounds specified." + LS;
	    if (getL(i) == getU(i))
		em += "Parameter '" + getVariableNameContinuous(i) +
		    "' has lower bound equal upper bound." + LS;
	    
	    // check that step is an integer value
	    if ( Math.rint(getDx0(i)) != getDx0(i) )
		em += "Parameter '" + getVariableNameContinuous(i) +
		    "' has a step size equal to '" + getDx0(i) + 
		    "'. Require an integer value." + LS;
	    // check that Step is not negative
	    if ( getDx0(i) < 0)
		em += "Parameter '" + getVariableNameContinuous(i) + 
		    "' has a negative value for 'Step'." + LS;
	}
	
	if (em.length() > 0)
	    throw new OptimizerException(em);
		
	// initialization
	xCon    = new double[dimCon];
	step = new int[dimCon];
	int i;
	nS = 1;
	// number of runs
	for (i = 0; i < dimCon; i++){
	    step[i] = Math.round(Math.round(getDx0(i))) + 1; 
	    nS *= step[i];
	    xCon[i] = getL(i);
	}
	println("Require " + nS + " function evaluations.");
    }

    /** Runs the evaluation 
     * @param x0 initial point
     * @return <CODE>+4</CODE> the only possible return value 
     * @exception Exception	  
     * @exception OptimizerException
     */
    public int run(Point x0) throws OptimizerException, Exception{
	poiVec = new Vector<Point>(nS);
	// this algorithm does not use the initial point
	perturb(dimX-1);
	executeSimulations();
	return 4;
    }

    /** Perturbs the point and evaluates a simulation
     *@param dimNr number of dimensions that have to be perturbed
	  * @exception Exception	  
	  * @exception OptimizerException
	  */
    private void perturb(int dimNr) throws OptimizerException, Exception
    {
	for (int i = 0; i < step[dimNr]; i++){
	    xCon[dimNr] = (step[dimNr] ==1) ? getL(dimNr) :
		getL(dimNr) + (double)(i)/(double)(step[dimNr]-1) * (getU(dimNr)-getL(dimNr));
	    if (dimNr > 0)
		perturb(dimNr-1);
	    else{
		Point poi = new Point(dimCon, dimDis, dimF);
		poi.setX(xCon);
		poi.setStepNumber(0);
		poiVec.add(roundCoordinates((Point)poi.clone()));  
		//		println( (int)( (float)(getSimulationNumber()) * 100 / (float)(nS)) + "% completed.");
	    }
	}
    }

    /** Executes all simulations
     * @exception Exception	  
     * @exception OptimizerException
     */
    private void executeSimulations()
	throws OptimizerException, Exception{
	// execute the simulations
	final int nPoi = poiVec.size();
	Point[] p = new Point[nPoi];
	for(int i = 0; i < nPoi; i++){
	    p[i] = (Point)(poiVec.get(i));
	}
	super.getF(p, stopAtError);
	for(int i = 1; i < nPoi; i++){
	    if ( p[i].getSimulationNumber() == p[i-1].getSimulationNumber() )
		p[i].setComment("Point already evaluated.");
	    else{
		p[i].setComment("Function evaluation successful.");
	    }
	}
	for(int i = 0; i < nPoi; i++){
	    report(p[i], SUBITERATION);
	    report(p[i], MAINITERATION);
	}
    }

    
    /** Evaluates a simulation and reports result
     *@param pt point to be evaluated 
     *@return a clone of the point with the new function values stored
     *@exception OptimizerException if an OptimizerException occurs or
     *           if the user required to stop GenOpt
     *@exception SimulationInputException if an error in writing the
     *           simulation input file occurs
     *@exception Exception if an I/O error in the simulation input file occurs
     */
     public Point getF(final Point pt)
	throws SimulationInputException, OptimizerException, Exception{
	 Point r =  roundCoordinates(pt);
	 try{
	     r = super.getF(r);
	     r.setComment("Function evaluation successful.");
	 }
	catch(SimulationInputException e){
	    // must throw such an exception
	    // since input is wrong
	    throw e;
	}
	catch(Exception e){
	    if(stopAtError || mustStopOptimization())
		throw e;
	    else{
		String em = "Point : ";
		em += r.toString() + LS;
		setWarning(em + e.getMessage(), r.getSimulationNumber() );
		double[] f = new double[dimF];
		for(int i=0; i<dimF; i++)
		    f[i] = 0;
		r.setF(f);
		r.setComment("Error during function evaluation. See log file.");
	    }
	}
	report(r, SUBITERATION);
	report(r, MAINITERATION);
	return r;
    }

    /** number of required function evalations */
    protected int nS;
    /** number of independent variables */
    protected int dimX;
    /** number of continuous independent variables */
    protected int dimCon;
    /** number of discrete independent variables */
    protected int dimDis;
    /** number of function values */
    protected int dimF;	
    /** independent continuous parameter */
    protected double[] xCon;
    /** number of steps for continuous parameters */
    protected int[]    step;
    /** flag whether run should stop or proceed if a simulation error occurs */
    protected boolean stopAtError;
    /** Vector of points to be evaluated */
    Vector<Point> poiVec;
}
