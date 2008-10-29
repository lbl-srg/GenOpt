package genopt.simulation;

import genopt.algorithm.Optimizer;
import genopt.algorithm.util.math.Point;
import genopt.simulation.SimulationInputException;
import genopt.lang.OptimizerException;
import java.util.concurrent.atomic.AtomicInteger;

/** Object for creating a thread that executes a simulation.
  *
  * The method {@link #genopt.algorithm.Optimizer.getF(Point[], boolean)}n
  * makes instances of this class to parallelize the simulations.
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
  * @version GenOpt(R) 2.1.0 (May 29, 2008)<P>
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

public class SimulationThread implements Runnable
{

    /** Constructor.
     *
     * @param optimizer reference to the optimizer instance
     * @param poi the point to be evaluated
     */
    public SimulationThread(Optimizer optimizer, Point poi){
	opt = optimizer;
	x = poi;
	exc = null;
	iExc = new AtomicInteger(0);
    }

    /** Runs the simulation.
     *
     *  This method stores all exceptions that may occur during its execution. The exceptions
     *  can be retrieved by calling {@link #throwStoredException() throwStoredException}
     */
    public void run(){
	if (iExc.get() == 0){
	    try{  opt.simulate(x); }
	    catch(SimulationInputException e){ simInpExc = e; iExc.incrementAndGet(); }
	    catch(OptimizerException e){ optExc = e; iExc.incrementAndGet(); }
	    catch(NoSuchMethodException e) { noSucMetExc = e; iExc.incrementAndGet(); }
	    catch(IllegalAccessException e){ illAccExc = e; iExc.incrementAndGet(); }
	    catch(Exception	e){ exc = e; iExc.incrementAndGet(); }
	}
	else if (!opt.mustStopOptimization()){
	    opt.println("Skipping evaluation of simulation " + 
			x.getSimulationNumber() + " because of previous simulation error.");
	}
	opt.done.countDown(); // count down the count down latch
    }

    /** Throws all exceptions that have been catched when running 
     *  {@link #run() run}.
     *
     * @exception SimulationInputException
     * @exception OptimizerException
     * @exception NoSuchMethodException
     * @exception IllegalAccessException
     * @exception Exception
     */
    public void throwStoredException() 
	throws SimulationInputException, OptimizerException, NoSuchMethodException,
	       IllegalAccessException, Exception{
	if ( iExc.get() > 0 ){
	    if ( simInpExc != null ) throw  simInpExc;
	    if ( optExc != null ) throw  optExc;
	    if ( noSucMetExc != null ) throw  noSucMetExc;
	    if ( illAccExc != null ) throw  illAccExc;
	    if ( exc != null ) throw  exc;
	}
    }

    /** The point to be evaluated */
    protected Point x;
    /** The reference to the GenOpt Optimizer instance */
    protected Optimizer opt;
    /** The number of exceptions that have been accumulated */
    protected static AtomicInteger iExc;
    /** The <b>SimulationInputException</b> exception */
    protected SimulationInputException simInpExc;
    /** The <b>OptimizerException</b> exception */
    protected OptimizerException optExc;
    /** The <b>NoSuchMethodException</b> exception */
    protected NoSuchMethodException noSucMetExc;
    /** The <b>IllegalAccessException</b> exception */
    protected IllegalAccessException illAccExc;
    /** The <b>Exception</b> exception */
    protected Exception exc;
}
