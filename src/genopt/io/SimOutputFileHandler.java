package genopt.io;

import genopt.io.FileHandler;
import genopt.lang.OptimizerException;
import java.io.*;

/** Object that extends FileHandler and offers additional methods
  *   to get the value of the objective function out of the file.
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
  * @version GenOpt(R) 3.0.0 alpha 2 (November 18, 2008)<P>
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

public class SimOutputFileHandler extends FileHandler
{
	/** System dependent line separator */
	private final static String LS = System.getProperty("line.separator");

    /** separator after the objective function value (will be initialized below)*/
    private static String separator = "";

	/** constructor
	  * @param FileLines array of file lines
	  */
	public SimOutputFileHandler(String[] FileLines, String Separator)
	{
		super(FileLines);
		ownConstructor(Separator);
	}

	/** constructor
	  * @param path Path of file that has to be represented by this Object
	  * @param name Name of file that has to be represented by this Object
	  * @exception IOException
	  */
	public SimOutputFileHandler(String path, String name, String Separator) throws IOException
	{
		super(path, name);
		ownConstructor(Separator);
	}

	/** constructor
	  * @param file Path and name of file that has to be represented by this Object
	  * @exception IOException
	  */
	public SimOutputFileHandler(String file, String Separator) throws IOException
	{
		super(file);
		ownConstructor(Separator);
	}

    private void ownConstructor(String Separator){
	if (Separator != null){
	    separator = new String(" \t" + Separator);
	}
	else
	    separator = new String(" \t");
    }
	/** gets the last number of the file content
	  * @param ObjectiveFunctiondelimiter String that indicates delimiter 
	  * (additional to the blank space) of the objective function value
	  *@return value of the objective function
	  *@exception OptimizerException if objective function value has not been found
	  */
	public double getObjectiveFunctionValue(String ObjectiveFunctiondelimiter)
		throws OptimizerException
	{
		delimiter = new String(ObjectiveFunctiondelimiter);
		delLen = delimiter.length();
	    return (delimiter.equals(" ") || delimiter.equals("")) ?
		getObjectiveFunctionValueEmpty() : getObjectiveFunctionValueNonEmpty();
	}
	
	
 	/** cuts all space and tab characters at the beginning of the String
	  *@param s the String to be cutted
	  *@return the String with all space and tab characters at the beginning
	  *        cutted away
	 */	
	public static final String cutBeginSpaceAndTab(String s)
	{
	    String r = new String(s);
	    while (r.startsWith(" ") || r.startsWith("\t"))
	    {
			r = new String(r.substring(1));
	    }
	    return r;
	}	
 	/** cuts all space and tab characters at the end of the String
	  *@param s the String to be cutted
	  *@return the String with all space and tab characters at the end
	  *        cutted away
	 */	
	public static final String cutEndSpaceAndTab(String s)
	{
	    String r = new String(s);
	    while (r.endsWith(" ") || r.endsWith("\t"))
	    {
		r = new String(r.substring(0, r.length()-1));
	    }
	    return r;
	}
	
 	/** gets the first index of the space character, tab character,
	  * semi-colon, colon, or comma 
	  *@param s the String to be tested
	  *@return the first index of the space or tab character, 
	  * or <CODE>-1</CODE> if non of them is found
	  */
	public static int getIndexOfSeparator(String s)
	{
	    String ts = s.trim();
	    // get the lowest index of the separator that is bigger than -1
	    int sepPos = -1;
	    //System.out.println("****** getIndexOfSeparator:"+separator+"####"); 
	    for(int i = 0; i < separator.length(); i++){
		int p = ts.indexOf(separator.charAt(i));
		if ( p > -1 ) // found a separator
		    if ( sepPos == -1)
			sepPos = p;
		    else
			sepPos = Math.min(sepPos, p);
	    }
	    return sepPos;
	}

 	/** gets the double that occurs after the <I>last</I> blank character
	  *@param s the String containing the double
	  *@return the double
	  *@exception OptimizerException	  
	  */
	private double getDoubleAfterLastSpace(String s) throws OptimizerException
	{
	    String ts = new String(s);
	    ts = s.trim();
	    int lasBlaPos = ts.lastIndexOf(" ");
	    if (lasBlaPos > 0)
			ts = new String(ts.substring(lasBlaPos));
	    return parseToDouble(ts);
	}
	
 	/** gets the <I>first</I> double that occurs in the String (which is
	  * separated from the next entries by either a space character, 
	  * a tab, a comma, or a semicolon, or a double dot)
	  *@param s the String containing the double
	  *@return the double
	  *@exception OptimizerException	  
	  */
	private double getFirstDouble(String s) throws OptimizerException
	{
	    String ts = new String(s);
	    ts = s.trim();
	    int p = getIndexOfSeparator(ts);
	    if (p != -1)
			ts = new String(s.substring(0, p+1));
	    return parseToDouble(ts);
	}
	
	/** gets the last number of the file content.<BR>
	  * This method is used if the delimiter is either
	  * a white space or an empty character
	  *@return value of the objective function
	  *@exception OptimizerException
	  */
	private double getObjectiveFunctionValueEmpty() throws OptimizerException
	{
	    for (int i = nLines - 1; i >= 0; i--)
	    {
	        String curLin = new String(FileContents[i]);
	        curLin = cutEndLineBreak(curLin);
	        curLin = cutEndSpaceAndTab(curLin);
	        if (curLin.length() > 0)
	        { // we found a line with something else than only spaces and tabs
		    return (delimiter.length() == 0) ?
			getFirstDouble(curLin) : 
			getDoubleAfterLastSpace(curLin);
	        }
	    }
	    throwObjectiveFunctionValueNotFound();
	    return 999; // to satisfy compiler	    
	}
	
	/** gets the last number of the file content.<BR>
	  * This method is used if the delimiter is neither
	  * a white space nor an empty character
	  *@return value of the objective function
	  *@exception OptimizerException
	 */	
    	private double getObjectiveFunctionValueNonEmpty() throws OptimizerException
	{
		int i, delPos, blaPos, tabPos, begInd, endInd;
		String curLin;
		
		for (i = nLines - 1; i >= 0; i--)
		{
		    curLin = new String(FileContents[i]);
		    begInd = curLin.lastIndexOf(delimiter);
		    if (begInd != -1)
		    {   // we found the line with the keyword
			curLin = curLin.trim(); // we need that to check curLin.lenght() == 0 below
			curLin = cutEndLineBreak(curLin);
			curLin = cutEndSpaceAndTab(curLin);
			if (curLin.endsWith(delimiter))
			{	// cut del. in case of xxx;xxxx;xxxx; and del. = ";"
			    begInd = curLin.lastIndexOf(delimiter);
			    curLin = new String(curLin.substring(0, begInd));
			}
			if (curLin.length() == 0) // we got only the delimiter on this line
			{
			    String errMes = 
					"Error in the objective function value: " + LS + 
					"  Delimiter '" + delimiter + "' was found on line " + (i+1) +
				    " but no function value.";
			    throw new OptimizerException(errMes);
			}
			begInd = curLin.lastIndexOf(delimiter); 
			if (begInd == -1) // we got only a delimiter at the end of the line
			{
			    String errMes = 
					"Error in the objective function value:" + LS + 
					"  Delimiter '" + delimiter + "' was found at end of line " +
				 (i+1) + " but no function value.";
			    throw new OptimizerException(errMes);
			}
			
			endInd = begInd + delLen;

			curLin = new String(curLin.substring(endInd));
			curLin = cutBeginSpaceAndTab(curLin);
			delPos = getIndexOfSeparator(curLin);
			if (delPos != -1)
			    curLin = new String(curLin.substring(0, delPos));
			return parseToDouble(curLin);
		}
	    }
	    // objective function value was not found in simulation output file
	    throwObjectiveFunctionValueNotFound();
	    return 999; // to satisfy compiler
	}


	/** Throws an <CODE>OptimizerException</CODE> with the error message
	  * that the objective function value could not be found.
	  *@exception OptimizerException
	 */	
	public void throwObjectiveFunctionValueNotFound()
	    throws OptimizerException
	{
	    String ErrMes =
	    	"Error in searching for the objective function value: " + LS +
	    	"  The objective function value could not be found." + LS + 
	    	"  - Check the optimization ini file for" + LS +
	    	"    the prober value of SimOutputFile." + LS + 
			"  - Check the SimOutputFile for the prober" + LS +
	    	"    structure." + LS +
	    	"  - Check the optimization configuration file for" + LS +
	    	"    the prober value of the objective function delimiter." + LS +
			"    Objective function delimiter is '" + delimiter + "'.";
	    throw new OptimizerException(ErrMes);
	}

	/** parses a String to a double value
	  *@param s the String to be parsed
	  *@return value of the objective function
	  *@exception OptimizerException if the String is not a number or
	  *     if it is infinite
	 */	
	private double parseToDouble(String s) throws OptimizerException
	{
	    Double objFunDou = new Double(0);
	    try {
		objFunDou = new Double(s);
	    }
	    catch( NumberFormatException e){
		String errMes =
		    "Error in the objective function value: " + LS +
		    "  The invalid String '" + s + 
		    "' was found as the objective function value." + LS +
		    "  Objective function delimiter is '" + delimiter + "'.";
		throw new OptimizerException(errMes);
	    }		
	    if (objFunDou.isInfinite() || objFunDou.isNaN()) {
	        String errMes =
		    "Error in the objective function value: " + LS +
		    "  The objective function value that is found in" + LS +
		    "  the simulation output file is equal to '" + objFunDou + "'." + LS + 
		    "  Your simulation might have had an overflow.";
	        throw new OptimizerException(errMes);
	    }
	    return objFunDou.doubleValue();
	}

	/** objective function delimiter */
	protected String delimiter;
	/** length of function delimiter */
	protected int delLen;

/*    	public static void main(String[] args) throws IOException, OptimizerException
	{
	    String dir = new String("/home/mwetter/proj/genopt/3_code/1.1/genopt/io/test.txt");
	    genopt.io.SimOutputFileHandler s = new genopt.io.SimOutputFileHandler(dir, ";:,");
	    
	    System.out.println(s.getObjectiveFunctionValue("123,"));
	    System.out.println("-------");
	}
*/   

}








