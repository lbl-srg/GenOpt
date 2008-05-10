package genopt.db;
import genopt.GenOpt;

/** Object for collecting messages that occur during the optimization.
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

public class MessageManager
{
	/** System dependent line separator */
	private final static String LS = System.getProperty("line.separator");

	/** Constant for increasing the array size
	  */
	private final int ARRAYINCREMENT = 25;

	/** Constructor
	  *@param genOptRef reference to GenOpt object
	  */
	public MessageManager(GenOpt genOptRef)
	{
		go = genOptRef;
		numOfMes = 0;
		arrSiz = ARRAYINCREMENT;
		mes   = new String[arrSiz];
	}
	
	/** sets a message
	  *@param message the message
	  */
	public void setMessage(String message)
	{
		numOfMes++;
		if (arrSiz == numOfMes) 
			increaseArraySize();
		go.println("**** " + kind + " ****" + LS + message + LS);
		mes[numOfMes-1] = new String(message);
	}
	
	/** gets a String with all messages
	  *@return String with all messages
	  */
	public String getMessages()
	{
		String r = new String("");
		for (int i = 0; i < numOfMes; i++)
			r += kind + " "+ (i+1) + ": " + mes[i] + LS;
		return r;
	}

	/** gets a the number of messages
	  *@return the number of messages
	  */
	public int getNumberOfMessages() { return numOfMes;	}

	/** increases the array size */
	private void increaseArraySize()
	{
		String[] ds = new String[arrSiz];
		System.arraycopy(mes,   0, ds, 0, arrSiz);
		int arrSizOld = arrSiz;
		arrSiz += ARRAYINCREMENT;
		mes   = new String[arrSiz];

		System.arraycopy(ds, 0, mes,   0, arrSizOld);
	}
	

	/** reference to GenOpt object (used to print messages) */
	private GenOpt go;
	/** array with the messages */
	private String[] mes;
	/** current size of array */
	private int arrSiz;
	/** 1-based counter for number of message */
	private int numOfMes;
	/** indicator whether this objects collects info or warnings */
	protected String kind;	
}







