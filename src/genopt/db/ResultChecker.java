package genopt.db;

import genopt.lang.OptimizerException;

/** Object that checks how often an objective
  * function value has already been achieved previously.
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
  * @version GenOpt(R) 3.0.1 (August 14, 2009)<P>
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

public class ResultChecker
{
	/** System dependent line separator */
	private final static String LS = System.getProperty("line.separator");

	/** Constant for increasing the array size
	  */
	private final int ARRAYINCREMENT = 5;

	/** Constructor
	  *@param maxNumberOfMatchingResults number how many results can be
	  *       equal without an OptimizerException is thrown
	  */
	public ResultChecker(int maxNumberOfMatchingResults)
	{
		maxMatVal = maxNumberOfMatchingResults;
		resNum = -1;
		matVal = 0;
		numMatVal = 1;
		arrSiz = ARRAYINCREMENT;
		f   = new double[arrSiz];
		num = new int[arrSiz];
		cou = new int[arrSiz];
		for (int i=0; i < arrSiz; i++)
		{
			f[i] = Double.NEGATIVE_INFINITY;
			num[i] = Integer.MIN_VALUE;
			cou[i] = 0;
		}
	}
	
	/** sets a new trial
	  *@param functionValue value of the objective function
	  *@param runNumber the number of the optimization run
	  */
	public void setNewTrial(double functionValue, int runNumber)
	{
		resNum++;
		if (arrSiz == resNum) 
			increaseArraySize();
		setRun(functionValue, runNumber);
	}
	
	/** checks whether the maximum number of matching results is reached.
	  * If so, an OptimizerException is thrown.
	  *@exception OptimizerException thrown if the maximum number
	  *    of equal results is reached
	  */
	public void check() throws OptimizerException
	{
		if (matVal > maxMatVal)
		{
			int[] pt = getMatchingNumbers();
			String em =
				"Optimiziation terminated due to no change" + LS +
				"  in objective function value." + LS;
			for (int i=0; i < pt.length; i++)
				em +="  Run number = " + num[pt[i]] + " ; f(x) = " + f[pt[i]] + LS;
			em +="  Variation too small. Write either the objective function value with a" + LS +
				"  higher accuracy or reduce the required accuracy of the optimization.";
			throw new OptimizerException(em);
		}
	}

	/** sets the maximal allowed number of matching results
	  *@param maxNumberOfMatchingResults number how many results can be
	  *       equal without an OptimizerException is thrown
	  */
	public void setNumberOfMatchingResults(int maxNumberOfMatchingResults)
	{
		if (maxNumberOfMatchingResults >= 0)
			maxMatVal = maxNumberOfMatchingResults;
	}	
	/** gets the total number of matching results
	  *@return the total number of matching results
	  */
	public int getNumberOfMatchingResults() { return numMatVal; }
	 
	/** gets an array with the indices of all matching numbers
	  *@return array with the indices of all matching numbers
	  */
	private int[] getMatchingNumbers()
	{
		if (matVal < 2) return null;
		int[] tr = new int[f.length];
		int j=0;
		for(int i=0; i < f.length-1; i++)
			if (cou[i] == matVal)
				tr[j++] = i;
		int[] r = new int[j];
		System.arraycopy(tr, 0, r, 0, j);
		return r;
	}

	/** increases the array size */
	private void increaseArraySize()
	{
		double[] tf   = new double[arrSiz];
		int[]    tnum = new int[arrSiz];
		int[]    tcou = new int[arrSiz];
		System.arraycopy(f,   0, tf, 0, arrSiz);
		System.arraycopy(num, 0, tnum, 0, arrSiz);
		System.arraycopy(cou, 0, tcou, 0, arrSiz);
		int arrSizOld = arrSiz;
		arrSiz += ARRAYINCREMENT;
		f   = new double[arrSiz];
		num = new int[arrSiz];
		cou = new int[arrSiz];
		System.arraycopy(tf,   0, f,   0, arrSizOld);
		System.arraycopy(tnum, 0, num, 0, arrSizOld);
		System.arraycopy(tcou, 0, cou, 0, arrSizOld);
		for (int i=arrSizOld; i < arrSiz; i++)
		{
			f[i] = Double.NEGATIVE_INFINITY;
			num[i] = Integer.MIN_VALUE;
			cou[i] = 0;
		}
	}
	
	/** sets the new trial
	  *@param functionValue value of the objective function
	  *@param runNumber the number of the optimization run	
	  */
	private void setRun(double functionValue, int runNumber)
	{
		int pos = resNum + 1;
		do
			pos--;
		while (pos > 0 && functionValue > f[pos-1]);
		// shift results down (new value inserted at top
		System.arraycopy(f,   pos, f,   pos+1, resNum-pos);
		System.arraycopy(num, pos, num, pos+1, resNum-pos);
		System.arraycopy(cou, pos, cou, pos+1, resNum-pos);
		f[pos] = functionValue;
		num[pos] = runNumber;
		cou[pos] = 1;
		// check whether results below have the same value
		// pos is the index of the currently inserted value
		if (pos > 0 && f[pos] == f[pos-1])
		{	// we have matching results
			cou[--pos]++;
			cou[pos+1] = cou[pos];
			// update the counter for the maximal equal values
			matVal = (cou[pos] > matVal) ? cou[pos] : matVal;
			// increment the counter for the total number of matching results
			numMatVal++;
		}
		// update counter for other elements with same results
		while(pos > 0 && f[pos] == f[pos-1])
			cou[--pos]++;
	}
	
	/** array with the objective function values, sorted with highest value at the
		0-th position */
	private double[] f;
	/** array with the run number, sorted with highest value at the
		0-th position*/	
	private int[]    num;
	/** array with the counter of equal results*/	
	private int[]    cou;
	/** current size of array */
	private int arrSiz;
	/** number of results, starting with 0 */
	private int resNum;
	/** counter for number of matching values.<BR>
        <B>Note:</B> i.e., {1,3,9} has matVal = 1; {1,1,2} has matVal=2;
		{1,1,2,2,2} has matVal=3; */
	private int matVal;
	/** counter for total number of matching values.<BR>
        <B>Note:</B> i.e., {1,3,9} has numMatVal = 1; {1,1,2} has numMatVal=2;
		{1,1,2,2,2} has numMatVal=5;*/	
	private int numMatVal;
	/** maximum allowed number of matching values */
	private int maxMatVal;
	
/*	public static void main(String args[])
	{
		System.out.println("**************");
		
		ResultChecker rc = new ResultChecker(2);
		int j = 0;
//			for(int i = 0; i < 24; i++)
//			{
//				//rc.setNewTrial(Math.sin((double)i), j++);
//			}
		rc.setNewTrial(60, j++);
		rc.setNewTrial(10, j++);
		rc.setNewTrial(1, j++);
		rc.setNewTrial(-5, j++);
		rc.setNewTrial(-5, j++);
		rc.setNewTrial(-5, j++);
		rc.setNewTrial(1, j++);
		System.out.println(rc.getNumberOfMatchingResults());
		genopt.algorithm.util.math.LinAlg.print(rc.getF());
		genopt.algorithm.util.math.LinAlg.print(rc.getCou());
		genopt.algorithm.util.math.LinAlg.print(rc.getNum());
		
		j = j;
	}
*/	
}







