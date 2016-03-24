package genopt.algorithm.util.linesearch;
import genopt.simulation.SimulationInputException;
import genopt.lang.OptimizerException;
import genopt.algorithm.DiscreteArmijoGradient;
import genopt.algorithm.Optimizer;
import genopt.algorithm.util.math.*;

/** Class for doing a line search using
  * the Armijo algorithm with reset option for the step-size.
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
  * GenOpt Copyright (c) 1998-2016, The Regents of the University of
  * California, through Lawrence Berkeley National Laboratory (subject 
  * to receipt of any required approvals from the U.S. Dept. of Energy).  
  * All rights reserved.
  *
  * @author <A HREF="mailto:MWetter@lbl.gov">Michael Wetter</A>
  *
  * @version GenOpt(R) 3.1.1 (March 24, 2016)<P>
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

public class Armijo{
    /** Constructor
     * @param optimizer a reference to the Optimizer object
     * @exception OptimizerException if an OptimizerException occurs or
     *           if the user required to stop GenOpt
     * @exception SimulationInputException if an error in writing the
     *           simulation input file occurs
     * @exception NoSuchMethodException if a method that should be invoked could not be found
     * @exception IllegalAccessException  if an invoked method enforces Java language access 
     *                                    control and the underlying method is inaccessible
     * @exception InvocationTargetException if an invoked method throws an exception
     * @exception Exception if an I/O error in the simulation input file occurs
     */
    public Armijo(DiscreteArmijoGradient optimizer, 
		  final int kSta, 
		  final int lMax,
		  final int kappa,
		  final double alpha, 
		  final double beta){
	Opt = optimizer;
	KSta = kSta;
	assert lMax >= 0 : "Negative value for LMax.";
	LMax = lMax;
	assert kappa >= 0 : "Negative value for Kappa.";
	Kappa = kappa;
	Alp = alpha;
	Bet = beta;
	K = new int[2];
    }

    /** Runs the line search.
     * @exception OptimizerException if an OptimizerException occurs or
     *           if the user required to stop GenOpt
     * @exception SimulationInputException if an error in writing the
     *           simulation input file occurs
     * @exception NoSuchMethodException if a method that should be invoked could not be found
     * @exception IllegalAccessException  if an invoked method enforces Java language access 
     *                                    control and the underlying method is inaccessible
     * @exception InvocationTargetException if an invoked method throws an exception
     * @exception Exception if an I/O error in the simulation input file occurs
     */
    public void run(final int i,
		    final Point x, 
		    final double[] h,
		    final int k, 
		    final double del)
	throws SimulationInputException, OptimizerException, NoSuchMethodException,
	       IllegalAccessException, Exception{

    xLS = new Point[2];
	xLS[0] = (Point)x.clone();
	    
	boolean resetK = false;
	if ( i == 0 )
	    K[0] = KSta;
	else
	    K[0] = k;

	Opt.println("Starting line search with k = " + K[0]);

	xLS[0].setComment("Line search.");
	double lam0 = 0;
	double lam1 = 0;
	xLS[1] = (Point)xLS[0].clone();
	int loopCount = 0;
	boolean iterate = true;
	boolean con0 = false;
	boolean con1 = false;
	do{
	    loopCount++;
	    // first condition
	    if ( con0 == false ){
		lam0 = StrictMath.pow(Bet, K[0]);
		xLS[0].setX( LinAlg.add( x.getX(), 
					 LinAlg.multiply( lam0, h) ));
		xLS[0].setComment("Line search. lambda = " + lam0 + ".");
		// if con1 == false, wait with evaluting xLS[0] so 
		// it can be evaluated in parallel with xLS[1]
		if ( con1 == true ){
		    xLS[0] = Opt.getF(xLS[0]);
		}
	    }
	    // second condition 
	    if ( con1 == false ){
		K[1] = K[0]-1;
		lam1 = StrictMath.pow(Bet, K[1]);
		xLS[1].setX( LinAlg.add( x.getX(), 
					 LinAlg.multiply( lam1, h) ));
		xLS[1].setComment("Line search. lambda = " + lam1  + ".");
		// if con0 == false, evaluate xLS[0] and xLS[1] here. Otherwise, 
		// evaluate only xLS[1]
		if ( con0 == false ){
		    xLS = Opt.getF(Optimizer.SUBITERATION, xLS);
		}
		else{
		    xLS[1] = Opt.getF(xLS[1]);
		}
		
	    }
	    // update the conditions
	    con0 = ( xLS[0].getF(0) - x.getF(0) <= lam0 * Alp * del );
	    con1 = ( xLS[1].getF(0) - x.getF(0) > lam1 * Alp * del );
	    // check conditions		    
	    if ( con1 == false ) {
		Opt.println("Increase step size.");
		K[0] = K[1];
		lam0 = lam1;
		xLS[0] = (Point)xLS[1].clone();
		K[1] -= 1;
	    }
	    else if ( con0 == false ){
		Opt.println("Reduce step size.");
		K[1] = K[0];
		lam1 = lam0;
		xLS[1] = (Point)xLS[0].clone();
		K[0] += 1;
	    }
	    // check whether we need to restart
	    iterate = (con0 == false || con1 == false);
	    if ( iterate && ( loopCount >= LMax || K[0] > (KSta + Kappa) )){
		resetK = true;
		iterate = false;
	    }
		
	} while ( iterate  );

	if ( resetK ){
	    Opt.println("Initialize step size for line search. k = " + K[0] + ".");
	    K[0] = KSta - 1;
	    boolean con = false;
	    xLS[0].setComment("Line search initialization.");
	    do{
		K[0] += 1;
		final double lam = StrictMath.pow(Bet, K[0]);
		xLS[0].setX( LinAlg.add( x.getX(), 
					 LinAlg.multiply( lam, h) ));
		xLS[0] = Opt.getF(xLS[0]);
		con = ( xLS[0].getF(0) - x.getF(0) <= lam * Alp * del );
	    } while(  con == false );
	}
	Opt.println("Ending line search with k = " + K[0] + ".");
    }
    /** Gets the exponent <CODE>k</CODE> of <CODE>Beta</CODE>.
     * @return the value <CODE>k</CODE> of the point
     */
    public int getKWithLowestCost(){ return K[ getIndexWithLowestCost() ];  }

    /** Gets the point with lowest cost.
     * @return the point with the lowest cost function value
     */
    final public Point getPointWithLowestCost(){
	return (Point)xLS[ getIndexWithLowestCost() ].clone();
    }

    /** Gets the index to the point with the lowest function value.
     * @return the index of the point with the lowest cost function value
     */
    final public int getIndexWithLowestCost(){ 
	if ( xLS[1] == null )
	    return 0;
	else{
	    if ( xLS[0].getF(0) < xLS[1].getF(0) )
		return 0;
	    else
		return 1;
	}
    }

    /** The reference to the Optimizer object */
    protected DiscreteArmijoGradient Opt;
    /** Algorithm parameter */
    private double Alp;
    /** Algorithm parameter */
    private double Bet;
    /** Algorithm parameter */
    private int KSta;
    /** Algorithm parameter */
    private int LMax;
    /** Algorithm parameter */
    private int Kappa;
    /** The two points of the line search */
    protected Point[] xLS;
    /** The exponents <CODE>k</CODE> and <CODE>k-1</CODE> of <CODE>Beta</CODE> */
    protected int[] K;
}







