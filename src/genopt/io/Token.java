package genopt.io;
import genopt.io.InputFormatException;
import java.io.*;

/** String tokenizer for file input.
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
  * @version GenOpt(R) 3.0.0 alpha 1 (November 12, 2008)<P>
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

public class Token{

    /** constant to indicate that all values must be found */
    public static final int  ALL = 0;
    /** constant to indicate that not all values must be found */
    public static final int  PART = -1;
    /** quote character */
    protected static final char QUOTECHAR = '\"';
    /** maximal number of error in InputFormatException before search will be
	stopped */
    protected static final int MAXERROR = 30;

    private final static String LS = System.getProperty("line.separator");
	
    /** checks whether the next Token is equal to the passed String.<br>
     * <B>Note:</B> after this method, the StreamTokenizer is pushed back
     *@param st Reference to StreamTokenizer
     *@param keyWord the keyword that has to be searched for
     *@return <CODE>true</CODE> if the next Token equals the value of <CODE>keyWord</CODE>,
     *        <CODE>false</CODE> otherwise
     *@exception IOException
     */
    public static boolean isNextToken(StreamTokenizer st, String keyWord) throws IOException
    {
	boolean r;
	skipJavaComments(st); 
	st.nextToken();
	if (keyWord.length() == 1)
	    r = (st.ttype == keyWord.charAt(0)) ? true : false;
	else
	    r = (st.ttype == st.TT_WORD && st.sval.equals(keyWord)) ?
		true : false;
	st.pushBack();
	return r;
    }


    /** gets a <I>single Object</I> from the current position in the stream.
     *@param st Reference to StreamTokenizer
     *@param expectedString String representation of Token that has 
     *       been expected but not received
     *@param e reference to InputFormatException. Error message is written into 
     *         this Object	  
     *@param fn name of file (for error report only) or <CODE>null</CODE> pointer
     */
    public static void setErrorWrongToken(StreamTokenizer st, String expectedString,
					  InputFormatException e, String fn)
    {
	String em = new String("");
	if (fn != null)
	    em = new String(fn);
	em += "(Line " + (st.lineno()) +
	    "): InputFormatException: Expected \"" + new String(expectedString) +
	    "\", got \"";
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
	em += "\".";
	e.setMessage(em);
    }


    /** gets a <I>single Object</I> from the current position in the stream.
     *@param st Reference to StreamTokenizer
     *@param firstDelimiter first delimiter of the searched object
     *@param secondDelimiter second delimiter of the searched object
     *@param type Type specification of the object (either <CODE>String</CODE>,
     *                 <CODE>Double</CODE> or <CODE>Integer</CODE>
     *@param fn name of file (for error report only) or <CODE>null</CODE> pointer
     *@return the object between <CODE>firstDelimiter</CODE> and
     *        <CODE>secondDelimiter</CODE>
     *@exception IOException
     *@exception InputFormatException if the input is mal formatted
     */
    private static Object _getValue(StreamTokenizer st, char firstDelimiter,
				   char secondDelimiter, String type, String fn) 
        throws IOException, InputFormatException // the exception must be thrown
    {                                            // since we do not know to what
        setWordChars(st);
        st.slashSlashComments(true);
        st.slashStarComments(true);

        Object r = new Object();
        String em = "";
        boolean er = false;
        final int fd = firstDelimiter;
        final int sd = secondDelimiter;

	if (type.equals("String")) {
	    st.quoteChar(QUOTECHAR);
	    st.ordinaryChar('.');
	    st.wordChars('.', '.');
	    st.wordChars('0', '9');
	    st.wordChars('-', '-');
	    st.wordChars('+', '+');
	    st.wordChars('%', '%');
	}

        skipJavaComments(st);
        st.nextToken();

        if(st.ttype == fd) {
	    skipJavaComments(st);
	    st.nextToken();
	    if (type.equals("String")){
		//		System.err.println("AAAAAA " + st.ttype + '\t' 
		//				   + st.sval + '\t' + st.nval  +
		//				   '\t' + (char)(st.ttype) );
		if ( st.ttype == st.TT_NUMBER ){
		    double num = st.nval;
		    st.ordinaryChars('\0', ' ');
		    st.nextToken();
		    st.whitespaceChars('\0', ' ');
		    if (st.ttype == StreamTokenizer.TT_WORD &&
			Character.toUpperCase(st.sval.charAt(0)) == 'E') {
			try {
			    final int exp = Integer.parseInt(st.sval.substring(1));
			    num *= Math.pow(10, exp);
			} 
			catch (NumberFormatException e) { st.pushBack(); }
		    } else if (!('\0' <= st.ttype && st.ttype <= ' ')) {
			st.pushBack();
		    }
		    if ( (int)num == num )
			r = Integer.toString( (int)num );
		    else
			r = Double.toString( num );
		    
		}
		else
		    r = new String(st.sval);
	    }
	    else if (type.equals("Double")) {
		// new code since Oct. 19, 2003
		// If the user encloses the double value in apostrophes, then
		// st.nval does not contain the numerical value
		//		System.err.println("Token.java: " + st.nval);
		//		System.err.println("Token.java: " + st.sval);
		if ( st.sval == null )
		    r = new Double(st.nval);
		else
		    r = new Double(st.sval);
		//		System.err.println("Token.java: " + r + "---");
	    }
	    else if (type.equals("Integer")) {
		try {
		    //		    System.err.println("Int. Token.java: " + st.nval);
		    //		    System.err.println("Int. Token.java: " + ( ( (st.sval) == null ) ? st.sval : st.sval.trim()));
		    if ( st.sval == null )
			r = new Integer((int)st.nval);
		    else
			r = new Integer(st.sval.trim());
		}
		catch(NumberFormatException e) {
		    em = new String("Expected an Integer'");
		    er = true;
		    r = new Integer(Integer.MAX_VALUE);
		}
		//		System.err.println("Token.java: " + r + "---");
	    }
	    
	    skipJavaComments(st);
	    
	    if (st.nextToken() == sd)
		return r;
	    
	    switch (st.ttype){
		// we did not get the second delimiter
	    case StreamTokenizer.TT_WORD:
		em = new String("Expected '" + (char)sd + "', got '" 
				+ st.sval + "'.");
		break;
	    case StreamTokenizer.TT_NUMBER:
		em = new String("Expected '" + (char)sd + "', got '" 
				+ st.nval + "'.");
		break;
	    case StreamTokenizer.TT_EOL:
		em = new String("Expected '" + (char)sd 
				+ "', got 'endOfLine'.");
	    case StreamTokenizer.TT_EOF:
		em = new String("Expected '" + (char)sd 
				+ "', got 'endOfFile'.");
	    default:
		em = new String("Expected '" + (char)sd + "', got '" + 
				String.valueOf((char)st.ttype) + "'.");
	    }// end switch
	    er = true;
	}
	else {
	    em = new String("Expected '" + (char)fd + "'");
	    er = true;
	}
	if (er) {
	    if (fn == null)
		em = new String
		    ("(Line " + st.lineno() +"): InputFormatException: " + em);
	    else
		em = new String(fn) + new String
		    ("(Line " + st.lineno() +"): InputFormatException: " + em);
	    throw new InputFormatException(em);
	}
	return r;
    }


    /** gets a <I>single keyword value</I> from the current position in the stream.<BR>
     * The value must be enclosed between <CODE>firstDelimiter</CODE> and
     *        <CODE>secondDelimiter</CODE>
     *@param st Reference to StreamTokenizer
     *@param firstDelimiter first delimiter of the searched object
     *@param secondDelimiter second delimiter of the searched object
     *@param keyWord the keyword that has to be searched for
     *@param e reference to InputFormatException. Error messages are written into 
     *         this Object
     *@param fn name of file (for error report only) or <CODE>null</CODE> pointer
     *@return the String between <CODE>firstDelimiter</CODE> and
     *        <CODE>secondDelimiter</CODE>
     *@exception IOException
     */
    public static int getIntegerValue(StreamTokenizer st, char firstDelimiter, 
				      char secondDelimiter, String keyWord, InputFormatException e, String fn)
	throws IOException
    {
        String[] sk = new String[1];
        Integer[] sv = new Integer[1];
        sk[0] = new String(keyWord);
        sv[0] = new Integer(0);
        getIntegerValue(st, firstDelimiter, secondDelimiter, sk, sv, e, fn, ALL);
        return sv[0].intValue();
    }
    
    /** gets a <I>single keyword value</I> from the current position in the stream.<BR>
     * The value must be enclosed between <CODE>firstDelimiter</CODE> and
     *        <CODE>secondDelimiter</CODE>
     *@param st Reference to StreamTokenizer
     *@param firstDelimiter first delimiter of the searched object
     *@param secondDelimiter second delimiter of the searched object
     *@param keyWord the keyword that has to be searched for
     *@param e reference to InputFormatException. Error messages are written into 
     *         this Object
     *@param fn name of file (for error report only) or <CODE>null</CODE> pointer
     *@return the String between <CODE>firstDelimiter</CODE> and
     *        <CODE>secondDelimiter</CODE>
     *@exception IOException
     */
    public static double getDoubleValue(StreamTokenizer st, char firstDelimiter, 
					char secondDelimiter, String keyWord, InputFormatException e, String fn)
	throws IOException
    {
        String[] sk = new String[1];
        Double[] sv = new Double[1];
        sk[0] = new String(keyWord);
        sv[0] = new Double(0);
        getDoubleValue(st, firstDelimiter, secondDelimiter, sk, sv, e, fn, ALL);
        return sv[0].doubleValue();
    }

    /** gets a <I>single keyword value</I> from the current position in the stream.<BR>
     * The value must be enclosed between <CODE>firstDelimiter</CODE> and
     *        <CODE>secondDelimiter</CODE>
     *@param st Reference to StreamTokenizer
     *@param firstDelimiter first delimiter of the searched object
     *@param secondDelimiter second delimiter of the searched object
     *@param keyWord the keyword that has to be searched for
     *@param e reference to InputFormatException. Error messages are written into 
     *         this Object
     *@param fn name of file (for error report only) or <CODE>null</CODE> pointer
     *@return the boolean value between <CODE>firstDelimiter</CODE> and
     *        <CODE>secondDelimiter</CODE>
     *@exception IOException
     */
    public static boolean getBooleanValue(StreamTokenizer st, char firstDelimiter, 
					  char secondDelimiter, String keyWord, InputFormatException e, String fn)
	throws IOException
    {
        String[] sk = new String[1];
        Boolean[] sv = new Boolean[1];
        sk[0] = new String(keyWord);
        sv[0] = new Boolean(false);
        getBooleanValue(st, firstDelimiter, secondDelimiter, sk, sv, e, fn, ALL);
        return sv[0].booleanValue();
    }

    
    
    /** gets a <I>single keyword value</I> from the current position in the stream.<BR>
     * The value must be enclosed between <CODE>firstDelimiter</CODE> and
     *        <CODE>secondDelimiter</CODE>
     *@param st Reference to StreamTokenizer
     *@param firstDelimiter first delimiter of the searched object
     *@param secondDelimiter second delimiter of the searched object
     *@param keyWord the keyword that has to be searched for
     *@param e reference to InputFormatException. Error messages are written into 
     *         this Object
     *@param fn name of file (for error report only) or <CODE>null</CODE> pointer
     *@return the String between <CODE>firstDelimiter</CODE> and
     *        <CODE>secondDelimiter</CODE>
     *@exception IOException
     */
    public static String getStringValue(StreamTokenizer st, char firstDelimiter, 
					char secondDelimiter, String keyWord, 
					InputFormatException e, String fn)
	throws IOException
    {
        String[] sk = new String[1];
        String[] sv = new String[1];
        sk[0] = new String(keyWord);
        sv[0] = new String("");
        getStringValue(st, firstDelimiter, secondDelimiter, sk, sv, e, fn, ALL);
        return new String(sv[0]);
    }

    /** gets a <I>single String</I> from the current position in the stream.<BR>
     * The String must be enclosed between <CODE>firstDelimiter</CODE> and
     *        <CODE>secondDelimiter</CODE>
     *@param st Reference to StreamTokenizer
     *@param firstDelimiter first delimiter of the searched object
     *@param secondDelimiter second delimiter of the searched object
     *@param fn name of file (for error report only) or <CODE>null</CODE> pointer
     *@return the String between <CODE>firstDelimiter</CODE> and
     *        <CODE>secondDelimiter</CODE>
     *@exception IOException
     *@exception InputFormatException if the input is mal formatted
     */
    private static String _getStringValue(StreamTokenizer st, char firstDelimiter,
					 char secondDelimiter, String fn)
        throws IOException, InputFormatException {
        return new String((String)_getValue(st, firstDelimiter, 
						secondDelimiter, "String", fn));
    }
    /** spools the pointer in the StreamTokenizer in front of a String.<BR>
     * The StreamTokenizer is read until either the <CODE>keyWord</CODE> is read 
     *     or until end-of-file occurs.<BR>
     * The pointer in the StreamTokenizer is moved in in front of 
     * the <CODE>keyWord</CODE>.<BR>
     * If the <CODE>keyWord</CODE> is not found, an error message is written into
     * the InputFormatException
     *@param st Reference to StreamTokenizer
     *@param keyWord the keyword to be found
     *@param e reference to InputFormatException. Error messages are written into 
     *         this Object
     *@param fn name of file (for error report only) or <CODE>null</CODE> pointer
     *@exception IOException
     */
    public static void spoolTo(StreamTokenizer st, String keyWord, 
			       InputFormatException e, String fn)
	throws IOException
    {
        setWordChars(st);
        skipJavaComments(st);
        boolean search = true;
        do
	    {
		st.nextToken();
		if (st.ttype == StreamTokenizer.TT_WORD && st.sval.equals(keyWord))
		    search = false;
		else if (st.ttype == StreamTokenizer.TT_EOF)
		    search = false;
	    } while (search);
        if (st.ttype == StreamTokenizer.TT_EOF)
	    {
		String em = new String(stringNotFound(st, fn));
		em += " Could not find '" + keyWord +"'.";
		e.setMessage(em);
	    }
        else
            st.pushBack();
        return;
    }

    /** gets the <I>start of a section</I> from the current position in the stream.<BR>
     * The StreamTokenizer is read until either the section keyword is read 
     *     or until end-of-file occurs.<BR>
     * The pointer in the StreamTokenizer is moved beyond the section start
     * character ('{')
     *@param st Reference to StreamTokenizer
     *@param keyWord the keyword of the section
     *@param e reference to InputFormatException. Error messages are written into 
     *         this Object
     *@param fn name of file (for error report only) or <CODE>null</CODE> pointer
     *@exception IOException
     */
    public static void getSectionStart(StreamTokenizer st, String keyWord, 
				       InputFormatException e, String fn)
	throws IOException
    {
        setWordChars(st);
        st.eolIsSignificant(false); // neglect EOL
        boolean er=false; // flag that we found an error
        String em = null;  // error message String
        int i;

        skipJavaComments(st);
        st.nextToken();
	if (st.ttype == StreamTokenizer.TT_WORD && st.sval.equals(keyWord))
            {   // we got the keyword
                skipJavaComments(st);
                st.nextToken();
                if (st.ttype != '{')
		    {   // we did not get the curly brace
			if (fn == null)
			    em = new String
				("(Line " + st.lineno() +
				 "): InputFormatException: Expected '{'.");
			else
			    em = new String(fn) + new String
				("(Line " + st.lineno() +
				 "): InputFormatException: Expected '{'.");
			er = true;
		    }
		else
		    skipJavaComments(st); // added 01/16/2003
            }
	else if (st.ttype == StreamTokenizer.TT_WORD)
            {
                // we did not get the keyWord but an other word
                if (fn == null)
		    {
			em = new String
			    ("(Line " + st.lineno() +
			     "): InputFormatException: Unknown or unexpected keyword '" +
			     st.sval + "'.");
			er = true;
		    }
                else
		    {
			em = new String(fn) + new String
			    ("(Line " + st.lineno() +
			     "): InputFormatException: Unknown or unexpected keyword '" +
			     st.sval + "'.");
			er = true;
		    }
            }
	else
            {  // we have not found a word
                em = new String(stringNotFound(st, fn));
                er = true;
            }
	if (er) 
            {   // set occured error into exception object
                e.setMessage(em);
                er = false;
            }
        return;
    }

    /** gets <I>various keyword values</I> from the current position in the stream.<BR>
     * The value must be enclosed between <CODE>firstDelimiter</CODE> and
     *        <CODE>secondDelimiter</CODE><LI>
     * The StreamTokenizer is read until either all keywords are read, the end of 
     * a section occurs or until end-of-file occurs.<LI>
     * All found keywords are written into sk.<LI>
     * Multiple occurences of a keyword leads to an error report 
     *       whereas the last occured value is written into sk.</LI>
     *@param st Reference to StreamTokenizer
     *@param firstDelimiter first delimiter of the searched object
     *@param secondDelimiter second delimiter of the searched object
     *@param sk array with all keywords.
     *@param sv array where all found values of the keyword will be written in
     *          (sk and sv must have the same length)
     *@param e reference to InputFormatException. Error messages are written into 
     *         this Object
     *@param fn name of file (for error report only) or <CODE>null</CODE> pointer
     *@param flag flag whether necessarily all values must be set or not.<BR>
     *            (If all values must be set, pass the constant Token.ALL, otherwise pass
     *            Token.PART)
     *@exception IOException
     */
    public static void getDoubleValue(StreamTokenizer st, char firstDelimiter, 
				      char secondDelimiter, String[] sk, Double[] sv, InputFormatException e, 
				      String fn, int flag)
	throws IOException {
        _getValue(st, firstDelimiter, secondDelimiter, sk, sv, e, "Double", fn, flag);
    }


    /** gets <I>various keyword values</I> from the current position in the stream.<BR>
     * The value must be enclosed between <CODE>firstDelimiter</CODE> and
     *        <CODE>secondDelimiter</CODE><LI>
     * The StreamTokenizer is read until either all keywords are read, the end of 
     * a section occurs or until end-of-file occurs.<LI>
     * All found keywords are written into sk.<LI>
     * Multiple occurences of a keyword leads to an error report 
     *       whereas the last occured value is written into sk.</LI>
     *@param st Reference to StreamTokenizer
     *@param firstDelimiter first delimiter of the searched object
     *@param secondDelimiter second delimiter of the searched object
     *@param sk array with all keywords.
     *@param sv array where all found values of the keyword will be written in
     *          (sk and sv must have the same length)
     *@param e reference to InputFormatException. Error messages are written into 
     *         this Object
     *@param fn name of file (for error report only) or <CODE>null</CODE> pointer
     *@param flag flag whether necessarily all values must be set or not.<BR>
     *            (If all values must be set, pass the constant Token.ALL, otherwise pass
     *            Token.PART)
     *@exception IOException
     */
    public static void getBooleanValue(StreamTokenizer st, char firstDelimiter, 
				       char secondDelimiter, String[] sk, Boolean[] sv, InputFormatException e, 
				       String fn, int flag)
	throws IOException {
        _getValue(st, firstDelimiter, secondDelimiter, sk, sv, e, "Boolean", fn, flag);
    }




    /** gets <I>various keyword values</I> from the current position in the stream.<BR>
     * The value must be enclosed between <CODE>firstDelimiter</CODE> and
     *        <CODE>secondDelimiter</CODE><LI>
     * The StreamTokenizer is read until either all keywords are read, the end of 
     * a section occurs or until end-of-file occurs.<LI>
     * All found keywords are written into sk.<LI>
     * Multiple occurences of a keyword leads to an error report 
     *       whereas the last occured value is written into sk.</LI>
     *@param st Reference to StreamTokenizer
     *@param firstDelimiter first delimiter of the searched object
     *@param secondDelimiter second delimiter of the searched object
     *@param sk array with all keywords.
     *@param sv array where all found values of the keyword will be written in
     *          (sk and sv must have the same length)
     *@param e reference to InputFormatException. Error messages are written into 
     *         this Object
     *@param fn name of file (for error report only) or <CODE>null</CODE> pointer
     *@param flag flag whether necessarily all values must be set or not.<BR>
     *            (If all values must be set, pass the constant Token.ALL, otherwise pass
     *            Token.PART)
     *@exception IOException
     */
    public static void getIntegerValue(StreamTokenizer st, char firstDelimiter, 
				       char secondDelimiter, String[] sk, Integer[] sv, InputFormatException e, 
				       String fn, int flag)
	throws IOException {
        _getValue(st, firstDelimiter, secondDelimiter, sk, sv, e, "Integer", fn, flag);
    }

    /** gets <I>various keyword values</I> from the current position in the stream.<BR>
     * The value must be enclosed between <CODE>firstDelimiter</CODE> and
     *        <CODE>secondDelimiter</CODE><LI>
     * The StreamTokenizer is read until either all keywords are read, the end of 
     * a section occurs or until end-of-file occurs.<LI>
     * All found keywords are written into sk.<LI>
     * Multiple occurences of a keyword leads to an error report 
     *       whereas the last occured value is written into sk.</LI>
     *@param st Reference to StreamTokenizer
     *@param firstDelimiter first delimiter of the searched object
     *@param secondDelimiter second delimiter of the searched object
     *@param sk String array with all keywords.
     *@param sv String array where all found values of the keyword will be written in
     *          (sk and sv must have the same length)
     *@param e reference to InputFormatException. Error messages are written into 
     *         this Object
     *@param fn name of file (for error report only) or <CODE>null</CODE> pointer
     *@param flag flag whether necessarily all values must be set or not.<BR>
     *            (If all values must be set, pass the constant Token.ALL, otherwise pass
     *            Token.PART)
     *@exception IOException
     */
    public static void getStringValue(StreamTokenizer st, char firstDelimiter, 
				      char secondDelimiter, String[] sk, String[] sv, InputFormatException e, 
				      String fn, int flag)
	throws IOException {
        _getValue(st, firstDelimiter, secondDelimiter, sk, sv, e, "String", fn, flag);
    }


    /** moves the pointer in the StreamTokenizer beyond the next <CODE>'}'</CODE> sign
     *@param st Reference to StreamTokenizer
     *@param e reference to InputFormatException. Error messages are written into 
     *         this Object
     *@param fn name of file (for error report only) or <CODE>null</CODE> pointer
     *@exception IOException
     */
    public static void moveToSectionEnd(StreamTokenizer st, 
					InputFormatException e, String fn) throws IOException
    {
        skipJavaComments(st);
        st.nextToken();
        if (st.ttype != '}')
	    {
		String em = new String();
		if (fn == null)
		    em = new String
			("(Line " + st.lineno() +
			 "): InputFormatException: End of section expected.");
		else
		    em = new String(fn) + new String
			("(Line " + st.lineno() +
			 "): InputFormatException: End of section expected.");
		e.setMessage(em);
		moveToEOL(st);
	    }
    }

    /** moves the pointer in the StreamTokenizer beyond the next <CODE>end-of-line</CODE>
     *@param st Reference to StreamTokenizer
     *@exception IOException
     */
    private static void moveToEOL(StreamTokenizer st) throws IOException
    {
        st.eolIsSignificant(true);
        do
	    {
		st.nextToken();
	    } while (st.ttype != StreamTokenizer.TT_EOF && st.ttype != StreamTokenizer.TT_EOL);
        st.eolIsSignificant(false);
    }

    private static void setWordChars(StreamTokenizer st)
    {
        st.wordChars('*', '*');
        st.wordChars('/', '/');
        st.wordChars(':', ':');
        st.wordChars('\\', '\\');
        st.wordChars('/', '/');
        st.wordChars('_', '_');
	st.wordChars('%', '%');
    }

    /** skips the pointer of the StreamTokenizer beyond the next <CODE>/*..*'/</CODE>
     * comment section if there is any.
     *@param st Reference to StreamTokenizer
     *@exception IOException
     */
    public static void skipSlashStarComments(StreamTokenizer st) throws IOException
    {
        setWordChars(st);
        st.nextToken();
        if (st.ttype == StreamTokenizer.TT_WORD && st.sval.startsWith("/*"))
	    {
		st.nextToken();
		while (st.ttype != StreamTokenizer.TT_WORD || !st.sval.startsWith("*/"))
		    {
			st.nextToken();
		    }
	    }
        else
            st.pushBack();
    }

    /** skips the pointer of the StreamTokenizer beyond the next <CODE>/*..*'/</CODE> or
     * <CODE>//..</CODE> comment section if there is any.
     *@param st Reference to StreamTokenizer
     *@exception IOException
     */
    public static void skipJavaComments(StreamTokenizer st) throws IOException
    {
        setWordChars(st);
        while (st.nextToken() == StreamTokenizer.TT_WORD && ((st.sval.startsWith("/*") ||
						 st.sval.startsWith("//"))))
	    {
		if (st.sval.startsWith("/*"))
		    {
			st.pushBack();
			skipSlashStarComments(st);
		    }
		else if (st.sval.startsWith("//"))
		    {
			st.pushBack();
			skipSlashSlashComments(st);
		    }
	    }
        st.pushBack();
    }

    /** skips the pointer of the StreamTokenizer beyond the next <CODE>//..//</CODE>
     * comment section if there is any.
     *@param st Reference to StreamTokenizer
     *@exception IOException
     */
    public static void skipSlashSlashComments(StreamTokenizer st) throws IOException
    {
        st.nextToken();
        while (st.ttype == StreamTokenizer.TT_WORD && st.sval.startsWith("//"))
	    {
		moveToEOL(st);
		st.nextToken();
	    }
        st.pushBack();
    }

    /** checks if all values of <CODE>val</CODE> are set.
     * For each value that is not set, an error message is written to
     * the InputFormatException
     *@param st Reference to StreamTokenizer
     *@param e reference to InputFormatException. Error messages are written into 
     *         this Object
     *@param variableName names of the variable (for error report only)
     *@param variableValue values of the variables corresponding to 'variableName'
     *@param fileName name of file (for error report only) or <CODE>null</CODE> pointer
     */
    public static void checkVariableSetting(StreamTokenizer st, InputFormatException e, 
					    String[] variableName, 
					    String[] variableValue, String fileName){
        for (int i = 0; i < variableValue.length; i++)
	    checkVariableSetting(st, e, variableName[i], variableValue[i], fileName);
    }

    /** checks if the value of <CODE>val</CODE> is set.
     * If the value is not set, an error message is written to
     * the InputFormatException
     *@param st Reference to StreamTokenizer
     *@param e reference to InputFormatException. Error messages are written into 
     *         this Object
     *@param variableName name of the variable (for error report only)
     *@param variableValue value of the variables corresponding to 'variableName'
     *@param fileName name of file (for error report only) or <CODE>null</CODE> pointer
     */
    public static void checkVariableSetting(StreamTokenizer st, InputFormatException e, 
					    String variableName, String variableValue, 
					    String fileName){
	if (variableValue == null || variableValue.length() == 0)
	    variableNotSet(st, e, variableName, fileName);
    }


    /** checks if the value of <CODE>val</CODE> is set.
     * If the value is not set, an error message is written to
     * the InputFormatException
     *@param st Reference to StreamTokenizer
     *@param e reference to InputFormatException. Error messages are written into 
     *         this Object
     *@param variableName name of the variable (for error report only)
     *@param keyWord keyword that is being tested (for error report only)
     *@param variableValue value of the variables corresponding to 'variableName'
     *@param fileName name of file (for error report only) or <CODE>null</CODE> pointer
     */
    public static void checkVariableSetting(StreamTokenizer st, InputFormatException e, 
					    String variableName, String keyWord,
					    String variableValue, String fileName){
	if (variableValue == null || variableValue.length() == 0)
	    variableNotSet(st, e, variableName, keyWord, fileName);
    }


    /** checks if the value of <CODE>val</CODE> is empty.
     * If the value is not empty, an error message is written to
     * the InputFormatException
     *@param st Reference to StreamTokenizer
     *@param e reference to InputFormatException. Error messages are written into 
     *         this Object
     *@param variableName name of the variable (for error report only)
     *@param variableValue value of the variables corresponding to 'variableName'
     *@param fileName name of file (for error report only) or <CODE>null</CODE> pointer
     */
    public static void checkVariableSettingEmpty(StreamTokenizer st, InputFormatException e, 
						 String variableName, String variableValue, 
						 String fileName){
	if (variableValue.length() != 0)
	    variableNotEmpty(st, e, variableName, fileName);
    }

    /** checks if the value of <CODE>val</CODE> is empty.
     * If the value is not empty, an error message is written to
     * the InputFormatException
     *@param st Reference to StreamTokenizer
     *@param e reference to InputFormatException. Error messages are written into 
     *         this Object
     *@param variableName name of the variable (for error report only)
     *@param keyWord keyword that is being tested (for error report only)
     *@param variableValue value of the variables corresponding to 'variableName'
     *@param fileName name of file (for error report only) or <CODE>null</CODE> pointer
     */
    public static void checkVariableSettingEmpty(StreamTokenizer st, InputFormatException e, 
						 String variableName, String keyWord,
						 String variableValue, String fileName){
	if (variableValue.length() != 0)
	    variableNotEmpty(st, e, variableName, keyWord, fileName);
    }



    /** sets a specified error message into the InputFormatException 
     * with the current line number and file name
     *@param st Reference to StreamTokenizer
     *@param e reference to InputFormatException. Error messages are written into 
     *         this Object
     *@param errorMessage error message to be written in the expection object
     *@param fileName name of file (for error report only) or <CODE>null</CODE> pointer
     */
    public static void setError(StreamTokenizer st, InputFormatException e, 
				String errorMessage, String fileName)
    {
        String fn = new String();
        if (fileName == null)
            fn = new String("");
        else
            fn = new String(fileName);
        String em = new String(fn) + new String
	    ("(Line " + st.lineno() +
	     "): InputFormatException: " + LS + "   " + errorMessage);
        e.setMessage(em);
        return;
    }


    /** writes an error message into the InputFormatException if a variable was not set
     *@param st Reference to StreamTokenizer
     *@param e reference to InputFormatException. Error messages are written into 
     *         this Object
     *@param variableName name of the variable that was not found (for error report only)
     *@param fileName name of file (for error report only) or <CODE>null</CODE> pointer
     */
    public static void variableNotSet(StreamTokenizer st, InputFormatException e, 
				      String variableName, String fileName){
        String em = "Variable value of '" + variableName +
	    "' is not set.";
        setError(st, e, em, fileName);
        return;
    }

    /** writes an error message into the InputFormatException if a variable was not set
     *@param st Reference to StreamTokenizer
     *@param e reference to InputFormatException. Error messages are written into 
     *         this Object
     *@param variableName name of the variable that was not found (for error report only)
     *@param keyWord keyword that is being tested (for error report only)
     *@param fileName name of file (for error report only) or <CODE>null</CODE> pointer
     */
    public static void variableNotSet(StreamTokenizer st, InputFormatException e, 
				      String variableName, String keyWord, String fileName){
        String em = "Variable '" + variableName + "', keyword '" +
	    keyWord + "' is not set.";
        setError(st, e, em, fileName);
        return;
    }

    /** writes an error message into the InputFormatException if a variable was not empty
     *@param st Reference to StreamTokenizer
     *@param e reference to InputFormatException. Error messages are written into 
     *         this Object
     *@param variableName name of the variable that was not empty (for error report only)
     *@param fileName name of file (for error report only) or <CODE>null</CODE> pointer
     */
    public static void variableNotEmpty(StreamTokenizer st, InputFormatException e, 
				      String variableName, String fileName) {
        String em = "Variable value of '" + variableName +
	    "' is set. This variable must not be set.";
        setError(st, e, em, fileName);
        return;
    }

    /** writes an error message into the InputFormatException if a variable was not empty
     *@param st Reference to StreamTokenizer
     *@param e reference to InputFormatException. Error messages are written into 
     *         this Object
     *@param variableName name of the variable that was not empty (for error report only)
     *@param keyWord keyword that is being tested (for error report only)
     *@param fileName name of file (for error report only) or <CODE>null</CODE> pointer
     */
    public static void variableNotEmpty(StreamTokenizer st, InputFormatException e, 
				      String variableName, String keyWord,
					String fileName) {
        String em = "Variable '" + variableName + "', keyword '" + 
	    keyWord + "' is set. This keyword must not be set.";
        setError(st, e, em, fileName);
        return;
    }

    /** writes an error message into the InputFormatException if a variable was not set
     *@param st Reference to StreamTokenizer
     *@param e reference to InputFormatException. Error messages are written into 
     *         this Object
     *@param variableName name of the variable (for error report only)
     *@param receivedValue received value
     *@param admissibleValue admissible value
     *@param fileName name of file (for error report only) or <CODE>null</CODE> pointer
     */
    public static void checkAdmissableValue(StreamTokenizer st, InputFormatException e, 
					    String variableName, String receivedValue,
					    String admissibleValue, String fileName){
	if ( receivedValue.compareToIgnoreCase(admissibleValue) != 0 ){
	    String em = "Variable '" + variableName +
		"' is set to '" + receivedValue + 
		"'. The only allowed value is '" + admissibleValue + "'.";
	    setError(st, e, em, fileName);
	}
        return;
    }

    /** writes an error message into the InputFormatException if a variable was not set
     *@param st Reference to StreamTokenizer
     *@param e reference to InputFormatException. Error messages are written into 
     *         this Object
     *@param variableName name of the variable (for error report only)
     *@param keyWord keyword that is being tested (for error report only)
     *@param receivedValue received value
     *@param admissibleValue admissible value
     *@param fileName name of file (for error report only) or <CODE>null</CODE> pointer
     */
    public static void checkAdmissableValue(StreamTokenizer st, InputFormatException e, 
					    String variableName, String keyWord, 
					    String receivedValue, String admissibleValue, 
					    String fileName){
	if ( receivedValue.compareToIgnoreCase(admissibleValue) != 0 ){
	    String em = "Variable '" + variableName + "', keyword '" +
		keyWord + "' is set to '" + receivedValue + 
		"'. The only allowed value is '" + admissibleValue + "'.";
	    setError(st, e, em, fileName);
	}
        return;
    }


    /** returns an error message if a String was not found
     *@param st Reference to StreamTokenizer
     *@param fileName name of file (for error report only) or <CODE>null</CODE> pointer
     *@exception IOException
     */
    private static String stringNotFound(StreamTokenizer st, String fileName)
    {
        String fn = new String();
        if (fileName == null)
            fn = new String("");
        else
            fn = new String(fileName);
        String em = new String(fn) + new String
	    ("(Line " + st.lineno() +
	     "): InputFormatException: String expected, got '");

        switch (st.ttype)
	    {
	    case (StreamTokenizer.TT_NUMBER):
		em = new String(em + st.nval);
		break;
	    case (StreamTokenizer.TT_EOF):
		em = new String(em + "EOF");
		break;
	    case (StreamTokenizer.TT_EOL):
		em = new String(em + "EOL");
		break;
	    default:
		em = new String(em +  String.valueOf((char)st.ttype));
	    }
        return em = new String(em + "'.");
    }



    /** gets <I>various keyword values</I> from the current position in the stream.<BR>
     * The value must be enclosed between <CODE>firstDelimiter</CODE> and
     *        <CODE>secondDelimiter</CODE><LI>
     * The StreamTokenizer is read until either all keywords are read, the end of 
     * a section occurs or until end-of-file occurs.<LI>
     * All found keywords are written into sk.<LI>
     * Multiple occurences of a keyword leads to an error report 
     *       whereas the last occured value is written into sk.</LI>
     *@param st Reference to StreamTokenizer
     *@param firstDelimiter first delimiter of the searched object
     *@param secondDelimiter second delimiter of the searched object
     *@param sk String array with all keywords.
     *@param sv Object array where all found values of the keyword will be written in
     *          (sk and sv must have the same length)
     *@param e reference to InputFormatException. Error messages are written into 
     *         this Object
     *@param type Type specification of the object (either <CODE>String</CODE>,
     *                 <CODE>boolean</CODE>, <CODE>Double</CODE> or <CODE>Integer</CODE>
     *@param fn name of file (for error report only) or <CODE>null</CODE> pointer
     *@param flag flag whether necessarily all values must be set or not.<BR>
     *            (If all values must be set, pass the constant <CODE>Token.ALL</CODE>,
     *            therwise pass <CODE>Token.PART</CODE>)
     *@exception IOException
     */
    private static void _getValue(StreamTokenizer st, char firstDelimiter, 
				  char secondDelimiter, String[] sk, Object[] sv, 
				  InputFormatException e, 
				  String type, String fn, int flag)
	throws IOException {
        setWordChars(st);
        st.slashStarComments(true);
        st.eolIsSignificant(false); // neglect EOL
        st.quoteChar(QUOTECHAR);

        boolean er=false;       // flag that we found an error
        boolean match = false;
        String em = null;       // error message String
        String tempS = null;    // temporar String
        int i;
        int iSetLast = -1;
        int iLack;              // number of entries that are not set yet
        int n = sk.length;      // number of keywords
        int[] c = new int[n];   // counter for how many times a string was specified
        do{
	    skipJavaComments(st);
	    st.nextToken();
	    
	    if (st.ttype == StreamTokenizer.TT_WORD || st.ttype == '"' ) {
		iSetLast = Math.min(++iSetLast, n-1);
		
		if (type.equals("String")){
		    if (st.sval.equals(sk[iSetLast]))
			match = true; // ensures sequential filling
		    else
			match = false;
		}
		
		for (i = 0; i < n; i++){ // check if founded word is a keyword
		    if (match) i = iSetLast;
		    
		    if (st.sval.equals(sk[i])){  // we got a known keyword
			try{
			    if (type.equals("String")){
				sv[i] = new String(Token._getStringValue(st, '=', ';', fn));
			    }
			    else if (type.equals("Boolean")) {
				tempS = new String(Token._getStringValue(st, '=', ';', fn));
				sv[i] = new Boolean(tempS);
				if (!(tempS.equals("true") || tempS.equals("false"))) {
				    em = new String("expected 'true' or 'false', got '" + tempS + "'.");
				    er = true;
				}
			    }
			    else if (type.equals("Double"))
				sv[i] = new Double(Token._getDoubleValue(st, '=', ';', fn).doubleValue());
			    else if (type.equals("Integer"))
				sv[i] = new Integer(Token._getIntegerValue(st, '=', ';', fn).intValue());

			    c[i]++;  // entry is set
			    iSetLast = i;
			    // check for parsing error
			    if (er) {
				if (fn == null)
				    em = new String
					("(Line " + st.lineno() +
					 "): InputFormatException: Keyword '" +
					 new String(sk[i]) + "': " + new String(em));
				else
				    em = new String(fn) + new String("(Line " + st.lineno() +
								     "): InputFormatException: Keyword '" +
								     new String(sk[i]) + "': " + new String(em));
				er = false;
				throw new InputFormatException(em);
			    }
			    
			    
			    if (c[i] > 1) { // we found word more than once
				if (fn == null)
				    em = new String("(Line " + st.lineno() +
						    "): InputFormatException: Multiple occurences of '" +
						    new String(sk[i]) + "'.");
				else
				    em = new String(fn) + new String("(Line " + st.lineno() +
								     "): InputFormatException: Multiple occurences of '" +
								     new String(sk[i]) + "'.");
				er = true;
			    }
			}
			catch (InputFormatException ife) {
			    em = new String(ife.getMessage());
			    er = true;
			}
			i = n+1; // escapes the loop for this TT_WORD
		    }
		}
		
		if (i == n){ // we found a word but it is not known
		    if (fn == null){
			    em = new String("(Line " + st.lineno() +
					    "): InputFormatException: Unknown or unexpected keyword '" +
					    st.sval + "'.");
			    er = true;
		    }
		    else {
			em = new String(fn) + new String
			    ("(Line " + st.lineno() +
			     "): InputFormatException: Unknown or unexpected keyword '" +
			     st.sval + "'.");
			er = true;
		    }
		}
		
	    }
	    else if (st.ttype != '}'){  // we have neither found a word nor the end of the section
		em = new String(stringNotFound(st, fn));
		er = true;
	    }
	    
// 20020528	    if (er && st.ttype != StreamTokenizer.TT_EOF && st.ttype != '}') { 
	    if(er){ 
		// set occured error into exception object
		e.setMessage(em);
		er = false;
		moveToEOL(st);
		if (e.getNumberOfErrors() > MAXERROR) return;				
	    }
	    // check whether we found all keywords
	    for (i = 0, iLack = 0; i < n && iLack < 1; i++)
		if (c[i] == 0) iLack++;
	} while (st.ttype != StreamTokenizer.TT_EOF && iLack > 0 && st.ttype != '}');

        if (st.ttype == '}') st.pushBack(); // we do not want to get out of a section
	
        if (iLack > 0 && flag == ALL) {   // we must find all values but not all were found
	    for (i = 0; i < n; i++){
		if(c[i] == 0) {
		    if (fn == null){
			em = new String("(Line " + st.lineno() +
					"): InputFormatException: Missing specification of '" + 
					sk[i] + "'.");
		    }
		    else {
			em = new String(fn) + new String
			    ("(Line " + st.lineno() +
			     "): InputFormatException: Missing specification of '" + 
			     sk[i] + "'.");
		    }
		    e.setMessage(em);
		}
	    }
	}
        return;
    }

    /** gets a <I>single Double value</I> from the current position in the stream.<BR>
     * The value must be enclosed between <CODE>firstDelimiter</CODE> and
     *        <CODE>secondDelimiter</CODE>
     *@param st Reference to StreamTokenizer
     *@param firstDelimiter first delimiter of the searched object
     *@param secondDelimiter second delimiter of the searched object
     *@param fn name of file (for error report only) or <CODE>null</CODE> pointer
     *@return the Double between <CODE>firstDelimiter</CODE> and
     *        <CODE>secondDelimiter</CODE>
     *@exception IOException
     *@exception InputFormatException if the input is mal formatted
     */
    private static Double _getDoubleValue(StreamTokenizer st, char firstDelimiter,
					 char secondDelimiter, String fn)
        throws IOException, InputFormatException {
        return ((Double)_getValue(st, firstDelimiter, secondDelimiter, "Double", fn));
    }

    /** gets a <I>single Integer value</I> from the current position in the stream.<BR>
     * The value must be enclosed between <CODE>firstDelimiter</CODE> and
     *        <CODE>secondDelimiter</CODE>
     *@param st Reference to StreamTokenizer
     *@param firstDelimiter first delimiter of the searched object
     *@param secondDelimiter second delimiter of the searched object
     *@param fn name of file (for error report only) or <CODE>null</CODE> pointer
     *@return the Integer between <CODE>firstDelimiter</CODE> and
     *        <CODE>secondDelimiter</CODE>
     *@exception IOException
     *@exception InputFormatException if the input is mal formatted
     */
    private static Integer _getIntegerValue(StreamTokenizer st, char firstDelimiter,
					   char secondDelimiter, String fn)
        throws IOException, InputFormatException {
        return ((Integer)_getValue(st, firstDelimiter, secondDelimiter, "Integer", fn));
    }
	

	
    /** checks whether we are at the end of the Stream
     *@param st Reference to StreamTokenizer
     *@param e reference to InputFormatException. Error message is written into 
     *         this Object
     *@param fn name of file (for error report only) or <CODE>null</CODE> pointer
     *@return <CODE>true</CODE> if end of Stream is reached, <CODE>false</CODE> 
     *        otherwise
     */
    public static boolean isEndOfStream(StreamTokenizer st, InputFormatException e, 
					String fn)
    {
	try
	    {
		Token.skipJavaComments(st);
		st.eolIsSignificant(false);
		st.nextToken();
	    }
	catch(IOException ioe)
	    {
		e.setThrowable(ioe);
	    }
			
	if ( ! (st.ttype == StreamTokenizer.TT_EOF))
	    {	// we got some other entries
		String em = "Expected end of file, got '";
		if (st.ttype == StreamTokenizer.TT_NUMBER)
		    em += String.valueOf(st.nval);
		else if (st.ttype == StreamTokenizer.TT_WORD)
		    em += st.sval;
		else
		    em += (char)st.ttype;
		em += "'.";
		setError(st, e, em, fn);
		return false;
	    }
	else
	    return true;
    }
}







