package genopt.algorithm;
import java.io.IOException;
import genopt.GenOpt;
import genopt.lang.OptimizerException;
import genopt.simulation.SimulationInputException;
import genopt.algorithm.util.math.*;
import genopt.algorithm.util.linesearch.Armijo;
import genopt.io.InputFormatException;

/** Class for minimizing a function using the 
  * Discrete Armijo Gradient algorithm.<P>
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
  * @version GenOpt(R) 3.0.2 (November 6, 2009)<P>
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

public class DiscreteArmijoGradient extends Optimizer
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
    public DiscreteArmijoGradient(GenOpt genOptData)
        throws OptimizerException, IOException, Exception, InputFormatException {

        super(genOptData, Optimizer.TRANSFORMED);
	ensureOnlyContinuousParameters();		
	
    dimX = getDimensionContinuous();

	/////////////////////////////////////////////////////
        // retrieve algorithm settings
	Alp  = getInputValueDouble("Alpha",
				   0, Optimizer.EXCLUDING,
				   1, Optimizer.EXCLUDING);

	Bet  = getInputValueDouble("Beta",
				   0, Optimizer.EXCLUDING,
				   1, Optimizer.EXCLUDING);

	Gam  = getInputValueDouble("Gamma",
				   0, Optimizer.EXCLUDING,
				   Double.MAX_VALUE, Optimizer.INCLUDING);

	K0    = getInputValueInteger("K0");
	KSta  = getInputValueInteger("KStar");
	LMax  = getInputValueInteger("LMax",
				     0, Optimizer.INCLUDING,
				     Integer.MAX_VALUE, Optimizer.INCLUDING);

	Kappa  = getInputValueInteger("Kappa",
				      0, Optimizer.INCLUDING,
				      Integer.MAX_VALUE, Optimizer.INCLUDING);

	EpsM = getInputValueDouble("EpsilonM",
				   0, Optimizer.EXCLUDING,
				   Double.MAX_VALUE, Optimizer.INCLUDING);

	EpsX = getInputValueDouble("EpsilonX",
				   0, Optimizer.EXCLUDING,
				   Double.MAX_VALUE, Optimizer.INCLUDING);
	
    }

    /** Runs the optimization process until a termination criteria
     * is satisfied
     * @param x0 initial point
     * @return <CODE>-1</CODE> if the maximum number of iteration
     *                         is exceeded
     *     <BR><CODE>+1</CODE> if the required accuracy is reached
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
    public int run(Point x0) throws
	SimulationInputException, OptimizerException, NoSuchMethodException,
	IllegalAccessException, Exception{

	int retFla;

	/////////////////////////////////////////////////////
	// check initial point for feasibility
	String errMes = "";
	setMode(Optimizer.ORIGINAL);
	for (int i=0; i < dimX; i++){
	    final double xi = super.getX0(i);
	    if (xi < getL(i))
		errMes += getVariableNameContinuous(i) + "=" + 
		    xi + ": Lower bound " + getL(i) + "." + LS;
	    else if (xi > getU(i))
		errMes += getVariableNameContinuous(i) + "=" + 
		    xi + ": Upper bound " + getU(i) + "." + LS;
	}
	if (errMes.length() > 0)
	    throw new OptimizerException("Initial point not feasible:" + 
					 LS + errMes);
	setMode(Optimizer.TRANSFORMED);
	xIni = x0.getX();
	dXIni = new double[dimX];
	for(int i = 0; i < dimX; i++)
	    dXIni[i] = super.getDx(i, xIni[i]);

	////////////////////////////////
	// initialize
	println("Initialize.");
	boolean iterate = true;
	InitializeNormalization = true;
	int iIte = 0;
	int m = 0;
	int k = KSta;
	////////////////////////////////
	// evaluate function at initial point
	Point x = (Point)x0.clone();
	for(int j = 0; j < dimX; j++)
	    x.setX(j, (double)0);
	x.setComment( "Initial point.");
	////////////////////////////////
	// optimization loop
	do{
	    ////////////////////////////////
	    // check accuracy of gradient approximation
	    if ( StrictMath.pow(Bet, m) < EpsM ){
		println("Maximum accuracy of gradient approximation achieved.");
		reportMinimum();
		return 1;
	    }
	    ////////////////////////////////
	    // compute search direction
	    println("Compute search direction.");
	    Point xSea[] = new Point[dimX];
	    for(int iP = 0; iP < dimX; iP++){
		xSea[iP] = (Point)x.clone();
		xSea[iP].setComment("Search direction computation.");
	    }
	    double[] h = new double[dimX];

	    final double eps = StrictMath.pow(Bet, K0+m);
	    for (int iP = 0; iP < dimX; iP++){
		    xSea[iP].setX(iP, x.getX(iP) + eps);
	    }
	    // do the function evaluation
	    // For the first iteration, x has not been evaluated yet.
	    // So, do this here so we can do it in parallel with the gradient
	    // evaluation
	    if ( InitializeNormalization ){
		// make temporary vector so we can evaluate [x, xSea] in parallel
		Point[] tem = new Point[dimX+1];
		tem[0] = (Point)x.clone();
		for (int iP = 0; iP < dimX; iP++)
		    tem[iP+1] = (Point)xSea[iP].clone();
		tem = getF(Optimizer.SUBITERATION, tem);
		// assign points from temporary vector back to variables
		x = (Point)tem[0].clone();
		for (int iP = 0; iP < dimX; iP++)
		    xSea[iP] = (Point)tem[iP+1].clone();
	    }
	    else // x has already been evaluated. 
		xSea = getF(Optimizer.SUBITERATION, xSea);
	    // assign the search direction
	    for (int iP = 0; iP < dimX; iP++){
		h[iP] = - ( xSea[iP].getF(0) - x.getF(0) ) / eps;
	    }
	    /////////////////////////////////
	    // compute decrease
	    println("Checking decrease.");
	    Point xDec[] = new Point[1];
	    xDec[0] = (Point)x.clone();
	    final double[] ste = LinAlg.multiply(eps, h);
	    xDec[0].setX( LinAlg.add( x.getX(), ste ) );
	    xDec[0].setComment("Decrease computation.");
	    xDec = getF(Optimizer.SUBITERATION, xDec);
	    final double del = ( xDec[0].getF(0) - x.getF(0) ) / eps;
	    /////////////////////////////////
	    // check for decrease
	    if ( del >= 0 ){
		println("No decrease obtained.");
		m++; // increases accuracy of gradient approximation
	    }
	    else{
		/////////////////////////////////
		// compute step size using the Armijo rule
		println("Decrease obtained. Start line search.");

		Armijo als = new Armijo(this,  
					KSta, LMax, Kappa,
					Alp, Bet);
		als.run(iIte, x, h, k, del);
		k = als.getKWithLowestCost();
		final Point xLS = als.getPointWithLowestCost();
		// check for sufficient decrease
		if ( ( xLS.getF(0) - x.getF(0) ) > - Gam * eps ){
		    println("Reject iterate. Insufficient decrease.");
		    // println("delF = " + (xLS.getF(0) - x.getF(0)) + "; Min. delF = " + - Gam * eps);
		    m++;
		}
		else{
		    final double[] dX = LinAlg.subtract( x.getX(), xLS.getX() );
		    final double norDX = LinAlg.twoNorm(dX);
		    println("Accept iterate. ||dX|| = " + norDX);
		    xLS.setComment("Successfull iterate. ||dX|| = " + norDX);
		    reportSuccessFullIterate(xLS);

		    if ( norDX < EpsX )
			iterate = false;

		    x = (Point)xLS.clone();
		    iIte++;
		}
	    } // else of 'if ( del > 0 )'
	} while ( iterate && (! maxIterationReached() ) );
    
	if (iterate){ // maximum number of simulation reached
	    reportCurrentLowestPoint();
	    retFla = -1;
	}
	else{
	    reportMinimum();
	    retFla = 1;
	}
	return retFla;
    }

    /** Reports the new trial and updates the parameters<UL>
     * <LI>updates the original value
     * <LI>updates the transformed value
     * <LI>updates the transformed step size
     * <LI>reports the new trial
     * <LI>reports the objective function value
     * <LI>increases the number of the iteration</UL>
     * <B>Note:</B> If a sub iteration is also a main iteration, then
     *              you have to call this function twice, first with
     *              <CODE>MainIteration = false</CODE> and then with
     *              <CODE>MainIteration = true</CODE>
     *@param x the point to be reported
     *@exception IOException if an I/O error in the optimization output files
     *               occurs
     *@exception OptimizerException thrown if the objective function value is the same
     *    between two following main iterations
     */
    public void reportSuccessFullIterate(Point x)
	throws IOException, OptimizerException{
	// point with coordinates of simulation input
	Point p = _convertPointToUserUnits(x);
	final double fUserUnits = x.getF(0) * fIni;
	p.setF(0, fUserUnits );
	//	super.report(p, Optimizer.MAINITERATION);
	super.report(p, false);
	super.report(p, true);
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
     *@param MainIteration <CODE>true</CODE> if step was a main iteration or
     *       <CODE>false</CODE> if it was a sub iteration
     *@param x the point being evaluated
     *@return a clone of the point with the new function values stored
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
    public Point[] getF(boolean MainIteration, Point x[])
	throws SimulationInputException, OptimizerException, NoSuchMethodException,
	       IllegalAccessException, Exception{

	// point with coordinates for simulation input
	final int nPoi = x.length;
	Point[] p = new Point[nPoi];
	for (int iP = 0; iP < nPoi; iP++)
	    p[iP] = _convertPointToUserUnits(x[iP]);
	
	// evaluate cost function
	final boolean stopAtError = true;
	p = super.getF(p, stopAtError);
	// store function value if first run
	if ( InitializeNormalization ){
	    // if f0 is very small, do not use normalization
	    if (StrictMath.abs( p[0].getF(0) ) >  1.E100 * Double.MIN_VALUE )
		fIni = p[0].getF(0);
	    else{
		fIni = 1.;
		final String mes = 
		    "Initial cost function value is close to zero." + LS +
		    "Algorithm does not use normalization of cost.";
		setInfo(mes, p[0].getSimulationNumber() );
	    }
	}
	// report results
	if ( InitializeNormalization ){
	    super.report(p[0], Optimizer.SUBITERATION); // the initial point is a main iteration
	    super.report(p[0], Optimizer.MAINITERATION); // the initial point is a main iteration
	}
	for (int iP = InitializeNormalization ? 1 : 0; iP < nPoi; iP++){
	    if ( MainIteration )
		super.report(p[iP], Optimizer.MAINITERATION);
	    super.report(p[iP], MainIteration);
	}
	// point with normalized coordinates and cost function
	Point[] r = new Point[nPoi];
	for (int iP = 0; iP < nPoi; iP++){
	    r[iP] = (Point)p[iP].clone();
	    r[iP].setX( x[iP].getX() );
	    r[iP].setF(0, p[iP].getF(0)/fIni );
	}
	InitializeNormalization = false;
	return r;
    }

    /** Evaluates the simulation based on the parameter set x and reports the point.
     *
     * This method simply calls the vectorized form {@link #getF(boolean, Point[])} 
     * and returns its value
     *
     *@param MainIteration <CODE>true</CODE> if step was a main iteration or
     *       <CODE>false</CODE> if it was a sub iteration
     *@param x the point being evaluated
     *@return a clone of the point with the new function values stored
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
    public Point getF(boolean MainIteration, Point x)
	throws SimulationInputException, OptimizerException, NoSuchMethodException,
	       IllegalAccessException, Exception{
	// call vectorized implementation
	Point[] r = new Point[1];
	r[0] = (Point)x.clone();
	r = getF(MainIteration, r);
	return (Point)r[0].clone();
    }

    /** Computes a function evaluation and reports the point as a sub iteration.
     * 
     * This method calls {@link #getF(boolean, Point)} with the boolean flag set to
     * <tt>Optimizer.SUBITERATION</tt>
     *@param x the point being evaluated
     *@return a clone of the point with the new function values stored
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
    public Point getF(Point x)
	throws SimulationInputException, OptimizerException, NoSuchMethodException,
	       IllegalAccessException, Exception{
	return this.getF(Optimizer.SUBITERATION, x);
    }


    /** Converts the argument's independent parameters
      * to the units used in the simulation input.
      *@param x the point being converted
      *@return the point with the independent parameters converted to
      *        the units used in the simulation input
      */
    private Point _convertPointToUserUnits(Point x){
 	Point p = (Point)x.clone();
	for(int i = 0; i < dimX; i++){
	    final double coo = xIni[i] + x.getX(i) * dXIni[i];
	    p.setX(i, coo );
	}
	return p;
    }

    /** The number of independent variables */
    private int dimX;

    /** Algorithm parameter */
    private double Alp;
    /** Algorithm parameter */
    private double Bet;
    /** Algorithm parameter */
    private double Gam;
    /** Algorithm parameter */
    private int KSta;
    /** Algorithm parameter */
    private int K0;
    /** Algorithm parameter */
    private int LMax;
    /** Algorithm parameter */
    private int Kappa;
    /** Relative accuracy of independent parameter before optimization stops */
    private double EpsX;
    /** Relative accuracy of gradient approximation */
    private double EpsM;
    /** Initial values of the independent parameters */
    private double[] xIni;
    /** Initial values of the cost function */
    private double fIni;
    /** Step size for scaling of the independent parameters */
    private double[] dXIni;
    /** Flag whether the normalization must be initialized */
    private boolean InitializeNormalization;
}
