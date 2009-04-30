package genopt.db;
import genopt.lang.ObjectiveFunctionLocation;
import genopt.io.FileHandler;

import java.io.IOException;
import java.io.File;



/** Object that stores the various file names that are used
  * by the optimization program.
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
  * @version GenOpt(R) 3.0.0 (May 4, 2009)<P>
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

public class OptimizationIni implements Cloneable
{

    /** System dependent file separator */
    protected final static String FS = System.getProperty("file.separator");

    /** constructor
     * @param SimulationInputTemplateFileName File name of
     *   the template input files of the simulation <b>input</b>
     * @param SimulationInputTemplatePath Path where
     *   the template input files of the simulation are
     * @param SimulationInputFileName File names of
     *   the input files of the simulation <b>input</b>
     * @param SimulationInputPath Paths where
     *   the input files of the simulation are
     * @param SimulationInputSavePath Path where
     *   the input files of the simulation have to be saved,
     *   or <CODE>""</CODE> if they should not be saved
     * @param SimulationOutputFileName File names of
     *   the output files of the simulation that contains the <b>results</b>
     * @param SimulationOutputPath Path where
     *   the output files of the simulation are
     * @param SimulationOutputSavePath Path where
     *   the output files of the simulation have to be saved,
     *   or <CODE>""</CODE> if they should not be saved
     * @param SimulationLogFileName File names of
     *   the log files of the simulation that contains the <b>error messages</b>
     * @param SimulationLogPath Path where
     *   the log files of the simulation are
     * @param SimulationLogSavePath Path where
     *   the log files of the simulation have to be saved,
     *   or <CODE>""</CODE> if they should not be saved
     * @param SimulationConfigFileName File name of
     *   the configuration file of the simulation program that is used by the
     *   optimization engine. This file contains specific informations 
     *   about the <b>simulation program</b>
     * @param SimulationConfigPath Path where
     *   the configuration file for the simulation engine is
     * @param OptimizationInitializationPath Path where
     *   the optimization configuration file is
     * @param OptimizationCommandFileName File name of
     *   the command file of the optimization
     * @param OptimizationCommandPath Path where
     *   the command file of the optimization is
     * @param SimulationCallPrefix prefix for simulation call
     *        might be a blank character
     * @param SimulationCallSuffix suffix for simulation call
     *        might be a blank character
     * @exception IOException If an I/O error occurs, which is possible because the construction of the 
     *                        canonical pathname may require filesystem queries
     */
    public OptimizationIni(String[] SimulationInputTemplateFileName,
			   String[] SimulationInputTemplatePath,
			   String[] SimulationInputFileName,
			   String[] SimulationInputPath,
			   String[] SimulationInputSavePath,
			   String[] SimulationOutputFileName,
			   String[] SimulationOutputPath,
			   String[] SimulationOutputSavePath,
			   String[] SimulationLogFileName,
			   String[] SimulationLogPath,
			   String[] SimulationLogSavePath,
			   String SimulationConfigFileName,
			   String SimulationConfigPath,
			   String OptimizationInitializationPath,
			   String OptimizationCommandFileName,
			   String OptimizationCommandPath,
			   String SimulationCallPrefix,
			   String SimulationCallSuffix)
	throws IOException
    {
	// replace path by canonical path
	SimulationInputTemplatePath = FileHandler.addCanonicalPaths(SimulationInputTemplatePath,
								    OptimizationInitializationPath);
	SimulationInputPath = FileHandler.addCanonicalPaths(SimulationInputPath,
							    OptimizationInitializationPath);
	SimulationInputSavePath = FileHandler.addCanonicalPaths(SimulationInputSavePath,
								OptimizationInitializationPath);
	SimulationOutputPath = FileHandler.addCanonicalPaths(SimulationOutputPath,
							     OptimizationInitializationPath);
	SimulationOutputSavePath = FileHandler.addCanonicalPaths(SimulationOutputSavePath,
								 OptimizationInitializationPath);
	SimulationLogPath = FileHandler.addCanonicalPaths(SimulationLogPath,
							  OptimizationInitializationPath);
	SimulationLogSavePath = FileHandler.addCanonicalPaths(SimulationLogSavePath,
							      OptimizationInitializationPath);
	SimulationConfigPath = FileHandler.addCanonicalPaths(SimulationConfigPath,
							     OptimizationInitializationPath);
	OptimizationCommandPath = FileHandler.addCanonicalPaths(OptimizationCommandPath,
								OptimizationInitializationPath);
	
	objFunMapIsSet = false;
	nInpFil = SimulationInputTemplateFileName.length;
	nOutFil = SimulationOutputFileName.length;
	nLogFil = SimulationLogFileName.length;


	// Resolve the path and file names. This section ensures that statements such as
        // File1 = "/myDir/myFile.txt";
	// are resolved so that they look like a specification
        // File1 = "myFile.txt";
        // Path1 = "/myDir";
	SimInpTemFilNam = new String[nInpFil];
	SimInpTemPat    = new String[nInpFil];
	for (int i = 0; i < nInpFil; i++){
	    File fil = new File(SimulationInputTemplatePath[i],
				SimulationInputTemplateFileName[i]);
	    SimInpTemFilNam[i] = fil.getName();
	    SimInpTemPat[i]    = fil.getParentFile().getCanonicalPath();

	}

	SimInpFilNam    = new String[nInpFil];
	SimInpPat       = new String[nInpFil];
	SimInpSavPat    = new String[nInpFil];
	for (int i = 0; i < nInpFil; i++){
	    File fil = new File(SimulationInputPath[i],
				SimulationInputFileName[i]);
	    SimInpFilNam[i] = fil.getName();
	    SimInpPat[i]    = fil.getParentFile().getCanonicalPath();

	    SimInpSavPat[i] =  SimulationInputSavePath[i].equals("") ? 
		"" : new File(SimulationInputSavePath[i]).getCanonicalPath();
	}


	SimOutFilNam = new String[nOutFil];
	SimOutPat    = new String[nOutFil];
	SimOutSavPat = new String[nOutFil];
	for (int i = 0; i < nOutFil; i++){
	    File fil = new File(SimulationOutputPath[i],
				SimulationOutputFileName[i]);
	    SimOutFilNam[i] = fil.getName();
	    SimOutPat[i]    = fil.getParentFile().getCanonicalPath();
	    SimOutSavPat[i] = SimulationOutputSavePath[i].equals("") ? 
		"" : new File(SimulationOutputSavePath[i]).getCanonicalPath();
	}


	SimLogFilNam = new String[nLogFil];
	SimLogPat    = new String[nLogFil];
	SimLogSavPat = new String[nLogFil];
	for (int i = 0; i < nLogFil; i++){
	    File fil = new File(SimulationLogPath[i],
				SimulationLogFileName[i]);
	    SimLogFilNam[i] = fil.getName();
	    SimLogPat[i]    = fil.getParentFile().getCanonicalPath();

	    SimLogSavPat[i] = SimulationLogSavePath[i].equals("") ? 
		"" : new File(SimulationLogSavePath[i]).getCanonicalPath();
	}

	File fil = new File(SimulationConfigPath, SimulationConfigFileName);
	SimConFilNam    = fil.getName();
	SimConPat       = fil.getParentFile().getCanonicalPath();

	OptIniPat       = new File(OptimizationInitializationPath).getCanonicalPath();

	fil = new File(OptimizationCommandPath, OptimizationCommandFileName);
	OptComFilNam    = fil.getName();
	OptComPat       = fil.getParentFile().getCanonicalPath();
	SimCalPre       = SimulationCallPrefix;		
	SimCalSuf       = SimulationCallSuffix;
    }

    /** Sets the entry of the section ObjectiveFunctionLocation
     * @param objectiveFunctionDelimiter <CODE>OrderedMap</CODE> with objective function delimiters
     */
    public void setObjectiveFunctionLocation(OrderedMap objectiveFunctionDelimiter)
    {
	objFunMap = objectiveFunctionDelimiter;
	objFunMapIsSet = true;
	return;
    }

    /**gets the number of simulation input files
     *@return the number of simulation input files
     */
    public final int getNumberOfInputFiles() { return nInpFil; }

    /**gets the number of function objects for the objective function
     *@return the number of function objects for the objective function
     */
    public final int getNumberOfFunctionObjects() {
	int nFun = 0;
	ObjectiveFunctionLocation[] ol = (ObjectiveFunctionLocation[])(objFunMap.getValues() );
	for (int i = 0; i < ol.length ; i++)
	    if ( ol[i].isFunction() )
		nFun++;
	return nFun;
    }

    /**gets the function objects for the objective function
     *@return the function objects for the objective function
     */
    public final ObjectiveFunctionLocation[] getFunctionObjects() {
	ObjectiveFunctionLocation[] r = new ObjectiveFunctionLocation[objFunMap.size()];
	for (int i = 0; i < r.length; i++)
	    r[i] = (ObjectiveFunctionLocation)(objFunMap.getValue(i));
	return r;
    }

    /**gets the number of simulation output files
     *@return the number of simulation output files
     */
    public final int getNumberOfOutputFiles() { return nOutFil; }

    /**gets the number of simulation log files
     *@return the number of simulation log files
     */
    public final int getNumberOfLogFiles() { return nLogFil; }

    /**gets the simulation input template file name
     *@param i the number of the file
     *@return the simulation input template file name
     */
    public final String getSimInpTemFilNam(int i)  {return new String(SimInpTemFilNam[i]);}

    /**gets the simulation input template path
     *@param i the number of the file
     *@return the simulation input template path
     */
    public final String getSimInpTemPat(int i)  {return new String(SimInpTemPat[i]);}

    /**gets the simulation input file name
     *@param i the number of the file
     *@return the simulation input file name
     */
    public final String getSimInpFilNam(int i)  {return new String(SimInpFilNam[i]);}
    
    /**gets the simulation input file names
     *@return the simulation input file names
     */
    public final String[] getSimInpFilNam()
    {
	String[] r = new String[nInpFil];
	System.arraycopy(SimInpFilNam, 0, r, 0, nInpFil);
	return r;
    }

    /**gets the simulation input path
     *@param i the number of the file
     *@return the simulation input path
     */
    public final String getSimInpPat(int i)  {return new String(SimInpPat[i]);}

    /**gets the simulation input path names
     *@return the simulation input path names
     */
    public final String[] getSimInpPat()
    {
	String[] r = new String[nInpFil];
	System.arraycopy(SimInpPat, 0, r, 0, nInpFil);
	return r;
    }    

   /**gets the path where the simulation input file has to be saved
     *@param i the number of the file
     *@return the path where the simulation input file has to be saved, or <CODE>null</CODE>
     *        if no save is required
     */
    public final String getSimInpSavPat(int i)
    {
	return SimInpSavPat[i].equals("") ? null : new String(SimInpSavPat[i]) ;
    }

    /**gets the simulation output file names
     *@return the simulation output file names
     */
    public final String[] getSimOutFilNam()
    {
	String[] r = new String[nOutFil];
	System.arraycopy(SimOutFilNam, 0, r, 0, nOutFil);
	return r;
    }

    /**gets the simulation output file name
     *@param i the number of the file
     *@return the simulation output file name
     */
    public final String getSimOutFilNam(int i){ return new String(SimOutFilNam[i]); }
	
    /**gets the simulation output paths
     *@return the simulation output paths
     */
    public final String[] getSimOutPat()
    {
	String[] r = new String[nOutFil];
	System.arraycopy(SimOutPat, 0, r, 0, nOutFil);
	return r;
    }

    /**gets the simulation output path
     *@param i the number of the file
     *@return the simulation output path
     */
    public final String getSimOutPat(int i)  {return new String(SimOutPat[i]) ;}

    /**gets the simulation input save paths
     *@return the simulation input save paths. 
     *        If a path has not been specified, then
     *        this element contains the String '""'
     */
    public final String[] getSimInpSavPat()
    {
	String[] r = new String[nInpFil];
	System.arraycopy(SimInpSavPat, 0, r, 0, nInpFil);
	return r;
    }

    /**gets the simulation output save paths
     *@return the simulation output save paths. 
     *        If a path has not been specified, then
     *        this element contains the String '""'
     */
    public final String[] getSimOutSavPat()
    {
	String[] r = new String[nOutFil];
	System.arraycopy(SimOutSavPat, 0, r, 0, nOutFil);
	return r;
    }

    /**gets the path where the simulation output file has to be saved
     *@param i the number of the file
     *@return the path where the simulation output file has to be saved, or <CODE>null</CODE>
     *        if no save is required
     */
    public final String getSimOutSavPat(int i)
    {
	return SimOutSavPat[i].equals("") ? null : new String(SimOutSavPat[i]) ;
    }

    /**gets the simulation log file name
     *@param i the number of the file
     *@return the simulation log file name
     */
    public final String getSimLogFilNam(int i)  {return new String(SimLogFilNam[i]) ;}

    /**gets the simulation log file names
     *@return the simulation log file names
     */
    public final String[] getSimLogFilNam()
    {
	String[] r = new String[nLogFil];
	System.arraycopy(SimLogFilNam, 0, r, 0, nLogFil);
	return r;
    }


    /**gets the simulation log paths
     *@return the simulation log paths
     */
    public final String[] getSimLogPat()
    {
	String[] r = new String[nLogFil];
	System.arraycopy(SimLogPat, 0, r, 0, nLogFil);
	return r;
    }

    /**gets the simulation log path
     *@param i the number of the file
     *@return the simulation log path
     */
    public final String getSimLogPat(int i)  {return new String(SimLogPat[i]) ;}

    /**gets the simulation log save paths
     *@return the simulation log save paths.
     *        If a path has not been specified, then
     *        this element contains the String '""'
     */
    public final String[] getSimLogSavPat()
    {
	String[] r = new String[nLogFil];
	System.arraycopy(SimLogSavPat, 0, r, 0, nLogFil);
	return r;
    }

   /**gets the path where the simulation log file has to be saved
     *@param i the number of the file
     *@return the path where the simulation log file has to be saved, or <CODE>null</CODE>
     *        if no save is required
     */
    public final String getSimLogSavPat(int i)
    {
	return SimLogSavPat[i].equals("") ? null : new String(SimLogSavPat[i]) ;
    }

    /**gets the simulation configuration file name
     *@return the simulation configuration file name
     */
    public final String getSimConFilNam()  {return new String(SimConFilNam) ;}

    /**gets the simulation configuration path
     *@return the simulation configuration path
     */
    public final String getSimConPat()  {return new String(SimConPat) ;}

    /**gets the optimization initialization path
     *@return Optimization initialization path
     */
    public final String getOptIniPat()  {return new String(OptIniPat) ;}

    /**gets the optimization command file name
     *@return Optimization command file name
     */
    public final String getOptComFilNam()  {return new String(OptComFilNam) ;}

    /**gets the optimization command path
     *@return Optimization command path
     */
    public final String getOptComPat() {return new String(OptComPat) ;}

    /* gets a <CODE>OrderedMap</CODE> with the delimiter of the objective function 
     * in the simulation output file
     *@return <CODE>OrderedMap</CODE> with the delimiter of the objective function 
     */
    //    public final OrderedMap getObjFunMap() {return objFunMap;}

    /**gets the delimiter of the objective function in the simulation output file
     *@param name name of the objective function
     *@return delimiter of the objective function in the simulation output file
     */
    public final String getObjFunDel(String name) {
	ObjectiveFunctionLocation ol = (ObjectiveFunctionLocation)
	    (objFunMap.get(name));
	return ol.getDelimiter();
    }

    /**gets the function definining the objective function
     *@param name name of the objective function
     *@return string representing the objective function
     */
    public final String getObjFunFun(String name) {
	ObjectiveFunctionLocation ol = (ObjectiveFunctionLocation)
	    (objFunMap.get(name));
	return ol.getFunction();
    }

    /** returns <code>true</code> if <code>name</code> is the name
     * of an objective function that is defined by a function object,
     * rather than by a direct simulation output
     *@param name name of the objective function
     *@return returns <code>true</code> if <code>name</code> is the name
     * of an objective function that is defined by a function object,
     * <code>false</code> otherwise
     */
    public final boolean isFunction(String name){
	ObjectiveFunctionLocation ol = (ObjectiveFunctionLocation)
	    (objFunMap.get(name));
	return ol.isFunction();
    }

    /**gets the simulation call prefix
     *@return simulation call prefix
     */
    public final String getSimCalPre()  {return new String(SimCalPre) ;}

    /**gets the simulation call suffix
     *@return simulation call suffix
     */
    public final String getSimCalSuf()  {return new String(SimCalSuf) ;}
	
    /** checks whether the objective function delimiter is set or not
     *@return <CODE>true</CODE> if it is set, <CODE>false</CODE> otherwise
     */
    public final boolean isObjectiveFunctionDelimiterSet() { return objFunMapIsSet; }

    /** converts the argument <code>path</code> to the temporary path that will be used
     *  for the simulation
     *@param path the original path as specified in GenOpt's files
     *@param dirName the name of the temporary directory (without the full path name)
     *@return the path name
    */
    public final String convertToTemporaryPath(final String path, final String dirName){
	final String ori = this.getOptIniPat();
	// insert new directory name if the original string starts with the path
	// of the optimization initialization file
	if ( path.startsWith(ori) ){
	    String rep;
	    // create new directory name
	    if (ori.endsWith(FS)) 
		rep = new String(ori + dirName + FS);
	    else
		rep = new String(ori + FS + dirName);
	    return new String(rep + path.substring(ori.length()));
	}
	else
	    return new String(path);
    }

    /** file name: Simulation input template */
    protected String[] SimInpTemFilNam;
    /** file name: Simulation input */
    protected String[] SimInpFilNam;
    /** file name: Simulation output */
    protected String[] SimOutFilNam;
    /** file name: Simulaton log file (contains error messages) */
    protected String[] SimLogFilNam;
    /** file name: Simulation program configuration */
    protected String   SimConFilNam;

    /** path name: Simulation input template */
    protected String[] SimInpTemPat;
    /** path name: Simulation input */
    protected String[] SimInpPat;
    /**  path name of save directory: Simulation input (String "" indicates no save required) */
    protected String[] SimInpSavPat;
    /** path name: Simulation output */
    protected String[] SimOutPat;
    /** path name of save directory: Simulation output (String "" indicates no save required) */
    protected String[] SimOutSavPat;
    /** path name: Simulaton log file (contains error messages) */
    protected String[] SimLogPat;
    /** path name of save directory: Simulaton log file (String "" indicates no save required) */
    protected String[] SimLogSavPat;
    /** path name: Simulation program configuration */
    protected String   SimConPat;

    /** path name: Optimization initialization */
    protected String   OptIniPat;
    /** file name: Optimization command file */
    protected String   OptComFilNam;
    /** path name: Optimization command */
    protected String   OptComPat;

    /** simulation call suffix */
    protected String   SimCalSuf;
    /** simulation call prefix */
    protected String   SimCalPre;

    /** delimiter of the objective function value in the output file */
    protected OrderedMap objFunMap;
    /** flag indicating whether the objective function value is set or not */
    protected boolean objFunMapIsSet;
    /** number of input files */
    protected int nInpFil;
    /** number of simulation log files */
    protected int nLogFil;
    /** number of output files */
    protected int nOutFil;
}
