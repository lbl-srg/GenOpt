package genopt.algorithm;
import java.io.IOException;
import genopt.GenOpt;
import genopt.simulation.SimulationInputException;
import genopt.algorithm.util.math.*;
import genopt.algorithm.util.gps.*;
import genopt.lang.OptimizerException;
import genopt.io.InputFormatException;

/** Class for minimizing a function using the Hooke-Jeeves
  * Generalized Pattern Search algorithm.<P>
  *
  * This algorithm replaces its older implementation in the
  * {@link HookeJeeves} class. Users should use this implementation
  * instead of {@link HookeJeeves}.
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

public class GPSHookeJeeves extends GPSCoordinateSearch
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
    public GPSHookeJeeves(GenOpt genOptData)
        throws OptimizerException, IOException, Exception, InputFormatException {
        super(genOptData);
	dimF = getDimensionF();
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
    public GPSHookeJeeves(final int meshSizeDivider,
			  final int initialMeshSizeExponent)
        throws OptimizerException, IOException, Exception, InputFormatException {
        super(meshSizeDivider,
	      initialMeshSizeExponent);
	dimF = getDimensionF();
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

	final int itNr = x.length-1;
	Point xBas = (Point)x[itNr].clone();
	xBas.setComment("Exploration base, Delta = " + getDelta() + ".");
	if ( itNr > 0 ){
	    double[] newCoo = LinAlg.multiply(2 , x[itNr].getX());
	    newCoo = LinAlg.subtract(newCoo, x[itNr-1].getX());
	    xBas.setX( newCoo );
	    xBas.setStepNumber( getStepNumber() );
	    xBas = getF(xBas);
	    // we need to reset the comment because it gets overwritten if the
	    // cost function is double-integrated
	    xBas.setComment("Exploration base, Delta = " + getDelta() + ".");
	    report(xBas, SUBITERATION);
	}
	Point[] r = explore(xBas, delta);
	return r;
    }

    /** Makes the exploration search
     *@param xBas base point for the exploration search
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
    private final Point[] explore(Point xBas, double delta) throws
	SimulationInputException, OptimizerException, NoSuchMethodException,
	IllegalAccessException, Exception{
	Point[] x = new Point[1];
	x[0] = (Point)xBas.clone();
	final Point[] r = super.localSearch(x, delta);
	return r;
    }

    /** The number of function values */
    private int dimF;

}
