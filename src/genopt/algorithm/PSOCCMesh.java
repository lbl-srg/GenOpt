package genopt.algorithm;
import java.io.IOException;
import genopt.GenOpt;
import genopt.simulation.SimulationInputException;
import genopt.algorithm.util.math.*;
import genopt.algorithm.util.pso.*;
import genopt.lang.OptimizerException;
import genopt.io.InputFormatException;

/** Class for minimizing a function using a mesh particle swarm optimization
  * algorithm with constriction coefficient for the particle location update
  * equation.
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

public class PSOCCMesh extends PSOCC{

    /** Constructor
     * @param genOptData a reference to the GenOpt object.<BR>
     * <B>Note:</B> the object is used as a reference.
     *              Hence, the data of GenOpt are modified
     *              by this Class.
     *@exception OptimizerException if an OptimizerException occurs or
     *           if the user required to stop GenOpt
     *@exception SimulationInputException if an error in writing the
     *           simulation input file occurs
     *@exception NoSuchMethodException if a method that should be invoked could not be found
     *@exception IllegalAccessException  if an invoked method enforces Java language access 
     *                                    control and the underlying method is inaccessible
     *@exception InvocationTargetException if an invoked method throws an exception
     *@exception Exception if an I/O error in the simulation input file occurs
     */
    public PSOCCMesh(GenOpt genOptData)
        throws OptimizerException, IOException, Exception, InputFormatException {
        super(genOptData);

        // retrieve settings for ModelGPS algorithm
	MesSizDiv  = getInputValueInteger("MeshSizeDivider",
					  1, Optimizer.EXCLUDING,
					  Integer.MAX_VALUE, Optimizer.EXCLUDING);
	
        IniMesSizExp = getInputValueInteger("InitialMeshSizeExponent",
					    0, Optimizer.INCLUDING,
					    Integer.MAX_VALUE, Optimizer.EXCLUDING);
	
	final double del =  1. / StrictMath.pow(MesSizDiv, IniMesSizExp);
	Delta = new double[dimCon];
	for(int i = 0; i < dimCon; i++)
	    Delta[i] = del * getDx0(i);
	// make sure that Delta[i] is not larger than upper bound minus lower bound
	String em = "";
	for(int i = 0; i < dimCon; i++){
	    if ( Delta[i] > ( getU(i) - getL(i) ) )
		em += "Parameter '" + getVariableNameContinuous(i) + 
		    "': Mesh size is too large. Increase 'Step', 'MeshSizeDivider', or 'InitialMeshSizeExponent'.";
	}
	if ( em.length() > 0 )
	    throw new OptimizerException(em);
	X0Con = getX0();
    }
    

    /** Evaluates the simulation based on the parameter set x<BR>
     * The value <CODE>constraints</CODE> determines in which mode the constraints
     * are treated<UL>
     * <LI>After this call, the parameters in the original <I>and</I> in the
     *     transformed space are set to the values that correspond to <CODE>x</CODE>
     * <LI>The step size in the transformed space is updated according
     *     to the transformation function
     * <LI>A new input file is writen
     * <LI>the simulation is launched
     * <LI>simulation errors are checked
     * <LI>the value of the objective function is returned</UL>
     *@param x the points to be evaluated
     *@param stopAtError set to false to continue with function evaluations even if there was an error
     *@return a clone of the points with the new function values stored
     *@exception OptimizerException if an OptimizerException occurs or
     *           if the user required to stop GenOpt
     *@exception SimulationInputException if an error in writing the
     *           simulation input file occurs
     *@exception NoSuchMethodException if a method that should be invoked could not be found
     *@exception IllegalAccessException  if an invoked method enforces Java language access 
     *                                    control and the underlying method is inaccessible
     *@exception InvocationTargetException if an invoked method throws an exception
     *@exception Exception if an I/O error in the simulation input file occurs
     */
    public Point[] getF(Point[] x, boolean stopAtError)
	throws SimulationInputException, OptimizerException, NoSuchMethodException,
	       IllegalAccessException, Exception{
	final int nPoi = x.length;
	Point[] r1 = new Point[nPoi];
	for (int iP = 0; iP < nPoi; iP++){
	    r1[iP] = Optimizer.roundCoordinates( x[iP] );
	    if ( ! isFeasible(r1[iP]) ){
		for(int i = 0; i < dimCon; i++)
		    r1[iP].setX(i, Optimizer.setToFeasibleCoordinate(x[iP].getX(i), getL(i), getU(i)));
		for(int i = 0; i < dimDis; i++)
		    r1[iP].setIndex(i, Optimizer.setToFeasibleCoordinate(x[iP].getIndex(i), 
								     0, getLengthDiscrete(i)-1));
	    }
	    // r1 are now feasible. Set to mesh point
	    final double[] xMes = genopt.algorithm.util.gps.ModelGPS.getClosestEuclideanMeshPoint( r1[iP].getX(), X0Con, Delta );
	    r1[iP].setX( xMes );
	    r1[iP] = Optimizer.roundCoordinates( r1[iP] );
	    // r need not be feasible since the closest mesh point may be outside the
	    // feasible domain.
	    // Make r feasible.
	    if ( ! isFeasible( r1[iP] ) ){
		for(int i = 0; i < dimCon; i++){
		    final double xi = r1[iP].getX(i);
		    if (xi > getU(i)) // upper bound violated
			r1[iP].setX(i, xi - Delta[i] );
		    else if (xi < getL(i))   // lower bound violated
			r1[iP].setX(i, xi + Delta[i] );
		    
		}
		r1[iP] = Optimizer.roundCoordinates(r1[iP]);
		assert isFeasible( r1[iP] ) : "Point is not feasible.";
	    }
	}
	return super.getF(r1, stopAtError);
    }


    /** Gets the mesh size divider.
     *@return the mesh size divider
     */
    final protected int getMeshSizeDivider() { return MesSizDiv; }

    /** Gets the initial mesh size exponent.
     *@return the initial mesh size exponent
     */
    final protected int getInitialMeshSizeExponent() { return IniMesSizExp; }

    /** Initial values of the continuous parameters */
    protected double[] X0Con;
    /** The mesh size divider 'r' */
    private int MesSizDiv;
    /** The initial mesh size exponent 's_k' */
    private int IniMesSizExp;
    /** Mesh size factor */
    private double[] Delta;
}
