package genopt.algorithm.util.linesearch;

import genopt.lang.OptimizerException;
import genopt.algorithm.Optimizer;
import genopt.algorithm.util.math.LinAlg;

import java.io.*;

/** Class for doing a line search using
  * the Fibonacci division.
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

public class FibonacciDivision extends IntervalDivider
{

    /** Constructor
      *@param opt a reference to the Optimizer object
      */
    public FibonacciDivision(Optimizer opt)
    {
		super(opt);
		this.setMaxIntRed(NINTREDMAXDEF); // to generate the Fibonacci numbers
    }

	/** Gets the reduction factor q = I(n+1)/I(n)
	  *@return the reduction factor q = I(n+1)/I(n)
	  */
	protected double getReductionFactor()
	{
		int i = nIntRedMax-nIntRed+1;
		return ((double)fibo[i])/((double)fibo[i+1]);
	}


	/** Gets the Fibonacci numbers
	  *@param N the number of elements in the Fibonacci serie
	  *@return an array containing the Fibonacci numbers
	  */
	public static int[] getFibonacci(int N)
	{
		int[] f = new int[N];
		int i = 0;
		f[i] = 1;
		if (N == ++i) return f;
		f[i] = 1;
		if (N == ++i) return f;
		for ( ; i < N; i++)
			f[i] = f[i-1] + f[i-2];
		return f;
	}
 
    /** Sets the fraction of the desired uncertainty interval (0..1)
      *@param dx the normalized fraction of the uncertainty interval
      */
    public void setUncertaintyInterval(double dx)
    {
        int n;
        if (dx > 0 && dx < 1)
		{
			n = -1;
			do
			{
				n++;
			} while(dx < 1./(double)getFibonacci(n+2)[n+1]);
		}
        else
            n = 0; // will be set to default on next line
        setMaxIntRed(n);
    }
	
	/** Set the minimal absolut difference between the lowest
	  * 3 function values as the stopping criteria
	  *@param dFMinimal the minimal difference between the lowest
	  *       3 function values that has to be obtained before the search stops
	  *@param nMax the maximum number of iteration before the
	  *       search stops (in case that <CODE>dFMinimal</CODE>
	  *       cannot be obtained within a reasonable number of trials)
	  *@exception OptimizerException if method is used for Fibonacci algorithm
	  */
	public void setAbsDFMin(double dFMinimal, int nMax) throws OptimizerException
	{
		throw new OptimizerException(
			"Accuracy of objective function cannot be specified for Fibonacci algorithm.");
	}
    
    /** Sets the maximum number of interval reductions
      *@param n the maximum number of interval reductions
      */
    public void setMaxIntRed(int n)
    {
        super.setMaxIntRed(n);
		fibo = getFibonacci(nIntRedMax+3); // nIntRedMax = n-1
    }
    
	/** The Fibonacci numbers */
	private int[] fibo;
}







