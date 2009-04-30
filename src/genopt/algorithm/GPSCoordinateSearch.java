package genopt.algorithm;
import java.io.IOException;
import genopt.GenOpt;
import genopt.simulation.SimulationInputException;
import genopt.algorithm.util.math.*;
import genopt.algorithm.util.gps.*;
import genopt.lang.OptimizerException;
import genopt.io.InputFormatException;

/** Class for minimizing a function using the coordinate search algorithm.
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
  * GenOpt Copyright (c) 1998-2009, The Regents of the University of
  * California, through Lawrence Berkeley National Laboratory (subject 
  * to receipt of any required approvals from the U.S. Dept. of Energy).  
  * All rights reserved.
  *
  * @author <A HREF="mailto:MWetter@lbl.gov">Michael Wetter</A>
  *
  * @version GenOpt(R) 3.0.0 (May 4, 2009)<P>
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

public class GPSCoordinateSearch extends ModelGPS
{
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
    public GPSCoordinateSearch(GenOpt genOptData)
        throws OptimizerException, IOException, Exception, InputFormatException {
        super(genOptData);

        dimX = getDimensionContinuous();

        cooPoi = new int[dimX];
        for (int i = 0; i < dimX; i++)
        	cooPoi[i] = 0;
    }

    /** Constructor used to run the algorithm in a hybrid algorithm for the
     *  last iterations.
     *
     *@param meshSizeDivider the mesh size divider. 
     *                       If set to <code>Integer.MAX_VALUE</code>, then
     *                       the value will be read from the command file
     *@param initialMeshSizeExponent the initial mesh size exponent. 
     *                       If set to <code>Integer.MAX_VALUE</code>, then
     *                       the value will be read from the command file
     *@exception OptimizerException
     *@exception Exception
     *@exception IOException if an I/O exception occurs
     *@exception InputFormatException
     */
    public GPSCoordinateSearch(final int meshSizeDivider,
			       final int initialMeshSizeExponent)
        throws OptimizerException, IOException, Exception, InputFormatException {
        super(meshSizeDivider,
	      initialMeshSizeExponent);
        dimX = getDimensionContinuous();

        cooPoi = new int[dimX];
        for (int i = 0; i < dimX; i++)
        	cooPoi[i] = 0;
    }



    /** Method that initializes the base direction matrix.<P>
     *  Each column vector is a direction, and the set of column vectors
     *  must be a positive span for the domain of independent parameters.
     */
    protected double[][] initializeBaseDirectionMatrix(){
	final int dim = getDimensionContinuous();
	double[][] r = new double[dim][2*dim];
	LinAlg.initialize(r, 0);
	for(int i = 0; i < dim; i++){
	    r[i][2*i] = getDx0(i);
	    r[i][2*i+1] = -getDx0(i);
	}
	return r;
    }

    /** Method for the global search (this method returns always <code>null</code>.
     *@param x Sequence of previous iterates
     *@param delta current mesh size
     *@return the <code>null</code> pointer
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
    protected Point[] globalSearch(Point[] x, double delta) throws
	SimulationInputException, OptimizerException, NoSuchMethodException,
	IllegalAccessException, Exception {
	return EMPTY_SEARCH_SET;
    }

    /** Method for the local search.
     *@param x Sequence of previous iterates
     *@param delta current mesh size
     *@return the sequence of points that have been evaluated in the local search.
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
    protected Point[] localSearch(Point[] x, double delta) throws
	SimulationInputException, OptimizerException, NoSuchMethodException,
	IllegalAccessException, Exception{
	// best currently known point
	// array of all points used in the local search
	Point[] xLoc = new Point[2*dimX + 1];
	xLoc[0] = (Point)x[ x.length-1 ].clone();
	// index of point with lowest function value
	int iLow = 0;
	// number of evaluated points
	int iEval = 0;
	for(int i = 0; i < dimX; i++){
	    if ( ! maxIterationReached() ){
		iEval++;
		xLoc[iEval] = perturb(xLoc[iLow], delta, i);
		if ( haveSufficientDecrease(xLoc[iEval], xLoc[iLow] ) ){
		    iLow = iEval;
		}
		else if  ( ! maxIterationReached() ){
		    // failure.
		    // try other coordinate direction
		    iEval++;
		    xLoc[iEval] = perturb(xLoc[iLow], delta, i);
		    if ( haveSufficientDecrease( xLoc[iEval], xLoc[iLow] ) ){
			iLow = iEval;
		    }
		}
	    }
	}
	// return value
	Point[] r = new Point[iEval];
	System.arraycopy(xLoc, 1, r, 0, iEval);
	return r;
    }
    
    /** Perturbs a given point in one direction.<P>
     *  The value of <code>cooPoi</code> determines whether a point will first be
     *  perturbed along the negative or positive coordinate direction.
     *@param xBes Point to be perturbed
     *@param delta current mesh size
     *@param i index of independent parameter that has to be perturbed
     *@return the point with the lowest obtained function value. If the method
     *        could not reduce the cost, then a clone of <code>xBes</code> is returned.
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
    private Point perturb(Point xBes, double delta, int i) throws
	SimulationInputException, OptimizerException, NoSuchMethodException,
	IllegalAccessException, Exception{

	final double oldCoo = xBes.getX(i);
	final double dir = basDirMat[i][2*i+cooPoi[i]];
	final double newCoo = oldCoo + delta * dir;
	// trial point
	Point xTri = (Point)xBes.clone();

	xTri.setX(i, newCoo);
	xTri = getF(xTri);
	if ( haveSufficientDecrease( xTri, xBes ) ){
	    // reduced cost. Accept point
	    reportSuccess(xTri, i);
	    return xTri;
	}
	else{
	    // report failure
	    reportFailure(xTri, i);
	    // switch index of possibly successfull direction
	    cooPoi[i] = ( cooPoi[i] == 0 ) ? 1 : 0;
	    return (Point)xBes.clone();
	}
    }


    /**
     * Reports a trial that reduced the cost function value.
     *
     * @param x the point that will be reported
     * @param i the coordinate that has been perturbed
     * @exception IOException if an error occurs
     * @exception OptimizerException if an error occurs
     */
    private void reportSuccess(Point x, int i) throws IOException, OptimizerException{
	final String dir = ( cooPoi[i] == 0 ) ? ModelGPS.POSDIR : ModelGPS.NEGDIR;
	final String mes = ModelGPS.SUCMOV + getVariableNameContinuous(i) +
	    dir + getVariableNameContinuous(i) + '.';
	x.setComment( mes  );
	report(x, SUBITERATION);
    }

    /**
     * Reports a trial that yield no reduction in the cost function value.
     *
     * @param x the point that will be reported
     * @param i the coordinate that has been perturbed
     * @exception IOException if an error occurs
     * @exception OptimizerException if an error occurs
     */
    private void reportFailure(Point x, int i) throws IOException, OptimizerException{
	final String dir = ( cooPoi[i] == 0 ) ? ModelGPS.POSDIR : ModelGPS.NEGDIR;
	final String mes = ModelGPS.FAIMOV + getVariableNameContinuous(i) +
	    dir + getVariableNameContinuous(i) + '.';
	x.setComment( mes  );
	report(x, SUBITERATION);
    }

    /** Array with element 0 or 1, if the previous step in this direction was
	successful or failed, respectively */
    private int[] cooPoi;

    /** The number of independent variables */
    private int dimX;
}
