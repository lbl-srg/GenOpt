package genopt.db;

import genopt.io.FileHandler;
import genopt.*;
import genopt.algorithm.util.math.Point;
import java.io.IOException;
import java.io.File;
import java.io.FileWriter;
import java.util.LinkedList;

/** Object that holds all Points of each main and sub iteration.
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

public class ResultManager
{
    /** System dependent line separator */
    private final static String LS = System.getProperty("line.separator");

    /** Name of output file with main iteration steps only */
    private static final String OUTFILNAM[] =
    {"OutputListingMain.txt", "OutputListingAll.txt"};
	
    /**@param GenOptRef reference to GenOpt object
	  *@param outputFilePath path of the output file
	  *@param outputFileHeader header to be written to the output file
	  *@param functionNames Array of String where each String holds the name of
	  *   a function value
	  *@param cPar array containing the continuous parameters
	  *@param dPar array containing the discrete parameters
	  *@exception IOException
	  */
    public ResultManager(GenOpt GenOptRef, String outputFilePath,
			 String outputFileHeader, String[] functionNames, 
			 ContinuousParameter[] cPar, DiscreteParameter[] dPar)
	throws IOException{
	go = GenOptRef;
	dimCon = (cPar == null) ? 0 : cPar.length;
	dimDis = (dPar == null) ? 0 : dPar.length;
	conPar = new ContinuousParameter[dimCon];
	disPar = new DiscreteParameter[dimDis];
	for (int i=0; i < dimCon; i++)
	    conPar[i] = (ContinuousParameter)cPar[i].clone();
	for (int i=0; i < dimDis; i++)
	    disPar[i] = (DiscreteParameter)dPar[i].clone();

	// names
	dimF = functionNames.length;
	nameF = new String[dimF];
	for (int i = 0; i < dimF; i++)
	    nameF[i] = new String(functionNames[i]);

	outFilNam = new String[OUTFILNAM.length];

	// make instance of lists containing the results
	ptsMai = new LinkedList<ResultPoint>();
	ptsSub = new LinkedList<ResultPoint>();

	simNum = 0;
	subIteNum = 1;
	maiIteNum = 0;
	resNum = 0;

	increaseResultNumber(0);

	// check and instanciate output file
	if (outputFilePath == null)
	    outFilPat = ".";
	else
	    outFilPat = outputFilePath;
		
	for (int i = 0; i < OUTFILNAM.length; i++)
	    outFilNam[i] = new String(outFilPat + File.separator + OUTFILNAM[i]);

	if (!outputFileHeader.endsWith(LS))
	    outputFileHeader = new String(outputFileHeader + LS);

	String[] head = new String[OUTFILNAM.length];
	head[0] = new String(LS + "Main Iterations" + LS + LS);
	head[0] += LS + "Simulation Number\tMain Iteration\tStep Number";

	head[1] = new String(LS + "All Iterations" + LS + LS);
	head[1] += LS + "Simulation Number\tMain Iteration\tSub Iteration\tStep Number";

	String sN = new String("");
	for (int i = 0; i < dimF; i++)
	    sN += "\t" + nameF[i];		
	for (int i = 0; i < dimCon; i++)
	    sN += "\t" + conPar[i].getName();
	for (int i = 0; i < dimDis; i++)
	    sN += "\t" + disPar[i].getName();
	sN += LS;
		
	for (int i = 0; i < OUTFILNAM.length; i++)
	    FileHandler.writeFile(outputFileHeader+head[i]+sN, outFilPat, OUTFILNAM[i]);
    }

    /** appends a String to the output listing files
     *@param s String to be appended to output listing files
     *@exception IOException
     */
    public void append(String s) throws IOException{
	for (int i = 0; i < OUTFILNAM.length; i++)
	    FileHandler.appendToFile(outFilNam[i], s);
    }
    

    /** Increases the number of function evaluations
     */
    public void increaseNumberOfFunctionEvaluation() { simNum++; }


    /** Resets the counter of the main and sub iterations to 1.
     */
    public void resetResultNumber(){
	// set counters
	subIteNum = 1;
	maiIteNum = 1;
    }


    /** Increases the number of the result entry, sets the number of the main
     * and the sub iteration and writes the output to the text files.<BR>
     * <b>You must call this function at every new iteration step and after the last
     *    iteration step of the optimization (to get the last result reported)</b>
     * @param runIde <CODE>0</CODE> if main iteration, <CODE>1</CODE> if
     *        sub iteration
     */
    private void increaseResultNumber(int runIde){
	// set counters
	if (runIde == 0){
	    maiIteNum++;
	    subIteNum = 1;
	}
	else{
	    subIteNum++;
	    resNum++;
	}
    }
    
    /** Sets the new point
     * @param x Point to be reported
     * @param runIde <CODE>0</CODE> if main iteration, 
     *               <CODE>1</CODE> if sub iteration
     * @exception IOException
     */
    public void setNewTrial(Point x, int runIde) throws IOException{
	ResultPoint rp = new ResultPoint(x);
	rp.setSimulationNumber(simNum, maiIteNum, subIteNum);
	
	// Add point to stored results
	switch (runIde) {
	case 0:
	    ptsMai.add(rp);
	    break;
	case 1: 
	    ptsSub.add(rp);
        break;
	default:
	    throw new IOException("Program error: Wrong value for parameter runIde.");
	}
	printPoint(rp, runIde);
	
	increaseResultNumber(runIde);
	
	if (go != null)
	    go.setNewTrial();
    }

    /** Returns the number of simulations
     * @return the number of simulations
     */
    public int getNumberOfSimulation()  { return simNum; }
    
    /** Returns the number of all results
     * @return the number of all results
     */
    public int getNumberOfAllResults()  { return resNum; }
    
    /** Returns the number of the main iterations
     * @return the number of the main iterations
     */
    public int getNumberOfMaiIteration()  { return maiIteNum; }

   
    /** Gets an array with the points of all iteration steps
     * <B>(including subiterations)</B>.<BR>
     * @param numberOfValues number of iteration steps of which the datas
     *                       are wanted, counting backwards from the current 
     *                       iteration step
     * @return array containing the points
     */
    public Point[] getAllPoint(final int numberOfValues){
	int j = 0;
	
	Point[] r = new Point[numberOfValues];
	
	for (int i = resNum - numberOfValues; i < resNum; i++, j++){
	    r[j] = (Point)(ptsSub.get(i));
	}

	return r;
    }
    
    /** Reports the minimum point to the output files
     * @exception IOException
     */
    public void reportMinimum(final String comment) throws IOException{
	ResultPoint rp = getMinimumResultPoint();
	if (comment != null)
	    rp.setComment(comment);
	printPoint(rp, 0);
	printPoint(rp, 1);
    }    

    /** Prints a ResultPoint to the output file
     *@param rp ResultPoint to be printed
     * @param runIde <CODE>0</CODE> if main iteration, <CODE>1</CODE> if
     *        sub iteration
     * @exception IOException
     */
    private void printPoint(ResultPoint rp, int runIde)
	throws IOException {
	String s = new String("");
	// write output to text file
	// run number
	s += rp.getSimulationNumber();
	// main iteration number
	s += "\t" + rp.getMainIterationNumber();
	// sub iteration number
	if (runIde > 0)
	    s += "\t" + rp.getSubIterationNumber();
	// step number
	s += "\t" + rp.getStepNumber();
	// function values, coordinates, and comment
	for (int i = 0; i < dimF; i++)
	    s += "\t" + rp.getF(i);
	for (int i = 0; i < dimCon; i++) // continuous parameters
	    s += "\t" + go.ioSet.toString(rp.getX(i));
	for (int i = 0; i < dimDis; i++) // discrete parameters
	    s += "\t" + disPar[i].getValueString( rp.getIndex(i) );

	if (rp.getComment() == null)
	    rp.setComment("");
	s += "\t" + rp.getComment() + LS;
	FileHandler.appendToFile(outFilPat + File.separator + OUTFILNAM[runIde], s);
    }

    /** Gets the point with the lowest objective function value of the main iterations
     * @return the point with the lowest objective function value of the main iterations
     */
    public Point getMinimumPoint(){
	return (Point)getMinimumResultPoint();
    }

    /** Gets the run number with the lowest objective function value of the main iterations
     * @return the run number with the lowest objective function value of the main iterations
     */
    public int getMinimumPointSimulationNumber(){
	return ((ResultPoint)getMinimumResultPoint()).getSimulationNumber();
    }

    /** Gets the ResultPoint with the lowest objective function value 
     *  of the main iterations with the highest step number.
     * @return the ResultPoint with the lowest objective function value of the main iterations
     */
    protected ResultPoint getMinimumResultPoint(){
	ResultPoint r = new ResultPoint((ResultPoint)ptsMai.get(ptsMai.size()-1));
	int step = r.getStepNumber();
	double fMin = r.getF(0);
	
	for (int j = ptsMai.size()-1; j > -1; j--) {
	    ResultPoint pt = (ResultPoint)(ptsMai.get(j));
	    if (step == pt.getStepNumber()) {
		if (fMin > pt.getF(0)) {
		    r = (ResultPoint)pt.clone();
		    fMin = r.getF(0);
		}
	    }
	    //	    else  we need to run through all steps because multi-start algo can
	    //      set the step number to 1 if they start a new search
		//		j = -1; // step number changed, break inner loop
	}
	return r;		
    }

    /** Gets the absolute difference of the last objective function value and 
	  *    the second last objective function value (but <b>not</b> the absolute value
	  *    of the difference) where both are values of the a main iteration 
	  * @return f(k)-f(k-1) where f is the objective function value and k is a
	  *    counter for the main iteration
	  */
    public double getAbsDifMaiObjFunVal()
    {
	int n = ptsMai.size();
	if (n > 2){
	    double f1 = ((Point)(ptsMai.get(n-1))).getF(0);
	    double f2 = ((Point)(ptsMai.get(n-2))).getF(0);
	    return f1-f2;
	}
	else
	    return Double.MAX_VALUE;
    }
    
    /** Gets the relative difference of the last objective function value and 
	  *    the second last objective function value where both are values of 
	  *    a main iteration 
	  * @return (f(k)-f(k-1))/f(k) where f is the objective function value and k is a
	  *    counter for the main iteration
	  */
    public double getRelDifMaiObjFunVal()
    {
	if (maiIteNum > 1) {
	    int nMax = ptsMai.size();
	    double fk =  Math.abs(((ResultPoint)ptsMai.get(nMax-2)).getF(0));
	    return (fk > 1E-10) ? 
		Math.abs(((ResultPoint)ptsMai.get(nMax-1)).getF(0) - fk ) / fk :
		1E-10;  // used to be 1E+10, 01/19/00
	}
	else
	    return Double.MAX_VALUE;
    }
    
    /** gets the names of the variables
     *@return an array with all the names of the variables
     */
    public String[] getNameContinuousAndDiscrete(){
	String[] r = new String[dimCon+dimDis];
	for(int i = 0; i < dimCon; i++)
	    r[i] = conPar[i].getName();
	for(int i = 0; i < dimDis; i++)
	    r[dimCon+i] = disPar[i].getName();
	return r;
    }

    /** gets the names of the function values
	  *@return an array with all the names of the function values
	  */
    public String[] getNameF() {
	String[] r = new String[dimF];
	System.arraycopy(nameF, 0, r, 0, dimF);
	return r;
    }	
	
    /** gets the number of continuous, free parameters
     *@return the number of continuous, free parameters
     */
    public int getDimensionContinuous() { return dimCon; }

    /** gets the number of discrete, free parameters
     *@return the number of discrete, free parameters
     */
    public int getDimensionDiscrete() { return dimDis; }


    /** gets the number of function values
     *@return the number of function values
     */
    public int getDimensionF() { return dimF; }	

    ///////////////////////////////////////////////////////////////////////////
    /** reference to GenOpt object */
    protected GenOpt go;
    /** array with all continuous, free parameters */
    protected ContinuousParameter[] conPar;
    /** array with all discrete, free parameters */
    protected DiscreteParameter[] disPar;

    /** list with results of the main interation */
    protected LinkedList<ResultPoint> ptsMai;

    /** list with results of the main interation */
    protected LinkedList<ResultPoint> ptsSub;

    /** number of continuous, free parameters */
    protected int dimCon;
    /** number of discrete, free parameters */
    protected int dimDis;
    /** number of function values */
    protected int dimF;
	
    /** function names */
    protected String[] nameF;
    /** path of the output files */
    protected String outFilPat;
    /** name (including path) of output files */
    protected String[] outFilNam;
    /** number of result entry of all and sub iterations, starting with 0*/
    protected int resNum;
    /** number of function evaluations */
    protected int simNum;
    /** number of the main iteration*/
    protected int maiIteNum;
    /** number of the sub iteration*/
    protected int subIteNum;

    ///////////////////////////////////////////////////////////////////////////////
    /** inner class with result point */
    protected class ResultPoint extends genopt.algorithm.util.math.Point
	implements Comparable
    {
	/** Constructor
	 *@param x a ResultPoint
	 */
	public ResultPoint(ResultPoint x){
	    super(x.getX(), x.getIndex(), x.getF(), x.getStepNumber(), x.getComment());
	    setSimulationNumber(x.getSimulationNumber(), x.getMainIterationNumber(),
			 x.getSubIterationNumber());
	}		

	/** Constructor
	 *@param x a point
	 */
	public ResultPoint(Point x){
	    super(x.getX(), x.getIndex(), x.getF(), x.getStepNumber(), x.getComment());
	}
					
	/** Constructor
	 *@param dimensionXContinuous the number of continuous independent variables
	 *@param dimensionXDiscrete the number of discrete independent variables
	 *@param dimensionF the number of function values
	 */
	public ResultPoint(int dimensionXContinuous, int dimensionXDiscrete, 
			   int dimensionF){
	    super(dimensionXContinuous, dimensionXDiscrete, dimensionF);
	}

	/** sets the simulation number
	 *@param simNumber the simulation number
	 *@param mainIterationNumber the main iteration number
	 *@param subIterationNumber the sub iteration number
	*/		
	public void setSimulationNumber(int simNumber, int mainIterationNumber,
				 int subIterationNumber){
	    rSimNum = simNumber;
	    rMaiIteNum = mainIterationNumber;
	    rSubIteNum = subIterationNumber;
	}
	    
	/** clones the ResultPoint
	     *@return the clone of the ResultPoint
	     */
	public Object clone(){
	    ResultPoint r = new ResultPoint((Point)super.clone());
	    r.setSimulationNumber(rSimNum, rMaiIteNum, rSubIteNum);
	    return r;
	}

	/** Compares this object with the specified object for order. 
	 *	Returns a negative integer, zero, or a
	 *	positive integer as this object is less than, equal to, or greater than the specified object.
	 *@param o Object to be compared
	 */
	public int compareTo(Object o){
	    ResultPoint rp = (ResultPoint)o;
	    if ( getF(0) < rp.getF(0) )
		return -1;
	    else if (getF(0) > rp.getF(0))
		return 1;
	    else
		return super.compareTo((Point)rp);
	}

	/** gets the run number
	 *@return the run number
	*/		
	public int getSimulationNumber() { return rSimNum;}

	/** gets the main iteration number
	 *@return the main iteration number
	*/	
	public int getMainIterationNumber() { return rMaiIteNum;}

	/** gets the sub iteration number
	 *@return the sub iteration number
	*/	
	public int getSubIterationNumber() { return rSubIteNum;}		
	/** run number */
	protected int rSimNum;
	/** main iteration number */
	protected int rMaiIteNum;
	/** sub iteration number */
	protected int rSubIteNum;
    }
}
