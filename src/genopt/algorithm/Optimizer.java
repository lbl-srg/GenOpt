package genopt.algorithm;

import genopt.GenOpt;
import genopt.io.*;
import genopt.db.OrderedMap;
import genopt.simulation.*;
import genopt.lang.*;
import genopt.algorithm.util.math.Point;
import genopt.algorithm.util.math.LinAlg;
import genopt.algorithm.util.math.FunctionEvaluator;
import java.io.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/** Abstract Class that represents the structure of an optimization 
  * algorithm class and offers generic methods to run the optimization.<BR>
  * All optimization algorithms must extend this class.
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

abstract public class Optimizer
{
    /** System dependent line separator */
    protected final static String LS = System.getProperty("line.separator");
    /** System dependent file separator */
    protected final static String FS = System.getProperty("file.separator");
    /** constant to indicate that it is a main iteration */
    public static final boolean MAINITERATION = true;
    /** constant to indicate that it is a sub iteration */
    public static final boolean SUBITERATION = false;
    /** constant for indicating that the optimization is in the original space */
    public static final int ORIGINAL = 0;
    /** constant for indicating that the optimization is in the transformed space */
    public static final int TRANSFORMED = 1;
    /** constant to indicate strict inequality */
    public static final int EXCLUDING = 0;
    /** constant to indicate weak inequality */
    public static final int INCLUDING = 1;
    /** flag for indicating how constraints are treated */
    private int conMode;

    /** Constructor
     * @param genOptData a reference to the GenOpt object.<BR>
     *     <B>Note:</B> The object is used as a reference.
     *                  Hence, the data of GenOpt are modified
     *                  by this Class.
     * @param constraintMode a flag indicating how constraints are treated<PRE>
     * 0: optimization is in original space (constraints are not taken into account)
     * 1: optimization is in transformed space</PRE>
     *@exception InputFormatException if an error occurs while searching for <CODE>Main</CODE>
     *           in the <CODE>Algorithm</CODE> section
     *@exception IOException if an I/O exception occurs
     *@exception Exception in an exception occurs
     */
    protected Optimizer(final GenOpt genOptData, final int constraintMode)
	throws InputFormatException, OptimizerException, IOException, Exception
    {
	assert genOptData != null : "Received 'null' as argument";
	assert (constraintMode == 0 || constraintMode == 1) : 
	    "Invalid constraint mode";
	doSmoothing = false;
	conMode = constraintMode;
	data = genOptData;
	dimCon   = data.conPar.length;
	dimDis   = data.disPar.length;
	dimX = dimCon + dimDis;
	dimInpFun = data.inpFun.length;
	inpFun = new String[dimInpFun];

	optComFilNam = new String(data.OptIni.getOptComPat() + FS +
				  data.OptIni.getOptComFilNam());
	separator = data.getSeparator();
	// get the StreamTokenizer and move beyond the entry 'Main'
	algorithm = data.getAlgorithmSection();
	this.getInputValueString("Main");
	stepNumber = 1;
	wriSteNum = data.OptSet.writeStepNumber();
	useSteNum = wriSteNum;
	// initialize the input file, log file, and output file related variables
	nSimInpFil = data.OptIni.getNumberOfInputFiles();
	nSimLogFil = data.OptIni.getNumberOfLogFiles();
	nSimOutFil = data.OptIni.getNumberOfOutputFiles();
	// get the objective function objects
	objFunObj  = data.OptIni.getFunctionObjects();

	simInpTemFilHan = new FileHandler[nSimInpFil];
	for (int i = 0; i < nSimInpFil ; i++)
	    simInpTemFilHan[i] = new FileHandler(data.OptIni.getSimInpTemPat(i),
						 data.OptIni.getSimInpTemFilNam(i));
	
	simLogFil = new String[nSimLogFil];
	simOutFil = new String[nSimOutFil];

	for (int i = 0; i < nSimLogFil; i++)
	    simLogFil[i] = new String(data.OptIni.getSimLogPat(i) + FS +
				      data.OptIni.getSimLogFilNam(i) );

	for (int i = 0; i < nSimOutFil; i++)
	    simOutFil[i] = new String(data.OptIni.getSimOutPat(i) + FS +
				      data.OptIni.getSimOutFilNam(i) );

	        
	dimF = objFunObj.length;
	nameF = new String[dimF];
	for (int i = 0; i < dimF; i++)
	    nameF[i] = objFunObj[i].getName();

	outFun = new String[dimF];
	funValPoi = new int[dimF];
	
	// delete old input, log and output save files if user specifies savePath
	_deleteRunFiles(data.OptIni.getSimInpSavPat(),
			data.OptIni.getSimInpFilNam());
	_deleteRunFiles(data.OptIni.getSimLogSavPat(),
			data.OptIni.getSimLogFilNam());
	_deleteRunFiles(data.OptIni.getSimOutSavPat(),
			data.OptIni.getSimOutFilNam());
	// Make directories specified by savePath.
	// We do this prior to the function
	// evaluation to make sure we have write access before spending 
	// time on evaluating the objective function.
	genopt.io.FileHandler.makeDirectory(data.OptIni.getSimInpSavPat());
	genopt.io.FileHandler.makeDirectory(data.OptIni.getSimLogSavPat());
	genopt.io.FileHandler.makeDirectory(data.OptIni.getSimOutSavPat());

	// initialize list with evaluated points
	evaPoi = new TreeMap<Point, Double[]>();
    }

    /** Constructor
     *
     */
    public Optimizer(){  }

    /** Sets the step number.
     * This method is used by multi-start algorithms. 
     * This method must not be used to decrease the step number
     *@param sN
     */
    protected void resetStepNumber(final int sN){
	assert ( sN <= stepNumber ) : "Method can only be used to decrease the step number.";
	assert ( sN > 0 ) : "Argument must be bigger than 0.";
	stepNumber = sN;
    }

    /** Gets the output path.
     *  <P>
     *  Algorithms that write their own output files should write them
     *  to the directory returned by this function.<BR>
     *  
     * @return the output path
     */
    protected String getOutputPath(){
	return data.OptIni.getOptComPat();
    }

    /** Post-process the objective function value.<BR>
     * If required, post-process the objective function value
     * in this method.
     * <P>
     * <B>Usage</B><P>
     * The number of elements of the array <CODE>f</CODE>
     * is equal to the number of <CODE>DelimiterN</CODE> (N=1, 2, 3, ...)
     * that are specified in the initialization file. <P>
     * Thus, if you want to minimize the sum of heating and 
     * cooling energy, you could specify in the initialization file
     * the section<PRE>
     // --- Start of section
     ObjectiveFunctionLocation{
     Delimiter1 = "Eheat=";  Name1 = "E_tot";
     Delimiter2 = "Eheat=";  Name2 = "E_heat";
     Delimiter3 = "Ecool=";  Name3 = "E_cool";
     }
     // --- End of section
     </PRE>
     * and define this method as<PRE>
     private void _postProcessObjectiveFunction(int iterationNumber,
     double[] f){
     f[0] = f[1] + f[2];
     if (iterationNumber == 1) 
     setInfo("Post-process objective function value.");
     return;
     }
     </PRE>
     * Then, the optimization algorithm will minimize the sum
     * of heating and cooling energy, and will write an information
     * to the log file to remind you that the objective function value
     * has been post-processed.<P>
     *
     * <B>Note:</B> Use <CODE>stepNumber</CODE> to implement penalty or
     * barrier functions.
     *
     * @deprecated You should use function objects, which can be defined
     *             in the input files, instead of this function.
     *
     * @param iterationNumber current iteration number
     * @param f array that contains the objective function values
     */
    private void _postProcessObjectiveFunction(final int iterationNumber,
					      double[] f){
	return;
    }

    /** Checks whether all independent parameters are continuous.
     * If any independent parameter is not continuous, then an
     * <CODE>OptimizerException</CODE> is thrown (with an 
     * descriptive information)
     *@exception OptimizerException if some independent parameters are not continuous
     */
    protected void ensureOnlyContinuousParameters()
	throws OptimizerException{
	if (dimDis > 0){
	    String claNam = getClass().getName();
	    String ga = "genopt.algorithm.";
	    if ( claNam.lastIndexOf(ga) > -1)
		claNam = claNam.substring( ga.length() );
	    String em = "Algorithm '" + claNam + 
		"' can only have continuous variables." + LS;
	    if (dimDis == 1)
		em += "  Variable '" + getVariableNameDiscrete(0) + 
		    "' is specified as discrete.";
	    else{
		em += "  Variables ";
		for (int i = 0; i < dimDis-1; i++)
		    em +=  "'" + getVariableNameDiscrete(i) + "', ";
		em +=  "and '" + getVariableNameDiscrete(dimDis-1) +
		    "' are specified as discrete.";
	    }
	    throw new OptimizerException(em);
	}
    }

    /** Checks whether all independent parameters are discrete.
     * If any independent parameter is not discrete, then an
     * <CODE>OptimizerException</CODE> is thrown (with an 
     * descriptive information)
     *@exception OptimizerException if some independent parameters are not discrete
     */
    protected void ensureOnlyDiscreteParameters()
	throws OptimizerException{
	if (dimCon > 0){
	    String claNam = getClass().getName();
	    String ga = "genopt.algorithm.";
	    if ( claNam.lastIndexOf(ga) > -1)
		claNam = claNam.substring( ga.length() );
	    String em = "Algorithm '" + claNam + 
		"' can only have discrete variables." + LS;
	    if (dimCon == 1)
		em += "  Variable '" + getVariableNameContinuous(0) + 
		    "' is specified as continuous.";
	    else{
		em += "  Variables ";
		for (int i = 0; i < dimCon-1; i++)
		    em +=  "'" + getVariableNameContinuous(i) + "', ";
		em +=  "and '" + getVariableNameContinuous(dimCon-1) +
		    "' are specified as continuous.";
	    }
	    throw new OptimizerException(em);
	}
    }


    /** Deletes the run files specified by path and name.
     *  Deletes the directory if it is empty
     * @param path path of the files
     * @param name name of the files
     * @exception Exception if a SecurityException occured
     */
    private void _deleteRunFiles(final String[] path, final String[] name)
	throws Exception
    {
	File dir;
	File f;
	for (int iF=0; iF < path.length ; iF++){
	    int iS = 1; // the simulation run number
	    f = new File(path[iF] + FS + iS + name[iF]);
	    while (f.exists()){
		try { f.delete(); }
		catch (SecurityException e){
		    throw new Exception("SecurityException occured during deleting of '" +
					f.getCanonicalPath() + FS + f.getName() + "': Message '" + 
					e.getMessage() + "'.");
		}
		f = new File(path[iF] + FS + (++iS) + name[iF]);
	    }
	}
    }

    /** Copies the files from <CODE>path</CODE> to <CODE>savePath</CODE> and
     *  adds the run number in front of the file name.
     * @param savePath path where the files have to be copied to.
     *  If it does not exist, it will be created.
     * @param path source path of the files
     * @param name name of the files
     * @exception SecurityException if a SecurityException occured
     * @exception Exception if the directory could not be made
     */
    private void _copyRunFiles(final String[] savePath, 
			       final String[] path, 
			       final String[] name)
	throws SecurityException, Exception {
	// first, make sure the directories exist, or create them otherwise.
	// This is because the user may delete it during the optimization 
	// (for whatever reason...)
	genopt.io.FileHandler.makeDirectory(savePath);
	// copy the files
	File f;
	final int nS = data.ResMan.getNumberOfSimulation();
	for (int iF=0; iF < path.length ; iF++){
	    if (!(savePath[iF].equals(""))){
		final String fn = path[iF] + FS + name[iF];
		f = new File(fn);
		try {
		    if ( f.exists() ){
			if (!f.renameTo(new File(savePath[iF] + FS + nS + name[iF])))
			    setWarning("Cannot rename file '" + fn + "'.");
		    }
		    else
			setWarning("File '" + fn + "' does not exist.");
		}
		catch (SecurityException e){
		    throw new SecurityException("Simulation " + nS + ": SecurityException occured during copying of '" +
						fn + "': Message '" + e.getMessage() + "'.");
		}
	    }
	}
    }

    /** A call to this method sets <code>useSteNum = true</code>.
     *  This method is typically called by multiple layer algorithm that
     *  optimize different functions during the whole optimization.
     *  An example for such an algorithm is one that -- after some iterations --
     *  constructs a surrogate function and then only attempts to optimize the
     *  surrogate, neglecting all previous results.
     */
    protected final void algorithmRequiresUsageOfStepNumber(){
	useSteNum = true;
    }
    /** Increases the step number without a function evaluation.<BR>
     * If <CODE>WriteStepNumber</CODE> is set to <CODE>false</CODE>, then the step number
     * is not increased
     * @see #increaseStepNumber(Point)
     */
    protected final void increaseStepNumber(){
	if (useSteNum){
	    stepNumber++;
	    data.ResMan.resetResultNumber();
	    /*	 aaaaa   if (wriSteNum){
		println("-------- Optimizer: evaPoi.clear().");
		evaPoi.clear();
		}*/
	    // If we don't write the step number, it won't be used 
	    // for computing the objective function.
	    // Thus, we do not need to clear the mapping.
	}
    }
    
    /** Increases the step number.<BR>
     * If the variable <CODE>WriteStepNumber</CODE> in the optimization
     * command file is set to <CODE>true</CODE>, the function
     * <CODE>getF(Point x)</CODE> is called and the new point is returned.<BR>
     * If <CODE>WriteStepNumber</CODE> is set to <CODE>false</CODE>, 
     * then the step number is not increased, and no simulation is done. 
     * The passed argument <CODE>x</CODE> is returned.<BR>
     * <B>Note:</B> You <I>must</I> call this function everytime
     * when the optimization constructed a convergent sequence.
     * This method increases a counter that can be used
     * to implement penalty function and barrier function
     * in the cost function.<BR>
     * @param x the new point for the simulation
     * @return the new point
     * @exception OptimizerException if an OptimizerException occurs
     * @exception SimulationInputException if an error in writing the
     *           simulation input file occurs
     * @exception Exception if an Exception occurs
     * @see #getF(Point)
     */
    protected final Point increaseStepNumber(final Point x)
	throws SimulationInputException, OptimizerException, Exception{
	assert x != null : "Received 'null' as argument";
	increaseStepNumber();
	if  (wriSteNum)
	    return getF(x);
	else{
	    updateParameterSetting(x);
	    return (Point)x.clone();
	}
    }

    /** Returns the flag that indicates whether the step number
     * has to written to the simulation input file or not.<BR>
     * <B>Note:</B> If this method returns <CODE>true</CODE>, then
     *          after each iteration step, the method
     *          <CODE>increaseStepNumber(...)</CODE> has to be called
     *          in order to allow the implementation of penalty function,
     *          barrier function and slackness variables.
     * @return <CODE>true</CODE> if the step number has to written,
     * <CODE>false</CODE> otherwise
     */
    public final boolean writeStepNumber() {return wriSteNum;}


    /** Returns the flag that indicates whether the step number is used by the optimization algorithm.
     *
     *  It is used if either <code>wriSteNum = true</code> or
     *  if the algorithm requires explicitly the step number to be used.
     *  Note that <code>useStepNumber()</code> always returns <code>true</code>
     *  if <code>writeStepNumber()</code> returns <code>true</code>, but
     *  <code>writeStepNumber()</code> may return <code>false</code> if
     *  <code>useStepNumber()</code> returns <code>true</code>.
     */
    public final boolean useStepNumber() {return useSteNum;}

    /** Gets the current step number
     * @return the current step number
     */
    protected int getStepNumber() { return stepNumber; }

    /** Checks whether the current section is closed and then moves to
     * the end of the file. If either of the operation fails, an
     * InputFormatException is thrown.<BR>
     * <B>Note:</B> This method is called from the GenOpt kernel to ensure
     *         that all the information in the command file is parsed
     *         properly
     *@exception InputFormatException if either of the checks fails
     *@exception IOException if the optimization command file cannot be accessed
     */
    public void goToEndOfCommandFile() throws InputFormatException, IOException{
	InputFormatException e = new InputFormatException();
	Token.moveToSectionEnd(algorithm, e, optComFilNam);
	if (e.getNumberOfErrors() > 0) throw e;
	Token.isEndOfStream(algorithm, e, optComFilNam);
	if (e.getNumberOfErrors() > 0) throw e;
    }

    /** Checks whether the next Token is equal to the passed String.<br>
     *@param keyWord the keyword that has to be searched for
     *@return <CODE>true</CODE> if the next Token equals the value of <CODE>keyWord</CODE>,
     *        <CODE>false</CODE> otherwise
     *@exception IOException if the optimization command file cannot be accessed
     */
    public final boolean isNextToken(final String keyWord) throws IOException{
	assert keyWord != null : "Received 'null' as argument";
	assert keyWord != "" : "Received \"\" as argument";
	return Token.isNextToken(algorithm, keyWord);
    }

    /** Gets the value of <CODE>keyWord</CODE> from the <CODE>algorithmEntry</CODE>
     * If another String than <CODE>keyWord</CODE> is at the current position
     * of the <CODE>algorithmEntry</CODE>, an InputFormatException is thrown
     *@param keyWord the expected <CODE>keyWord</CODE>
     *@return the value of <CODE>keyWord</CODE>
     *@exception InputFormatException if another String than <CODE>keyWord</CODE>
     *           is read or if it is a invalid type
     *@exception IOException if the optimization command file cannot be accessed
     */
    protected final String getInputValueString(final String keyWord)
	throws InputFormatException, IOException{
	return getInputValueString(keyWord, null);
    }

    /** Gets the value of <CODE>keyWord</CODE> from the <CODE>algorithmEntry</CODE>
     * If another String than <CODE>keyWord</CODE> is at the current position
     * of the <CODE>algorithmEntry</CODE>, an InputFormatException is thrown
     *@param keyWord the expected <CODE>keyWord</CODE>
     *@param admVal the admissible values
     *@return the value of <CODE>keyWord</CODE>
     *@exception InputFormatException if another String than <CODE>keyWord</CODE>
     *           is read or if it is a invalid type
     *@exception IOException if the optimization command file cannot be accessed
     */
    protected final String getInputValueString(final String keyWord,
					       final String[] admVal)
	throws InputFormatException, IOException{
	assert keyWord != null : "Received 'null' as argument";
	assert keyWord != "" : "Received \"\" as argument";
	InputFormatException e = new InputFormatException();
	String r = Token.getStringValue(algorithm, '=', ';', 
					keyWord, e, optComFilNam);
	if (e.getNumberOfErrors() > 0) throw e;
	boolean found = false;
	if ( admVal != null ){ // check for correct input
	    for(int i = 0; i < admVal.length; i++){
		if ( r.equals(admVal[i]) ){
		    found = true;
		    break;
		}
	    }
	    if ( ! found ){
		String em = "Invalid value for '" + keyWord + "'. Must be one of '";
		for(int i = 0; i < admVal.length; i++){
		    em += admVal[i] + "'";
		    if ( i != admVal.length - 1 )
			em += ", '";
		}	    
	    }
	}
	return r;
    }

    /** Gets the value of <CODE>keyWord</CODE> from the <CODE>algorithmEntry</CODE>
     * If another String than <CODE>keyWord</CODE> is at the current position
     * of the <CODE>algorithmEntry</CODE>, an InputFormatException is thrown
     *@param keyWord the expected <CODE>keyWord</CODE>
     *@return the value of <CODE>keyWord</CODE>
     *@exception InputFormatException if another String than <CODE>keyWord</CODE>
     *           is read or if it is a invalid type
     *@exception IOException if the optimization command file cannot be accessed
     */
    protected final boolean getInputValueBoolean(final String keyWord)
	throws InputFormatException, IOException{
	assert keyWord != null : "Received 'null' as argument";
	assert keyWord != "" : "Received \"\" as argument";
	InputFormatException e = new InputFormatException();
	boolean r = Token.getBooleanValue(algorithm, '=', ';', keyWord, e, optComFilNam);
	if (e.getNumberOfErrors() > 0) throw e;
	return r;
    }

    /** Gets the value of <CODE>keyWord</CODE> from the <CODE>algorithmEntry</CODE>
     * If another String than <CODE>keyWord</CODE> is at the current position
     * of the <CODE>algorithmEntry</CODE>, an InputFormatException is thrown
     *@param keyWord the expected <CODE>keyWord</CODE>
     *@return the value of <CODE>keyWord</CODE>
     *@exception InputFormatException if another String than <CODE>keyWord</CODE>
     *           is read or if it is a invalid type
     *@exception IOException if the optimization command file cannot be accessed
     */
    protected final int getInputValueInteger(final String keyWord)
	throws InputFormatException, IOException{
	return getInputValueInteger(keyWord,
				    -Integer.MAX_VALUE, Optimizer.INCLUDING,
				    Integer.MAX_VALUE, Optimizer.INCLUDING);
    }

    /** Gets the value of <CODE>keyWord</CODE> from the <CODE>algorithmEntry</CODE>
     * If another String than <CODE>keyWord</CODE> is at the current position
     * of the <CODE>algorithmEntry</CODE>, an InputFormatException is thrown
     *@param keyWord the expected <CODE>keyWord</CODE>
     *@param min the minimum allowed value
     *@param minEqu flag whether the minimum equality is strict or weak,
     *              as defined by <CODE>Optimizer.EXCLUDING</CODE> and 
     *              <CODE>Optimizer.INCLUDING</CODE>
     *@param max the maximum allowed value
     *@param maxEqu flag whether the maximum equality is strict or weak,
     *              as defined by <CODE>Optimizer.EXCLUDING</CODE> and 
     *              <CODE>Optimizer.INCLUDING</CODE>
     *@return the value of <CODE>keyWord</CODE>
     *@exception InputFormatException if another String than <CODE>keyWord</CODE>
     *           is read or if it is a invalid type
     *@exception IOException if the optimization command file cannot be accessed
     */
    protected final int getInputValueInteger(final String keyWord,
					     final int min,
					     final int minEqu,
					     final int max,
					     final int maxEqu)
	throws InputFormatException, IOException{
	assert keyWord != null : "Received 'null' as argument";
	assert keyWord != "" : "Received \"\" as argument";
	assert ( minEqu == Optimizer.EXCLUDING || minEqu == Optimizer.INCLUDING );
	assert ( maxEqu == Optimizer.EXCLUDING || maxEqu == Optimizer.INCLUDING );
	InputFormatException e = new InputFormatException();
	int r = Token.getIntegerValue(algorithm, '=', ';', keyWord, e, optComFilNam);
	if (e.getNumberOfErrors() > 0) throw e;
	if ( minEqu == Optimizer.EXCLUDING ){
	    if (! ( r > min ) )
		throwInputError("value larger than '" + min + 
				"' for '" + keyWord + "'. Received '" + r + "'");
	}
	else{ 
	    if (! ( r >= min ) )
		throwInputError("value larger than or equal to '" + min + 
				"' for '" + keyWord + "'. Received '" + r + "'");
	}
	if ( maxEqu == Optimizer.EXCLUDING ){
	    if (! ( r < max ) )
		throwInputError("value smaller than '" + max + 
				"' for '" + keyWord + "'. Received '" + r + "'");
	}
	else{
	    if (! ( r <= max ) )
		throwInputError("value smaller than or equal to '" + max + 
				"' for '" + keyWord + "'. Received '" + r + "'");
	}
	return r;
    }

    /** Gets the value of <CODE>keyWord</CODE> from the <CODE>algorithmEntry</CODE>
     * If another String than <CODE>keyWord</CODE> is at the current position
     * of the <CODE>algorithmEntry</CODE>, an InputFormatException is thrown
     *@param keyWord the expected <CODE>keyWord</CODE>
     *@return the value of <CODE>keyWord</CODE>
     *@exception InputFormatException if another String than <CODE>keyWord</CODE>
     *           is read or if it is a invalid type
     *@exception IOException if the optimization command file cannot be accessed
     */
    protected final double getInputValueDouble(final String keyWord)
	throws InputFormatException, IOException{
	return getInputValueDouble(keyWord, 
				   -Double.MAX_VALUE, Optimizer.INCLUDING,
				   Double.MAX_VALUE, Optimizer.INCLUDING);
    }

    /** Gets the value of <CODE>keyWord</CODE> from the <CODE>algorithmEntry</CODE>
     * If another String than <CODE>keyWord</CODE> is at the current position
     * of the <CODE>algorithmEntry</CODE>, an InputFormatException is thrown
     *@param keyWord the expected <CODE>keyWord</CODE>
     *@param min the minimum allowed value
     *@param minEqu flag whether the minimum equality is strict or weak,
     *              as defined by <CODE>Optimizer.EXCLUDING</CODE> and 
     *              <CODE>Optimizer.INCLUDING</CODE>
     *@param max the maximum allowed value
     *@param maxEqu flag whether the maximum equality is strict or weak,
     *              as defined by <CODE>Optimizer.EXCLUDING</CODE> and 
     *              <CODE>Optimizer.INCLUDING</CODE>
     *@return the value of <CODE>keyWord</CODE>
     *@exception InputFormatException if another String than <CODE>keyWord</CODE>
     *           is read or if it is a invalid type
     *@exception IOException if the optimization command file cannot be accessed
     */
    protected final double getInputValueDouble(final String keyWord,
					       final double min,
					       final double minEqu,
					       final double max,
					       final double maxEqu)
	throws InputFormatException, IOException{
	assert keyWord != null : "Received 'null' as argument";
	assert keyWord != "" : "Received \"\" as argument";
	assert ( minEqu == Optimizer.EXCLUDING || minEqu == Optimizer.INCLUDING );
	assert ( maxEqu == Optimizer.EXCLUDING || maxEqu == Optimizer.INCLUDING );
	InputFormatException e = new InputFormatException();
	double r = Token.getDoubleValue(algorithm, '=', ';', keyWord, e, optComFilNam);
	if (e.getNumberOfErrors() > 0) throw e;
	if ( minEqu == Optimizer.EXCLUDING ){
	    if (! ( r > min ) )
		throwInputError("value larger than '" + min + 
				"' for '" + keyWord + "'. Received '" + r + "'");
	}
	else{ 
	    if (! ( r >= min ) )
		throwInputError("value larger than or equal to '" + min + 
				"' for '" + keyWord + "'. Received '" + r + "'");
	}
	if ( maxEqu == Optimizer.EXCLUDING ){
	    if (! ( r < max ) )
		throwInputError("value smaller than '" + max + 
				"' for '" + keyWord + "'. Received '" + r + "'");
	}
	else{
	    if (! ( r <= max ) )
		throwInputError("value smaller than or equal to '" + max + 
				"' for '" + keyWord + "'. Received '" + r + "'");
	}
	return r;
    }
    

    /** Throws an input error. Use this method if the value that you got with
     * one of the <CODE>getInputValue...</CODE> method is not valid.<BR>
     * <B>Note:</B> You have to call this
     * method immediately after getting the value so that the error message
     * points to the right line number
     *@param expectedValue a String that specifies the value you expected
     *@exception InputFormatException the thrown exception with the error message
     */
    protected final void throwInputError(final String expectedValue)
	throws InputFormatException{
	assert expectedValue != null : "Received 'null' as argument";
	assert expectedValue != "" : "Received \"\" as argument";

	String em = new String("Invalid value. Expected " + expectedValue +
			       ".");
	InputFormatException e = new InputFormatException();
	Token.setError(algorithm, e, em, optComFilNam);
	throw e;
    }


    /** Gets the number of independent variables (sum of continuous and discrete)
     *@return the number of independent variables (sum of continuous and discrete)
     */
    public final int getDimensionX() { return dimX; }

    /** Gets the number of independent, continuous variables
     *@return the number of independent, continuous variables
     */
    public final int getDimensionContinuous() { return dimCon; }

    /** Gets the number of independent, continuous variables
     *@return the number of independent, continuous variables
     */
    public final int getDimensionDiscrete() { return dimDis; }


    /** Gets the number of function values
     *@return the number of function values
     */
    public final int getDimensionF() { return dimF; }

    /** Sets the mode of the optimization
     * @param constraintMode a flag indicating how constraints are treated<PRE>
     * 0: optimization is in original space (constraints are not taken into account)
     * 1: optimization is in transformed space</PRE>
     */
    public final void setMode(final int constraintMode) {
	assert (constraintMode == ORIGINAL || constraintMode == TRANSFORMED) : 
	    "Invalid constraint mode";
	conMode = constraintMode;
    }

    /** Gets the mode of the optimization
     * @return the constraints flag indicating how constraints are treated<PRE>
     * 0: optimization is in original space (constraints are not taken into account)
     * 1: optimization is in transformed space</PRE>
     */
    public final int getMode() { return conMode; }

    /** Abstract method for running the optimization algorithm
     *     until a termination criteria is satisfied
     * @return <CODE>-1</CODE> if the maximum number of iteration
     *                         is exceeded
     *     <BR><CODE>+1</CODE> if any required accuracy is reached
     *     <BR><CODE>+2</CODE> if the absolute accuracy is reached
     *     <BR><CODE>+3</CODE> if the relative accuracy is reached
     *     <BR><CODE>+4</CODE> if run is finished without checking
     *                         a convergence criteria
     *                         (e.g., parametric runs)
     * @exception OptimizerException if an OptimizerException occurs
     * @exception InputFormatException if an InputFormatException occurs
     * @exception NoSuchMethodException if a method that should be invoked could not be found
     * @exception IllegalAccessException  if an invoked method enforces Java language access 
     *                                    control and the underlying method is inaccessible
     * @exception InvocationTargetException if an invoked method throws an exception
     *@exception Exception if an Exception occurs
     */
    abstract public int run()
	throws OptimizerException, SimulationInputException, 
	       NoSuchMethodException, IllegalAccessException, InvocationTargetException,
	       Exception;

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
	assert x != null : "Received 'null' as argument";
	// check whether GenOpt has to be stopped due to a user interaction
	if (data.mustStopOptimization()){
	    reportCurrentLowestPoint();
	    throw new OptimizerException(data.USER_STOP_MESSAGE);
	}
	// make sure step number is set to the current value
	Point r = (Point)x.clone();
	r.setStepNumber(stepNumber);
	if ( doSmoothing )
	    return smoo.getF(r);
	else
	    return _getF(r);
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
    private Point _getF(final Point x)
	throws SimulationInputException, OptimizerException, NoSuchMethodException,
	       IllegalAccessException, Exception{
	updateParameterSetting(x);
	Point r = (Point)x.clone();
	////////////////////////////////////////////////////////
	// check whether this point has already been evaluated
	Point key = (Point)x.clone();

	if (wriSteNum) // step number is written, hence it may be used for penalty functions
	    key.setStepNumber(stepNumber);
	else // step number is not used. Set to 1 
	    key.setStepNumber(1);

	// make sure step number is set to the current value
	if( evaPoi.containsKey(key) ){
	    ////////////////////////////////////////////////////////
	    // point already evaluated
	    //  println("Point already evaluated. Take function value from database.");
	    // get the function value corresponding to this point
	    Double[] valD = (Double[])(evaPoi.get(key));
	    double[] val = new double[valD.length];
	    for (int i = 0; i < valD.length; i++)
		val[i] = valD[i].doubleValue();

	    r.setF(val);
	    return r;
	    ////////////////////////////////////////////////////////
	}
	else{
	    ////////////////////////////////////////////////////////
	    // point not yet evaluated
	    data.ResMan.increaseNumberOfFunctionEvaluation();
	    
	    /* since Windows NT4WS has problems with IO operation
	       (i.e., after around a thousand calls of this function,
	       it can happen that a file is not written properly
	       to the disk) we give a second chance to perform
	       the operation.
	       But we do not retry it in the very first call since
	       then the optimization/simulation is very likely set up
	       inproperly
	    */

	    if (data.ResMan.getNumberOfSimulation() > 1){
		try{
		    key = _evaluateSimulation(r);
		}
		catch(Exception e){
		    key = _retryEvaluateSimulation(r, e);
		}
	    }
	    else{
		key = _evaluateSimulation(r);
	    }
	    
	    /////////////////////////////////////////////////////
	    // copy input, log and output files, if required
	    _copyRunFiles();

	    // add point and function value to the map of evaluated points
	    Double[] val = new Double[key.getDimensionF()];
	    for (int i = 0; i < val.length; i++)
		val[i] = new Double(key.getF(i));

	    if (wriSteNum) // step number is written, hence it may be used for penalty functions
		key.setStepNumber(stepNumber);
	    else // step number is not used in function evaluation. Set to 1 
		key.setStepNumber(1);
	    
	    // we must clone the object that we put into the TreeMap
	    // Otherwise, it's coordinates get changed since the map
	    // contains only a reference to the instance.
	    evaPoi.put((Point)key.clone(), val);
	    key.setStepNumber(stepNumber);
	    return key;
	}
    }

    /** Copies the files from <CODE>path</CODE> to <CODE>savePath</CODE> and
     *  adds the run number in front of the file name.
     * @exception SecurityException if a SecurityException occured
     * @exception Exception if the directory could not be made
     */
    private void _copyRunFiles() throws SecurityException, Exception{
	_copyRunFiles(data.OptIni.getSimInpSavPat(),
		      data.OptIni.getSimInpPat(),
		      data.OptIni.getSimInpFilNam());
	_copyRunFiles(data.OptIni.getSimLogSavPat(),
		      data.OptIni.getSimLogPat(),
		      data.OptIni.getSimLogFilNam());
	_copyRunFiles(data.OptIni.getSimOutSavPat(),
		      data.OptIni.getSimOutPat(),
		      data.OptIni.getSimOutFilNam());
    }
    
    /** Tries to evaluate the simulation a second time if an exception has been
     * thrown
     *@param x the point being evaluated
     *@param t the caught Throwable
     *@return a clone of the point with the new function values stored
     *@exception OptimizerException if an OptimizerException occurs or
     *           if the user required to stop GenOpt
     *@exception SimulationInputException if an error in writing the
     *           simulation input file occurs
     * @exception NoSuchMethodException if a method that should be invoked could not be found
     * @exception IllegalAccessException  if an invoked method enforces Java language access 
     *                                    control and the underlying method is inaccessible
     * @exception InvocationTargetException if an invoked method throws an exception
     *@exception Exception if an exception occurs
     */
    private Point _retryEvaluateSimulation(final Point x, final Throwable t)
	throws SimulationInputException, OptimizerException, 
	       NoSuchMethodException, IllegalAccessException, InvocationTargetException, 
	       Exception{
	// check whether GenOpt wants to be stopped by a user interaction
	if (data.mustStopOptimization()){
	    reportCurrentLowestPoint();
	    throw new OptimizerException(data.USER_STOP_MESSAGE);
	}
	
	String infMes = "Caught '" +
	    t.getClass().getName() + "' with message:" +
	    "   " + t.getMessage() + LS +
	    "   Try to evaluate simulation a second time.";
	if (data.DEBUG) data.printStackTrace(t);
	setInfo(infMes);
	return _evaluateSimulation(x);
    }

    /** Updates the settings of the current value of the continuous
     *  and discrete parameters.
     *
     * @param x Point whose values will be used to update the continuous and
     *          discrete parameters.
     */
    protected void updateParameterSetting(final Point x){
	//continuous parameters
	if (conMode == ORIGINAL)
	    for (int i = 0; i < dimCon; i++)
		data.conPar[i].setOriginalValue(x.getX(i));
	else if (conMode == TRANSFORMED)
	    for (int i = 0; i < dimCon; i++)
		data.conPar[i].setTransformedValue(x.getX(i));
	// discrete parameters
	for (int i = 0; i < dimDis; i++)
	    data.disPar[i].setIndex(x.getIndex(i));
    }

    /** appends a String to the output listing files
     *@param s String to be appended to output listing files
     *@exception IOException
     */
    public final void appendToOutputListing(final String s) throws IOException{
	assert s != null : "Received 'null' as argument";
	assert s != "" : "Received \"\" as argument";
	data.ResMan.append(s);
    }


    /** Reports the current lowest point for the case that GenOpt
     * has to terminate.<BR>
     * This method calls the function report with a
     * corresponding comment and reports the minimum
     * point to the output device
     *@exception IOException if an I/O error in the optimization output files
     *               occurs
     */
    protected void reportCurrentLowestPoint() throws IOException{
	if (data.ResMan.getNumberOfMaiIteration() > 1){
	    Point xCurMin = data.ResMan.getMinimumPoint();
	    
	    data.ResMan.reportMinimum("Current lowest point.");
	    String mes = new String(LS +
				    "Current lowest point:" + LS + 
				    "Simulation " + data.ResMan.getMinimumPointSimulationNumber() + LS);

	    for (int i = 0; i < dimF; i++)
		mes += nameF[i] + "\t= " + xCurMin.getF(i) + LS;	
	    println(mes);
	}
    }
		
    /** Evaluates the simulation<UL>
     * <LI>writes a new input file
     * <LI>launches the simulation
     * <LI>checks for simulation errors
     * <LI>returns an array with the values of the objective function</UL>
     * @param x the point being evaluated
     * @return a clone of the points object with the new function values stored
     * @exception OptimizerException if an OptimizerException occurs
     * @exception SimulationInputException if an error in writing the
     *            simulation input file occurs
     * @exception NoSuchMethodException if a method that should be invoked could not be found
     * @exception IllegalAccessException  if an invoked method enforces Java language access 
     *                                    control and the underlying method is inaccessible
     * @exception InvocationTargetException if an invoked method throws an exception
     * @exception Exception if an exception occurs
     */
    private Point _evaluateSimulation(final Point x)
	throws OptimizerException, SimulationInputException, 
	       NoSuchMethodException, IllegalAccessException, InvocationTargetException, Exception{
	// flag used for collecting Exceptions before throwing them
	boolean exit = false;
	// replace values in input file contents and write input file
	FileHandler[] SimulationInput = new FileHandler[nSimInpFil];
	// simulation input files
	for (int i = 0; i < nSimInpFil; i++)
	    SimulationInput[i] = new FileHandler(simInpTemFilHan[i].getFileContentsString());
	
	// Formulas of the input function objects
	for (int i = 0; i < dimInpFun; i++){
	    inpFun[i] = data.inpFun[i].getFunction();
	}
	// Formulas of the output function objects
	for (int i = 0; i < dimF; i++)
	    outFun[i] = objFunObj[i].isFunction() ? objFunObj[i].getFunction() : null;

	// by convention, 0...dimCon-1 are continuous parameters
	//                dimCon...dimX(=dimCon+dimDis) are discrete parameters
	for (int j = 0; j < dimX; j++){
	    final String varNam = ( j < dimCon) ? 
		getVariableNameContinuous(j) : getVariableNameDiscrete(j-dimCon);
	    final String varVal = ( j < dimCon) ?
		data.ioSet.toString(data.conPar[j].getOriginalValue()) :
		data.disPar[j-dimCon].getValueString();
	    final String repl = "%" + varNam + "%";

	    boolean found = _replaceInInputFile(repl, varVal, SimulationInput);
	    found = ( _replaceInInputFunction(repl, varVal) || found );
	    found = ( _replaceInOutputFunction(repl, varVal) || found  );
	    // check whether we found the value at least once
	    if (!found) // variable was not found in input file
		_variableNotFound(repl);
	}
	// replace step number in simulation input files, in input function objects
	// and in output function objects
	if (wriSteNum){
	    final String repl = "%stepNumber%";
	    final String varVal = String.valueOf(stepNumber);
	    boolean found = _replaceInInputFile(repl, varVal, SimulationInput);
	    found = ( _replaceInInputFunction(repl, varVal) || found );
	    found = ( _replaceInOutputFunction(repl, varVal) || found );
	    
	    // check for wrong input file specification
	    if (!found){ // variable was not found in input file
		final String ErrMes = "Variable was not found in any simulation input template file," +
		    LS +"or in any function objects:" +
		    LS + "    " + "Searching for String: '%stepNumber%'. Cannot find string." +
		    LS + "    " + "'%stepNumber%' must occur in at least one simulation" +
		    LS + "    " + "input template file, or in at least one function object," +
		    LS + "    " + "if 'WriteStepNumber' is set to 'true' in optimization command file.";
		throw new SimulationInputException(ErrMes);
	    }
	}
	// evaluate input function objects
	for(int k = 0; k < dimInpFun; k++){
	    boolean found = ( data.inpFun[k].getReferenceCounter() > 0 );
	    final String varNam = data.inpFun[k].getName();
	    final String repl   = '%' + varNam + '%';
	    FunctionEvaluator fe = new FunctionEvaluator( varNam, inpFun[k]);
	    final String varVal = String.valueOf(fe.evaluate());
	    found = ( _replaceInInputFile(repl, varVal, SimulationInput) || found );
	    // replace result in output function objects 
	    // (there are no references to input function objects)
	    found = ( _replaceInOutputFunction(repl, varVal) || found );
	    // check whether we found the value at least once
	    if (!found) // variable was not found in input file
		_variableNotFound(repl);
	}

	// write simulation input files
	for (int i = 0; i < nSimInpFil; i++)
	    SimulationInput[i].writeFile(data.OptIni.getSimInpPat(i),
					 data.OptIni.getSimInpFilNam(i));
	
	// delete simulation output and simulation log file
	// (to ensure that the files being read
	// are really new written)
	File of;
	String errMes="";
	for (int iFil = 0; iFil < nSimOutFil; iFil++){
	    of = new File(simOutFil[iFil]);
	    try { of.delete(); }
	    catch (SecurityException e){
		errMes += "SecurityException occured during deleting of '" +
		    simOutFil[iFil] + "': Message '" + e.getMessage() + "'." + LS;
		exit = true;
	    }
	}
	for (int iFil = 0; iFil < nSimLogFil; iFil++){
	    of = new File(simLogFil[iFil]);
	    try { of.delete(); }
	    catch (SecurityException e){
		errMes += "SecurityException occured during deleting of '" +
		    simLogFil[iFil] + "': Message '" + e.getMessage() + "'.";
		exit = true;
	    }
	}
	if (exit) throw new OptimizerException(errMes);

	////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////
	// start simulation
	data.SimSta.run();
	////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////
	// check if simulation log files exist
	for (int iLogFil = 0; iLogFil < nSimLogFil; iLogFil++){
	    if (!new File(simLogFil[iLogFil]).exists())	{
		errMes +=  "File: '" + simLogFil[iLogFil] + 
		    "' does not exist after simulation call." + LS;
		exit = true;
	    }
	    else{  // check for errors
		FileHandler logFil = new FileHandler(simLogFil[iLogFil]);
		Vector simErrMes = data.ErrChe.check(logFil.getFileContentsString(),
						     simLogFil[iLogFil] + 
						     ": Following error was found:");
		if (!simErrMes.isEmpty()){ // Simulation wrote error message
		    for (int i = 0; i < simErrMes.size(); i++)
			errMes += (String)simErrMes.elementAt(i);
		    exit = true;
		}
	    }
	}
	if (exit) throw new OptimizerException(errMes);
	////////////////////////////////////////////////////
	// read and return values of objective function
	for (int iOutFil = 0; iOutFil < nSimOutFil; iOutFil++){
	    if (!new File(simOutFil[iOutFil]).exists()){
		errMes +=  "File: '" + simOutFil[iOutFil] +
		    "' does not exist after simulation call." + LS;
		exit = true;
	    }
	}
	if (exit) throw new OptimizerException(errMes);

	SimOutputFileHandler[] simOutFilHan = new SimOutputFileHandler[nSimOutFil];
	double[] objFunVal = new double[dimF];
	final int runNum = data.ResMan.getNumberOfSimulation();

	for (int iOutFil = 0; iOutFil < nSimOutFil; iOutFil++)
	    simOutFilHan[iOutFil] = new SimOutputFileHandler(simOutFil[iOutFil],
							     separator);
	
	// in first function call, construct the pointer "funValPoi" that shows
	// which function value is in what file
	if (runNum == 1){ // first call
	    for (int iFx = 0; iFx < dimF; iFx++){
		
		if ( objFunObj[iFx].isFunction() ){
		    // objective function is defined by a function object
		    funValPoi[iFx] = -1;
		}
		else{
		    final String del = objFunObj[iFx].getDelimiter();
		    for(int iFil=0; iFil < nSimOutFil; iFil++){
			try{
			    objFunVal[iFx] = simOutFilHan[iFil].getObjectiveFunctionValue(del);
			    funValPoi[iFx] = iFil;
			    iFil = nSimOutFil; // to get out of the inner for loop
			}
			catch(OptimizerException e){
			    if (iFil == (nSimOutFil-1)) throw e; // f(x) not found in any file
			}
		    }
		}
	    }
	}
	else{ // all other calls
	    for (int iFx = 0; iFx < dimF; iFx++){
		if ( ! objFunObj[iFx].isFunction() ){
		    String del = objFunObj[iFx].getDelimiter();
		    objFunVal[iFx] = simOutFilHan[funValPoi[iFx]].getObjectiveFunctionValue(del);
		}
	    }
	}
	/////////////////////////////////////////////////////
	// process function objects
	objFunVal = _processResultFunction(outFun, objFunVal);
	/////////////////////////////////////////////////////
	_postProcessObjectiveFunction(runNum, objFunVal);
	/////////////////////////////////////////////////////
	// write result to GUI or console
	for (int iFx = 0; iFx < dimF; iFx++)
	    println("Simulation " + runNum + ": " + nameF[iFx] + "\t= " + objFunVal[iFx]);

	/////////////////////////////////////////////////////
	// data handling
	Point r = (Point)x.clone();
	r.setF(objFunVal);
	return r;
    }

    /** Replaces <code>text</code> with <code>value</code> in <code>simulationInput</code>.
     *@param text text to be searched for
     *@param value value that will replace <code>text</code>
     *@param simulationInput file handler in which search will take place
     *@return <code>true</code> if <code>text</code> was found, <code>false</code> otherwise
     */
    private boolean _replaceInInputFile(final String text, final String value,
					final FileHandler[] simulationInput){
	boolean found = false;
	for (int i = 0; i < simulationInput.length; i++)
	    if ( simulationInput[i].replaceString(text, value) )
		found = true;
	return found;
    }

    /** Replaces <code>text</code> with <code>value</code> in input functions.
     *@param text text to be searched for
     *@param value value that will replace <code>text</code>
     *@return <code>true</code> if <code>text</code> was found, <code>false</code> otherwise
     */
    private boolean _replaceInInputFunction(final String text, final String value){
	boolean found = false;
	for (int i = 0; i < inpFun.length; i++){
	    if ( inpFun[i].indexOf(text) != -1 ){ // found string
		inpFun[i] = FileHandler.replaceAll(text, value, inpFun[i]);
		found = true;
	    }
	}
	return found;
    }

    /** Replaces <code>text</code> with <code>value</code> output functions.
     *@param text text to be searched for
     *@param value value that will replace <code>text</code>
     *@return <code>true</code> if <code>text</code> was found, <code>false</code> otherwise
     */
    private boolean _replaceInOutputFunction(final String text, final String value){
	boolean found = false;
	for (int i = 0; i < outFun.length; i++){
	    if ( outFun[i] != null && outFun[i].indexOf(text) != -1 ){ // found String
		outFun[i] = FileHandler.replaceAll(text, value, outFun[i]);
		found = true;
	    }
	}
	return found;
    }

    /** Throws a <code>SimulationInputException</code> that says that the variable
     *  could not be found.
     *@param variableName name of the variable that was not found.
     *@exception SimulationInputException
     */
    private void _variableNotFound(final String variableName) throws SimulationInputException{
	final String ErrMes = "Variable was not found in any simulation input template file," +
	    LS +"or in any function objects:" +
	    LS + "    " + "Searching for String: '" + variableName +
	    "'. Cannot find string." +
	    LS + "    " + "'" + variableName +
	    "' must occur in at least one simulation" +
	    LS + "    " + "input template file, or in at least one function object.";
	throw new SimulationInputException(ErrMes);
    }

    /**
     * Process the function objects for post processing of the objective function.
     *
     * @param formula all formulas or <code>null</code>, if no function object has been specified
     *                for an objective function
     * @param objFunVal the objective function values for which no 
     *                  function object has been specified
     * @return an array with all objective function values
     * @exception OptimizerException if not all arguments in a function could be replaced
     * @exception NoSuchMethodException if the method that should be invoked could not be found
     * @exception IllegalAccessException  if an invoked method enforces Java language access 
     *                                    control and the underlying method is inaccessible
     * @exception InvocationTargetException if an invoked method throws an exception
     * @exception IOException if an error occurs
     */
    private double[] _processResultFunction(final String[] formula, 
					    final double[] objFunVal)
	throws OptimizerException, NoSuchMethodException, 
	       IllegalAccessException, InvocationTargetException, IOException {
	for (int iFx = 0; iFx < dimF; iFx++){
	    if ( formula[iFx] != null ){
		for (int iVal = 0; iVal < dimF; iVal++){
		    // replace all function values that are the result of the simulation
		    if ( formula[iVal] == null ){
			formula[iFx] = 
			    FileHandler.replaceAll('%' + objFunObj[iVal].getName() + '%',
						   Double.toString(objFunVal[iVal]),
						   formula[iFx]);
		    }
		}
	    }   
	}
	// evaluate the functions
	for(int iFx = 0; iFx < dimF; iFx++){
	    if ( formula[iFx] != null ) 
		objFunVal[iFx] = (new FunctionEvaluator(objFunObj[iFx].getName(),
							formula[iFx])).evaluate();
	}
	return objFunVal;
    }

    /** Checks whether the last objective function value has already been
     * obtained previously.<BR>
     * If it has been obtained previously, an information message is reported.<BR>
     * If the maximum number of matching function value is obtained, an exception
     * is thrown.
     *@exception OptimizerException thrown if the maximum number of matching
     * function value is obtained
     */
    public void checkObjectiveFunctionValue()
	throws OptimizerException{
	final int matVal = data.resChe.getNumberOfMatchingResults();
	final int numOfSim = data.ResMan.getNumberOfSimulation();
	final double funVal = (data.ResMan.getAllPoint(1)[0]).getF(0);
	data.resChe.setNewTrial(funVal,	numOfSim);

	if (matVal != data.resChe.getNumberOfMatchingResults()){
	    String mes = "f(x[" + numOfSim + "]) = " + funVal + LS +
		"Same result already obtained previously." + LS +
		"This may lead to abnormal termination.";
	    setInfo(mes);
	}
	data.resChe.check();
    }

    /** Sets a message in the InformationManager
     * The message will be displayed in the output stream
     * (GUI in WinGenOpt, command shell otherwise) and in
     * the log file
     *@param s the message
     *@see #setWarning(java.lang.String)
     */
    protected void setInfo(final String s) {
	assert s != null : "Received 'null' as argument";
	assert s != "" : "Received \"\" as argument";

	final String mes = "Simulation " + data.ResMan.getNumberOfSimulation() + ": " + s;
	data.infMan.setMessage(mes);
    }

    /** Sets a message in the WarningManager.
     * The message will be displayed in the output stream
     * (GUI in WinGenOpt, command shell otherwise) and in
     * the log file
     *@param s the message
     *@see #setInfo(java.lang.String)
     */
    protected void setWarning(final String s) {
	assert s != null : "Received 'null' as argument";
	assert s != "" : "Received \"\" as argument";

	final String mes = "Simulation " + data.ResMan.getNumberOfSimulation() + ": " + s;
	data.warMan.setMessage(mes);
    }

    /** Sets the maximal allowed number of matching results
     *@param maxNumberOfMatchingResults the number how many results can be
     *       equal before an OptimizerException is thrown
     */
    protected void setNumberOfMatchingResults(final int maxNumberOfMatchingResults)
    {
	data.resChe.setNumberOfMatchingResults(maxNumberOfMatchingResults);
    }

    /** Gets the value of <CODE>x[i]</CODE><BR>
     * <B>Note:</B> <CODE>x[i]</CODE> might be in the transformed space
     * depending on the value of <CODE>constraints</CODE>
     * @param i the number of the variable (zero-based counter)
     * @return the value of the variable
     */
    public double getX(final int i){
	assert ( i >= 0 && i < dimCon) : 
	    "Wrong argument. Received 'i=" + i +"'";
	if (conMode == ORIGINAL)
	    return data.conPar[i].getOriginalValue();
	else
	    return data.conPar[i].getTransformedValue();
    }

    /** Gets the value (i.e., the index) of <CODE>x[i]</CODE><BR>
     * @param i the number of the variable (zero-based counter)
     * @return the index of the variable
     */
    public int getIndex(final int i){
	assert ( i >= 0 && i < dimDis) : 
	    "Wrong argument. Received 'i=" + i +"'";
	return data.disPar[i].getIndex();
    }


    /** Gets the double representation of the <CODE>variableNumber</CODE>-th 
     *  <I>discrete</I> parameter.<BR>
     *
     * If the variable represents discrete <I>numerical</I> values, then the double
     * value of the currently selected value is returned.<BR>
     * If the variable represents different <I>string</I> values, then the currently
     * selected index is returned.<P>
     *
     * <B>Note:</B>
     * Prior to calling this method, you need to call either
     * {@link #setIndex(int, int) setIndex(int, int)} (for each component), 
     * or {@link #getF(Point) getF(Point)}, or
     * {@link #increaseStepNumber(Point) increaseStepNumber(Point)}.
     * Either of these methods update the index of the discrete parameter.
     *
     * @param variableNumber the number of the variable (zero-based counter)
     * @return the double representation of the currently selected value
     */
    public double getDiscreteValueDouble(final int variableNumber){
	assert ( variableNumber >= 0 && variableNumber < dimDis) : 
	    "Wrong argument. Received 'i=" + variableNumber +"'";
	return data.disPar[variableNumber].getValueDouble();
    }


    /** Gets the values of the continuous variable <CODE>x</CODE><BR>
     * <B>Note:</B> <CODE>x</CODE> might be in the transformed space
     * depending on the value of <CODE>constraints</CODE>
     * @return the vector of the independent variables
     */
    public double[] getX(){
	double[] r = new double[dimCon];
	for (int i = 0; i < dimCon; i++)
	    r[i] = getX(i);
	return r;
    }

    /** Gets the indices of the discrete variable <CODE>x</CODE><BR>
     * @return the vector of indices of the independent variables
     */
    public int[] getIndex(){
	int[] r = new int[dimDis];
	for (int i = 0; i < dimDis; i++)
	    r[i] = getIndex(i);
	return r;
    }

    /** Gets the number of elements of the i-th discrete variable
     * @param i the number of the variable (zero-based counter)
     * @return the number of elements of the i-th discrete variable
     */
    public final int getLengthDiscrete(final int i) {
	assert ( i >= 0 && i < dimDis) : 
	    "Wrong argument. Received 'i=" + i +"'";
	return data.disPar[i].length();
    }


    /** Gets the name of the continuous variable <CODE>x[i]</CODE>
     * @param i number of the variable (zero-based counter)
     * @return the name of <CODE>x[i]</CODE>
     */
    public final String getVariableNameContinuous(final int i){
	assert ( i >= 0 && i < dimCon) : 
	    "Wrong argument. Received 'i=" + i +"'";
	return data.conPar[i].getName();
    }

    /** Gets the name of the discrete variable <CODE>x[i]</CODE>
     * @param i number of the variable (zero-based counter)
     * @return the name of <CODE>x[i]</CODE>
     */
    public final String getVariableNameDiscrete(final int i){
	assert ( i >= 0 && i < dimDis) : 
	    "Wrong argument. Received 'i=" + i +"'";
	return data.disPar[i].getName();
    }

    /** Gets the name of the <CODE>i</CODE>-th objective function value.
     * 
     *  The name of the objective function value is specified by the key word
     *  <CODE>Namei</CODE> (<CODE>i = 1, 2, ... getDimensionF()</CODE>)
     *  in the section <CODE>ObjectiveFunctionLocation</CODE>.<BR>
     *
     *  <B>Note:</B>
     *       <CODE>i</CODE> is zero-based in the Java code, but one-based
     *       in the input file. Thus, to get the name of the first objective
     *       function value, you need to call this function with argument 
     *       <CODE>0</CODE>.
     *
     * @param i number of the objective function value (zero-based counter)
     * @return the name of the <CODE>[i]</CODE>-th objective function value
     */
    public final String getObjectiveFunctionName(final int i){
	assert ( i >= 0 && i < dimF) : 
	    "Wrong argument. Received 'i=" + i +"'";
	return objFunObj[i].getName();
    }

    /** Gets the lower bound <CODE>l[i]</CODE> of the continuous
     * variable <CODE>x[i]</CODE>
     * @param i the number of the variable (zero-based counter)
     * @return the lower bound of the variable
     */
    public double getL(final int i){
	assert ( i >= 0 && i < dimCon) : 
	    "Wrong argument. Received 'i=" + i +"'";
	return data.conPar[i].getMinimum();
    }

    /** Gets the upper bound <CODE>u[i]</CODE> of the continuous 
     * variable <CODE>x[i]</CODE>
     * @param i the number of the variable (zero-based counter)
     * @return the upper bound of the variable
     */
    public double getU(final int i){
	assert ( i >= 0 && i < dimCon) : 
	    "Wrong argument. Received 'i=" + i +"'";
	return data.conPar[i].getMaximum();
    }

    /** Gets the kind of constraint that is imposed on the i-th continuous
     * variable 
     * @param i the number of the variable (zero-based counter)
     * @return the kind of constraint that is imposed on the parameter.<BR>
     * <B>Possible constraints:</B><BR>
     * <pre> 1: no under boundary, no upper boundary
     * 2: under boundary,    no upper boundary
     * 3: under boundary,    upper boundary
     * 4: no under boundary, upper boundary </pre>
     */
    public int getKindOfConstraint(final int i){
	assert ( i >= 0 && i < dimCon) : 
	    "Wrong argument. Received 'i=" + i +"'";
	return data.conPar[i].getKindOfConstraint();
    }

    /** Gets the step size <CODE>dx[i]</CODE> of the i-th continuous variable<BR>
     * <B>Note:</B> <CODE>dx[i]</CODE> might be in the transformed space
     * depending on the value of <CODE>constraints</CODE>.<BR>
     * Do not forget to set x before using this method in an
     * other mode than <CODE>contraints = 0</CODE>
     * @param i the number of the variable (zero-based counter)
     * @return the value of the variable
     */
    public double getDx(final int i){
	assert ( i >= 0 && i < dimCon) : 
	    "Wrong argument. Received 'i=" + i +"'";
	if (conMode == ORIGINAL)
	    return data.conPar[i].getOriginalStepSize();
	else if (conMode == TRANSFORMED)
	    return data.conPar[i].getTransformedStepSize();
	else
	    return 0;
    }

    /** Sets the value of the continuous variable <CODE>x[i]</CODE> to <CODE>value</CODE>.<BR>
     * <B>Note:</B> <CODE>x[i]</CODE> might have to be in the transformed space
     * depending on the value of <CODE>constraints</CODE>
     * @param i the number of the variable (zero-based counter)
     * @param value the value of the variable
     */
    public void setX(final int i, final double value) {
	assert ( i >= 0 && i < dimCon) : 
	    "Wrong argument. Received 'i=" + i +"'";
	if (conMode == ORIGINAL){
	    assert ( (value >= getL(i)) && (value <= getU(i)) ) : 
		"Variable out of bounds. Received 'x[" + i + "]=" + value +"'";
	    data.conPar[i].setOriginalValue(value);
	}
	else
	    data.conPar[i].setTransformedValue(value);
    }

    /** Sets the index of the discrete variable <CODE>x[variableNumber]</CODE>
     *  to the value <CODE>index</CODE>.
     * 
     * @param variableNumber the number of the variable (zero-based counter)
     * @param index the index of the variable
     */
    public void setIndex(final int variableNumber, final int index){
	assert ( variableNumber >= 0 && variableNumber < dimDis) : 
	    "Wrong argument. Received 'variableNumber=" + variableNumber +"'";
	assert ( index >= 0 && 
		 index < data.disPar[variableNumber].length() ) :
	    "Index out of bounds. Received 'x[" + variableNumber + "]=" + 
	    index +"'";
	data.disPar[variableNumber].setIndex(index);
    }


    /** Prints a message to the output device without finishing the line<BR>
     * <B>Note:</B> Use this method instead of <CODE>System.out.printl(String)</CODE>,
     * otherwise it won't be reported in the GUI
     * @param text the text to be printed
     */
    public void print(final String text) {
	assert text != null : "Received 'null' as argument";
	data.print(text); }

    /** Prints a message to the output device, and then finishs the line<BR>
     * <B>Note:</B> Use this method instead of <CODE>System.out.println(String)</CODE>,
     * otherwise it won't be reported in the GUI
     * @param text the text to be printed
     */
    public void println(final String text) {
	assert text != null : "Received 'null' as argument";
	data.println(text); }

    /** Reports the minimum point.<BR>
     * This method gets the minimum point from the data base,
     * calls the function report with an
     * corresponding comment and reports the minimum
     * point to the output device.
     *@exception IOException if an I/O error in the optimization output files
     *               occurs
     */
    protected void reportMinimum() throws IOException{
	if (data.ResMan.getNumberOfMaiIteration() > 1){
	    final Point xMin = getMinimumPoint(); // this may be a call to ModelGPS.getMinimumPoint()
	    data.ResMan.reportMinimum("Minimum point.");
	    final String mes = LS + LS + "Minimum: f(x*) = " + xMin.getF(0) + LS;
	    println(mes);
	}
    }

    /** Gets the minimum point.<BR>
     * This method gets the minimum point from the data base.
     *@return the point with the lowest function value
     *@exception IOException if an I/O error in the optimization output files
     *               occurs
     */
    public Point getMinimumPoint(){
	if (data.ResMan.getNumberOfMaiIteration() > 1){
	    return data.ResMan.getMinimumPoint();
	}
	else
	    return null;
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
     *@param MainIteration <CODE>true</CODE> if step was a main iteration or
     *       <CODE>false</CODE> if it was a sub iteration
     *@exception IOException if an I/O error in the optimization output files
     *               occurs
     */
    public void report(final Point x, final boolean MainIteration)
	throws IOException{
	assert x != null : "Received 'null' as argument";
	for(int i = 0; i < dimCon; i++){
	    assert x.getX(i) != Double.POSITIVE_INFINITY : "Overflow.";
	    assert x.getX(i) != Double.NEGATIVE_INFINITY : "Overflow.";
	}
	Point r = (Point)x.clone();
	// parse point to original domain
	if (conMode == TRANSFORMED){
	    for (int i = 0; i < dimCon; i++){
		data.conPar[i].setTransformedValue(r.getX(i));
		r.setX(i, data.conPar[i].getOriginalValue());
	    }
	}
	data.ResMan.setNewTrial(r, (MainIteration) ? 0 : 1);
	return;
    }


    /** Checks if the maximum number of iteration is reached.
     * @return <CODE>true</CODE> if the number of simulation is
     *         equal or bigger than the maximum number of iteration,
     *         <CODE>false</CODE> otherwise
     */
    protected final boolean maxIterationReached() {
	return (getSimulationNumber() >= getMaxIterationNumber());
    }

    /** Gets the absolute accuracy of the last two main iterations
     * @return the absolute accuracy of the last two main iterations
     */
    protected final double getAbsAccuracyFunction(){
	return Math.abs( data.ResMan.getAbsDifMaiObjFunVal() );
    }

    /** Gets the relative accuracy of the last two main iterations
     * @return the relative accuracy of the last two main iterations
     */
    protected final double getRelAccuracyFunction(){
	return Math.abs( data.ResMan.getRelDifMaiObjFunVal() );
    }
    
    /** Gets the main iteration number
     * @return the main iteration number
     */
    protected final int getMainIterationNumber(){
	return data.ResMan.getNumberOfMaiIteration();
    }

    /** Gets the number of simulation.
     *  <B>Note:</B> This counter counts only the number of function
     *               evaluations that required a call to the simulation
     *               program.
     *               If the same point is evaluated twice -- which
     *               does not require a function evaluation --
     *               then the counter returned by this method is not
     *               increased.
     *
     * @return the number of simulation
     */
    protected final int getSimulationNumber(){
	return data.ResMan.getNumberOfSimulation();
    }

    /** Gets the maximum number of allowed main iterations
     * @return the maximum number of allowed main iterations
     */
    protected final int getMaxIterationNumber(){
	return data.OptSet.getMaxIteration();
    }

    /** Checks if the maximum number of iteration is exceeded
     * @return <CODE>true</CODE> if the main iteration number is
     *         equal or bigger than the maximum number of iteration,
     *         <CODE>false</CODE> otherwise
     */
    protected boolean checkMaxIteration(){
	return (getMainIterationNumber() >= getMaxIterationNumber());
    }

    /** Returns a flag that indicates whether GenOpt must be stopped after 
     * the current simulation
     * (due to a user request).
     *@return <CODE>true</CODE> if GenOpt has to be stopped, <CODE>false</CODE>
     *        otherwise
     */
    protected final boolean mustStopOptimization(){
	return data.mustStopOptimization();
    }

    /** Rounds the coordinates of the continuous parameters to float format.
     *
     * @param x the point to be rounded.
     * @return the point with rounded values for the independent parameters
     */
    static protected Point roundCoordinates(final Point x){
	Point r = (Point)x.clone();
	for(int i=0; i < r.getDimensionContinuous(); i++){
	    final double xD = Double.parseDouble( Float.toString( (float)x.getX(i) ) );
	    r.setX(i, xD);
	}
	return r;
    }

    /** Checks whether a point is feasible.
     *@param x the point to be checked
     *@return <CODE>true</CODE> if point is feasible,
     *        <CODE>false</CODE> otherwise
     */
    protected boolean isFeasible(final Point x) {
	for(int i = 0; i < dimCon; i++){
	    final double xi = x.getX(i);
	    if (xi > getU(i) || xi < getL(i) ){
		return false;
	    }
	}
	for(int i = 0; i < dimDis; i++){
	    final int xi = x.getIndex(i);
	    if (xi >= getLengthDiscrete(i) || xi < 0 )
		return false;
	}
	return true;
    }
    
    /** Restricts the value of <code>x</code> such that <code>l <= x <= u</code>.
     * 
     *  This method recursively reassigning 
     *  <code>x := 2 * l - x</code> if <code>x < l</code>,
     *  or <code>x := 2 * u - x</code> if <code>x < u</code>.
     *  If <code>x</code> is feasible, then it returns <code>x</code> unmodified.
     *@param x the independent paramter
     *@param l the lower bound
     *@param u the upper bound
     *@return a feasible value of <code>x</code>, such that <code>l <= x <= u</code>
     */
    static public double setToFeasibleCoordinate(double x, double l, double u){
	assert ( l < u );
	double xPre;
	do{
	    xPre = x;
	    // update coordinate until we are feasible
	    x = _setToFeasibleCoordinate(x, l, u);
	} while( xPre != x);
	return xPre;
    }

    /** Computes <code>x := 2 * l - x</code> if <code>x < l</code>,
     *  or <code>x := 2 * u - x</code> if <code>x < u</code>.
     *  If <code>x</code> is feasible, then it returns <code>x</code> unmodified.
     *@param x the independent paramter
     *@param l the lower bound
     *@param u the upper bound
     *@return a feasible value of <code>x</code>, such that <code>l <= x <= u</code>
     */
    static private double _setToFeasibleCoordinate(double x, double l, double u){
	if ( x < l )
	    return 2. * l - x;
	else if ( x > u )
	    return 2. * u - x;
	else
	    return x;
    }

    /** Restricts the value of <code>x</code> such that <code>l <= x <= u</code>.
     * 
     *  This method recursively reassigning 
     *  <code>x := 2 * l - x</code> if <code>x < l</code>,
     *  or <code>x := 2 * u - x</code> if <code>x < u</code>.
     *  If <code>x</code> is feasible, then it returns <code>x</code> unmodified.
     *@param x the independent paramter
     *@param l the lower bound
     *@param u the upper bound
     *@return a feasible value of <code>x</code>, such that <code>l <= x <= u</code>
     */
    static public int setToFeasibleCoordinate(int x, int l, int u){
	assert ( l < u );
	int xPre;
	do{
	    xPre = x;
	    // update coordinate until we are feasible
	    x = _setToFeasibleCoordinate(x, l, u);
	} while( xPre != x);
	return xPre;
    }

    /** Computes <code>x := 2 * l - x</code> if <code>x < l</code>,
     *  or <code>x := 2 * u - x</code> if <code>x < u</code>.
     *  If <code>x</code> is feasible, then it returns <code>x</code> unmodified.
     *@param x the independent paramter
     *@param l the lower bound
     *@param u the upper bound
     *@return a feasible value of <code>x</code>, such that <code>l <= x <= u</code>
     */
    static private int _setToFeasibleCoordinate(int x, int l, int u){
	if ( x < l )
	    return 2 * l - x;
	else if ( x > u )
	    return 2 * u - x;
	else
	    return x;
    }


    /** The number of independent, continuous variables */
    static private int dimCon;
    /** The number of independent, discrete variables */
    static private int dimDis;
    /** The number of independent variables (sum of continuous and discrete) */
    static private int dimX;
    /** The number of input functions */
    static private int dimInpFun;
    /** Text representation of the input functions */
    static private String[] inpFun;
    /** Text representation of the output functions */
    static private String[] outFun;
    /** The number of cost function values */
    static private int dimF;
    /** The name of the function values */
    static private String[] nameF;

    /** The reference to the GenOpt kernel */
    static private GenOpt data;
    /** The section <CODE>Algorithm</CODE> of the command file */
    static private StreamTokenizer algorithm;
    /** The name of the optimization command file */
    static private String optComFilNam;
    /** Separator that separate the objective function value from the values behind it.
	Separators additional to whitespace characters are specified in the properties.txt file */
    static private String separator;
    /** The step number (for implementing variation of the weighting factors
	for penalty function and barrier function */
    static private int stepNumber;
    /** A flag to indicate whether the step number has to be written in the simulation
	input file (and hence a simulation done) */
    static private boolean wriSteNum;
    /** A flag to indicate whether the step number is used by the optimization algorithm.
	It is used if either <code>wriSteNum = true</code> or
	if the algorithm requires explicitly the step number to be used.
	This is typically the case for algorithm that construct at some point a 
	surrogate function and then only seek to optimize the surrogate. */
    static private boolean useSteNum;
    /** The simulation input template file handler */
    static private FileHandler[] simInpTemFilHan;
    /** The simulation log file names (incl. path) */
    static private String[] simLogFil;
    /** The simulation output file names (incl. path) */
    static private String[] simOutFil;
    /** The objective function objects */
    static private ObjectiveFunctionLocation[] objFunObj;
    /** The number of the simulation input files */
    static private int nSimInpFil;
    /** The number of function objects */
    static private int nFunObj;
    /** The number of the simulation log files */
    static private int nSimLogFil;
    /** The number of the simulation output files */
    static private int nSimOutFil;
    /** pointer that assign to each objective function value 
     *  the number of the output file in which it is stored */
    static private int[] funValPoi;
    /** The list with evaluated points and its function values.
        Prior to evaluating the cost function, this list is checked whether it contains
        the same point, where equality is defined by the implementation of
        <CODE>genopt.algorithm.util.math.Point.compareTo(java.lang.Object o)</CODE>
    */
    static private TreeMap<Point, Double[]> evaPoi;

    /** Instance for smoothing the cost function */
    static private Smoothing smoo;
    /** Flag whether the cost function should be smoothed or not */
    static private boolean doSmoothing;

    /** Calls the constructor for the double integration.
     *  This method must be called in the constructor of all
     *  optimization algorithms that uses smoothing of the cost function
     */
    public void constructIntegrator() throws OptimizerException, 
					     IOException, Exception, InputFormatException{
	smoo = new Smoothing();
	algorithmRequiresUsageOfStepNumber();
    }

    /** Activate smoothing of cost function */
    public void activateSmoothing() {
	assert smoo != null : "Integrator not constructed.";
	doSmoothing = true;
	increaseStepNumber();
    }

    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    /** Inner class for smoothing the cost function.
     */
    private class Smoothing{
	public Smoothing()
	    throws OptimizerException, IOException, Exception, InputFormatException{
	    final int dimX = getDimensionContinuous();
	    // --- IntegrationDomainFactor
	    // Factor how much bigger the integration domain must be compared to the step size
	    final double intDomFac = getInputValueDouble("IntegrationDomainFactor",
							 0, Optimizer.EXCLUDING,
							 Double.MAX_VALUE, Optimizer.EXCLUDING);
	    
	    // --- NumberOfSupportPoint
	    // Number of support points for integration along each coordinate direction
	    NumSupPoi = getInputValueInteger("NumberOfSupportPoint");
	    
	    if ( ! ( NumSupPoi == 3 || NumSupPoi == 5 || NumSupPoi ==  7) )
		throwInputError("3, 5, or 7 for 'NumberOfSupportPoint'");
	    
	    // Number of integrations
	    final String nI = getInputValueString("NumberOfIntegration").trim();
	    NumOfInt = new int[dimX];
	    if ( nI.length() == 1 ){
		int val = 0;
		if ( nI.equals("0") )
		    val = 0;
		else if ( nI.equals("1") )
		    val = 1;
		else if ( nI.equals("2") )
		    val = 2;
		else
		    throwInputError("Invalid value for 'NumberOfIntegration'. Expected '0', '1' or '2'");
		for(int i = 0; i < dimX; i++)
		    NumOfInt[i] = val;
	    }
	    else{
		if ( nI.length() != dimX )
		    throwInputError("'NumberOfIntegration' must have '1' or '" + dimX + "' digits");
		for(int i = 0; i < dimX; i++){
		    if (nI.charAt(i) == '0')
			NumOfInt[i] = 0;
		    else if (nI.charAt(i) == '1')
			NumOfInt[i] = 1;
		    else if (nI.charAt(i) == '2')
			NumOfInt[i] = 2;
		    else
			throwInputError("Invalid value for 'NumberOfIntegration'. Expected '0', '1' or '2' for all elements");
		    
		}
	    }

	    Delta = new double[dimX];
	    for(int i = 0; i < dimX; i++)
		Delta[i] = getDx(i) * intDomFac;
	    
	    _initializeRungeCotteCoefficients();
	    // initialize list with evaluated points after smoothing
	    evaPoiF1 = new TreeMap<Point, Double[]>();
	    evaPoiF2 = new TreeMap<Point, Double[]>();
	}

	/** Initializes the coefficients for the Runge-Cotte integration */
	private void _initializeRungeCotteCoefficients(){
	    switch( NumSupPoi ){
	    case 3:
		ANewCot = new double[NumSupPoi];
		ANewCot[0] = 1./3.;
		ANewCot[1] = 4./3.;
		ANewCot[2] = 1./3.;
		break;
	    case 5:
		ANewCot = new double[NumSupPoi];
		ANewCot[0] = 14./45.;
		ANewCot[1] = 64./45.;
		ANewCot[2] = 24./45.;
		ANewCot[3] = 64./45.;
		ANewCot[4] = 14./45.;
		break;
	    case 7:
		ANewCot = new double[NumSupPoi];
		ANewCot[0] =  41./140.;
		ANewCot[1] = 216./140.;
		ANewCot[2] =  27./140.;
		ANewCot[3] = 272./140.;
		ANewCot[4] =  27./140.;
		ANewCot[5] = 216./140.;
		ANewCot[6] =  41./140.;
		break;
	    default:
		assert true : "Wrong value for 'NumSupPoi'.";
	    }
	}
    
	/** Gets the objective function value and registers it into
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
	 */
	public Point getF(Point x) throws
	    SimulationInputException, OptimizerException, NoSuchMethodException,
	    IllegalAccessException, Exception{
	    final int iSim = getSimulationNumber();
	    final Point r = this.getF2(x);
	    return r;
	}


	/** Gets the function value of the smoothed cost function using double integration.
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
	public Point getF2(Point x) throws
	    SimulationInputException, OptimizerException, NoSuchMethodException,
	    IllegalAccessException, Exception{

	    Point poi = (Point)x.clone();
	    if(evaPoiF2.containsKey(poi)){
		////////////////////////////////////////////////////////
		// point already evaluated
		//println("Point already evaluated. Take function value from database.");
		// get the function value corresponding to this point
		Double[] val = (Double[])(evaPoiF2.get(poi));
		for (int i = 0; i < dimF; i++)
		    poi.setF( i, val[i].doubleValue() );
		return poi;
	    }
	    else{
		//println("------- Start second smoothing.");
		final Point base = this.getF1(x);
		// don't report the support point because otherwise it would be querried
		// in the search for the point with lowest function value
		// second integral
		double[] funVal = base.getF();
	    
		for(int i = 0; i < dimX; i++){
		    if (NumOfInt[i] > 1){ // integrate along this coordinate
			final int M = NumSupPoi-1;
			for(int j=0; j <= M; j++){
			    if ( j != M/2 ){
				final double inc = (double)(2 * j - M) / (double)(M) * Delta[i];
				final double xNew = base.getX(i) + inc;
				Point xS = (Point)base.clone();
				xS.setX(i, xNew);
				xS = this.getF1( xS );
				double[] delF = LinAlg.subtract( xS.getF(), base.getF() );
				final double aM = ANewCot[j] / (double)M;
				delF = LinAlg.multiply( aM, delF );
				funVal = LinAlg.add( funVal, delF );
			    }
			}
		    }
		}
		poi.setF(funVal);
		// add point and function value to the map of evaluated points
		Double[] val = new Double[dimF];
		for (int i = 0; i < dimF; i++)
		    val[i] = new Double(funVal[i]);

		if ( writeStepNumber() ) // step number is written, hence it may be used for penalty functions
		    poi.setStepNumber( getStepNumber() );
		else // step number is not used in function evaluation. Set to 1 
		    poi.setStepNumber(1);
	    
		// we must clone the object that we put into the TreeMap
		// Otherwise, it's coordinates get changed since the map
		// contains only a reference to the instance.
		evaPoiF2.put((Point)poi.clone(), val);
		poi.setStepNumber( getStepNumber() );

		//println("------- Finished second smoothing.");
		return poi;
	    }
	}

	/** Gets the function value of the smoothed cost function using one integration.
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
	public Point getF1(Point x) throws
	    SimulationInputException, OptimizerException, NoSuchMethodException,
	    IllegalAccessException, Exception{

	    Point poi = (Point)x.clone();
	    if(evaPoiF1.containsKey(poi)){
		////////////////////////////////////////////////////////
		// point already evaluated
		//println("Point already evaluated. Take function value from database.");
		// get the function value corresponding to this point
		Double[] val = (Double[])(evaPoiF1.get(poi));
		for (int i = 0; i < dimF; i++)
		    poi.setF(i, val[i].doubleValue() );
		return poi;
	    }
	    else{
		//println("------- Start first smoothing.");
		final Point base = this._evaluateSupportPoint(x);
		// don't report the support point because otherwise it would be querried
		// in the search for the point with lowest function value
		// second integral
		double[] funVal = base.getF();
	    
		for(int i = 0; i < dimX; i++){
		    if (NumOfInt[i] > 0){ // integrate along this coordinate
			final int M = NumSupPoi-1;
			for(int j=0; j <= M; j++){
			    if ( j != M/2 ){
				final double inc = (double)(2 * j - M) / (double)(M) * Delta[i];
				final double xNew = base.getX(i) + inc;
				Point xS = (Point)base.clone();
				xS.setX(i, xNew);
				xS = this._evaluateSupportPoint( xS );
				double[] delF = LinAlg.subtract( xS.getF(), base.getF() );
				final double aM = ANewCot[j] / (double)M;
				delF = LinAlg.multiply( aM, delF );
				funVal = LinAlg.add( funVal, delF );
			    }
			}
		    }
		}
		poi.setF(funVal);
		// add point and function value to the map of evaluated points
		Double[] val = new Double[dimF];
		for (int i = 0; i < dimF; i++)
		    val[i] = new Double(funVal[i]);

		if ( writeStepNumber() ) // step number is written, hence it may be used for penalty functions
		    poi.setStepNumber( getStepNumber() );
		else // step number is not used in function evaluation. Set to 1 
		    poi.setStepNumber(1);
	    
		// we must clone the object that we put into the TreeMap
		// Otherwise, it's coordinates get changed since the map
		// contains only a reference to the instance.
		evaPoiF1.put((Point)poi.clone(), val);
		poi.setStepNumber( getStepNumber() );

		//println("------- Finished first smoothing.");
		return poi;
	    }
	}

	/** Evaluates a support point for the numerical approximation to the
	 *  double integral
	 *@param p the point being evaluated
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
	private Point _evaluateSupportPoint(final Point p) throws
	    SimulationInputException, OptimizerException, NoSuchMethodException,
	    IllegalAccessException, Exception{
	    Point poi = (Point)p.clone();
	    if ( getMode() == Optimizer.ORIGINAL ){
		// Optimization is done in original space.
		// Transform all unfeasible support points to the feasible domain
		// by reflecting each unfeasible coordinate value at the violated domain boundaries.
		for(int i = 0; i < dimX; i++){
		    final double xFea = setToFeasibleCoordinate( poi.getX(i), getL(i), getU(i) );
		    poi.setX(i, xFea );
		}
	    }
	    poi.setComment("Support point for smoothing.");
	    //	    call the _getF(...) of Optimizer
	    poi = _getF( poi );
	    report(poi, Optimizer.SUBITERATION);
	    return poi;
	}


	/** Half width of the length of the integration domain for 
	    each coordinate direction */
	private double[] Delta;
	/** Number of support points for the integration 
	    for each coordinate direction */
	private int NumSupPoi;
	/** Coefficients for the Newton-Cotes integration formula */
	private double[] ANewCot;
	/** Number of integrations along the coordinate directions */
	private int[] NumOfInt;
	/** The list with evaluated points and its function values 
	    after the first smoothing.
	    Prior to evaluating the cost function, this list is checked whether
	    the same point has already been evaluated, 
	    where equality is defined by the implementation of
	    <CODE>genopt.algorithm.util.math.Point.compareTo(java.lang.Object o)</CODE>
	*/
	private TreeMap<Point, Double[]> evaPoiF1;
	/** The list with evaluated points and its function values 
	    after the second smoothing.
	    Prior to evaluating the cost function, this list is checked whether
	    the same point has already been evaluated, 
	    where equality is defined by the implementation of
	    <CODE>genopt.algorithm.util.math.Point.compareTo(java.lang.Object o)</CODE>
	*/
	private TreeMap<Point, Double[]> evaPoiF2;
    }
}
