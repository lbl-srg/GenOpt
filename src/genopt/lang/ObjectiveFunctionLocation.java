package genopt.lang;

/** Object representing the objective function location.
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
  * GenOpt Copyright (c) 1998-2010, The Regents of the University of
  * California, through Lawrence Berkeley National Laboratory (subject 
  * to receipt of any required approvals from the U.S. Dept. of Energy).  
  * All rights reserved.
  *
  * @author <A HREF="mailto:MWetter@lbl.gov">Michael Wetter</A>
  *
  * @version GenOpt(R) 3.0.3 (April 26, 2010)<P>
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

public class ObjectiveFunctionLocation
{
    /**
     * Line separator
     */
    private final static String LS = System.getProperty("line.separator");
    
	/** Constructor.
	 *
	 * Either <code>delimiter</code> or <code>function</code> must be
	 * <code>null</code>, but not both.
	 *@param name name of the function
	 *@param delimiter objective function delimiter, or <code>null</code>
	 *                 pointer
	 *@param function string representation of the function, or <code>null</code>
	 *                 pointer
	  */
    public ObjectiveFunctionLocation(String name,
				     String delimiter,
				     String function){
	Name = name;
	if ( delimiter == null && function != null && function.length() > 0){
	    IsFunction = true;
	    Delimiter = null;
	    Function = new String(function);
	}
	else if ( delimiter == null && function != null && function.length() == 0){
	    throw new 
		IllegalArgumentException("Wrong arguments for Constructor of 'ObjectiveFunctionLocation'." 
					 + LS 
					 + "'function.length() == 0' does not make sense.");  
	}
	else if ( delimiter != null && function == null ){
	    IsFunction = false;
	    Delimiter = new String(delimiter);
	    Function = null;
	}
	else
	    throw new 
		IllegalArgumentException("Wrong arguments for Constructor of 'ObjectiveFunctionLocation'."
					 + LS 
					 + "Either 'delimiter' or 'function' must be 'null'.");
    }
    
    public boolean isFunction() { return IsFunction; }
    public String getName() { return new String(Name); }
    public String getDelimiter() { return new String(Delimiter); }
    public String getFunction() { return new String(Function); }
    
    /** The name of the objective function */
    protected String Name;
    /** The delimiter of the objective function, or <code>null</code> */
    protected String Delimiter;
    /** The function defining the objective function, or <code>null</code> */
    protected String Function;
    /** Flag whether this instance is a function or not */
    protected boolean IsFunction;
}
