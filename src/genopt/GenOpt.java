package genopt;
import genopt.lang.*;
import genopt.algorithm.*;
import genopt.algorithm.util.math.*;
import genopt.io.*;
import genopt.db.*;
import genopt.simulation.*;
import genopt.util.*;
import java.io.*;

import javax.swing.*;
import java.util.*;
import java.lang.reflect.*;

/** Object for optimizing an objective function computed by
  * a simulation program.
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
  * The version numbering format is M.m.P.s, where M is the major version
  * number, m is the minor version number, P is the patch level, and 
  * s is the snapshot number. 
  * Full releases have P set to zero, and it is incremented for each 
  * subsequent bug fix release on the post-release stable branch. 
  * The snapshot number s is present only for between-release 
  * snapshots of the development and stable branches.
  *
  * <h3>Copyright Notice</h3>
  *
  * GenOpt Copyright (c) 1998-2010, The Regents of the University of 
  * California, through Lawrence Berkeley National Laboratory (subject 
  * to receipt of any required approvals from the U.S. Dept. of Energy).  
  * All rights reserved.<p>
  *
  * If you have questions about your rights to use or distribute this software, 
  * please contact Berkeley Lab's Technology Transfer Department at TTD@lbl.gov.
  * <p>
  * 
  * NOTICE.  This software was developed under partial funding from the U.S. 
  * Department of Energy.  As such, the U.S. Government has been granted for 
  * itself and others acting on its behalf a paid-up, nonexclusive, 
  * irrevocable, worldwide license in the Software to reproduce, prepare 
  * derivative works, and perform publicly and display publicly. Beginning 
  * five (5) years after the date permission to assert copyright is obtained 
  * from the U.S. Department of Energy, and subject to any subsequent 
  * five (5) year renewals, the U.S. Government is granted for itself and 
  * others acting on its behalf a paid-up, nonexclusive, irrevocable, 
  * worldwide license in the Software to reproduce, prepare derivative 
  * works, distribute copies to the public, perform publicly and 
  * display publicly, and to permit others to do so.
  * 
  * <h3>License agreement</h3>
  *
  * GenOpt Copyright (c) 1998-2010, The Regents of the University of
  * California, through Lawrence Berkeley National Laboratory (subject 
  * to receipt of any required approvals from the U.S. Dept. of Energy).  
  * All rights reserved.
  * <p>
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions are met:
  * <p>
  * (1) Redistributions of source code must retain the above copyright notice, 
  * this list of conditions and the following disclaimer.
  * <p>
  * (2) Redistributions in binary form must reproduce the above copyright 
  * notice, this list of conditions and the following disclaimer in the 
  * documentation and/or other materials provided with the distribution.
  * <p>
  * (3) Neither the name of the University of California, Lawrence Berkeley 
  * National Laboratory, U.S. Dept. of Energy nor the names of its 
  * contributors may be used to endorse or promote products derived from 
  * this software without specific prior written permission.
  * <p>
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
  * <p>
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
  *
  * @author <A HREF="mailto:MWetter@lbl.gov">Michael Wetter</A>
  *
  * @version GenOpt(R) 3.0.3 (April 26, 2010)<P>
  */

/* Revision history:
 *******************
 2009, Nov.  6 wm Released version 3.0.2
 2009, Nov.  5 wm Added call to System.gc() in Optimizer.java before deleting 
                  the output and error files. Otherwise, Java may not release 
                  the files, and they cannot be deleted.
                  This problem happened on Windows only.
 2009, Oct. 29 wm Revised SimOutputFileHandler.java and ErrorChecker.java 
                  to use a BufferedReader, based on the suggestion of 
                  Andreas Edqvist Kissavos at Equa.
                  The old implementation required a long time to read IDA output file.
		  With the new implementation, reading an output from a file that 
                  is 10 times the length of a EPW file, the computing time 
                  has been reduced from 63 seconds to 0.6 seconds.
 2009, Aug. 14 wm Released version 3.0.1
 2009, Jul. 24 wm Bugfix: If the objective function delimiter was not found,
                  GenOpt went into a deadlock which prevented the error message
                  from being written.
 2009, Apr. 30 wm Updated to 3.0.0
 2009, Mar. 16 wm Parallelized initial function evaluations in InternalDivider.
                  Refactored code.
                  Changed Mesh to enable earlier drawing of graphical results.
 2009, Mar. 13 wm Fixed problem with SavePath. Now, if SavePath is a
                  relative path, then it is relative to the path
                  where the optimization initialization file is,
	          and not the directory from which GenOpt was started.
 2009, Feb. 23 wm Updated to 3.0.0 beta 2. (Fixed cfg files for Windows.)
 2009, Feb. 20 wm Updated to 3.0.0 beta 1
 2009, Jan  12 wm Updated to 3.0.0 alpha 4
 2009, Jan  08 wm Changed EquMesh. Now, also allowed are discrete parameters and 
                  continuous parameters with logarithmic spacing.
 2008, Dec  08 wm Added class ThreadedInputStream. This is needed to avoid a deadlock
                  if a simulation program writes a large amount of data to the 
                  error stream.
 2008, Dec  02 wm Changed Parametric so that also a single point can be simulated.
                  Fixed NullPointerException is Optimizer.
 2008, Dec  01 wm Converted EquMesh to parallel version
 2008, Nov  20 wm Updated to 3.0.0 alpha 3
 2008, Nov  14 wm In genopt.algorithm.util.pso.ModelPSO, replaced System.arraycopy with clone.
                  Using arraycopy resulted in wrong values for the local best points.
 2008, Nov  10 wm Implemented parallel computation.
 2008, Oct  24 wm Deleted algorithm HookeJeeves.java. This is replaced by GPSHookeJeeves.java
 2008, Oct  04 wm Deleted the experimental feature for smoothing in Optimizer.java, 
                  including the optimization algorithms that used this feature.
 2008, July 28 wm In FileHandler.java, rewrote constructor FileHandler(File).
                  The previous version added a new line at the end of the 
                  simulation input file, which caused problems in ESP-r. 
                  See email from Leen Peeters
                  In GenOpt.java, added System.out.flush() to ensure that
                  buffer is flushed.
 2008, June 30 wm In GPSPSOCCHJ, added code to report mininum point to output files.
 2008, May  10 wm Changed WinGenOpt,GenOpt and SimulationStarter so that the working directory
                  for the simulation is set to the directory of the optimization initialization
                  file. This allows to start GenOpt (e.g., for the examples) from directories 
                  other than the ones where the optimization initialization file is, without
                  providing the full path to the files.
 2004, Feb.  4 wm In GPSHJSmoothing, allowed to use zero step reduction on
                  non-smoothed cost function.
 2004, Jan.  5 wm Released GenOpt 2.0.0
 2003, Nov. 24 wm In FunctionEvaluator.evaluate(), added catch(IndexOutOfBoundsException e)
                  This exception is thrown for (invalid) functions of the form
                  Name = abc; where abc is any string.
 2003, Nov. 19 wm In ModelGPS, changed keyword 'Random' to 'Uniform'.
 2003, Nov. 12 wm In ModelGPS, changed precision control scheme from using
                  the psi function to using phi^alpha.
 2003, Oct. 27 wm Implemented multi-start option for GPS algorithms.
                  In Optimizer, removed the evaPoi.clear() statements
		  so that all points are stored and hence the multi-start
                  algorithm can reuse points.
 2003, Oct. 25 wm Implemented ModelGPS.reportMinimum(boolean) to prevent
                  reporting the minimum for algorithms that run
		  ModelGPS more than once.
 2003, Oct. 19 wm Added sufficient decrease condition to GPS algorithms.
 2003, Oct. 19 wm In Token.java, changed parsing of Double and Integer values
                  because these values, if enclosed in apostrophes, are in the
                  sval field and not in the nval field.
 2003, Sep. 25 wm Inserted calls to Optimizer.roundCoordinates(...) in
                  PSOCCMesh.getF(...), and fixed bug in setting point 
                  to feasible coordinate in the same method.
 2003, July    wm Compiled with Java 1.4.2. Updated wrong Javadoc info.
 2003, May     wm Implemented rounding for EquMesh and Parametric algorithm.
 2003, May     wm In GenOpt.java, rounded the discrete parameters to float
                  format if they are computed using min, max, and step.
 2003, May     wm Modified GenOpt.getOptimizer() such that sub classes of Optimizer
                  can have more than one constructor.
 2003, May     wm Added Process.destroy() in SimulationStarter due to
                  Java's Bug Id 4637504 and 4784692.
		  Otherwise, the system does not release its resources.
 2003, May     wm Added Particle Swarm Optimization algorithm on a mesh.
 2003, Apr.    wm Added Particle Swarm Optimization algorithms.
                  Implemented double integration as a class of Optimizer.java.
 2003, Apr.    wm Added DiscreteArmijoGradient.java.
                  Fixed bug in initialization of online chart.
                  In HookeJeeves.java, added increase of StepNumber such
                  that penalty functions can be implemented correctly.
 2003, Jan.    wm Added 'st.wordChars('%', '%');' in Token.java. Otherwise,
                  the entry %x1% is parsed as % in 'getStringValue, unless
                  the argument is enclosed in apostrophes.
		  Added 'skipJavaComment' in Token.getSectionStart(...).
 2002, Dec.    wm In Token.java, modified reading of Strings
                  such that numbers in exponential format are parsed
                  even if not enclosed in apostrophes.
 2002, Dec.    wm Implemented input function objects.
 2002, Dec.    wm In Token.java, modified reading of String such that
                  the tokens '" "' are returned as one blank.
 2002, Oct.    wm ResultManager: In searching for the minimum point, only
                  points of the main iterations are querried.
                  This change was necessairy to implement algorithms
                  that optimize a surrogate function.
 2002, Sept.   wm Implemented Point.areEqual(double, double)
 2002, August  wm Implemented a map that checks whether a point has
                  already been evaluated previously. If so, the 
                  function value of the previously evaluated point is
		  returned without doing a simulation.
		  Deleted the implementation of above method in the HookeJeeves
		  algorithm.
 2002, August  wm Made keyword PathN optional. If not specified,
                  the value of this variable is set to USERDIR.
 2002, August  wm Implemented FunctionEvaluator.
                  Modified FileHandler.java.
 2002, May     wm Update kernel to allow optimization involving discrete parameters.
 2002, April   wm Release bugfix 1.1.2
 2002, April   wm Introduced method 
                  Optimizer.postProcessObjectiveFunction(int, double[])
 2002, April   wm Modified JFileChooser call
 2002, April   wm Modified io.SimOutputFileHandler such that not only tab and
                  space character are delimiter after the objective function
                  value, but also the characters ;:,
                  This fix was required to read some EnergyPlus outputs.
 2001, Mar.    wm Ported to Sun's Java 1.3.0_02.
                  Changed constants access of StreamTokenizer. Now, class
                  name used rather than variable name (didn't work in
                  case statements).
 2001, Mar.    wm Introduced mandatory keyword StopAtError in EquMesh algorithm.
 2001, Feb.    wm Updated to version 2.0 alpha.
                  Added class Parametric. Modified error report to log file.
 2000, Dec.    wm Release bugfix 1.1.1
 2000, Dec.    wm Set entry of Prefix and Suffix to "" if the section CallParameter
                  is not specified. Otherwise, a null pointer exception occurs.
 2000, Nov.    wm Final release version 1.1
 2000, Oct.    wm Modified Hooke-Jeeves algorithm. Now, unfeasible points are
                  assigend f^i(x) = Double.MAX_VALUE for all i.
		  This gurantees convergence to a local minimizer under
		  conditions stated by Lewis/Torczon, SIAM
                  J. Optim. Vol. 9, No.4, pp. 1082-1099
 2000, Oct.    wm Fixed bug in Hooke-Jeeves algorithm concerning the TreeMap
                  (key is now cloned before it is put into the TreeMap,
		  and stepNumber=1 always in the TreeMap)
 2000, Aug.    wm Fixed bug in lang.ScientificFormat
 2000, Aug.    wm Replace HashMap with new Object OrderedMap, that stores
                  the objective function delimiters and names, since
                  HashMap does not guarantee that the order is preserved
 2000, July    wm Release 1.1 beta2
 2000, May     wm Keyword "SavePath" implemented.
 2000, May     wm Class io.GenOptFileFilter rewritten.
 2000, May        Release 1.1 beta1.
 2000, Jan.    wm implemented class Point.
                  Changed data management using class Point.
 1999, Sept.   wm Class EquMesh added.
 1999, July    wm Variable DEBUG introduced.
                  ExceptionHandling in GenOpt and WinGenOpt modified.
		  Display of GUI corrected.
 1999, June    wm GUI converted to Swing. Class Preference added.
 1999, Jun 01: wm FileOpen: setModal(false); removed in method
                  getFilePathAndName(Frame, String, String).
		  WinGenOpt.
		  Added Panel to Center of Container
		  to fix display problem with Microsoft Jview in Class
		  BorderLayout
 1999, Feb 16: wm Optimizer: if getF(double[]) has an IOException,
                  OptimizerException, or SimulationInputException
		  a second call is performed (unless in the first call).
		  This is due to IO problems observed under Windows NT4WS.
		  NelderMeadONeill: moving direction of simplex normalized
		  in order to get the cos(alpha) of the direction change
		  NelderMeadONeill: in "Partial inside contraction",
		  now (fNN < fX[w]) instead of (fNN <= fX[w]) to trap out
		  of nullspace of some vertices.
		  Multiple input files now possible.
 1999, Feb 12: wm Expansion of stepsize added in Perturber.
                  Minor changes in NelderMeadONeill algorithm (calculating
		  of number of worst point).
 1999, Jan 04: wm Update online graphic modified.
                  Class ResultChecker added.
 1998, Aug 25: wm Window Interface added.
 1998, Aug 11: wm FileHandler (after release of 1.2)
                  FileInputStream closed.
 1998, Jul 30: wm all classes
                  line break "\n" replaced by System.getProperty("line.separator").
		  Update to Windows Version.
 1998, Jul 20: wm ResultManager
                  Output now written in float instead of double format.
 1998, Jul 19: wm SimOutputFileHandler
                  Method getObjectiveFunctionValue() rewritten.
 1998, Jul 17: wm Optimizer
	          Free parameter now written as Float to Simulation input file
		  since Double field was to long for DOE 2.1e.
 1998, Jul 16: wm FileHandler
                  RandomAccessFile replaced with FileInputStream to eliminate
		  reading errors of programs compiled with MS Fortran 77
		  Powerstation for Windows NT 4.
 */

public class GenOpt extends Thread
{
    ///////////////////////////////////////////////////////////////////////
    // static data member

    /**
     * Line separator
     */
    private final static String LS = System.getProperty("line.separator");

    /**
     * Describe constant <code>VERSION_NUMBER</code> here.
     *
     */
    public final static String VERSION_NUMBER = "3.0.3";
    /**
     * Describe constant <code>VERSION_ID</code> here.
     *
     */
    public final static String VERSION_ID     = "";//"alpha1 or -rc3";
    //    public final static String VERSION_ID     = "beta2";//"alpha1 or -rc3";
                                                    
    /**
     * Describe constant <code>VERSION</code> here.
     *
     */
    public final static String VERSION =
	VERSION_NUMBER + VERSION_ID + ", April 26, 2010";
    /**
     * Describe constant <code>COPYRIGHT</code> here.
     *
     */
    public final static String COPYRIGHT =
	"GenOpt Copyright (c) 1998-2010, The Regents of the" + LS +
        "University of California, through Lawrence Berkeley" + LS +
        "National Laboratory (subject to receipt of any " + LS +
	"required approvals from the U.S. Dept. of Energy)." + LS +
        "All rights reserved.";

    /**
     * Describe constant <code>AUTHOR</code> here.
     *
     */
    public final static String AUTHOR =
	"Lawrence Berkeley National Laboratory" + LS +
	"http://simulationresearch.lbl.gov" + LS +
	"Michael Wetter, MWetter@lbl.gov";

    /**
     * Describe constant <code>ACKNOWLEDGMENT</code> here.
     *
     */
    public final static String ACKNOWLEDGMENT =
	"The development of GenOpt is supported by" + LS +
	"the U.S. Department of Energy (DOE)," + LS +
	"the Swiss Academy of Engineering Sciences (SATW)," + LS +
	"the Swiss National Energy Fund (NEFF), and" + LS +
	"the Swiss National Science Foundation (SNSF).";

    /**
     * Version string.
     *
     */
    public final static String VERSIONINFO = "GenOpt(R) " + VERSION;

    /**
     * Program information.
     *
     */
    public final static String PROGRAMINFO =
	VERSIONINFO + LS + LS + COPYRIGHT + LS + LS + ACKNOWLEDGMENT + LS + LS +
	"Developed by" + LS + AUTHOR;
    /**
     * Divider for output.
     *
     */
    public final static String DIVIDER =
	"_______________________________________________________________";
    /**
     * Describe constant <code>RUNHEADER</code> here.
     *
     */
    public final static String RUNHEADER =
	LS + PROGRAMINFO + LS;
    /*    public final static String RUNHEADER =
	LS + DIVIDER + LS +
	LS + PROGRAMINFO + LS + DIVIDER;
    */
    /**
     * Message that appears if the user stops GenOpt.
     *
     */
    public final static String USER_STOP_MESSAGE =
	"GenOpt stopped by a user interaction.";

    /**
     * Flag whether we are in debug mode or not.
     *
     */
    public static boolean DEBUG = false;

    /**
     * String containing the debug warning.
     *
     */
    public final static String DEBUG_WARNING ="Debug mode is on." + LS;

    /**
     * User directory (working directory).
     *
     */
    public String USERDIR = System.getProperty("user.dir", "");

    ///////////////////////////////////////////////////////////////////////
    /** allocates a new GenOpt Object containing nothing
	  */
    public GenOpt() {}

    ///////////////////////////////////////////////////////////////////////
    /**
     * allocates a new GenOpt Object
     * @param optIniFileName a <code>String</code> value
     * @param inpForExc refernce to InputFormatException. InputFormatException will be
     *        written in this Object
     * @param windowVersion reference to WinGenOpt Object or <CODE>null</CODE> pointer
     * @exception FileNotFoundException
     * @exception IOException
     */
    public GenOpt(String optIniFileName, InputFormatException inpForExc, WinGenOpt windowVersion)
	throws FileNotFoundException, IOException

    {
	wgo = windowVersion;
	if (wgo == null) // we run the console version
	    setupProperties();
	DEBUG = isDebug();
	if (DEBUG) println(GenOpt.DEBUG_WARNING);
	warMan = new WarningManager(this); // reference used to print warnings
	infMan = new InfoManager(this); // reference used to print infos

	int maxEquRes = 5; // default value, might be overwritten if specified by user

	stopGenOpt = false; // reset of stop flag
	int nErr = 0;     // counter for error messages

	// get optimization ini name
	initializeOptimizationIniName(optIniFileName);
	// place startDate here, otherwise we count the time
	// it takes to choose the initalization file from the GUI
	startDate = new Date();
	OrderedMap objFunDel = instantiateOptimizationIni(inpForExc);
	if (nErr < inpForExc.getNumberOfErrors()) return;
	// delete the log file if it already exists
	File lf = new File(optIniPat + File.separator + "GenOpt.log");
	try { lf.delete(); }
	catch (SecurityException e)
	    {
		inpForExc.setThrowable(e);
	    }

	instantiateErrChe_SimSta(inpForExc);

	instantiateFreePar_OptSet_ResChe_maiAlg(inpForExc, maxEquRes);

	if (nErr < inpForExc.getNumberOfErrors()) return;

	// check Delimiter if it has been found in the
	// ini file
	String fn = optIniPat + File.separator + optIniFilNam;

	if (objFunDel != null)
	    {	// value has been found
		// set warning if we have it already set
		if (OptIni.isObjectiveFunctionDelimiterSet())
		    infMan.setMessage(
				      "Section 'ObjectiveFunctionLocation' appears twice." + LS +
				      "  The one from '" + fn + "' will be used.");
		OptIni.setObjectiveFunctionLocation(objFunDel);
	    }
	else
	    {	// check whether we have the section specified before
		if (! OptIni.isObjectiveFunctionDelimiterSet())
		    inpForExc.setMessage(
					 "Section 'ObjectiveFunctionLocation' not found. " +
					 "Check 'ini' and 'cfg' file.");
	    }
	instantiateResultManager(inpForExc);
    } // end of constructor


    /**
     * gets an optimization initialization file chooser
     * @param IniStartUpFile a <code>File</code> value
     * @return a <code>JFileChooser</code> value
     */
    public static JFileChooser getInitializationFileChooser(File IniStartUpFile)
    {
	// Resolve relative paths. This is needed when the preference file
	// contains only the relative path, which happens when GenOpt is called
	// from the command line with the ini file name as argument
	File iniFil = IniStartUpFile.getAbsoluteFile(); 
	//construct file dialog
	JFileChooser fc = new JFileChooser();
	fc.setDialogTitle("Choose initialization file");
	fc.setMultiSelectionEnabled(false);

	fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
	GenOptFileFilter filter = new GenOptFileFilter("ini", "GenOpt Initialization Files");

	fc.addChoosableFileFilter(filter);
	fc.setFileFilter(filter);
	// set current file and directory
	try{
	    if (iniFil.isFile()){
		fc.setSelectedFile(iniFil);
	    }
	    else if (iniFil.isDirectory()){
		fc.setCurrentDirectory(iniFil);
	    }
	    else{
		fc.setCurrentDirectory( new File(System.getProperty("user.dir"))  );
	    }
	}
	catch(ArrayIndexOutOfBoundsException e){
	    // this exception is sometimes thrown
	    // it is not clear when that happens, since under the same conditions,
	    // the above code works sometimes, and sometimes not!!!
	    if (DEBUG) System.out.println("***** Caught ArrayIndexOutOfBoundsException *********");
	}
	return fc;
    }


    /** gets the initialization startup file
     *@return the initialization file startup file
     */
    protected File getIniStartUpFile()
    {
	// set it to the current working directory for the console version
	File fLast = new File((String)pref.get("file.ini.startUp"));
	if (fLast.isFile()){ // file still exists
	    String pare = fLast.getParent();
	    if ( pare.equals(  System.getProperty("user.dir") ))
		// are in same directory as last time
		return fLast;
	}
	return new File(System.getProperty("user.dir"));
    }

    ///////////////////////////////////////////////////////////////////////
    /** initializes the optimization ini file name and path
     * @param optIniFileName name of optimization ini file or null pointer
     * @exception IOException If an I/O error occurs, which is possible because the construction of the 
     *                        canonical pathname may require filesystem queries
     */
    private void initializeOptimizationIniName(String optIniFileName)
	throws IOException
    {
	// constructor for FileHandler Optimization ini File
	if (optIniFileName == null)
	    {
		//construct file dialog if no file name is passed
		JFileChooser fc = getInitializationFileChooser( getIniStartUpFile() );

		if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
		    {
			File iniFil = fc.getSelectedFile();
			optIniPat    = iniFil.getParent();
			optIniFilNam = iniFil.getName();
		    }
		else
		    {
			System.runFinalization();
			System.exit(0);
		    }
	    }
	else
	    {
		int lasSep = optIniFileName.lastIndexOf(File.separator);
		if ( lasSep == -1)
		    {
			optIniPat    = new String(System.getProperty("user.dir") );
			optIniFilNam = optIniFileName;
		    }
		else
		    {
			optIniPat    = optIniFileName.substring(0, lasSep);
			optIniFilNam = optIniFileName.substring(lasSep+1);
		    }
	    }
	optIniPat = (new File(optIniPat)).getCanonicalPath();
    }

    ///////////////////////////////////////////////////////////////////////
    /** makes an instance of OptimizationIni, and returns the objective function delimiters
     * @param inpForExc refernce to InputFormatException. InputFormatException will be
     *        written in this Object
     * @return a <CODE>OrderedMap</CODE> of the form <CODE>[[Name1, Delimiter1]
     *         , ... , [NameN, DelimiterN]]</CODE>
     *         if present or a null pointer otherwise
     * @exception FileNotFoundException
     * @exception IOException If the canonical path of the user directory cannot be obtained
     */
    private OrderedMap instantiateOptimizationIni(InputFormatException inpForExc)
	throws FileNotFoundException, IOException
    {
	int nErr = inpForExc.getNumberOfErrors();
	String fn = optIniPat + File.separator + optIniFilNam;
	USERDIR = (new File(optIniPat)).getCanonicalPath(); // the SimulationStarter will use this directory
	    // as the working directory


	StreamTokenizer optIniStrTok = new StreamTokenizer( new FileReader(fn) );

	// Entries for the OptimizationIni. This instance will not be returned.
	Hashtable<String, String> entries = new Hashtable<String, String>();
	String id; // an identifier (temporary) for setting the entries
	try{
	    Token.getSectionStart(optIniStrTok, "Simulation", inpForExc, fn);
	    Token.getSectionStart(optIniStrTok, "Files", inpForExc, fn);
	    Token.getSectionStart(optIniStrTok, "Template", inpForExc, fn);
	}
	catch(IOException e){
	    inpForExc.setThrowable(e);
	}
	//check for errors
	if (nErr < inpForExc.getNumberOfErrors()) return null;

	// section Template
	String[] val = new String[1];
	String[] key = {"File", "Path"};
	try{
	    val = null;
	    boolean[] req = {true, false};
	    val = getMultipleStringPairs(key, req,
					 optIniStrTok, inpForExc, fn);
	    if (nErr < inpForExc.getNumberOfErrors()) return null;
	    Token.moveToSectionEnd(optIniStrTok, inpForExc, fn);
	}
	catch(IOException e){
	    inpForExc.setThrowable(e);
	}
	if (nErr < inpForExc.getNumberOfErrors()) return null;

	final int nInpFil = val.length / 2;
	//int nFil = 4 + nInpFil;   // file + path counts as 1
	id = "Simulation.Files.Template.";
	for (int i = 0; i < nInpFil; i++)
	    for(int j = 0; j < key.length; j++){
		if ( (j == 1) && ( val[key.length*i+j] == null ))
		     val[key.length*i+j] = USERDIR; // default directory for files
		entries.put(id + key[j] + (i+1), val[key.length*i+j]);
	    }
	
	key = null;
	key = new String[3];
	val = new String[1];
	key[0] = "File"; key[1] = "Path"; key[2] = "SavePath";
	
	// section Input
	try{
	    boolean[] req = {true, false, false};
	    Token.getSectionStart(optIniStrTok, "Input", inpForExc, fn);
	    val = getMultipleStringPairs(key, req,
					 optIniStrTok, inpForExc, fn);
	    if (nErr < inpForExc.getNumberOfErrors()) return null;
	    Token.moveToSectionEnd(optIniStrTok, inpForExc, fn);
	}
	catch(IOException e){
	    inpForExc.setThrowable(e);
	}
	if (nErr < inpForExc.getNumberOfErrors()) return null;
	
	id = "Simulation.Files.Input.";
	// check that we do not have too many keywords specified
	// since method getMultiple.... increases counter
	// automatically
	if (val.length > key.length * nInpFil){
	    String em = "Too many keywords specified in section '" +
		"Simulation.Files.Input'";
	    Token.setError(optIniStrTok, inpForExc, em, fn);
	    if (nErr < inpForExc.getNumberOfErrors()) return null;
	}
	// make new array, val2, that has the length 3 * nInpFil
	// since the array val may be shorter if not all of the last
	// keywords are specified. Otherwise, we would have to catch
	// an ArrayIndexOutOfBoundsException
	String[] val2 = new String[key.length*nInpFil];
	for (int i = 0; i < val.length; i++){ // note upper bound is val.length
	    val2[i] = val[i];
	}

	for (int i = 0; i < nInpFil; i++){
	    for(int j = 0; j < key.length; j++){
		// if SavePath not specified, OK.
		// if Path not specified, set to USERDIR
		final int ind = key.length*i+j;
		if (val2[ind] == null){
		    if ( j == 1 )
			val2[ind] = USERDIR;
		    else if ( j == 2 )
			val2[ind] = "";
		}

		if (val2[ind] != null)
		    entries.put(id + key[j] + (i+1), val2[ind]);
		else{
		    String em = "Keyword '" + key[j] + (i+1) +
			"' not specified.";
		    Token.setError(optIniStrTok, inpForExc, em, fn);
		}
	    }
	}
	
	// get the other files (Log, Output, Configuration)
	String sec[] = {"Log", "Output", "Configuration"};
	key = null;
	key = new String[3];
	key[0] = "File"; key[1] = "Path"; key[2] = "SavePath";

	boolean[] req = {true, false, false};
	val = null;
	int secCou;
	int nLogFil = 0;
	int nOutFil = 0;
	id = new String("Simulation.Files.");
	for (secCou = 0; secCou < sec.length; secCou++){
	    try{
		val = null;
		Token.getSectionStart(optIniStrTok, sec[secCou], inpForExc, fn);
		val = getMultipleStringPairs(key, req,
					     optIniStrTok, inpForExc, fn);

		if (nErr < inpForExc.getNumberOfErrors()) return null;

		Token.moveToSectionEnd(optIniStrTok, inpForExc, fn);
	    }
	    catch(IOException e){
		inpForExc.setThrowable(e);
	    }
	    if (nErr < inpForExc.getNumberOfErrors()) return null;
	    // set the entries
	    int iKey = 0;
	    for (int iVal=0; iVal < val.length; iVal++){
		if ( val[iVal] == null && iKey == 1 )
		    val[iVal] = USERDIR;
		if (val[iVal] != null)
		    entries.put(id + sec[secCou] + "."
				+ key[iKey] + (iVal/key.length + 1), val[iVal]);
		if (iKey < (key.length-1) )
		    iKey++;
		else{
		    iKey = 0;
				// increase the number of Log and Output files
		    if (secCou == 0) nLogFil++;
		    if (secCou == 1) nOutFil++;
		}
	    }
	}
	// get out of the Files section
	try{
	    Token.moveToSectionEnd(optIniStrTok, inpForExc, fn);
	}
	catch(IOException e){
	    inpForExc.setThrowable(e);
	}
	if (nErr < inpForExc.getNumberOfErrors()) return null;
	
       	// check whether the section 'CallParameter' is present
	// and get its values
	String secKey = new String("CallParameter");

	key = null;
	key = new String[2];
	key[0] = "Prefix"; key[1] = "Suffix";
	val = null;
	val = new String[key.length];

	for (int iVal = 0; iVal < key.length; iVal++)
	    val[iVal] = ""; // need at least empty String

	try{
	    if (Token.isNextToken(optIniStrTok, secKey)){
		// section is present
		Token.getSectionStart(optIniStrTok, secKey, inpForExc, fn);
		Token.getStringValue(optIniStrTok, '=', ';',
				     key, val, inpForExc, fn, Token.PART);
		Token.moveToSectionEnd(optIniStrTok, inpForExc, fn);
		// set entries
		for (int iVal = 0; iVal < 2; iVal++)
		    entries.put("Simulation." + secKey + "." + key[iVal], val[iVal]);
	    }
	    else // section is not present (need to set empty String)
		for (int iVal = 0; iVal < 2; iVal++)
		    entries.put("Simulation." + secKey + "." + key[iVal], val[iVal]);
		
	}
	catch(IOException e){
	    inpForExc.setThrowable(e);
	}

	// check whether section ObjectiveFunctionLocation is present
	//////////////////////////////
	boolean objFunDelFound = false;
	OrderedMap objFunDel = null;
	secKey = new String("ObjectiveFunctionLocation");
	try{
	    if (Token.isNextToken(optIniStrTok, secKey)){
		// section is present. Get all values
		objFunDel = getObjectiveFunctionDelimiter(optIniStrTok,
							  inpForExc, fn);
		if (nErr < inpForExc.getNumberOfErrors())
		    return null;
		objFunDelFound = true;
	    }

	    // check if user set CallParameter after ObjectiveFunctionLocation
	    // (and tell him not to do so)
	    if (Token.isNextToken(optIniStrTok, "CallParameter")){
		Token.setError(optIniStrTok, inpForExc,
			       "'CallParameter' must not be specified after " +
			       "'ObjectiveFunctionLocation'.", fn);
	    }
	    if (nErr < inpForExc.getNumberOfErrors())
		return (objFunDelFound) ? objFunDel : null;
	    Token.moveToSectionEnd(optIniStrTok, inpForExc, fn);
	}
	catch(IOException e){
	    inpForExc.setThrowable(e);
	}
	//////////////////////////////
	// get the optimization section and files
	try{
	    Token.getSectionStart(optIniStrTok, "Optimization", inpForExc, fn);
	    Token.getSectionStart(optIniStrTok, "Files", inpForExc, fn);
	    Token.getSectionStart(optIniStrTok, "Command", inpForExc, fn);
	}
	catch(IOException e){
	    inpForExc.setThrowable(e);
	}
	if (nErr < inpForExc.getNumberOfErrors())
	    return (objFunDelFound) ? objFunDel : null;
	key = new String[2];
	key[0] = "File1"; key[1] = "Path1";
	val = new String[2];
	try{
	    Token.getStringValue(optIniStrTok, '=', ';',
				 key, val, inpForExc, fn, Token.PART);
	    Token.checkVariableSetting(optIniStrTok, inpForExc, "Optimization.Files.Command.File1",
				       val[0], fn);
	    if ( val[1] == null )
		val[1] = USERDIR;

	    for (int i=0; i < 3; i++)
		Token.moveToSectionEnd(optIniStrTok, inpForExc, fn);
	    // set entries
	    for (int iVal=0; iVal < key.length; iVal++){
		if ( val[iVal] == null )
		    val[iVal] = USERDIR;
		entries.put("Optimization.Files.Command." + key[iVal], val[iVal]);
	    }
	}
	catch(IOException e){
	    inpForExc.setThrowable(e);
	}
	if (nErr < inpForExc.getNumberOfErrors())
	    return (objFunDelFound) ? objFunDel : null;

	// substitute references
	key = null; val = null;
	key = new String[entries.size()]; // all keys
	val = new String[key.length];
	Enumeration<String> enu = entries.keys();

	for (int iVal = 0; iVal < key.length; iVal++){
	    key[iVal] = (String)(enu.nextElement());
	    val[iVal] = (String)(entries.get(key[iVal]));
	}
	try{
	    val = Assigner.assign(key, val);
	}
	catch (IllegalArgumentException e){
	    Token.setError(optIniStrTok, inpForExc, e.getMessage(), fn);
	}
	// set the resolved entries back to the hash table
	entries.clear();

	for (int iVal = 0; iVal < key.length; iVal++){
	    entries.put(key[iVal], val[iVal]);
	}

	String[] SimInpTemFilNam = new String[nInpFil];
	String[] SimInpTemPat    = new String[nInpFil];
	String[] SimInpFilNam    = new String[nInpFil];
	String[] SimInpPat       = new String[nInpFil];
	String[] SimInpSavPat       = new String[nInpFil];

	for(int i = 0; i < nInpFil; i++) {
	    SimInpTemFilNam[i] = (String)entries.get("Simulation.Files.Template.File" + (i+1));
	    SimInpTemPat[i]    = (String)entries.get("Simulation.Files.Template.Path" + (i+1));
	    SimInpFilNam[i]    = (String)entries.get("Simulation.Files.Input.File" + (i+1));
	    SimInpPat[i]       = (String)entries.get("Simulation.Files.Input.Path" + (i+1));
	    SimInpSavPat[i]       = (String)entries.get("Simulation.Files.Input.SavePath" + (i+1));
	}

	String[] SimLogFilNam = new String[nLogFil];
	String[] SimLogPat    = new String[nLogFil];
	String[] SimLogSavPat = new String[nLogFil];
	for(int i = 0; i < nLogFil; i++) {
	    SimLogFilNam[i] = (String)entries.get("Simulation.Files.Log.File" + (i+1));
	    SimLogPat[i]    = (String)entries.get("Simulation.Files.Log.Path" + (i+1));
	    SimLogSavPat[i] = (String)entries.get("Simulation.Files.Log.SavePath" + (i+1));
	    if (SimLogSavPat[i] == null)
		SimLogSavPat[i] = "";
	}

	String[] SimOutFilNam = new String[nOutFil];
	String[] SimOutPat    = new String[nOutFil];
	String[] SimOutSavPat = new String[nOutFil];
	for(int i = 0; i < nOutFil; i++) {
	    SimOutFilNam[i] = (String)entries.get("Simulation.Files.Output.File" + (i+1));
	    SimOutPat[i]    = (String)entries.get("Simulation.Files.Output.Path" + (i+1));
	    SimOutSavPat[i] = (String)entries.get("Simulation.Files.Output.SavePath" + (i+1));
	    if (SimOutSavPat[i] == null)
		SimOutSavPat[i] = "";
	}

	String SimConFilNam    = (String)entries.get("Simulation.Files.Configuration.File1");
	String SimConPat       = (String)entries.get("Simulation.Files.Configuration.Path1");
	String SimCalPre       = (String)entries.get("Simulation.CallParameter.Prefix");
	String SimCalSuf       = (String)entries.get("Simulation.CallParameter.Suffix");
	String OptComFilNam    = (String)entries.get("Optimization.Files.Command.File1");
	String OptComPat       = (String)entries.get("Optimization.Files.Command.Path1");

	// make instance of OptimizationIni
  	OptIni = new OptimizationIni(SimInpTemFilNam, SimInpTemPat,
  				     SimInpFilNam,     SimInpPat, SimInpSavPat,
  				     SimOutFilNam,     SimOutPat, SimOutSavPat,
  				     SimLogFilNam,     SimLogPat, SimLogSavPat,
  				     SimConFilNam,     SimConPat,
  				     optIniPat,
  				     OptComFilNam,     OptComPat,
  				     SimCalPre   ,     SimCalSuf);

	Token.isEndOfStream(optIniStrTok, inpForExc, fn);
	return (objFunDelFound) ? objFunDel : null;
    }

    ///////////////////////////////////////////////////////////////////////
    /** gets the entries of the objective function delimiter
     * @param st StreamTokenizer where pointer is set before the section start
     * @param ife refernce to InputFormatException. InputFormatException will be
     *        written in this Object
     * @param fn associated file name. Used for error report only
     * @exception IOException
     */
    private OrderedMap getObjectiveFunctionDelimiter(StreamTokenizer st, 
						     InputFormatException ife,
						     String fn) throws IOException
    {
	int nErr = ife.getNumberOfErrors();
	final String secKey = new String("ObjectiveFunctionLocation");
	Token.getSectionStart(st, secKey, ife, fn);
	final String[] key = {"Name", "Delimiter", "Function"};
	final boolean[] req = {true, false, false};
	String[] val;
	OrderedMap objFunDelLis = new OrderedMap(); // don't use Hashtable here

	    val = getMultipleStringPairs(key, req, st, ife, fn);
	    // return if error
	    if (nErr < ife.getNumberOfErrors())
		return null;
	    for(int iEnt = 0; iEnt < val.length / 3; iEnt++){
		// check that (i) name is set, and 
		// (ii) either Delimiter or Function is set, but not both
		if (val[0+3*iEnt] != null && val[0+3*iEnt].equals(""))
		    Token.setError(st, ife,
				   "Value of '" + key[0] + (iEnt+1) + 
				   "' must not be empty.", fn);
		
		
		if (val[1+3*iEnt] == null && val[2+3*iEnt] == null )
		    Token.setError(st, ife,
				   "Either '" + key[1] + (iEnt+1) + 
				   "' or '" + key[2] + (iEnt+1) + 
				   "' must be set. Currently, none is set.", 
				   fn);


		if (val[1+3*iEnt] != null && val[1+3*iEnt].length() > 0 && 
		    val[2+3*iEnt] != null && val[2+3*iEnt].length() > 0)
		    Token.setError(st, ife,
				   "Either '" + key[1] + (iEnt+1) + 
				   "' or '" + key[2] + (iEnt+1) + 
				   "' must be set, but not both. Currently, both are set.", 
				   fn);
		// set function to null if empty
		if (val[2+3*iEnt] != null && val[2+3*iEnt].trim().equals(""))
		    val[2+3*iEnt] = null;
		
		// check for uniqueness of entries, and that name is not empty
		if (val[0+3*iEnt] != null && objFunDelLis.containsKey(val[0+3*iEnt]))
		    Token.setError(st, ife,
				   "'" + key[0] + (iEnt+1) + 
				   "' must not have the same value as a previous entry.", fn);
		
		if (nErr == ife.getNumberOfErrors()){
		    objFunDelLis.put(val[0+3*iEnt], 
				     new ObjectiveFunctionLocation(val[0+3*iEnt],
								   val[1+3*iEnt],
								   val[2+3*iEnt]));
		}
	    }
	    if (nErr < ife.getNumberOfErrors())
		return null;

	    Token.moveToSectionEnd(st, ife, fn);
	
	    // solve the functions such that each function object is independent of other
	    // function objects
	    Object[] obj = objFunDelLis.getValues();
	    int[] ptr = new int[obj.length];
	    int nFun = 0;
	    for(int i = 0; i < obj.length; i++){
		if ( ((ObjectiveFunctionLocation)obj[i]).isFunction() ){
		    nFun++;
		    ptr[i] = i;
		}
		else
		    ptr[i] = -1;
	    }
	    String[] nam = new String[nFun];
	    String[] rep = new String[nFun];
	    String[] fun = new String[nFun];
	    int j = 0;
	    for(int i = 0; i < obj.length; i++){
		if ( ptr[i] != -1 ){
		    nam[j] = ((ObjectiveFunctionLocation)obj[i]).getName();
		    rep[j] = '%' + nam[j] + '%';
		    fun[j] = ((ObjectiveFunctionLocation)obj[i]).getFunction();
		    j++;

		}
	    }
	    fun = Assigner.assign(rep, fun);

	    j = 0;
	    for(int i = 0; i < obj.length; i++){
		if ( ptr[i] != -1 ){
		    objFunDelLis.setValue(i, new ObjectiveFunctionLocation(nam[j], null, fun[j]));
		    j++;
		}
	    }
	    
	    return objFunDelLis;
    }



    ///////////////////////////////////////////////////////////////////////
    /** makes an instance of ErrorChecker, SimulationStarter, and sets the data
     * member 'ObjectiveFunctionLocation' in the instance of OptimizationIni
     * @param inpForExc refernce to InputFormatException. InputFormatException will be
     *        written in this Object
     * @exception FileNotFoundException
     */
    private void instantiateErrChe_SimSta(InputFormatException inpForExc)
	throws FileNotFoundException
    {
	int nErr = inpForExc.getNumberOfErrors();
	String fn = new String(OptIni.getSimConPat() +
			       File.separator + OptIni.getSimConFilNam());

	try
	    {
		StreamTokenizer optCfgStrTok =
		    new StreamTokenizer(new FileReader(fn));
		instantiateErrorChecker(optCfgStrTok, inpForExc, fn);
		if (nErr < inpForExc.getNumberOfErrors()) return;
		instantiateIOSettings(optCfgStrTok, inpForExc, fn);
		if (nErr < inpForExc.getNumberOfErrors()) return;
		instantiateSimulationStarter(optCfgStrTok, inpForExc, fn);
		if (nErr < inpForExc.getNumberOfErrors()) return;
		// checks whether the section ObjectiveFunctionLocation is present
		String secKey = "ObjectiveFunctionLocation";

		if (Token.isNextToken(optCfgStrTok, secKey))
		    {	// section is present
			OrderedMap objFunDel = getObjectiveFunctionDelimiter(optCfgStrTok, inpForExc, fn);
			if (nErr < inpForExc.getNumberOfErrors()) return;
			if (objFunDel != null)
			    OptIni.setObjectiveFunctionLocation(objFunDel);
		    }
		Token.isEndOfStream(optCfgStrTok, inpForExc, fn);
	    }
	catch(FileNotFoundException e)
	    {
		String em = new String(
				       "Cannot find optimization configuration file: '" + fn + "'.");
		inpForExc.setMessage(em);
	    }
	catch(IOException e)
	    {
		inpForExc.setThrowable(e);
	    }
    }

    ///////////////////////////////////////////////////////////////////////
    /** gets the entries of the <CODE>keys</CODE> pair in a string array. The order of
     *  the keys in the file is arbitrary.
     * @param keys a pair of Strings where each String is a keyword for the entry
     *        to be read
     * @param required <CODE>true</CODE> if key must be specified, <CODE>false</CODE> if key
     *        is optional
     * @param st StreamTokenizer from which will be read
     * @param ife reference to InputFormatException. InputFormatException will be
     *        written in this Object
     * @param fn file name (including path) of file that is being read
     * @return a String array with all the entries that have been found in the order
     *         <CODE>[val1(key[1]), val1(key[2]), val2(key[1]), ....]</CODE>.
     *         If a key is not required and was not specified, then the current
     *         element is set to <CODE>null</CODE>
     */
    private String[] getMultipleStringPairs(String[] keys, boolean[] required, 
					    StreamTokenizer st,
					    InputFormatException ife, String fn)
    {
	assert (keys.length == required.length) : 
	    "Error in source code: keys.length != required.length";

	Hashtable<String, String> hash = new Hashtable<String, String>();
	ArrayList<String> keyWords = new ArrayList<String>();
	String key = new String("");
	String val = new String("");
	int curKeyNum = 1;
	int oldKeyNum = 0;
	try{
	    Token.skipJavaComments(st);
	    st.nextToken();
	    // check whether we are done
	    if (st.ttype == StreamTokenizer.TT_WORD){
		do{
		    key = new String(st.sval);
		    // update the valid keywords in the Hashtable
		    for (int i = 0; i < keys.length; i++){
			if (key.startsWith(keys[i])){
			    String suf = key.substring(keys[i].length());
			    try	{
				curKeyNum = Integer.parseInt(suf, 10);
				if ((curKeyNum - oldKeyNum) > 1 ||
				    curKeyNum < oldKeyNum)
				    throw new NumberFormatException("Wrong increment.");
			    }
			    catch(NumberFormatException e){
				Token.setError(st, ife,
					       "Unknown keyword '" + key + "'.", fn);
				st.pushBack();
				return null;
			    }
			    if (curKeyNum > oldKeyNum){
				oldKeyNum = curKeyNum;
				// fill the Hashtable to allow calling
				// program to check value settings
				// 2002-08-08 for (int j = 0; j < keys.length; j++)
				// 2002-08-08 hash.put(keys[j] + curKeyNum, "");
				for(int j = 0; j < keys.length; j++){
				    keyWords.add(keys[j] + curKeyNum);
				}
			    }
			    i = keys.length; // to get out
			} // end of if (key.startsWith(keys[i])
		    }
		    
		    // check whether it is a valid keyword
		    if (!(keyWords.contains(key))){
			Token.setError(st, ife,
				       "Unknown keyword '" + key + "'.", fn);
			st.pushBack();
			return null;
		    }
		    // key is valid
		    st.pushBack();
		    val = Token.getStringValue(st, '=', ';', key, ife, fn);
		    if ( hash.put(key, val) == null ){
			Token.skipJavaComments(st);
			st.nextToken();
		    }
		    else{
			Token.setError(st, ife,
				       "Keyword '" + key + "' specified twice.", fn);
			return null;
		    }
		} while (st.ttype == StreamTokenizer.TT_WORD);
	    }
	    else{ // we did not have any execution of the above section
		for(int j = 0; j < keys.length; j++){
		    keyWords.add(keys[j] + curKeyNum);
		}
	    }
	    if(st.ttype == '}'){
		// we are done, get values out of the Hashtable
		st.pushBack();
		final int nEnt = keyWords.size();
		int keyNum = 0;  // the number of the key, 0, 1, ... , keys.length-1
		String[] r = new String[keys.length * curKeyNum];
		int nCurEnt = 1;
		
		for (int i = 0; i < nEnt; i++){
		    if ( hash.containsKey( keys[keyNum] + nCurEnt ) )
			val = (String)(hash.get(keys[keyNum] + nCurEnt));
		    else
			val = null;
		    if ( val == null )
			if (required[keyNum]){ // no val obtained, but key required
			    Token.setError(st, ife,
					   "Required keyword '" + keys[keyNum] +
					   nCurEnt + "' not set.", fn);
			}
			else // no key obtained, and no key required
			    r[i] = null;
		    else // key obtained
			r[i] = new String(val);
		    if ( keyNum > (keys.length-2) ){
			keyNum = 0;
			nCurEnt++;
		    }
		    else
			keyNum++;
		}
		return r;
	    }
	    else if (st.ttype != StreamTokenizer.TT_WORD){
		String em = new String("String expected, got '");
		if (st.ttype == StreamTokenizer.TT_WORD)
		    em += st.sval;
		else if (st.ttype == StreamTokenizer.TT_NUMBER)
		    em += st.nval;
		else if (st.ttype == StreamTokenizer.TT_EOL)
		    em += "EOL";
		else if (st.ttype == StreamTokenizer.TT_EOF)
		    em += "EOF";
		else
		    em += (char)(st.ttype);
		em += "'.";
		Token.setError(st, ife, em, fn);
		st.pushBack();
		Token.moveToSectionEnd(st, ife, fn);
		return null;
	    }
	    else{
		Token.setError(st, ife, "Unknown keyword '" + st.ttype + "'.", fn);
		st.pushBack();
		Token.moveToSectionEnd(st, ife, fn);
		return null;
	    }
	}
	catch(IOException e){
	    ife.setThrowable(e);
	    return null;
	}
    }
    ///////////////////////////////////////////////////////////////////////
    /** makes an instance of ErrorChecker
     * @param optCfgStrTok StreamTokenizer of the optimization configuration file
     * @param inpForExc reference to InputFormatException. InputFormatException will be
     *        written in this Object
     * @param fn file name (including path) of optimization configuration file
     */
    private void instantiateErrorChecker(StreamTokenizer optCfgStrTok, InputFormatException inpForExc,
					 String fn)
    {
	int nErr = inpForExc.getNumberOfErrors();
	try
	    {
		Token.getSectionStart(optCfgStrTok, "SimulationError", inpForExc, fn);
	    }
	catch(IOException e)
	    {
		inpForExc.setThrowable(e);
	    }
	//check for error
	if (nErr < inpForExc.getNumberOfErrors()) return;

	boolean stay;
	String[] ts = new String[1]; // temporary String
	int i = 0;
	String kwErrMes = new String("ErrorMessage");
	String[] errInd = new String[1];
	do
	    {
		stay = false;
		try
		    {
			errInd[i] = Token.getStringValue(optCfgStrTok, '=', ';', kwErrMes,
							 inpForExc, fn);
		    }
		catch(IOException e)
		    {
			inpForExc.setThrowable(e);
		    }

		// scan next entry
		try
		    {
			Token.skipJavaComments(optCfgStrTok);
			optCfgStrTok.nextToken();
		    }
		catch(IOException e)
		    {
			inpForExc.setThrowable(e);
		    }

		if (optCfgStrTok.ttype == StreamTokenizer.TT_WORD) // we got again a word
		    {
			if (optCfgStrTok.sval.equals(kwErrMes))   // and the right one
			    {
				stay = true;
				ts =  new String[i+1];
				System.arraycopy(errInd, 0, ts, 0, i+1);
				errInd = new String[i+2];
				System.arraycopy(ts, 0, errInd, 0, i+1);
				i++;
			    }
		    }
		optCfgStrTok.pushBack();
	    } while(stay);
	// check whether at least one entry of kwErrMes is set
	if (errInd[0] == null)
	    Token.variableNotSet(optCfgStrTok, inpForExc, kwErrMes, fn);

	try
	    {
		Token.moveToSectionEnd(optCfgStrTok, inpForExc, fn);
	    }
	catch(IOException e)
	    {
		inpForExc.setThrowable(e);
	    }

	//check for error
	if (nErr < inpForExc.getNumberOfErrors()) return;

	ErrChe = new ErrorChecker(errInd);
    }

    ///////////////////////////////////////////////////////////////////////
    /** sets the accuracy of the file writing (number format)
     * @param optCfgStrTok StreamTokenizer of the optimization configuration file
     * @param inpForExc reference to InputFormatException. InputFormatException will be
     *        written in this Object
     * @param fn file name (including path) of optimization configuration file
     */
    private void instantiateIOSettings(StreamTokenizer optCfgStrTok,
				       InputFormatException inpForExc,
				       String fn)
    {
	int nErr = inpForExc.getNumberOfErrors();
	String numFor = new String();
	try
	    {
		Token.getSectionStart(optCfgStrTok, "IO", inpForExc, fn);
		if (nErr < inpForExc.getNumberOfErrors())
		    return;
		numFor = Token.getStringValue(optCfgStrTok, '=', ';',
					      "NumberFormat", inpForExc, fn);

		Token.moveToSectionEnd(optCfgStrTok, inpForExc, fn);
	    }
	catch(IOException e)
	    {
		inpForExc.setThrowable(e);
	    }
	String[] expVal = {"Double", "Float"};
	boolean inpOK = false;
	int i = 0;
	do{
	    if (numFor.equals(expVal[i]))
		inpOK = true;
	}while((!inpOK) && (++i < expVal.length));
	if (!inpOK)
	    {
		String em = "Expected ";
		for (i = 0; i < expVal.length-1; i++)
		    em += "\"" + expVal[i] + "\", ";
		em += "or \""  + expVal[i] + "\", received \"" + numFor + "\".";
		Token.setError(optCfgStrTok, inpForExc, em, fn);
	    }

	if (nErr < inpForExc.getNumberOfErrors())
	    return;
	// Constructor IOSettings
	ioSet = new IOSettings(numFor);

    }

    ///////////////////////////////////////////////////////////////////////
    /** makes an instance of SimulationStarter
     * @param optCfgStrTok StreamTokenizer of the optimization configuration file
     * @param inpForExc refernce to InputFormatException. InputFormatException will be
     *        written in this Object
     * @param fn file name (including path) of optimization configuration file
     */
    void instantiateSimulationStarter(StreamTokenizer optCfgStrTok, InputFormatException inpForExc,
				      String fn)
    {
	int nErr = inpForExc.getNumberOfErrors();
	String comEnt = new String();
	boolean proFilExt = false;
	try
	    {
		Token.getSectionStart(optCfgStrTok, "SimulationStart", inpForExc, fn);
		comEnt = Token.getStringValue(optCfgStrTok, '=', ';',
					      "Command", inpForExc, fn);
		proFilExt = Token.getBooleanValue(optCfgStrTok, '=', ';',
						  "WriteInputFileExtension", inpForExc, fn);
		Token.moveToSectionEnd(optCfgStrTok, inpForExc, fn);
	    }
	catch(IOException e)
	    {
		inpForExc.setThrowable(e);
	    }

	if (nErr < inpForExc.getNumberOfErrors())
	    return;

	// Constructor SimulationStarter
	try{ 
	    SimSta = new SimulationStarter(comEnt, proFilExt, USERDIR, OptIni); 
	}
	catch(IOException e){
	    inpForExc.setThrowable(e);
	}
    }

    ///////////////////////////////////////////////////////////////////////
    /** makes an instance of FreePar, OptimizationSettings, and
	  *   ResultChecker
	  * @param inpForExc refernce to InputFormatException. InputFormatException will be
	  *        written in this Object
	  * @param maxEqualResults default value for maximum number of equal result
	  *            before GenOpt terminates with an error
	  * @exception FileNotFoundException
	  * @exception IOException
	  */
    private void instantiateFreePar_OptSet_ResChe_maiAlg(InputFormatException inpForExc,
							 int maxEqualResults) throws FileNotFoundException, IOException
    {
	int nErr = inpForExc.getNumberOfErrors();
	String fn = new String(OptIni.getOptComPat() +
			       File.separator + OptIni.getOptComFilNam());

	StreamTokenizer optComStrTok = 
	    new StreamTokenizer( new FileReader(new String(fn)) );

	instantiateFreePar(optComStrTok, inpForExc, fn);
	if (nErr < inpForExc.getNumberOfErrors()) return;

	instantiateOptimizationSettings_ResultChecker(optComStrTok, inpForExc,
						      fn, maxEqualResults);
	if (nErr < inpForExc.getNumberOfErrors()) return;

	// get main algorithm name
	Token.getSectionStart(optComStrTok, "Algorithm", inpForExc, fn);
	maiAlg = Token.getStringValue(optComStrTok, '=', ';', "Main",
				      inpForExc, fn);
    }


    ///////////////////////////////////////////////////////////////////////
    /** makes all instances of FreePar and input functions
     * @param optComStrTok StreamTokenizer of the optimization command file
     * @param inpForExc refernce to InputFormatException. InputFormatException will be
     *        written in this Object
     * @param fn file name (including path) of optimization command file
     */
    private void instantiateFreePar(StreamTokenizer optComStrTok,
				    InputFormatException inpForExc, String fn)
    {
	final int nErr = inpForExc.getNumberOfErrors();
	try{
	    Token.getSectionStart(optComStrTok, "Vary", inpForExc, fn);
	}
	catch(IOException e){
	    inpForExc.setThrowable(e);
	}
	
	// check for error
	if (nErr < inpForExc.getNumberOfErrors()) return;

	boolean getPar = false;
	boolean getFun = false;
	dimCon = 0; // number of continuous parameters
	dimDis = 0; // number of discrete parameters
	dimInpFun = 0; // number of input functions
	conPar = new ContinuousParameter[0];
	disPar = new DiscreteParameter[0];
	inpFun = new InputFunction[0];
	
	do{
	    if ( getPar )
		getParameterInput(optComStrTok, inpForExc, fn);
	    else if (getFun)
		getFunctionInput(optComStrTok, inpForExc, fn);
	    else{
		try{ optComStrTok.nextToken(); }
		catch(IOException e){
		    inpForExc.setThrowable(e);
		}
		if (nErr < inpForExc.getNumberOfErrors()) return;
	    }
	    //check for error
	    if (nErr < inpForExc.getNumberOfErrors()) return;
	    // determine next entry
	    if (optComStrTok.ttype == StreamTokenizer.TT_WORD){ // we got again a word
		if (optComStrTok.sval.equals("Parameter")){   // and the right one
		    optComStrTok.pushBack();
		    getPar = true;
		    getFun = false;
		}
		else if(optComStrTok.sval.equals("Function")){   // and the right one
		    optComStrTok.pushBack();
		    getFun = true;
		    getPar = false;
		}
	    }
	    else{
		    getFun = false;
		    getPar = false;
	    }
	} while(getPar || getFun);

	if (optComStrTok.ttype != '}') // check if section is closed correctly
	    Token.setErrorWrongToken(optComStrTok, "}", inpForExc, fn);

	// count references and then 
	// assign links to other function objects in function objects
	String[] nam = new String[dimInpFun];
	String[] rep = new String[dimInpFun];
	String[] fun = new String[dimInpFun];
	for(int i = 0; i < dimInpFun; i++){
	    nam[i] = inpFun[i].getName();
	    rep[i] = '%' + nam[i] + '%';
	    fun[i] = inpFun[i].getFunction();
	}
	// make sure the function names are unique, and count references
	for(int i = 0; i < dimInpFun; i++){
	    for(int k = 0; k < dimInpFun; k++){
		// check that function name is not used for other function
		if ( i != k && nam[i].equals(nam[k]) ) {
		    final String em = "Function name '" + nam[k] +
			"' is used more than once.";
		    inpForExc.setMessage(em);
		}
		// check that function does not reference itself
		if ( fun[i].indexOf(rep[k]) != -1 ){
		    if ( i != k ){
			inpFun[k].increaseReferenceCounter();
		    }
		    else{
			final String em = "Function '" + nam[k] +
			    " = " + fun[k] + "' references itself.";
			inpForExc.setMessage(em);
		    }
		}
	    }
	    // check that function name is not used for other variable
	    for( int l = 0; l < dimCon; l++)
		if ( nam[i].equals( conPar[l].getName() ) ){
		    final String em = "Function name '" + nam[i] +
			"' is used for a continuous parameter. Name must be unique.";
		    inpForExc.setMessage(em);
		}
	    for( int l = 0; l < dimDis; l++)
		if ( nam[i].equals( disPar[l].getName() ) ){
		    final String em = "Function name '" + nam[i] +
			"' is used for a discrete parameter. Name must be unique.";
		    inpForExc.setMessage(em);
		}
	    
	    // check for error
	    if (nErr < inpForExc.getNumberOfErrors()) return;
	}
	int[] refCou = new int[dimInpFun];
	for(int i = 0; i < dimInpFun; i++)
	    refCou[i] = inpFun[i].getReferenceCounter();

	fun = Assigner.assign(rep, fun);
	inpFun = null;
	inpFun = new InputFunction[dimInpFun];
	for(int i = 0; i < dimInpFun; i++){
	    inpFun[i] = new InputFunction(nam[i], fun[i], refCou[i]);
	}
    }
    ///////////////////////////////////////////////////////////////////////
    /** makes an instance of a function input.
     *  This function must be called when the keyword "Function" is found in
     *  the input file.
     * @param optComStrTok StreamTokenizer of the optimization command file
     * @param inpForExc refernce to InputFormatException. InputFormatException will be
     *        written in this Object
     * @param fn file name (including path) of optimization command file
     */
    private void getFunctionInput(StreamTokenizer optComStrTok,
				  InputFormatException inpForExc, String fn){
	int nErr = inpForExc.getNumberOfErrors();
	final int nParCom = 2;
	
	String[] varVal = new String[nParCom];
	final String[] key1 = { "Name", "Function" };
	
	for (int i = 0; i < nParCom; i++) 
	    varVal[i] = new String("");
	
	try{
	    Token.getSectionStart(optComStrTok, "Function", inpForExc, fn);
	    Token.getStringValue(optComStrTok, '=', ';',
				 key1, varVal, inpForExc, fn, Token.ALL);
	    Token.moveToSectionEnd(optComStrTok, inpForExc, fn);
	}
	catch(IOException e){
	    inpForExc.setThrowable(e);
	}
	
	if (nErr < inpForExc.getNumberOfErrors()) return;
	
	// parse input
	dimInpFun++;
	    
	////////////////////////////////////
	// increase memory
	InputFunction[] fe = new InputFunction[dimInpFun-1];
	System.arraycopy(inpFun, 0, fe, 0, dimInpFun-1);
	inpFun = new InputFunction[dimInpFun];
	System.arraycopy(fe, 0, inpFun, 0, dimInpFun-1);
	// parse values
	inpFun[dimInpFun-1] = new InputFunction(varVal[0], varVal[1], 0);
	////////////////////////////////////////////////////
	try{
	    Token.skipJavaComments(optComStrTok);
	    optComStrTok.nextToken();
	}
	catch(IOException e){
	    inpForExc.setThrowable(e);
	}
    }
    ///////////////////////////////////////////////////////////////////////
    /** makes an instance of a free Parameter.
     *  This function must be called when the keyword "Parameter" is found in
     *  the input file.
     * @param optComStrTok StreamTokenizer of the optimization command file
     * @param inpForExc refernce to InputFormatException. InputFormatException will be
     *        written in this Object
     * @param fn file name (including path) of optimization command file
     */
    private void getParameterInput(StreamTokenizer optComStrTok,
				   InputFormatException inpForExc, String fn){
	int nErr = inpForExc.getNumberOfErrors();
	final int nParCom = 7;
	
	String[] varVal = new String[nParCom];
	String[] key1 = new String[nParCom];
	key1[0] = "Name";
	key1[1] = "Min";
	key1[2] = "Ini";
	key1[3] = "Max";
	key1[4] = "Step";
	key1[5] = "Type";
	key1[6] = "Values";
	
	for (int i = 0; i < nParCom; i++) 
	    varVal[i] = new String("");
	
	try{
	    Token.getSectionStart(optComStrTok, "Parameter", inpForExc, fn);
	    Token.getStringValue(optComStrTok, '=', ';',
				 key1, varVal, inpForExc, fn, Token.PART);
	    Token.moveToSectionEnd(optComStrTok, inpForExc, fn);
	}
	catch(IOException e){
	    inpForExc.setThrowable(e);
	}
	
	if (nErr < inpForExc.getNumberOfErrors()) return;
	
	Token.checkVariableSetting(optComStrTok, inpForExc, key1[0], varVal[0], fn);
	// set default values
	
	
	if ( varVal[4].length() != 0 && varVal[5].length() == 0 ) // Step spec., Type empty
	    varVal[5] = "CONTINUOUS";
	if ( varVal[6].length() != 0 && varVal[5].length() == 0 ) // Values spec., Type empty
	    varVal[5] = "SET";
	// In any case, either Step or Values must be specified
	if ( varVal[4].length() == 0 && varVal[6].length() == 0 ){
	    String em = "Parameter '" + varVal[0] + "', keyword '" + key1[4] + "' or '" +
		key1[6] + "' must be specified.";
	    Token.setError(optComStrTok, inpForExc, em, fn);
	    // make a generic parameter so we can test the other parameters
	    varVal[1] = "9999";
	    varVal[2] = "99999";
	    varVal[3] = "999999";
	    varVal[4] = "999";
	    varVal[5] = "CONTINUOUS";
	    varVal[6] = "";
	    nErr = inpForExc.getNumberOfErrors(); // local error counter
	}
	// parse continuous variables
	if ( varVal[5].length() == 0 || // default is continuous
	     varVal[5].compareToIgnoreCase("CONTINUOUS") == 0){
	    dimCon++;
	    // set optional values
	    if (varVal[1].length() == 0) varVal[1] = new String("SMALL");
	    if (varVal[3].length() == 0) varVal[3] = new String("BIG");
	    // check mandatory values
	    Token.checkVariableSetting(optComStrTok, inpForExc, 
				       varVal[0], key1[2], varVal[2], fn);
	    Token.checkVariableSetting(optComStrTok, inpForExc, 
				       varVal[0], key1[4], varVal[4], fn);
	    // check that not allowed values are empty
	    Token.checkVariableSettingEmpty(optComStrTok, inpForExc, 
					    varVal[0], key1[6], varVal[6], fn);
	    // in case of error, make generic entries to keep going
	    if ( nErr < inpForExc.getNumberOfErrors() ){
		varVal[1] = "SMALL";
		varVal[2] = "99999";
		varVal[3] = "BIG";
		varVal[4] = "99999";
		varVal[5] = "CONTINUOUS";
		varVal[6] = "";
		nErr = inpForExc.getNumberOfErrors(); // local error counter
	    }
	    
	    ////////////////////////////////////
	    // increase memory
	    ContinuousParameter[] cPar = new ContinuousParameter[dimCon];
	    System.arraycopy(conPar, 0, cPar, 0, dimCon-1);
	    conPar =  new ContinuousParameter[dimCon];
	    System.arraycopy(cPar, 0, conPar, 0, dimCon-1);
	    // parse values
	    conPar[dimCon-1] = parseContinuousPar(varVal, inpForExc, 
						  optComStrTok.lineno(), fn);
	}
	////////////////////////////////////////
	else if (varVal[6].length() != 0 || // Values has an entry, or
		 varVal[5].compareToIgnoreCase("SET") == 0){ // Type=SET specified
	    // increase memory
	    dimDis++;
	    DiscreteParameter[] dPar = new DiscreteParameter[dimDis];
	    System.arraycopy(disPar, 0, dPar, 0, dimDis-1);
	    disPar =  new DiscreteParameter[dimDis];
	    System.arraycopy(dPar, 0, disPar, 0, dimDis-1);
	    // parse values
	    disPar[dimDis-1] = parseDiscretePar(optComStrTok, key1, varVal, 
						inpForExc, fn);
	}
	else{ // have neither CONTINUOUS nor SET. Write error.
	    String em = "Parameter '" + varVal[0] + "', keyword '" + key1[5] + 
		"', 'CONTINUOUS' or 'SET' expected. Received '" + varVal[5] + "'.";
	    Token.setError(optComStrTok, inpForExc, em, fn);
	}
	if (nErr < inpForExc.getNumberOfErrors()) return;
	////////////////////////////////////////////////////
	// check that parameter names are unique
	TreeSet<String> ts = new TreeSet<String>();
	for(int i = 0; i < conPar.length; i++)
	    if ( ! ts.add(conPar[i].getName() ) ){
		String em = "Parameter name '" + conPar[i].getName() + "' is not unique.";
		Token.setError(optComStrTok, inpForExc, em, fn);
	    }
	for(int i = 0; i < disPar.length; i++)
	    if ( ! ts.add(disPar[i].getName() ) ){
		String em = "Parameter name '" + disPar[i].getName() + "' is not unique.";
		Token.setError(optComStrTok, inpForExc, em, fn);
	    }


		     
		
	////////////////////////////////////////////////////
	
	try{
	    Token.skipJavaComments(optComStrTok);
	    optComStrTok.nextToken();
	}
	catch(IOException e){
	    inpForExc.setThrowable(e);
	}
    }

    ///////////////////////////////////////////////////////////////////////
    /** makes an instance of OptimizationSettings and ResultChecker
     * @param optComStrTok StreamTokenizer of the optimization command file
     * @param inpForExc refernce to InputFormatException. InputFormatException will be
     *        written in this Object
     * @param fn file name (including path) of optimization command file
     * @param maxEqualResults default value for maximum number of equal result
     *            before GenOpt terminates with an error
     */
    private void instantiateOptimizationSettings_ResultChecker(StreamTokenizer optComStrTok,
							       InputFormatException inpForExc, String fn, int maxEqualResults)
    {
	int nErr = inpForExc.getNumberOfErrors();
	int numOfEnt = 4;
	String[] key = new String[numOfEnt];
	String[] val    = new String[numOfEnt];

	key[0] = "MaxIte";
	key[1] = "MaxEqualResults";
	key[2] = "WriteStepNumber";
	key[3] = "UnitsOfExecution";

	for (int i = 0; i < numOfEnt; i++)
	    val[i] = new String("");

	// get the section start
	try
	    {
		Token.getSectionStart(optComStrTok, "OptimizationSettings", inpForExc, fn);
	    }
	catch(IOException e)
	    {
		inpForExc.setThrowable(e);
		//check for error
		if (nErr < inpForExc.getNumberOfErrors()) return;
	    }

	try
	    {
		Token.getStringValue(optComStrTok, '=', ';',
				     key, val, inpForExc, fn, Token.PART);
		if (val[1].length() == 0) val[1] = new Integer(maxEqualResults).toString();
		if (val[3].length() == 0) val[3] = new Integer(0).toString();
		Token.checkVariableSetting(optComStrTok, inpForExc, key, val, fn);
		Token.moveToSectionEnd(optComStrTok, inpForExc, fn);
	    }
	catch(IOException e)
	    {
		inpForExc.setThrowable(e);
	    }
	//check for error
	if (nErr < inpForExc.getNumberOfErrors()) return;

	// parse result
	int maxIte = parseInteger(optComStrTok,	key[0], val[0],
				  0, Integer.MAX_VALUE, inpForExc, fn);
	if (maxIte < 1)
	    {
		String em = "'" + key[0] + "' must be greater than 0.";
		Token.setError(optComStrTok, inpForExc, em, fn);
	    }
	int maxEquRes = parseInteger(optComStrTok, key[1], val[1],
				     0, Integer.MAX_VALUE, inpForExc, fn);
	if (maxEquRes < 2)
	    {
		String em = "'" + key[2] + "' must be greater than 1.";
		Token.setError(optComStrTok, inpForExc, em, fn);
	    }
	boolean wriSteNum = parseBoolean(optComStrTok, key[2], val[2],
					 inpForExc, fn);

	int uniOfExe = parseInteger(optComStrTok, key[3], val[3],
				    0, Integer.MAX_VALUE, inpForExc, fn);

	//check for error
	if (nErr < inpForExc.getNumberOfErrors()) return;

	OptSet = new OptimizationSettings(maxIte, wriSteNum, uniOfExe);
	resChe = new ResultChecker(maxEquRes);
    }

    /** parses a String to a boolean an writes error into the InputFormatException
	  * @param st the StreamTokenizer
	  * @param keyWord the keyWord associated with the value <CODE>val</CODE>
	  * @param value the value received for <CODE>keyWord</CODE>
	  * @param inpForExc refernce to InputFormatException. InputFormatException will be
	  *        written in this Object
	  * @param fileName name of the file where input comes from
	  */
    private static boolean parseBoolean(StreamTokenizer st, String keyWord, String value,
					InputFormatException inpForExc,	String fileName)
    {
	boolean r = false;
	if (value.equals("true"))
	    r = true;
	else if (value.equals("false"))
	    r = false;
	else
	    setWrongClassTypeInputFormatException(st,
						  keyWord, value, inpForExc, new Boolean(false), fileName);
	return r;
    }

    /** parses a String to an int and writes error into the InputFormatException
	  * @param st the StreamTokenizer
	  * @param keyWord the keyWord associated with the value <CODE>val</CODE>
	  * @param value the value received for <CODE>keyWord</CODE>
	  * @param minVal minimal allowed value
	  * @param maxVal maximal allowed value
	  * @param inpForExc refernce to InputFormatException. InputFormatException will be
	  *        written in this Object
	  * @param fileName name of the file where input comes from
	  * @return the <CODE>int</CODE> value of <CODE>val</CODE>
	  */
    private static int parseInteger(StreamTokenizer st, String keyWord,	String value,
				    int minVal, int maxVal, InputFormatException inpForExc,
				    String fileName)
    {
	int r = 0;
	try{
	    r = Integer.parseInt(value);
	    if (r < minVal)
		Token.setError(st, inpForExc,
			       keyWord + " out of range." + LS + 
			       "   Received '" + r +
			       "', minimal allowed is '" + minVal + 
			       "'.", fileName);
	    if (r > maxVal)
		Token.setError(st, inpForExc,
			       keyWord + " out of range." + LS + 
			       "   Received '" + r +
			       "', maximal allowed is '" + maxVal + 
			       "'.", fileName);
	}
	catch(NumberFormatException e){
	    setWrongClassTypeInputFormatException(st, keyWord, value, inpForExc, 
						  new Integer(0), fileName);
	}
	return r;
    }

    /** parses a String to an int and writes error into the InputFormatException
	  * @param st the StreamTokenizer
	  * @param variableName the variable name associated with the value <CODE>val</CODE>
	  * @param keyWord the keyWord associated with the value <CODE>val</CODE>
	  * @param value the value received for <CODE>keyWord</CODE>
	  * @param minVal minimal allowed value
	  * @param maxVal maximal allowed value
	  * @param inpForExc refernce to InputFormatException. InputFormatException will be
	  *        written in this Object
	  * @param fileName name of the file where input comes from
	  * @return the <CODE>int</CODE> value of <CODE>val</CODE>
	  */
    private static int parseInteger(StreamTokenizer st, String variableName, String keyWord, 
				    String value, int minVal, int maxVal, 
				    InputFormatException inpForExc,
				    String fileName){
	String varAndKey = "Parameter '" + variableName + "', keyword '" + keyWord + "'";
	return parseInteger(st, varAndKey, value, minVal, maxVal, inpForExc, fileName);
    }

    /** parses a String to a double and writes error into the InputFormatException
	  * @param st the StreamTokenizer
	  * @param variableName the variable name associated with the value <CODE>val</CODE>
	  * @param keyWord the keyWord associated with the value <CODE>val</CODE>
	  * @param value the value received for <CODE>keyWord</CODE>
	  * @param minVal minimal allowed value
	  * @param maxVal maximal allowed value
	  * @param inpForExc refernce to InputFormatException. InputFormatException will be
	  *        written in this Object
	  * @param fileName name of the file where input comes from
	  * @return the <CODE>double</CODE> value of <CODE>val</CODE>
	  */
    private static double parseDouble(StreamTokenizer st, String variableName, String keyWord, 
				    String value, double minVal, double maxVal, 
				    InputFormatException inpForExc,
				    String fileName){
	String varAndKey = "Parameter '" + variableName + "', keyword '" + keyWord + "'";
	return parseDouble(st, varAndKey, value, minVal, maxVal, inpForExc, fileName);
    }


    /** parses a String to a double and writes error into the InputFormatException
	  * @param st the StreamTokenizer
	  * @param keyWord the keyWord associated with the value <CODE>val</CODE>
	  * @param value the value received for <CODE>keyWord</CODE>
	  * @param minVal minimal allowed value
	  * @param maxVal maximal allowed value
	  * @param inpForExc refernce to InputFormatException. InputFormatException will be
	  *        written in this Object
	  * @param fileName name of the file where input comes from
	  */
    private static double parseDouble(StreamTokenizer st, String keyWord, String value,
				      double minVal, double maxVal, 
				      InputFormatException inpForExc,
				      String fileName)
    {
	double r = 0;
	try{
	    r = Double.parseDouble(value);
	    if (r < minVal)
		Token.setError(st, inpForExc,
			       keyWord + " out of range." + LS + 
			       "   Received '" + r +
			       "', minimal allowed is '" + minVal + 
			       "'.", fileName);
	    if (r > maxVal)
		Token.setError(st, inpForExc,
			       keyWord + " out of range." + LS + 
			       "   Received '" + r +
			       "', maximal allowed is '" + minVal + 
			       "'.", fileName);
	}
	catch(NumberFormatException e){
	    setWrongClassTypeInputFormatException(st, keyWord, value, inpForExc, 
						  new Double(0), fileName);
	}
	return r;
    }

    /** sets an error message in the InputFormatException instance indicating that a wrong
	  * data type has been received
	  * @param st the StreamTokenizer
	  * @param keyWord the keyWord associated with the value <CODE>val</CODE>
	  * @param value the value received for <CODE>keyWord</CODE>
	  * @param inpForExc refernce to InputFormatException. InputFormatException will be
	  *        written in this Object
	  * @param wrapperClass any of the wrapper class of the primitive Java type
	  * @param fileName name of the file where input comes from
	  */
    private static void setWrongClassTypeInputFormatException(StreamTokenizer st,
							      String keyWord,	String value, InputFormatException inpForExc,
							      Object wrapperClass, String fileName)
    {
	if ( ! keyWord.startsWith("'") )
	    keyWord = "'" + keyWord + "'";
	String	em = new String(fileName) + "(Line " + st.lineno() +
	    "): InputFormatException: " + wrapperClass.getClass().getName() +
	    " expected for " +	keyWord + ", got '" + new String(value) + "'.";
	inpForExc.setMessage(em);
    }
    ///////////////////////////////////////////////////////////////////////
    /** makes an instance of ResultManager
     * @param inpForExc refernce to InputFormatException. InputFormatException will be
     *        written in this Object
     */
    private void instantiateResultManager(InputFormatException inpForExc){

	ObjectiveFunctionLocation[] objFunLoc = OptIni.getFunctionObjects();
	int nF = objFunLoc.length;
	String[] nameF = new String[nF];
	for (int i = 0; i < nF; i++)
	    nameF[i] = objFunLoc[i].getName();

	String outputHeader = new String("GenOpt(R) Version " + VERSION + LS + LS);
	outputHeader += AUTHOR + LS + LS + COPYRIGHT + LS;
	outputHeader += DIVIDER + LS + LS;
	outputHeader += "Results of " + maiAlg + " algorithm" + LS;
	outputHeader += "Start time: " + getStartDate().toString() ;

	try{
	    ResMan = new ResultManager(this, OptIni.getOptComPat(), 
				       outputHeader, nameF, conPar, disPar);
	    if (wgo != null)
		wgo.initializeSeries(ResMan);
	}
	catch (IOException e) {
	    inpForExc.setThrowable(e);
	}
    }
    ///////////////////////////////////////////////////////////////////////
    /** gets a new instance of a ContinuousParameter Object based on the value in 'val'.
     *
     *@param val String array with following elements:<CODE>
     *           val[0] = Name; val[1] = Min ; val[2] = Ini;
     *           val[3] = Max ; val[4] = Step;
     *@param e reference to InputFormatException. Error messages are written into
     *         this Object
     *@param lineNumber line number to which the error report will be referred to
     *@param fileName name of file (for error report only) or <CODE>null</CODE> pointer
     *@return the ContinuousParameter Object
     */
    private static ContinuousParameter parseContinuousPar(String[] val, InputFormatException e,
					int lineNumber, String fileName)
    {
	String fn;
	if (fileName != null)
	    fn = new String(fileName);
    else
        fn = new String("");
	String em;
	boolean undBouRes, uppBouRes;
	double min;
	double ori;
	double max;
	double oriSte;
	int res;
	// check the name
	if (val[0].length() == 0)
	    {	// name field is empty
		em = new String(fn) + new String
		    ("(Line " + lineNumber +
		     "): InputFormatException: Parameter name not specified.");
		e.setMessage(em);
		ContinuousParameter fp = new ContinuousParameter("error", 
								 new Double(Double.NEGATIVE_INFINITY).doubleValue(),
					 0, new Double(Double.POSITIVE_INFINITY).doubleValue(), .999, 0);
		return fp; // we have junk anyway
	    }

	// set under boundary
	if (val[1].compareToIgnoreCase("SMALL") == 0){
	    undBouRes = false;
	    min = new Double(Double.NEGATIVE_INFINITY).doubleValue();
	}
	else{
	    undBouRes = true;
	    try { min = new Double(val[1]).doubleValue(); }
	    catch(NumberFormatException nfe){
		em = new String(fn) + 
		    new String("(Line " + lineNumber +
			       "): InputFormatException: 'SMALL' or numerical value expected " +
			       "for minimum of parameter '" + new String(val[0]) + "'.");
		e.setMessage(em);
		min = new Double(Double.NEGATIVE_INFINITY).doubleValue();
	    }   
	}

	// set original start value
	try { ori = new Double(val[2]).doubleValue(); }
	catch(NumberFormatException nfe){
	    em = new String(fn) + new String
		("(Line " + lineNumber +
		 "): InputFormatException: Numerical value expected " +
		 "for initial of parameter '" + new String(val[0]) + "'.");
	    e.setMessage(em);
	    ori = 0;
	}

	//set upper boundary
	if (val[3].compareToIgnoreCase("BIG") == 0){
	    uppBouRes = false;
	    max = new Double(Double.POSITIVE_INFINITY).doubleValue();
	}
	else{
	    uppBouRes = true;
	    try { max = new Double(val[3]).doubleValue(); }
	    catch(NumberFormatException nfe){
		em = new String(fn) + new String
		    ("(Line " + lineNumber +
		     "): InputFormatException: 'BIG' or numerical value expected " +
		     "for maximum of parameter '" + new String(val[0]) + "'.");
		e.setMessage(em);
		max = new Double(Double.POSITIVE_INFINITY).doubleValue();
	    }
	}

	//set original step size
	try { oriSte = new Double(val[4]).doubleValue(); }
	catch(NumberFormatException nfe){
	    em = new String(fn) + new String
		("(Line " + lineNumber +
		 "): InputFormatException: Numerical value expected " +
		 "for step size of parameter '" + new String(val[0]) + "'.");
	    e.setMessage(em);
	    oriSte = new Double(Double.POSITIVE_INFINITY).doubleValue();
	}

	// set kind of variable
	if (undBouRes == false && uppBouRes == false)
	    res = 1;
	else if (undBouRes == true  && uppBouRes == false)
	    res = 2;
	else if (undBouRes == true  && uppBouRes == true )
	    res = 3;
	else
	    res = 4;

	// set all the data members
	ContinuousParameter fp = new ContinuousParameter(val[0], min, ori, max, oriSte, res);
	return fp;
    }


    ///////////////////////////////////////////////////////////////////////
    /** gets a new instance of a DiscreteParameter Object.
     *
     *@param optComStrTok StreamTokenizer of the optimization command file
     *@param key String array with the keywords:<CODE>
     *           key[0] = Name; key[1] = Min ; key[2] = Ini;
     *           key[3] = Max ; key[4] = Step; key[5] = Type;
     *           key[6] = Values;
     *@param val String array the values for <CODE>key</CODE>
     *@param inpForExc reference to InputFormatException. Error messages are written into
     *         this Object
     *@param fileName name of file (for error report only)
     *@return the ContinuousParameter Object
     */
    private static DiscreteParameter parseDiscretePar(StreamTokenizer optComStrTok,
						      String[] key, 
						      String[] val, 
						      InputFormatException inpForExc, 
						      String fileName){
	int nErr = inpForExc.getNumberOfErrors();
	////////////////////////////////////
	// if Values has an entry, Type could potentially be set to any junk.
	// Make sure it is set to SET
	if ( val[6].length() != 0 ){
	    //////////////////////////////////////////////////////////////
	    // Values has an entry
	    //////////////////////////////////////////////////////////////
	    // check that optional keyword is set to the right value

	    if ( val[5].length() > 0 )
		Token.checkAdmissableValue(optComStrTok, inpForExc, 
					   val[0], key[5], val[5], "SET", fileName);
	    // check that Min and Max are empty
	    Token.checkVariableSettingEmpty(optComStrTok, inpForExc, 
					    val[0], key[1], val[1], fileName);
	    Token.checkVariableSettingEmpty(optComStrTok, inpForExc, 
					    val[0], key[3], val[3], fileName);
	    Token.checkVariableSettingEmpty(optComStrTok, inpForExc, 
					    val[0], key[4], val[4], fileName);
	    
	    if (nErr == inpForExc.getNumberOfErrors()) {
		// parse Values into a string array
		// count the number of elements
		int nEle = 1;
		for (int iPos = 1; iPos < val[6].length(); iPos++)
		    if (val[6].charAt(iPos) == ',') nEle++;
		String[] Values = new String[nEle];
		int iStart = 0;
		for(int i = 0; i < nEle; i++){
		    int iEnd = val[6].indexOf(',', iStart);
		    if (iEnd == -1)
			Values[i] = val[6].substring(iStart);
		    else
			Values[i] = val[6].substring(iStart, iEnd);
		    iStart = iEnd+1;
		}
		// ini is a 1-based index in the input file!!!
		int ini = parseInteger(optComStrTok, val[0], key[2], val[2],
				       1, Values.length, inpForExc, fileName)-1;
		if (nErr == inpForExc.getNumberOfErrors())
		    return new DiscreteParameter(val[0], ini, Values);
		else // had an error
		    return new DiscreteParameter(); // empty constructor
	    }
	    else // had an error
		return new DiscreteParameter(); // empty constructor
	}
	//////////////////////////////////////////////////////////////
	else{
	    //////////////////////////////////////////////////////////////
	    // Values has no entry
	    //////////////////////////////////////////////////////////////
	    // check that Min, Max, Ini and Step are set
	    Token.checkVariableSetting(optComStrTok, inpForExc, 
				       val[0], key[1], val[1], fileName);
	    Token.checkVariableSetting(optComStrTok, inpForExc, 
				       val[0], key[2], val[2], fileName);
	    Token.checkVariableSetting(optComStrTok, inpForExc, 
				       val[0], key[3], val[3], fileName);
	    Token.checkVariableSetting(optComStrTok, inpForExc, 
				       val[0], key[4], val[4], fileName);
	    // check that set is specified
	    Token.checkAdmissableValue(optComStrTok, inpForExc, 
				       val[0], key[5], val[5], "SET", fileName);
	    // parse values if no errors, otherwise keep generic values to keep going
	    double min = 9999;
	    double max = 999999;
	    int step = 1;
	    if ( nErr == inpForExc.getNumberOfErrors() ){
		min = parseDouble(optComStrTok, val[0], key[1], val[1],
					 -Double.MAX_VALUE, Double.MAX_VALUE, 
					 inpForExc, fileName);
		//////////////
		step = parseInteger(optComStrTok, val[0], key[4], val[4],
					-Integer.MAX_VALUE, Integer.MAX_VALUE, 
					inpForExc, fileName);
		//////////////
		max = parseDouble(optComStrTok, val[0], key[3], val[3],
					 -Double.MAX_VALUE, Double.MAX_VALUE, 
					 inpForExc, fileName);
	    }
	    //////////////
	    if ((min - max) == 0){
		    Token.setError(optComStrTok, inpForExc, "Parameter '" + val[0] +
				   "': Value for 'Min' and 'Max' are equal.",
				   fileName);
		max = min + 10.; // to keep going here
	    }
	    // now, we can construct the required set.
	    if ( step < 0 ){
		if (min <= 0.) { 
		    Token.setError(optComStrTok, inpForExc, "Parameter '" + val[0] +
				   "': Value for 'Min' must be bigger than zero " +
				   "because 'Step' is negative." + 
				   "   Received '" + min + "'.", fileName);
		    min = 10.; // to keep going here
		}
		if (max <= 0.) {
		    Token.setError(optComStrTok, inpForExc, "Parameter '" + val[0] +
				   "': Value for 'Max' must be bigger than zero " +
				   "because 'Step' is negative." + 
				   "   Received '" + max + "'.", fileName);
		    max = 2*min; // to keep going here
		}
	    }
	    if ( nErr == inpForExc.getNumberOfErrors() ){
		double[] numValues = new double[Math.abs(step)+1];
		numValues = Fun.getSpacing(step, min, max);
		// round the values to float format
		for(int i = 0; i < numValues.length; i++)
		    numValues[i] = Double.parseDouble( Float.toString( (float)numValues[i] ) );
		String[] Values = new String[Math.abs(step)+1];
		for (int i = 0; i < Values.length; i++)
		    Values[i] = String.valueOf(numValues[i]);
		
		// ini is a 1-based index in the input file!!!
		int ini = parseInteger(optComStrTok, val[0], key[2], val[2],
				       1, Values.length, inpForExc, fileName)-1;
	    
		if ( nErr == inpForExc.getNumberOfErrors() )
		    return new DiscreteParameter(val[0], ini, Values);
		else // had an error
		    return new DiscreteParameter( );
	    }
	    else // had an error
		return new DiscreteParameter( );
	    //////////////////////////////////////////////////////////////
	}
    }

    ///////////////////////////////////////////////////////////////////////
    /**
     * gets the name of the main algorithm
     * @return a <code>String</code> value
     */
    public String getMainAlgorithm() { return new String(maiAlg); }

    ///////////////////////////////////////////////////////////////////////
    /**
     * gets a StreamTokenizer whereas its pointer points to the start of
     * the <CODE>Algorithm</CODE> section<BR>
     * The pointer in the StreamTokenizer is moved beyond the section start
     * character ('{'). Text that occurs before the keyWord 'Algorithm' is skipped
     * without an error message
     * @return a <code>StreamTokenizer</code> value
     * @exception InputFormatException
     * @exception IOException
     */
    public StreamTokenizer getAlgorithmSection()
	throws InputFormatException, IOException
    {
	InputFormatException e = new InputFormatException();
	StreamTokenizer r = getAlgorithmSection(e);
	if (e.getNumberOfErrors() > 0)
	    throw e;
	return r;
    }

    ///////////////////////////////////////////////////////////////////////
    /**
     * gets a StreamTokenizer whereas its pointer points to the start of
     * the <CODE>Algorithm</CODE> section<BR>
     * The pointer in the StreamTokenizer is moved beyond the section start
     * character ('{'). Text that occurs before the keyWord 'Algorithm' is skipped
     * without an error message
     * @param e reference to InputFormatException. Error messages are written into
     *         this Object
     * @return a <code>StreamTokenizer</code> value
     * @exception IOException
     */
    public StreamTokenizer getAlgorithmSection(InputFormatException e)
	throws IOException
    {
	// get the section start "Algorithm" in the command file
	String comFilNam = new String(OptIni.getOptComPat() +
				      File.separator + OptIni.getOptComFilNam());

	StreamTokenizer r =
	    new StreamTokenizer(new FileReader(comFilNam));

	Token.spoolTo(r, "Algorithm", e, comFilNam);

	Token.getSectionStart(r, "Algorithm",
			      e, comFilNam);

	return r;
    }

    ///////////////////////////////////////////////////////////////////////
    /** writes the log file <CODE>GenOpt.log</CODE> to
     * the log directory.<BR>
     * <B>Note:</B> Use this method only if an error occured
     * @param errorMessage the error message
     */
    public void writeLogFile(String errorMessage)
    {
	String em = new String(getRunInfo() + LS + LS);

	em += "Optimization terminated with error.";
	em += LS + LS + "Error message:" + LS + "**************" + LS + errorMessage;
	try
	    {
		if (warMan.getNumberOfMessages() > 0)
		    em += LS + LS + "Warning messages:" + LS +"*****************" +
			LS + warMan.getMessages();

		if (infMan.getNumberOfMessages() > 0)
		    em += LS + LS + "Info messages:" + LS + "**************" +
			LS + infMan.getMessages();
	    }
	catch(NullPointerException e) {}

	flushLogFile(em);
    }
    ///////////////////////////////////////////////////////////////////////
    /** writes the log file <CODE>GenOpt.log</CODE> to
     * the log directory.<BR>
     * <B>Note:</B> Use this method only if no error occured
     */
    public void writeLogFile()
    {
	String em = new String(getRunInfo() + LS + LS);

	em += "Optimization completed successfully." + LS;

	if (warMan.getNumberOfMessages() > 0)
	    em += LS + LS + "Warning messages:" + LS +"*****************" +
		LS + warMan.getMessages();

	if (infMan.getNumberOfMessages() > 0)
	    em += LS + LS + "Info messages:" + LS + "*****************" +
		LS + infMan.getMessages();

	flushLogFile(em);
    }
    ///////////////////////////////////////////////////////////////////////
    /** writes physically the log file <CODE>GenOpt.log</CODE> to
     * the log directory.<BR>
     * <B>Note:</B> Use this method only if no error occured
     * @param content the content of the file
     */
    private void flushLogFile(String content)
    {
	try
	    {
		FileHandler.writeFile(content, optIniPat, "GenOpt.log");
	    }
	catch(IOException e)
	    {
		System.err.println("IOException: Cannot write log file.");
		if (DEBUG) printStackTrace(e);
		System.err.println(content);
	    }
    }

    /** gets the optimization ini file
	  *@return the optimization ini file
	  */
    public File getOptimizationIniFile()
    {
	return (optIniPat != null && optIniFilNam != null) ?
	    new File(optIniPat + File.separator + optIniFilNam) : null;
    }

    ///////////////////////////////////////////////////////////////////////
    /** gets the GenOpt run info for output report
     * @return the GenOpt run info
     */
    protected String getRunInfo()
    {
	String me = "GenOpt(R) Optimization Version " + VERSION + LS + LS;
	me += AUTHOR + LS + LS + COPYRIGHT + LS + LS + ACKNOWLEDGMENT + LS ;
	me += DIVIDER + LS + LS;
	try
	    {
		if (optIniPat != null && optIniFilNam != null)
		    me += "Configuration file : " +
			optIniPat + File.separator + optIniFilNam + LS;
		me += "Command file       : " +
		    OptIni.getOptComPat() + File.separator +
		    OptIni.getOptComFilNam() + LS + LS;
		me += "Optimization started  at : " + getStartDate().toString() + LS;
		Date endDate = new Date();
		me += "Optimization finished at : " + endDate.toString() + LS;
		me += "Execution time           :            " + getRuntime(endDate);
	    }
	catch (NullPointerException e) { }

	me += LS + DIVIDER;
	return me;
    }

    ///////////////////////////////////////////////////////////////////////
    /**
     * gets the current runtime of the optimization in the format <CODE>xhh:mm:ss</CODE>
     * @param now a <code>Date</code> value
     * @return the current runtime
     */
    public String getRuntime(Date now)
    {
	long dt = Math.round((double)(now.getTime() - startDate.getTime())/ 1000) ; // in seconds
	long h = dt / 3600;
	dt -= h * 3600;
	long m = dt / 60;
	dt -= m * 60;
	long s = dt;
	String r = new String();
	r =  (h < 10) ? ("0" + new Long(h).toString()) : new Long(h).toString();
	r+= ":";
	r += (m < 10) ? ("0" + new Long(m).toString()) : new Long(m).toString();
	r+= ":";
	r += (s < 10) ? ("0" + new Long(s).toString()) : new Long(s).toString();
	return r;
    }

    ///////////////////////////////////////////////////////////////////////
    /** gets the start date of the optimization
     *@return the date when the optimization started
     */
    public Date getStartDate() { return startDate; }

    ///////////////////////////////////////////////////////////////////////
    /**
     * reports a new optimization trial to the GUI
     */
    public void setNewTrial()
    {
	if (wgo != null)
	    wgo.setNewTrial();
    }

    ///////////////////////////////////////////////////////////////////////
    /** prints a message to the output device without finishing the line
     * @param text the text to be printed
     */
    public void print(String text)
    {
	if (wgo != null)
	    wgo.append(text);
	else{
	    System.out.print(text);
	    System.out.flush(); // added 2008-07-28 to ensure buffer is flushed
	}
	return;
    }

    ///////////////////////////////////////////////////////////////////////
    /** prints a message to the output device, and then finishs the line
     * @param text the text to be printed
     */
    public void println(String text)
    {
	print(text + LS);
	return;
    }

    ///////////////////////////////////////////////////////////////////////
    /** prints an error to the output device
     * @param text the text to be printed
     */
    public void printError(String text)
    {
	if (wgo != null)
	    wgo.printError(text);
	else
	    System.err.print(text);
    }

    ///////////////////////////////////////////////////////////////////////
    /** runs the optimization method
     */
    public void run()
    {
	int flag = 0;
	String errMes = new String("");
	try
	    {
		try{ flag = this.getOptimizer().run(); }
		catch(InvocationTargetException e) { throw e.getTargetException(); }
		exiFla = 0;
	    }

	catch(ClassNotFoundException e)
	    {
		if (DEBUG) printStackTrace(e);
		errMes = e.getClass().getName() + ": " + LS +
		    e.getMessage();
		errMes += LS + "Check whether file '" + e.getMessage()
		    + ".class' is in the correct path.";
		exiFla = 1;
	    }
	catch (NoConvergenceException e)
	    {
		errMes = e.getClass().getName() + ": " + LS +
		    e.getMessage();
		exiFla = 1;
	    }
	catch (OptimizerException e)
	    {
		if (errMes.indexOf(USER_STOP_MESSAGE) == -1 && DEBUG)
		    printStackTrace(e);
		errMes = e.getClass().getName() + ": " + LS +
		    e.getMessage();
		exiFla = 1;
	    }
	catch (Throwable t)
	    {
		if (DEBUG) printStackTrace(t);
		errMes = t.getClass().getName() + ": " + LS +
		    t.getMessage();
		exiFla = 1;
	    }
	if (exiFla != 0)
	    flag = 0; // we got an Exception

	switch (flag){
	case 0:
		// Exception in optimization
		errMes += LS + LS;
		if (errMes.indexOf(USER_STOP_MESSAGE) == -1)
		    errMes += "GenOpt terminated with error." + LS;
		printError(errMes  + 
			   "See logfile for further information." + LS);
		writeLogFile(errMes);
		break;
	case -1:
		// Maximum number of iteration exceeded
		errMes += LS + new
		    String("Maximum number of iteration exceeded." + LS +
			   "GenOpt terminated.");
		    printError(errMes);
		    writeLogFile(errMes + LS);
		    break;
	    case 1:
		    println("Required accuracy is reached." + LS +
			    "GenOpt completed successfully.");
		    writeLogFile();
		    break;
	    case 2:
		    println("Absolute accuracy is reached." + LS +
			    "GenOpt completed successfully.");
		    writeLogFile();
		    break;
	    case 3:
		    println("Relative accuracy is reached." + LS +
			    "GenOpt completed successfully.");
		    writeLogFile();
		    break;
	    case 4:
		// for parametric runs
		println("GenOpt completed successfully.");
		writeLogFile();
		break;
	    }
	if (wgo != null )
	    wgo.finalizeOptimization();
	else
	    pref.write();
	return;
    }

    ///////////////////////////////////////////////////////////////////////
    /** forces GenOpt to stop the optimization after the current simulation
     */
    public void stopOptimization() { stopGenOpt = true; }

    ///////////////////////////////////////////////////////////////////////
    /**
     * suspends GenOpt if <CODE>susp</CODE> is <CODE>true</CODE>, otherwise
     * resume GenOpt
     * @param susp a <code>boolean</code> value
     */
    public void sleepGenOpt(boolean susp) { suspend = susp; }

    /** returns a flag whether GenOpt must be stopped after the current simulation
	  *@return <CODE>true</CODE> if GenOpt has to be stopped, <CODE>false</CODE>
	  *        otherwise
	  */
    public synchronized boolean mustStopOptimization()
    {
	try {
	    if (suspend) {
		synchronized(this){
		    while (suspend)
			sleep(1000);
		}
	    }
	}
	catch (InterruptedException e) {  }
	return stopGenOpt;
    }

    ///////////////////////////////////////////////////////////////////////
    /** gets the number of the continuous parameters
     *@return the number of continuous parameters
     */
    public int getDimensionContinuous() { return dimCon; }

    /** gets the number of discrete parameters
     *@return the number of discrete parameters
     */
    public int getDimensionDiscrete() { return dimDis; }

    ///////////////////////////////////////////////////////////////////////
    /** gets a new instance of the Optimizer object
     */
    private Optimizer getOptimizer()
	throws InstantiationException, ClassNotFoundException, IllegalAccessException,
	       InvocationTargetException, InputFormatException, IOException {
	final String maiAlg = this.getMainAlgorithm();

	final Class cl = Class.forName("genopt.algorithm." + maiAlg);
	Class[] arg = new Class[1];
	arg[0] = Class.forName("genopt.GenOpt");
	try{
	    final Constructor co = cl.getConstructor( arg );
	    Object[] ob = new Object[1];
	    ob[0] = this;
	    final Optimizer o = (Optimizer)co.newInstance(ob);
	    o.goToEndOfCommandFile(); // to ensure that all the parameters are parsed properly
	    return o;
	}
	catch(NoSuchMethodException nsme){
	    final String em = new String("Error in Optimization Code: Class '" +
					 maiAlg + 
					 "' must have a constructor with argument 'genopt.GenOpt'.");
	    InstantiationException e = new InstantiationException(em);
	    throw e;   
	}
    }

    ///////////////////////////////////////////////////////////////////////
    /** gets the exit flag of the optimization run
     *@return the exit flag of the optimization run
     */
    public int getExitFlag() { return exiFla; }

    ///////////////////////////////////////////////////////////////////////
    /** prints a stackTrace to the output and error stream
     *@param t Throwable
     */
    public static void printStackTrace(Throwable t)
    {
	t.printStackTrace(); // to error stream
	if (System.out != System.err)
	    t.printStackTrace(System.out);
    }

    /** sets up the properties */
    private void setupProperties()
    {
	String home = System.getProperty("user.home");
	String fs   = System.getProperty("file.separator");
	File file = new File(home + fs + ".genopt"
		+ GenOpt.VERSION_NUMBER + GenOpt.VERSION_ID + fs + "properties.txt");
	pref = new Preference(file);
    }

    /** gets the separator, as specified in properties.txt
     *@return the separator
     */
    public String getSeparator(){
	if (wgo == null){
	    return (String)pref.get("simulation.result.separator");
	}
	else
	    return wgo.getSeparator();
		
    }

    /** checks whether we should run in debug mode
     *@return <CODE>true</CODE> if we run in debug mode, <CODE>false</CODE> otherwise
     */
    public boolean isDebug(){
	if (wgo == null){
	    String deb = (String)pref.get("debug");
	    return deb.equals("true");
	}
	else
	    return wgo.isDebug();
    }


    ///////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////
    /** reference to WinGenOpt */
    protected WinGenOpt wgo;
    /** dimension of the continuous parameters */
    protected int dimCon;
    /** dimension of the discrete parameters */
    protected int dimDis;
    /** dimension of the input functions */
    protected int dimInpFun;
    /** warning manager */
    public WarningManager warMan;
    /** info manager */
    public InfoManager infMan;
    /** continuous parameters */
    public ContinuousParameter conPar[];
    /** discrete parameters */
    public DiscreteParameter disPar[];
    /** input functions */
    public InputFunction inpFun[];
    /** optimization initialization */
    public OptimizationIni OptIni;
    /** optimization settings */
    public OptimizationSettings OptSet;
    /** io settings */
    public IOSettings ioSet;
    /** simulation starter */
    public SimulationStarter SimSta;
    /** error checker */
    public ErrorChecker ErrChe;
    /** result manager */
    public ResultManager ResMan;
    /** result Checker */
    public ResultChecker resChe;
    /** name of the main algorithm */
    private String maiAlg;
    /** date when the optimization started */
    protected Date startDate;
    /** path of optimization ini file */
    private String optIniPat;
    /** name of optimization ini file */
    private String optIniFilNam;
    /** exit flag of the optimization */
    private static int exiFla = 999999;
    /** flag to indicate whether GenOpt has to be stopped
	  * after the current simulation */
    private static boolean stopGenOpt;
    /** flag to indicate whether GenOpt has to be suspended */
    private static volatile boolean suspend = false;
    /** user preference */
    private Preference pref;
    ///////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////
    /** Main routine
     *@param args optional parameter for optimization initialization file
     */
    public static void main(String[] args){
	System.out.println(DIVIDER);
	System.out.println(RUNHEADER);
	System.out.println(DIVIDER);
	if (DEBUG) System.err.println(DEBUG_WARNING);
	try{
	    String optIniFilNam;
	    
	    if (args.length > 0 && args[0] != null)
		optIniFilNam = args[0];
	    else
		optIniFilNam = null;
	    
	    InputFormatException inpForExc = new InputFormatException();
	    // GenOpt constructor
	    GenOpt gen = new GenOpt();
	    
	    int exiFla = 1; // exit flag, unless overwritten below
	    try{
		gen = new GenOpt(optIniFilNam, inpForExc, null);
		
		// check for error during initialization
		if (inpForExc.getNumberOfErrors() > 0)
		    throw inpForExc;
	    }
	    catch (Throwable t)
		{
		    if (DEBUG) printStackTrace(t);
		    String em = t.getClass().getName() + ": " + LS +
			t.getMessage();
		    System.err.println(em);
		    gen.writeLogFile(em);
		    System.exit(exiFla);
		}
	    gen.run();
	    exiFla = gen.getExitFlag();
	    System.exit(exiFla);
	}
	
	catch (NoClassDefFoundError e) // if it doesn't run at all
	    {
		printStackTrace(e); // print to Stack also if not in debug mode
		String em = DIVIDER + LS;
		em += "GenOpt(R) Optimization Version " + VERSION + LS + LS;
		em += "Optimization terminated with error." + LS;
		em += "Java class not found. Check the setting of your CLASSPATH variable.";
		System.err.println( LS + LS + em + LS + DIVIDER);
		
		try{
		    FileWriter FilWri = new FileWriter("GenOpt.log");
		    FilWri.write(em);
		    FilWri.close();
		}
		catch(Throwable t){  // print always to StackTrace
		    printStackTrace(t);
		}
	    }
	System.exit(1);
    }
}
