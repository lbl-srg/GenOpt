package genopt.io;

import java.io.File;
import java.util.Hashtable;
import java.util.Enumeration;
import javax.swing.filechooser.*;

/** Object for filtering initialization files in the JFileChooser object
  *
  * <P><I>This project was carried out at:</I>
  * <UL><LI><A HREF="http://www.lbl.gov">
  * Lawrence Berkeley National Laboratory (LBNL)</A>,
  * <A HREF="http://simulationresearch.lbl.gov">
  * Simulation Research Group</A>,</LI></UL>
  * <I>and supported by</I><UL>
  * <LI>the <A HREF="http://www.energy.gov">
  * U.S. Department of Energy (DOE)</A>,
  * <LI>the <A HREF="http://www.satw.ch">
  * Swiss Academy of Engineering Sciences (SATW)</A>,
  * <LI>the Swiss National Energy Fund (NEFF), and
  * <LI>the <A HREF="http://www.snf.ch">
  * Swiss National Science Foundation (SNSF)</A></LI></UL><P>
  *
  * GenOpt Copyright (c) 1998-2011, The Regents of the University of
  * California, through Lawrence Berkeley National Laboratory (subject 
  * to receipt of any required approvals from the U.S. Dept. of Energy).  
  * All rights reserved.
  *
  * @author <A HREF="mailto:MWetter@lbl.gov">Michael Wetter</A>
  *
  * @version GenOpt(R) 3.1.0 (December 8, 2011)<P>
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

public class GenOptFileFilter extends FileFilter
{   
    /** Constructor
     */
     public GenOptFileFilter() {
	 fil = new Hashtable<String, GenOptFileFilter>();
     }

     /** Constructor
     * @param extension file extension
     * @param description file description
     */
    public GenOptFileFilter(String extension, String description) {
	// make an instance
	this();
	if(extension!=null) addExtension(extension);
 	if(description!=null) setDescription(description);
    }

    /** returns <CODE>true</CODE> if a file is accepted for display,
     * or <CODE>false</CODE> otherwise
     * @param f File to be checked
     * @return <CODE>true</CODE> if it should be displayed,
     *        <CODE>false</CODE> otherwise
     */    
    public boolean accept(File f)
    {
	if (f.isDirectory()) {
	    return true;
	}
	if(f != null){
	    if(f.isDirectory())
		return true;
	    String extension = getExtension(f);
	    if(extension != null && fil.get(getExtension(f)) != null)
		return true;
	}
	return false;
    }

    /*
 * Get the extension of a file.
 */  
    public static String getExtension(File f) {
	String ext = null;
	String s = f.getName();
	int i = s.lastIndexOf('.');

	if (i > 0 &&  i < s.length() - 1) {
	    ext = s.substring(i+1).toLowerCase();
	}
	return ext;
    }
    /** adds an extension to this file filter
     * @param ext extension to be added
     */
    public void addExtension(String ext)
    {
	fil.put(ext.toLowerCase(), this);
    }
    
    /** adds the human readable description */
    public void setDescription(String description) {
	des = description;
    }

    /** gets the human readable description */
    public String getDescription(){
	String r;
	r = (des == null) ? "(" : des + " (";
	// add extension list
	Enumeration<String> ext = fil.keys();
	if(ext != null) {
	    r += "*." + (String) ext.nextElement();
	    while (ext.hasMoreElements()) {
		r += ", *" + (String) ext.nextElement();
	    }
	    r += ")";
	}
	return r;
    }    

    /** the Hashtable with the filter entries */
    Hashtable<String, GenOptFileFilter> fil;
    /** textual description of the files */
    private String des;
}
