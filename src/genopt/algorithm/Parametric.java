package genopt.algorithm;

import genopt.GenOpt;
import genopt.io.InputFormatException;
import genopt.lang.OptimizerException;
import genopt.algorithm.util.math.Point;
import genopt.algorithm.util.math.Fun;
import genopt.simulation.SimulationInputException;
import java.io.IOException;
import java.util.TreeMap;

/** Class for doing a parametric run where one parameter
   * is perturbed at a time while the others are fixed. 
   * Linear and logarithmic spacing can be selected for each 
   * parameter independently.<BR>
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

public class Parametric extends Optimizer
{       
    /** Constructor
     * @param genOptData a reference to the GenOpt object.<BR> 
     * <B>Note:</B> the object is used as a reference. 
     *              Hence, the datas of GenOpt are modified
     *              by this Class.
     * @exception OptimizerException
     *@exception IOException if an I/O exception occurs
     * @exception Exception
     * @exception InputFormatException
     */
    public Parametric(GenOpt genOptData)
	throws OptimizerException, IOException, Exception, InputFormatException
    {
	super(genOptData, 0);
	dimCon = getDimensionContinuous();
	dimDis = getDimensionDiscrete();

	dimF = getDimensionF();
	String em = "";

	// get additional input
	stopAtError = getInputValueBoolean("StopAtError");

	// check input for errors
	// check whether all lower and upper bounds are set
	for (int i = 0; i < dimCon; i++){
	    if (getKindOfConstraint(i) != 3)
		em += "Parameter '" + getVariableNameContinuous(i) + 
		    "' does not have lower and upper bounds specified.";
	}
	if (em.length() > 0)
	    throw new OptimizerException(em);

	for (int i = 0; i < dimCon; i++){
	    // check that all values are positive if 
	    // logarithmic spacing is required
	    if (getDx(i) < 0){ // have logarithmic scale
		if(getL(i) <= 0)
		    em += "Parameter '" + getVariableNameContinuous(i) +
			"' has logarithmic scale and lower bound '" + getL(i) + "'." + LS;
		if(getU(i) <= 0)
		    em += "Parameter '" + getVariableNameContinuous(i) +
			"' has logarithmic scale and upper bound '" + getU(i) + "'." + LS;
	    }
	    // check that l != u if step != 0
	    if (getDx(i) != 0 && getL(i) == getU(i))
		em += "Parameter '" + getVariableNameContinuous(i) +
		    "' has step size unequal 0 but its lower bound equal to its upper bound." + LS;
	    // check that step is an integer value
	    if ( Math.rint(getDx(i)) != getDx(i) )
		em += "Parameter '" + getVariableNameContinuous(i) +
		    "' has a step size equal to '" + getDx(i) + "'. Require an integer value." + LS;
	    
	}
	
	if (em.length() > 0)
	    throw new OptimizerException(em);
	
	// all input is OK
	// initialize list with evaluated points
	evaPoi = new TreeMap<Point, Double[]>();
    }

    /** Runs the evaluation 
     * @return <CODE>+4</CODE> the only possible return value 
     * @exception Exception	  
     * @exception OptimizerException
     */
    public int run() throws OptimizerException, Exception {
	Point poi = new Point(dimCon, dimDis, dimF);
	// initialize points with current settings
	poi.setXIndex( getX(), getIndex() );
	poi.setStepNumber(0);
	final Point defPoi = (Point)poi.clone();
	// vary continuous parameters
	for(int iC = 0; iC < dimCon; iC++){
	    // reset point to default values, so all coordinates are at their inital values
	    poi = (Point)defPoi.clone();
	    
	    int nStep = Math.round( (float)getDx(iC) );
	    if ( nStep != 0 ){
		// set up spacing
		double[] xSp;
		xSp = null;
		xSp = Fun.getSpacing(nStep, getL(iC), getU(iC));
		
		for(int iS = 0; iS < xSp.length; iS++){
		    poi.setX(iC, xSp[iS]);
		    this.getF(poi);
		}
	    }
	}
	// vary discrete parameters
	for(int iD = 0; iD < dimDis; iD++){
	    // reset point to default values, so all coordinates are at their initial values
	    poi = (Point)defPoi.clone();
	    
	    int len = getLengthDiscrete(iD);
	    if ( len != 1 ){
		for (int ind = 0; ind < len; ind++){
		    poi.setIndex(iD, ind);
		    this.getF(poi);
		}
	    }
	}
	return 4;
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
	throws SimulationInputException, OptimizerException, Exception
    {
	Point r = roundCoordinates( pt ); 
	r.setStepNumber(1);

	
	if(evaPoi.containsKey(r)){	// point already evaluated
	    println("Point already evaluated. Take function value from database.");
	    Double[] fD = (Double[])(evaPoi.get(r));
	    double[] f = new double[fD.length];
	    for (int i = 0; i < fD.length; i++)
		f[i] = fD[i].doubleValue();
	    r.setF(f);
	    r.setComment("Point already evaluated.");
	}
	else{	// point not yet evaluated
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
		    String em = "Exception in evaluating x = ( ";
		    for (int i=0; i < dimCon-1; i++)
		        em += r.getX(i) + ", ";
		    if (dimDis == 0)
			em += r.getX(dimCon-1) + ")." + LS;
		    else{
			em += r.getX(dimCon-1) + "; ";
			for (int i=0; i < dimDis-1; i++)
			    em += r.getIndex(i) + ", ";
			em += r.getIndex(dimDis-1) + ")." + LS;
		    }
		    setWarning( em + e.getMessage() );
		    double[] f = new double[dimF];
		    for(int i=0; i<dimF; i++)
			f[i] = 0;
		    r.setF(f);
		    r.setComment("Error during function evaluation. See log file.");
		}
		// proceed as usual
	    }
	    
	    Double[] fD = new Double[r.getDimensionF()];
	    for (int i = 0; i < fD.length; i++)
		fD[i] = new Double(r.getF(i));
	    // we must clone the object that we put into the TreeMap
	    // Otherwise, it's coordinates get changed since the map
	    // contains only a reference to the instance.
	    evaPoi.put((Point)r.clone(), fD);
	}
	report(r, SUBITERATION);
	report(r, MAINITERATION);
	return r;
    }

    /** number of independent continuous variables */
    protected int dimCon;
    /** number of independent discrete variables */
    protected int dimDis;
    /** number of function values */
    protected int dimF;
    /** flag whether run should stop or proceed if a simulation error occurs */
    protected boolean stopAtError;
    /** list with evaluated points and its function values */
    protected TreeMap<Point, Double[]> evaPoi;

    
}
