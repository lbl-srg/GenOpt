package genopt.algorithm;
import java.io.IOException;
import genopt.GenOpt;
import genopt.simulation.SimulationInputException;
import genopt.algorithm.util.math.*;
import genopt.algorithm.util.pso.*;
import genopt.lang.OptimizerException;
import genopt.io.InputFormatException;

/** Class for minimizing a function the particle swarm optimization
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

public class PSOCC extends genopt.algorithm.util.pso.ModelPSO
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
    public PSOCC(GenOpt genOptData)
        throws OptimizerException, IOException, Exception, InputFormatException {
        super(genOptData);
	
	final double conGai = getInputValueDouble("ConstrictionGain", 
						  0, Optimizer.EXCLUDING,
						  1, Optimizer.INCLUDING);

	setConstrictionCoefficientParameter(conGai);
    }

    /** Sets the constriction coefficient parameter
     *@param conGai gain for constriction factor
     */
    protected final void setConstrictionCoefficientParameter(final double conGai){
	final double phi = CogAcc + SocAcc;
	if (phi > 4)
	    ConFac = 2. * conGai / StrictMath.abs( 2. - phi - StrictMath.sqrt( phi * ( phi - 4. ) ) );
	else
	    ConFac = conGai;
    }


    /** Updates the particle velocity
     */
    protected void updateVelocity(){
	final double rho1 = CogAcc * RanGen.nextDouble();
	final double rho2 = SocAcc * RanGen.nextDouble();
	for(int iP = 0; iP < NumPar; iP++){
	    // velocity of continuous parameters
	    if ( dimCon > 0 ){
		VelCon[iP] = LinAlg.add( VelCon[iP], 
					 LinAlg.multiply(rho1, 
							 LinAlg.subtract(LocBes[iP].getX(), 
									 CurPop[iP].getX())));
		VelCon[iP] = LinAlg.add( VelCon[iP], 
					 LinAlg.multiply(rho2, 
							 LinAlg.subtract(GloBes[iP].getX(),
									 CurPop[iP].getX())));
		VelCon[iP] = LinAlg.multiply(ConFac, VelCon[iP]);

		// clamp velocity
		for(int j = 0; j < dimCon; j++)
		    VelCon[iP][j] = ModelPSO.clampVelocity( VelCon[iP][j], VelMaxCon[j]);
	    }
	    // velocity of discrete parameter
	    for(int iV = 0; iV < dimDis; iV++){
		VelDis[iP][iV] = LinAlg.add( VelDis[iP][iV], 
					     LinAlg.multiply(rho1, 
							     LinAlg.subtract(LocBes[iP].getGrayBinaryString(iV), 
									     CurPop[iP].getGrayBinaryString(iV))));
		VelDis[iP][iV] = LinAlg.add( VelDis[iP][iV], 
					     LinAlg.multiply(rho2, 
							     LinAlg.subtract(GloBes[iP].getGrayBinaryString(iV),
									     CurPop[iP].getGrayBinaryString(iV))));
		// clamp velocity
		// to prevent saturation of the sigmoid function
		for(int iB = 0; iB < LenBitStr[iV]; iB++)
		    VelDis[iP][iV][iB] = ModelPSO.clampVelocity( VelDis[iP][iV][iB], MaxVelDis);
	    }
	}
    }
    
    /** Constriction Factor */
    private double ConFac;
}
