package genopt.algorithm.util.gps;
import java.io.IOException;
import java.util.Random;
import genopt.GenOpt;
import genopt.simulation.SimulationInputException;
import genopt.algorithm.Optimizer;
import genopt.algorithm.util.math.*;
import genopt.lang.OptimizerException;
import genopt.io.InputFormatException;
import java.lang.reflect.*;

/** Abstract class with model Generalized
  * Pattern Search algorithm.<P>
  *
  * <P><I>This project was carried out at:</I>
  * <UL><LI><A HREF="http://www.lbl.gov">
  * Lawrence Berkeley National Laboratory (LBNL)</A>,
  * <A HREF="http://simulationresearch.lbl.gov">
  * Simulation Research Group</A>,</LI></UL>
  * <I>and supported by</I><UL>
  * <LI>the <A HREF="http://www.energy.gov">
  * U.S. Department of Energy (DOE)</A>,
  * <LI>the <A HREF="http://www.satw.ch">
  * Swiss Academy of Engineering Sciences (SATW)</A>,
  * <LI>the Swiss National Energy Fund (NEFF), and
  * <LI>the <A HREF="http://www.snf.ch">
  * Swiss National Science Foundation (SNSF)</A></LI></UL><P>
  *
  * GenOpt Copyright (c) 1998-2011, The Regents of the University of
  * California, through Lawrence Berkeley National Laboratory (subject 
  * to receipt of any required approvals from the U.S. Dept. of Energy).  
  * All rights reserved.
  *
  * @author <A HREF="mailto:MWetter@lbl.gov">Michael Wetter</A>
  *
  * @version GenOpt(R) 3.1.0 (December 8, 2011)<P>
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

abstract public class ModelGPS extends Optimizer
{

    /** Array of points with zero elements.
     */
    public static final Point[] EMPTY_SEARCH_SET = new Point[0];

    /** Constructor used to run it as a single algorithm.
     *
     *@param genOptData a reference to the GenOpt object.<BR>
     * <B>Note:</B> The object is used as a reference.
     *              Hence, its data are modified by this Class.
     *@exception OptimizerException
     *@exception Exception
     *@exception IOException if an I/O exception occurs
     *@exception InputFormatException
     */
    public ModelGPS(GenOpt genOptData)
        throws OptimizerException, IOException, Exception, InputFormatException {
        super(genOptData, Optimizer.ORIGINAL);
	ensureOnlyContinuousParameters();		
        dimX = getDimensionContinuous();
	dimF = getDimensionF();
	fNor = Double.MAX_VALUE;
	Alpha = Double.MAX_VALUE;
	parseCommandFile(Integer.MAX_VALUE, Integer.MAX_VALUE);
	RepMin = true;
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
    public ModelGPS(final int meshSizeDivider,
		    final int initialMeshSizeExponent)
        throws OptimizerException, IOException, Exception, InputFormatException {
        super();
	// For hybrid algorithm, can have discrete parameters (which will be fixed in this algorithm),
	// but must have at least one continuous parameter
	Alpha = Double.MAX_VALUE;
        dimX = getDimensionContinuous();
	if ( dimX < 1 )
	    throw new OptimizerException("Optimization algorithm needs at least one continuous parameter.");
	dimF = getDimensionF();
	fNor = Double.MAX_VALUE;
	parseCommandFile(meshSizeDivider, initialMeshSizeExponent);
    }


    /** Parses the command file.
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
    private void parseCommandFile(final int meshSizeDivider,
				  final int initialMeshSizeExponent)
	throws OptimizerException, IOException, Exception, InputFormatException{
	
        // retrieve settings for ModelGPS algorithm
	// check if we have the multistart algorithm
	if ( isNextToken("MultiStart") ){
	    final String mulSta = getInputValueString("MultiStart");
	    if ( ! mulSta.equals("Uniform") )
		throwInputError("'Uniform'");
	    final long seed = (long)(getInputValueInteger("Seed",
							  Integer.MIN_VALUE, Optimizer.INCLUDING,
							  Integer.MAX_VALUE, Optimizer.INCLUDING));
	    
	    NumIniPoi = getInputValueInteger("NumberOfInitialPoint",
					     1, Optimizer.INCLUDING,
					     Integer.MAX_VALUE, Optimizer.EXCLUDING);

	    /////////////////////////////////////////////////////
	    // check whether all lower and upper bounds are set
	    final int dimCon = getDimensionContinuous();
	    String em = "";
	    for (int i = 0; i < dimCon; i++){
		if (getKindOfConstraint(i) != 3)
		    em += "Parameter '" + getVariableNameContinuous(i) + 
			"' does not have lower and upper bounds specified." + LS;
		if (getL(i) == getU(i))
		    em += "Parameter '" + getVariableNameContinuous(i) +
			"' has lower bound equal upper bound." + LS;
	    }
	    
	    if (em.length() > 0)
		throw new OptimizerException(em);
 
	    /////////////////////////////////////////////////////
	    RanGen = new Random(seed);
	    // need to call nextDouble, otherwise the seed is not used for the first
	    // random number.
	    RanGen.nextDouble();
	    //////////////////////////////////////////////////////////////////////////////
	}
	else
	    NumIniPoi = 1;

	// ----------------------------
	// check for optional keyword 'Zeta' and, if specified, mandatory keyword 'Phi'
	PhiFun = null;
	if ( isNextToken("Zeta") ){
	    Zeta = getInputValueDouble("Zeta", 0, Optimizer.INCLUDING,
				       Double.MAX_VALUE, Optimizer.EXCLUDING);
	    PhiFun = _getPrecisionControlFunction("Phi", Optimizer.INCLUDING);
	    if ( isNextToken("Alpha") ){
		Alpha = getInputValueDouble("Alpha", 0, Optimizer.EXCLUDING, 1, Optimizer.EXCLUDING);
	    }
	    SUCMOV = "Cost sufficiently reduced     at ";
	    FAIMOV = "Cost not sufficiently reduced at ";
	}
	else{
	    Zeta = 1;
	    SUCMOV = "Cost reduced     at ";
	    FAIMOV = "Cost not reduced at ";
	}
	// ----------------------------
	// retrieve other inputs
	mesSizDiv = ( meshSizeDivider == Integer.MAX_VALUE ) ?
	    getInputValueInteger("MeshSizeDivider",
				 1, Optimizer.EXCLUDING,
				 Integer.MAX_VALUE, Optimizer.EXCLUDING) :
	    meshSizeDivider;

	iniMesSizExp = ( initialMeshSizeExponent == Integer.MAX_VALUE ) ?
	    getInputValueInteger("InitialMeshSizeExponent",
				 0, Optimizer.INCLUDING,
				 Integer.MAX_VALUE, Optimizer.EXCLUDING) :
	    initialMeshSizeExponent;
	
        mesSizExpInc = getInputValueInteger("MeshSizeExponentIncrement",
					    0, Optimizer.EXCLUDING,
					    Integer.MAX_VALUE, Optimizer.EXCLUDING);
	
	nMaxSteRed = getInputValueInteger("NumberOfStepReduction",
					  0, Optimizer.EXCLUDING,
					  Integer.MAX_VALUE, Optimizer.EXCLUDING);
	
	// check initial point for feasibility
	String errMes = "";
	for (int i=0; i < dimX; i++)
	    if (getX0(i) < getL(i))
		errMes += getVariableNameContinuous(i) + "=" + 
		    getX0(i) + ": Lower bound " + getL(i) + LS;
	    else if (getX0(i) > getU(i))
		errMes += getVariableNameContinuous(i) + "=" + 
		    getX0(i) + ": Upper bound " + getU(i) + LS;
	if (errMes.length() > 0)
	    throw new OptimizerException("Initial point not feasible:" + 
					 LS + errMes);
	
	basDirMat = initializeBaseDirectionMatrix();
	assert( basDirMat != null ) :
	    "Program error: initializeBaseDirectionMatrix() returns 'null'.";
	
	assert( basDirMat.length == dimX ) :
	    "Program error: Base direction matrix has wrong column length.";
	assert( basDirMat[0].length > dimX ) :
	    "Program error: Base direction matrix has not enough columns to be a positive spanning matrix.";
    }

    /** Makes an instance of the function that is used for a sufficient decrease condition.
     *@param funNam the keyword of the function
     *@param include flag that specifies if zero is includes as a function value or not
     *@return the function that is used for a sufficient decrease condition
     *@exception OptimizerException
     *@exception Exception
     *@exception IOException
     *@exception InputFormatException
     */
    private final FunctionEvaluator _getPrecisionControlFunction(final String funNam,
								 final int include)
	throws OptimizerException, IOException, Exception, InputFormatException{
	String fStr = getInputValueString(funNam);
	FunctionEvaluator fun = new FunctionEvaluator(funNam, fStr);
	boolean gotExc = false;
	double funVal = 0;
	try{
	    funVal = fun.evaluate();
	}
	catch(Exception e){
	    gotExc = true;
	}
	if ( include == Optimizer.INCLUDING ){
	    if ( (!gotExc) && ( funVal != 0 ) ){
		String em = "The function must tend to zero from above or be identical to zero."
		    + LS + "Function name  : " + fun.getName()
		    + LS + "Function       : " + fun.getFunction()
		    + LS + "Function value : " + funVal;
		throw new OptimizerException(em);
	    }
	}
	else{
	    if ( (!gotExc) && ( funVal > 0 ) ){
		String em = "The function must tend to zero from above or be bigger than zero."
		    + LS + "Function name  : " + fun.getName()
		    + LS + "Function       : " + fun.getFunction()
		    + LS + "Function value : " + funVal;
		throw new OptimizerException(em);
	    }
	}

	// stepNumber may be used in this function, in which case we need to 
	// set the flag in Optimizer.java.
	if ( fStr.indexOf("%stepNumber%") != -1 )
	    algorithmRequiresUsageOfStepNumber();
	return fun;
	
    }
    
    /** Method that initializes the base direction matrix.<P>
     *  Each column vector is a direction, and the set of column vectors
     *  must be a positive span for the domain of independent parameters.
     *@return baseDireMat the base direction matrix
     */
    abstract protected double[][] initializeBaseDirectionMatrix();

    /** Initializes the iterates for the multi-start algorithm.
     *@param numPoi number of initial points
     *@return the initial iterates
     */
    private Point[] getRandomPoints(int numPoi){
	final int dimCon = getDimensionContinuous();
	final int dimDis = getDimensionDiscrete();
	double[] fun = new double[dimF];
	for(int i = 0; i < dimF; i++)
	    fun[i] = Double.MAX_VALUE;

	Point[] poi = new Point[numPoi];
	poi[0] = new Point( dimCon, dimDis, dimF );
	// initialize first point as in input file
	poi[0].setX( getX0() );
	poi[0].setIndex( getIndex0() );
	poi[0].setStepNumber(getStepNumber());
	poi[0].setF(fun);
	double[] dDom = new double[dimCon];
	for(int i = 0; i < dimCon; i++){
	    dDom[i] = getU(i) - getL(i);
	}
	
	for(int iP = 1; iP < numPoi; iP++)
	    poi[iP] = new Point( dimCon, dimDis, dimF );
	
	// set maximum value for all variables
	for(int iV = 0; iV < dimDis; iV++){
	    final int maxVal = getLengthDiscrete(iV)-1; // -1 because if length=2, can have values 0 and 1
	    for(int iP = 0; iP < numPoi; iP++)
		poi[iP].setMaximumIndex(iV, maxVal);
	}
    
	// initialize parameters randomly
	final double delMes =  1. / StrictMath.pow(mesSizDiv, iniMesSizExp);
	double[] delta = new double[dimCon];
	for(int i = 0; i < dimCon; i++)
	    delta[i] = delMes * getDx0(i);


	for(int iP = 1; iP < numPoi; iP++){
	    // continuous parameter
	    for(int iV = 0; iV < dimCon; iV++){
		final double r = RanGen.nextDouble();
		final double x = getL(iV) + r * dDom[iV];
		poi[iP].setX(iV, x);
	    }
	    poi[iP].setX( getClosestEuclideanMeshPoint(poi[iP].getX(), poi[0].getX(), delta) );


	    poi[iP] = Optimizer.roundCoordinates( poi[iP] );
	    // poi[iP] need not be feasible since the closest mesh point may be outside the
	    // feasible domain.
	    // Make poi[iP] feasible.
	    if ( ! isFeasible( poi[iP] ) ){
		for(int i = 0; i < dimCon; i++){
		    final double xi = poi[iP].getX(i);
		    if (xi > getU(i)) // upper bound violated
			poi[iP].setX(i, xi - delta[i] );
		    else if (xi < getL(i))   // lower bound violated
			poi[iP].setX(i, xi + delta[i] );
		    
		}
		poi[iP] = Optimizer.roundCoordinates( poi[iP] );
		assert isFeasible( poi[iP] ) : "Point is not feasible.";
	    }
	    // discrete parameter
	    if ( dimDis > 0 ){
		for(int iV = 0; iV < dimDis; iV++){
		    final int r = RanGen.nextInt( getLengthDiscrete(iV) );
		    poi[iP].setIndex(iV, r);
		}
	    }
	    poi[iP].setStepNumber(getStepNumber());
	    poi[iP].setF(fun);
	}
	return poi;
    }

    /** Runs the optimization process until a termination criteria
     * is satisfied
     *@param x0 Initial iterate
     *@return <CODE>-1</CODE> if the maximum number of iteration
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
	if ( NumIniPoi == 1 ){
	    Point xIni = (Point)x0.clone();
	    xIni.setComment("Initial point");
	    return _run( xIni );
	}
	else{
	    int r = -1;
	    BesPoi = getRandomPoints(NumIniPoi);
	    Point xMin = null;
	    for(int iP = 0; iP < NumIniPoi; iP++){
		// reset the step number for the new point.
		resetStepNumber(1);
		println("Start search for point number " + (iP+1) + ".");
		int retVal = _run( BesPoi[iP] );
		if ( retVal == 1 )
		    r = retVal;
	        BesPoi[iP] = (Point)x[k];
		println("Minimum cost obtained for this point " + BesPoi[iP].getF(0) + ".");
		if ( ( iP == 0 ) )
		    xMin = (Point)BesPoi[iP].clone();
		else if ( BesPoi[iP].getF(0) < xMin.getF(0) ){
		    xMin = (Point)BesPoi[iP].clone();
		    println("Point number " + (iP+1) + " further decreased the cost.");
		}
		else
		    println("Point number " + (iP+1) + " did not further decrease the cost.");
	    }
	    super.reportMinimum();
	    return r;
	}
    }




    /** Runs the optimization process until a termination criteria
     * is satisfied
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
    private int _run(final Point xIni) throws
	SimulationInputException, OptimizerException, NoSuchMethodException,
	IllegalAccessException, Exception{
	// number of step reductions done
	// intialize it here because run() is called from the implementations
	// that use double-integrations
	nSteRed = 0;

	k = 0;
        boolean iterate = true;
	int step = 0;
        int retFla = 0;
	x = null; // set to null for multistart algorithms
	x = new Point[1];
	Point[] xGlo = null;
	Point[] xLoc = null;
	int mesSizExp = iniMesSizExp;
	Delta = 1. / StrictMath.pow(mesSizDiv, iniMesSizExp);
	
        // optimization loop
	do{
	    switch (step){
	    case 0: // initialize
		println("Initialize.");
		// evaluate function at base point
        x[k] = (Point)xIni.clone();
		x[k] = this.getF(x[k]);
		// need to set comment again because it may be overwritten
		// if f is smoothed
		x[k].setComment("Initial point."); 
		report(x[k], SUBITERATION);
		report(x[k], MAINITERATION);
	    case 1: // global search
		println("Perform global search.");
		xGlo = globalSearch(x, Delta);
		assert(xGlo != null) : "xGlo = null in case 1.";
		// get index of lowest point of search set
		if ( xGlo.length != 0 &&
		     haveSufficientDecrease(xGlo[ getIndexLowestFunctionValue(xGlo) ], x[k] ) ){
		    // global search reduced cost.
		    // skip local search
		    xLoc = EMPTY_SEARCH_SET;
		    step = 3;
		    break;
		}
		else{
		    // global search did not yield improvement.
		    // go to local search
		    step = 2;
		}
	    case 2: // local search
		println("Perform local search.");
		xLoc = localSearch(x, Delta);
		assert(xLoc != null) : "xLoc = null in case 2.";
		// in any case, we go to step 3
	    case 3: // parameter update
		println("Update Parameter.");
		assert ( xGlo.length != 0 || xLoc.length != 0 ) :
		    "Program error: Did not do a global nor a local search.";

		// increase vector of iterates by one element
		x = increaseSize(x);
		final Point[] seaSet = add(xLoc, xGlo);
		final int iLow = getIndexLowestFunctionValue(seaSet);

		if ( ( iLow != -1 ) && // iLow == -1 if seaSet.lengh == 0 (if max iter. exceeded)
		     haveSufficientDecrease(seaSet[iLow], x[k] )){
		    // reduced cost
		    println("Iteration step " 
			    + ( ( PhiFun == null ) ? "" : "sufficiently " )
			    + "reduced cost.");
		    x[k+1] = (Point)seaSet[iLow].clone();
		    final String com = ( iLow < xLoc.length ) ?
			( "Local search reduced cost" 
			  + ( ( PhiFun == null ) ? "" : " sufficiently " )
			  + "." ) : 
			( "Global search reduced cost" 
			  + ( ( PhiFun == null ) ? "" : " sufficiently " )
			  + "." );
		    x[k+1].setComment(com);
		    report(x[k+1], SUBITERATION);
		    report(x[k+1], MAINITERATION);
		}
		else{
		    ////////////////////////////////////////////////////////////////////
		    // no improvement in local and global search
		    println("Iteration step did not " 
			    + ( ( PhiFun == null ) ? "" : "sufficiently " )
			    + "reduce cost.");
		    // check that user implemented algorithm in the right way
		    if ( xLoc.length <= dimX && ( ! maxIterationReached() ) ){
			final String em = "Error in algorithm implementation:" + LS + 
			    "Local search returned after searching in " + xLoc.length +
			    " directions without reducing cost." + LS +
			    "Local search directions were not a positive span.";
			throw new OptimizerException(em);
		    }
		    // update parameter
		    x[k+1] = (Point)x[k].clone();
		    x[k+1].setComment("");
		    if ( Alpha == Double.MAX_VALUE ){
			////////////////////////////////////////////////////////////////////
			// this is the precision control scheme in which 
			// the mesh size exponent is incremented if no
			// sufficient decrease is obtained.
			mesSizExp += mesSizExpInc;
			Delta = 1. / StrictMath.pow((double)mesSizDiv, (double)mesSizExp);
			
			if (nSteRed == nMaxSteRed) {
			    // Maximum number of step reductions has been reached. 
			    // Final output.
			    final String com = "Iteration step did not " 
				+ ( ( PhiFun == null ) ? "" : "sufficiently " )
				+ "reduce cost. Maximum number of step reductions reached.";
			    x[k+1].setComment(com);
			    report(x[k+1], SUBITERATION);
			    report(x[k+1], MAINITERATION);
			    iterate = false;
			    reportMinimum();
			    retFla = 1;
			}
			else{
			    // Maximum number of step reductions has not yet been reached. 
			    final String dis = "Reduce step size to '" + Delta + "'.";
			    final String com = "Iteration step did not " 
				+ ( ( PhiFun == null ) ? "" : "sufficiently " )
				+ "reduce cost. "
				+ dis;
			    println(com);
			    x[k+1].setComment(com);
			    report(x[k+1], SUBITERATION);
			    report(x[k+1], MAINITERATION);
			    
			    // Increase step number.
			    // (Can be used for penalty function, barrier function
			    // or slack variables, or adaptive precision function evaluations )
			    if ( useStepNumber() ){
				x[k+1] = increaseStepNumber(x[k+1]);
				report(x[k+1], SUBITERATION);
				report(x[k+1], MAINITERATION);
			    }
			    nSteRed++;
			}
			////////////////////////////////////////////////////////////////////
		    }
		    else{
			////////////////////////////////////////////////////////////////////
			// this is the precision control scheme in which the mesh size
			// exponent is incremented such that Delta
			// satisfies phi(N)^alpha/Delta >= Delta
			////////////////////////////////////////////////////////////////////
			if (nSteRed == nMaxSteRed) {
			    // Maximum number of step reductions has been reached. 
			    // Final output.
			    final String com2 = "Iteration step did not " 
				+ ( ( PhiFun == null ) ? "" : "sufficiently " )
				+ "reduce cost. Maximum number of step reductions reached.";
			    x[k+1].setComment(com2);
			    report(x[k+1], SUBITERATION);
			    report(x[k+1], MAINITERATION);
			    iterate = false;
			    reportMinimum();
			    retFla = 1;
			}
			else{
			    // Maximum number of step reductions has not yet been reached. 

			    // check that phi is decreasing
			    final double phiOld = _getFunctionValue(PhiFun, x[k+1]);

			    final String com = "Iteration step did not sufficiently reduce cost.";
			    println(com);
			    x[k+1].setComment(com);
			    x[k+1] = increaseStepNumber(x[k+1]);
			    report(x[k+1], SUBITERATION);
			    report(x[k+1], MAINITERATION);
			    nSteRed++;
			    // evaluate phi
			    final double phiVal = _getFunctionValue(PhiFun, x[k+1]);
			    String em = "";
			    if ( phiVal >= phiOld ){
				em += "The function Phi is not decreasing." + LS
				    + "Function      : " + PhiFun.getFunction() + LS;
			    }
			    if ( em.length() > 0 )
				throw new OptimizerException(em);
			    // check if Delta needs to be reduced
			    if ( ( StrictMath.pow(phiVal, Alpha) / Delta ) < Delta ){	
				// Delta needs to be reduced.
				// Determine the biggest mesh size exponent that satisfies
				// the above test
				final int t =
				    (int)(mesSizExpInc * 
					  StrictMath.ceil( StrictMath.log(Delta /
									  StrictMath.pow(phiVal, Alpha/2.)) /
							   StrictMath.log( (double)mesSizDiv ) / mesSizExpInc ));
				if ( t <= 0 ){
				    final String em2 = "Error in implementing Phi and Alpha. Received t_k = " 
					+ t + "." + 
					LS + "But t_k should be bigger than 0.";
				    throw new OptimizerException(em2);
				}
				mesSizExp += t;
				Delta = 1. / StrictMath.pow((double)mesSizDiv, (double)mesSizExp);
				setInfo("Set N = " + getStepNumber() + 
					"; Phi = " + phiVal + 
					"; t = " + t + "; Delta = " + Delta + ".", 
					x[k+1].getSimulationNumber());
			    }
			    else
				setInfo("Set N = " + getStepNumber() + 
					"; Phi = " + phiVal + ".", 
					x[k+1].getSimulationNumber() );

			}
		    }
		}
		xGlo = null;
		xLoc = null;
		k++;
		step = 1;
	    } // end of switch
	} while (iterate && (! maxIterationReached() ) );
	
	if (iterate){ // maximum number of simulation reached
	    reportCurrentLowestPoint();
	    retFla = -1;
	}
	return retFla;
    }
	
    private final double _getFunctionValue(final FunctionEvaluator f,
					   final Point x)
	throws OptimizerException, NoSuchMethodException, IllegalAccessException,
	       InvocationTargetException{
	String s = f.getFunction();
	// replace variables in function
	s = genopt.io.FileHandler.replaceAll("%Delta%", 
					     Double.toString(Delta), s);
	s = genopt.io.FileHandler.replaceAll("%stepNumber%", 
					     Double.toString( getStepNumber() ), s);
	for(int i = 0; i < dimF; i++){
	    s = genopt.io.FileHandler.replaceAll("%" + getObjectiveFunctionName(i) + "%",
						 Double.toString( x.getF(i) ), s);
	}
	// evaluate function
	//	println( f.getName() + " = " + s);
	return (new FunctionEvaluator(f.getName(), s)).evaluate();
    }
    
    protected final boolean haveSufficientDecrease(final Point xNew,
						   final Point xBestOld)
	throws OptimizerException, NoSuchMethodException, IllegalAccessException,
	       InvocationTargetException{
	if ( PhiFun == null ) // have no sufficient decrease condition
	    return  ( xNew.getF(0) < xBestOld.getF(0) );
	else{ // have sufficient decrease condition
	    // evaluate function
	    if ( fNor == Double.MAX_VALUE ){
		// need to initialize fNor
		if ( StrictMath.abs( xBestOld.getF(0) ) > 1E-50 )
		    fNor = xBestOld.getF(0);
		else if ( StrictMath.abs( xNew.getF(0) ) > 1E-50 )
		    fNor = xNew.getF(0);
	    }
	    final double sufDec = _getFunctionValue(PhiFun, xNew);
	    // validate sufficient decrease function
	    if ( sufDec < 0 ){
		String em = "Require non-negative value for sufficient decrease." + LS
		    + "Function      : " + PhiFun.getFunction() + LS
		    + "Function value: " + sufDec;
		throw new OptimizerException(em);
	    }
	    // check sufficient decrease condition
	    //	    println(" Minimum decrease: " + Zeta + " *** " + (Zeta*sufDec) );
	    final double dif = xNew.getF(0) - xBestOld.getF(0);
	    final double norDec = ( fNor == Double.MAX_VALUE ) ? 
		dif : ( dif / fNor );
	    return ( norDec < - (Zeta*sufDec) );
	}
    }

    /** Adds two vectors of points
     *@param x0 first vector of points
     *@param x1 second vector of points
     *@return the new vector of points
     */
    protected final Point[] add(Point[] x0, Point[] x1){
	Point[] r = new Point[x0.length + x1.length];
	System.arraycopy(x0, 0, r, 0, x0.length);
	System.arraycopy(x1, 0, r, x0.length, x1.length);
	return r;
    }

    /** Gets the initial mesh size divider
     *@return the initial mesh size divider
     */
    protected final double getInitialDelta(){
	return 1. / StrictMath.pow(mesSizDiv, iniMesSizExp);
    }

    /** Gets the current mesh size factor
     *@return the current mesh size factor
     */
    protected final double getDelta() { return Delta; }

    /** Gets the iteration number
     *@return the iteration number
     */
    protected final int getIterationNumber() { return k; }

    private static final Point[] increaseSize(Point[] x){
	Point[] r = new Point[x.length + 1];
	System.arraycopy(x, 0, r, 0, x.length);
	return r;
    }

    protected static int getIndexLowestFunctionValue(Point[] x){
	if ( x.length == 0 ) // can happen if max. number of iteration is reached
	    return -1;
	else{
	    int iLow = 0;
	    for(int i = 1; i < x.length; i++){
		if ( x[i].getF(0) < x[iLow].getF(0) )
		    iLow = i;
	    }
	    return iLow;
	}
    }

    /** Sets a flag that determines whether the minimum will be reported or not.
     * This is used by algorithms that run ModelGPS multiple times, such as
     * multi-start methods,
     * and need to prevent it from reporting the minimum.
     *@param doReport
     */
    protected void reportMinimum(boolean doReport){
	RepMin = doReport;
    }

    /** Reports the minimum point depending on the flag set by 
     * <CODE>protected void reportMinimum(boolean)</CODE>.<BR>
     * This method gets the minimum point from the data base,
     * calls the function report with an
     * corresponding comment and reports the minimum
     * point to the output device.
     *@exception IOException if an I/O error in the optimization output files
     *               occurs
     */
    protected void reportMinimum() throws IOException{
	if (RepMin )
	    if (NumIniPoi == 1 ) // don't report for multi-start
		super.reportMinimum();
	    else{
		Point r = (Point)x[k].clone();
		r.setComment("Best iterate of current search.");
		report(r, SUBITERATION);
		report(r, MAINITERATION);
	    }
    }

    /** Gets the minimum point.<BR>
     *@return the point with the lowest function value
     */
    public Point getMinimumPoint(){
	if ( NumIniPoi == 1 )
	    return (Point)x[k].clone();
	else{
	    Point r = (Point)BesPoi[0].clone();
	    for(int iP = 1; iP < NumIniPoi; iP++){
		if ( BesPoi[iP].getF(0) < r.getF(0) )
		    r = (Point)BesPoi[iP].clone();
	    }
	    return r;
	}
    }    

    /** Abstract method for the global search.
     *@param x Sequence of previous iterates
     *@param delta current mesh size
     *@return the sequence of points that have been evaluated in the global search.
     *        Each point must be a mesh point. 
     *        If no point has been evaluated, then this method must return <code>EMPTY_SEARCH_SET</code>.
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
    protected abstract Point[] globalSearch(Point[] x, double delta) throws
	SimulationInputException, OptimizerException, NoSuchMethodException,
	IllegalAccessException, Exception;

    /** Abstract method for the local search.
     *@param x Sequence of previous iterates
     *@param delta current mesh size
     *@return the sequence of points that have been evaluated in the local search, regardless
     *        whether they reduced the cost function value or not.
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
    protected abstract Point[] localSearch(Point[] x, double delta) throws
	SimulationInputException, OptimizerException, NoSuchMethodException,
	IllegalAccessException, Exception;
    
    /** Checks whether the cost function value has already been
     * obtained previously.<BR>
     * If it has been obtained previously, an information message is reported.
     * If the maximum number of matching function value is obtained, an exception
     * is thrown.
     *@param x the point to be checked
     *@exception OptimizerException thrown if the maximum number of matching
     *           function value is obtained
     */
    public void checkObjectiveFunctionValue(final Point x)
	throws OptimizerException {
	if (checkObjFun) super.checkObjectiveFunctionValue(x);
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

    /** Gets the cost function value and registers it into
     * the data base
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
     *@return a clone of the point with the new function values stored
     */
    public Point getF(Point x) throws
	SimulationInputException, OptimizerException, NoSuchMethodException,
	IllegalAccessException, Exception{
	// set to next mesh point to prevent rounding errors.
	Point poi = roundCoordinates(x);
	// first, check for feasibility
	if (! isFeasible(poi) ){ // unfeasible
	    println("Unfeasible iterate.");
	    checkObjFun = false;
	    double[] f = new double[dimF];
	    for (int i = 0; i < dimF; i++)
		f[i] = Double.MAX_VALUE;
	    poi.setF(f);
	    return poi;
	}
	else{
	    final int iSim = getSimulationNumber();
	    Point r = super.getF(poi);
	    checkObjFun = ( iSim < getSimulationNumber() );
	    return r;
	}
    }
    
    /** Gets the number of step reductions done up to now.
	The return value is a zero-based counter.
     *@return the number of step reductions done up to now
     */
    protected int getNumberOfStepReduction(){ return nSteRed; }

    /** Gets the maximum number of step reductions.
     *@return the maximum number of step reductions
     */
    protected int getMaximumNumberOfStepReduction(){ return nMaxSteRed; }

    /** Sets the maximum number of step reductions.
     *@param maxNumberOfStepReductions the maximum number of step reductions
     */
protected void setMaximumNumberOfStepReduction(int maxNumberOfStepReductions){ 
	assert ( maxNumberOfStepReductions >= 0 ) : "Number of step reductions must be non-negative.";
	nMaxSteRed = maxNumberOfStepReductions;
    }


    /** Gets the mesh point, on a rectangular mesh, that is closest to the argument.
     *
     *@param x independent parameter
     *@param xMes any point on the mesh
     *@param delta mesh size factor times the step size of the variable
     *@return the mesh point that is closest to <code>x</code> in the Euclidean norm.
     */
    public static double[] getClosestEuclideanMeshPoint(double[] x,
							double[] xMes,
							double[] delta){
	assert ( x.length == delta.length );
	assert ( x.length == xMes.length );

	double[] r = new double[x.length];
	for (int i = 0; i < x.length; i++){
	    final double mul = StrictMath.rint( ( x[i] - xMes[i] ) / delta[i] );
	    r[i] = xMes[i] + mul * delta[i];
	}
	return r;
    }

    /** The number of independent variables */
    private int dimX;
    /** The number of function values */
    private int dimF;

    /** Factor that will be multiplied with the function <code>PhiFun</code>
     * for sufficient decrease in cost */
    private double Zeta;
    /** Function evaluator for the sufficient decrease condition and the mesh size control, i.e., the function phi */
    private FunctionEvaluator PhiFun;
    /** Exponent used for the control of the mesh size if adaptive precision function evaluations are used */
    private double Alpha;

    /** The base direction matrix.<BR>
	Each column vector is a direction.
    */
    public double[][] basDirMat;
    /** The mesh size divider 'r' */
    private int mesSizDiv;
    /** The initial mesh size exponent 's_k' */
    private int iniMesSizExp;
    /** The increment of the mesh size exponent 't_k' (fixed for all k) */
    private int mesSizExpInc;
    /** The maximal number of step reduction before the algorithm terminates */
    protected int nMaxSteRed;
    /** The number of step reduction done up to now */
    private int nSteRed;
    /** The sequence of iterates */
    private Point[] x;
    /** The cost function value that will be used in the normalization of the sufficient decrease condition */
    private double fNor;
    /** The iteration number (counting only the main loop) */
    private int k;
    /** The mesh size factor */
    private double Delta;
    /** A flag whether the cost function value has to be checked for previous
     * matching results.
     */
    protected boolean checkObjFun;

    /** Number of intial point of the multi-start algorithm */
    private int NumIniPoi;
    /** Points for the multi-start algorithms.
     * This array holds the best iterate obtained from each starting point.*/
    private Point[] BesPoi;
    /** Flag that determines whether the minimum will be reported or not */
    private boolean RepMin;

    /** Random number generator (used for multistart algorithm) */
    private Random RanGen;


    protected static String SUCMOV = "Cost sufficiently reduced   at ";
    protected static String FAIMOV = "Cost not sufficiently reduced at ";
    protected static final String POSDIR = "+d";
    protected static final String NEGDIR = "-d";

}
