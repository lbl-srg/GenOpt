package genopt.simulation;
import genopt.*;
import java.util.*;

/** Object that scans a String array for any number of possible
  * error messages.
  *
  * Instances of this class are used to detect whether the simulation program
  * terminated with an error.
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

public class ErrorChecker implements Cloneable
{
	/** System dependent line separator */
	private final static String LS = System.getProperty("line.separator");

	/** Constructor. Does nothing.
	  */
	public ErrorChecker() {}


	/** Constructor that assigns the strings of error messages that may be written
	  *  by the simulation program in case the simulation terminates with an error.
	  * 
	  * @param ErrorIndicator Array of strings that contain all messages
	  *   that are written to a log file by the simulation program
	  *   in case of an error.
	  */
	public ErrorChecker(String[] ErrorIndicator)
	{
		nErr = ErrorIndicator.length;
		errInd = new String[nErr];
		for (int i = 0; i < nErr; i++)
			errInd[i] = new String(ErrorIndicator[i]);
	}

	/** Clones the object.
	  */
	protected Object clone()
	{	
		try	{ return super.clone(); }
		catch(CloneNotSupportedException e)	{return null;}
	}



	/** Checks the argument for possible error messages.
	  * 
          * This method checks the argument <code>StringToCheck</code> for
          * any error messages. If no errors are found, it returns
	  * an empty Vector. Otherwise, a Vector with all
	  * found errors and an additional error information as the first entry are returned.
          *
	  *@param StringToCheck Array of strings that has to be checked for errors
	  *@param AdditionalErrorInformation Additional string that is added at the beginning
	  *   of the returned the error message
	  *@return A trimed empty Vector if no error message could be found, otherwise
	  *   a trimed Vector with all the error messages that were found and the string
	  *   'ErrorMessage' as the first entry.
	  */
	public Vector check(String[] StringToCheck, String AdditionalErrorInformation)
	{
		Vector<String> ErrMesVec = new Vector<String>();
		int i, j;
		int k = 0;
		int ResStrLen = StringToCheck.length;
		String ErrMes = new String(AdditionalErrorInformation);
		// check for error
		for (i = 0; i < ResStrLen; i++)
		{
			for (j = 0; j < nErr; j++)
			{
				if (StringToCheck[i].indexOf(errInd[j]) != -1)
				{	//error was found
					if ( k == 0) ErrMesVec.addElement(ErrMes);
					ErrMes = new String(LS + "Error on line " + i + ":" + LS + "   "
						+ StringToCheck[i] + LS);
					ErrMesVec.addElement(ErrMes);
					k++;
				}
			}
		}

		ErrMesVec.trimToSize(); 

		return ErrMesVec;

	}

	/** Gets the number of possible error strings.<br>
	  *
	  * <b>Note:</b> This method returns the number possible error strings
	  *    that were set by the constructor {@link #ErrorChecker(String[] ErrorIndicator)}
          *    and not the number of found error strings passed by 
          *    {@link #check(String[] StringToCheck, String AdditionalErrorInformation)}.
          *
	  * @return Number of possible error strings
	  */
	public int getNumberOfErrors() { return nErr; }

	/** Gets a single possible error string.<br>
	  *
	  * <b>Note:</b> This method returns a possible error string that was set
	  *    that were set by the constructor {@link #ErrorChecker(String[] ErrorIndicator)}
          *    and not the number of found error Strings passed by 
          *    {@link #check(String[] StringToCheck, String AdditionalErrorInformation)}.
          *
	  * @param Position 0-based index of error string
	  * @return A possible error string
	  */
	public String getErrorString(int Position) 
		{ return errInd[Position]; }

	  
	/** Array with error messages that may be written by the simulation program.  */
	protected String[] errInd;
		
	/** Number of error messages.  */
	protected int nErr;


}







