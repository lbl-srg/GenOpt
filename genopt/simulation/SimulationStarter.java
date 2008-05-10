package genopt.simulation;

import genopt.db.OptimizationIni;
import genopt.io.FileHandler;
import genopt.lang.OptimizerException;
import java.util.Vector;
import java.io.*;

/** Object for calling a simulation program with free selectable
  * structure of the calling command.
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
  * @version GenOpt(R) 2.0.0 (Jan. 5, 2004)<P>
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

public class SimulationStarter implements Cloneable
{
    /** System dependent line separator */
    private final static String LS = System.getProperty("line.separator");

    /** constructor
     * @param command the command line (with placeholder for the variables)
     * @param promptInputFileExtension flag for specifying whether the extension
     *        of the inputfile must be written in the command or not.<d>
     *        (Set it <CODE>true</CODE> if the extension has to be written, 
     *        <CODE>false</CODE> otherwise.)
     */
    public SimulationStarter(String command, boolean promptInputFileExtension)
    {
	CommandLine          = command;
	PrombtFileExtension  = promptInputFileExtension;
    }

    /** updates the command line. This command must be used to update the command
     * line that was set by the constructor before the method
     * <A HREF="#runSimulation">runSimulation</A> or
     * <A HREF="#getCommandLine">getCommandLine</A>
     * is called the
     * first time. It adds the additional informations
     * that are contained in the command file to the command line.<dd>
     * <b>Note:</b> All parameter might also be empty strings.
     * @param OptIni Instance of Class OptimizationIni
     */
    public void updateCommandLine(OptimizationIni OptIni)
    {
	//update the command line so that it is ready to use
	//  and set a flag that the CommandLine is now ready to use
		
	// cut the file extension apart the string if necessairy
	int nSimInpFil = OptIni.getNumberOfInputFiles();
	String[] SimInputFileCal = new String[nSimInpFil];
	for(int i = 0; i < nSimInpFil; i++)
	    SimInputFileCal[i] = OptIni.getSimInpFilNam(i);
	if (!PrombtFileExtension)
	    {
		int j;
		for(int i = 0; i < nSimInpFil; i++)
		    {	
			j = SimInputFileCal[i].lastIndexOf('.');
			if (j != -1)	//cut the extension only if there is really one
			    SimInputFileCal[i] = new String(SimInputFileCal[i].substring(0, j));
		    }
	    }
	for(int i = 0; i < nSimInpFil; i++)
	    {
		CommandLine = replaceString(CommandLine, "%Simulation.Files.Template.File" + (i+1) +"%",
					    OptIni.getSimInpTemFilNam(i) );
		CommandLine = replaceString(CommandLine, "%Simulation.Files.Input.File" + (i+1) +"%",
					    SimInputFileCal[i]           );
		CommandLine = replaceString(CommandLine, "%Simulation.Files.Template.Path" + (i+1) +"%",
					    OptIni.getSimInpTemPat(i)    );
		CommandLine = replaceString(CommandLine, "%Simulation.Files.Input.Path" + (i+1) +"%",
					    OptIni.getSimInpPat(i)       );
		if (OptIni.getSimInpSavPat(i) != null)
		    CommandLine = replaceString(CommandLine, "%Simulation.Files.Input.SavePath" + (i+1) + "%",
						OptIni.getSimInpSavPat(i));
	    }

	for (int i = 0; i < OptIni.getNumberOfLogFiles(); i++){
	    CommandLine = replaceString(CommandLine, "%Simulation.Files.Log.File" + (i+1) + "%",
					OptIni.getSimLogFilNam(i));
	    CommandLine = replaceString(CommandLine, "%Simulation.Files.Log.Path" + (i+1) + "%",
					OptIni.getSimLogPat(i));
	}

	for (int i = 0; i < OptIni.getNumberOfOutputFiles(); i++){
	    CommandLine = replaceString(CommandLine, "%Simulation.Files.Output.File" + (i+1) + "%",
					OptIni.getSimOutFilNam(i));
	    CommandLine = replaceString(CommandLine, "%Simulation.Files.Output.Path" + (i+1) + "%",
					OptIni.getSimOutPat(i));
	    if (OptIni.getSimOutSavPat(i) != null)
		CommandLine = replaceString(CommandLine, "%Simulation.Files.Output.SavePath" + (i+1) + "%",
					    OptIni.getSimOutSavPat(i));
	}

	CommandLine = replaceString(CommandLine, "%Simulation.Files.Configuration.Name1%"   , OptIni.getSimConFilNam()       );
	CommandLine = replaceString(CommandLine, "%Simulation.Files.Configuration.Path1%"   , OptIni.getSimConPat()       );
	CommandLine = replaceString(CommandLine, "%Simulation.CallParameter.Prefix%"        , OptIni.getSimCalPre()       );
	CommandLine = replaceString(CommandLine, "%Simulation.CallParameter.Suffix%"        , OptIni.getSimCalSuf()       );

	CommandLine = replaceString(CommandLine, "%Optimization.Files.Command.File1%"       , OptIni.getOptComFilNam()    );
	CommandLine = replaceString(CommandLine, "%Optimization.Files.Command.Path1%"       , OptIni.getOptComPat()       );

	CommandLine = replaceString(CommandLine, "%CLASSPATH%"            , System.getProperty("java.class.path")       );
	return;
    }

    /** Replace all occurences of the String 'Find' with the String 'Set'
     *  in the String 'Original' even if 'Find' appears several times.<dd>
     * <b>Note:</b> The String 'Find' must not necessarily be found in String Original
     *   and could also be an empty string.
     * @param Original String that has be scanned for 'Find'
     * @param Find String that has to be found and replaced
     * @param Set String that has to be set at the position of 'Find'
     * @return String 'Original' where each occurence of 'Find' is replaced
     *   with 'Set'
     */
    public static String replaceString(String Original, String Find, String Set)
    {
	String WorStr = new String(Original);
	int j;
	int k = 0;
	int SetLen    = Set.length();
	int FinLen    = Find.length();

	do 
	    {
		j = WorStr.indexOf(Find, k);
		if (j != -1)
		    {
			WorStr = new String(WorStr.substring(0, j)) 
			    + Set + new String(WorStr.substring(j + FinLen));
			k = j + SetLen;
		    }
	    } while (j != -1);

	return WorStr;			
    }

    /** Get the command line.<dd>
     * @return Command line
     * @see SimulationStarter#updateCommandLine
     */
    public String getCommandLine()
    {
	return CommandLine;
    }

    /** Run the simulation program<dd>
     * <b>Note:</b> This method works only if the command line is already updated.
     * @exception IOException
     * @exception OptimizerException
     * @exception Exception
     * @see SimulationStarter#updateCommandLine
     */
    public void run() throws IOException, OptimizerException, Exception
    {
	try
	    {
		pro = Runtime.getRuntime().exec(this.getCommandLine());
		pro.waitFor();
		// sleep for some milliseconds
		//			System.err.print("Go to sleep...   ");
		//			Thread.sleep(2000);
		//			System.err.println("Woke up");
		int ev = 0;
		try { ev = pro.exitValue(); }
		catch (NullPointerException e){ // if process was destroyed
		    // The next line is new in GenOpt 2.0.0 due to Java's Bug Id 4637504 and 4784692.
		    // Otherwise, the system does not release its resources, and
		    // the exception "java.io.IOException: Too many open files" is
		    // thrown after a few hundred or thoussands of simulations.
		    destroyProcess();

		    throw new OptimizerException(genopt.GenOpt.USER_STOP_MESSAGE);
		}
		if (ev != 0){
		    InputStream ips = null;
		    try { ips = pro.getErrorStream(); }
		    catch (NullPointerException e){ // if process was destroyed
			// The next line is new in GenOpt 2.0.0 due to Java's Bug Id 4637504 and 4784692.
			// Otherwise, the system does not release its resources, and
			// the exception "java.io.IOException: Too many open files" is
			// thrown after a few hundred or thoussands of simulations.
			destroyProcess();

			throw new OptimizerException(genopt.GenOpt.USER_STOP_MESSAGE);
		    }					
		    int len = ips.available();
		    String sem;
		    if (len > 0){
			byte[] by = new byte[len];
			ips.read(by);
			sem = LS + new String(by);
			ips.close();
		    }
		    else
			sem = new String("Simulation program did not return an error stream.");
		 
		    String ErrMes =
			LS + "Error in executing the simulation program" +
			LS + "Exit value of the simulation program: " + ev +
			LS + "Current command String              : '" + this.getCommandLine() + "'." +
			LS + "Error stream of simulation program  : " + sem + LS;

		    // The next line is new in GenOpt 2.0.0 due to Java's Bug Id 4637504 and 4784692.
		    // Otherwise, the system does not release its resources, and
		    // the exception "java.io.IOException: Too many open files" is
		    // thrown after a few hundred or thoussands of simulations.
		    destroyProcess();

		    throw new OptimizerException(ErrMes);
		}
	    }
	catch(InterruptedException e){
	    String ErrMes =
		LS + "InterruptedException in executing the simulation program" + LS +
		LS + "Current command String: '" + this.getCommandLine() + "'." + LS +
		"Exception message: " + LS + e.getMessage(); 
	    // The next line is new in GenOpt 2.0.0 due to Java's Bug Id 4637504 and 4784692.
	    // Otherwise, the system does not release its resources, and
	    // the exception "java.io.IOException: Too many open files" is
	    // thrown after a few hundred or thoussands of simulations.
	    destroyProcess();
	    throw new OptimizerException(ErrMes);
	}
	catch(SecurityException e){
	    String ErrMes =
		LS + "SecurityException in executing the simulation program" + LS +
		LS + "Current command String: '" + this.getCommandLine() + "'." + LS +
		"Exception message: " + LS + e.getMessage();
	    // The next line is new in GenOpt 2.0.0 due to Java's Bug Id 4637504 and 4784692.
	    // Otherwise, the system does not release its resources, and
	    // the exception "java.io.IOException: Too many open files" is
	    // thrown after a few hundred or thoussands of simulations.
	    destroyProcess();
	    throw new OptimizerException(ErrMes);
	}
	// Note that we do NOT catch IOException separately since Java is buggy.
	// If e.g. a program is called that does not exist, then an IOException is
	// returned. Thus, we catch this as an Exception only
	catch(Exception e){ // any other exception
	    String ErrMes =
		LS + "Exception in executing the simulation program" + LS  +
		LS + "Current command String: '" + this.getCommandLine() + "'." + LS +
		"Exception message: " + LS + e.getMessage(); 
	    // The next line is new in GenOpt 2.0.0 due to Java's Bug Id 4637504 and 4784692.
	    // Otherwise, the system does not release its resources, and
	    // the exception "java.io.IOException: Too many open files" is
	    // thrown after a few hundred or thoussands of simulations.
	    destroyProcess();
	    throw new Exception(ErrMes);
	}
	// The next line is new in GenOpt 2.0.0 due to Java's Bug Id 4637504 and 4784692.
	// Otherwise, the system does not release its resources, and
	// the exception "java.io.IOException: Too many open files" is
	// thrown after a few hundred or thoussands of simulations.
	destroyProcess();
    }
	
    /** destroys the process if it exists
     */
    public void destroyProcess(){
	if (pro != null){
	    pro.destroy();
	    pro = null;
	}
    }

    protected String CommandLine;
    protected boolean PrombtFileExtension;
    protected Process pro;
	
}







