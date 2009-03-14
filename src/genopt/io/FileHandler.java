package genopt.io;
import java.io.*;
import java.io.FileWriter;
import java.util.StringTokenizer;

/** Object that handles file reading and writing and offers
  *   various manipulation and access methods of the file contents.
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
  * @version GenOpt(R) 3.0.0 beta 2 (February 23, 2009)<P>
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

/* Data storing:
   The whole content of the file 'FileName' is set in a array of String
*/

public class FileHandler implements Cloneable{
    /** System dependent line separator */
    private final static String LS = System.getProperty("line.separator");
    /** System dependent file separator */
    protected final static String FS = System.getProperty("file.separator");

    /** Increment for array that stores the Strings */
    private final int INCREMENT = 1;
    // leave this at 1: If set to one, then file reading is much faster.
    // For example, changing this from 1 to 5 results in a 6 times
    // longer CPU time for reading an annual weather file.


    /** constructor
	  * @param FileLines where
	  *    each element is a line of the file stored in a String
	  */
    public FileHandler(String[] FileLines)
    {
	nLines = FileLines.length;
	FileContents = new String[nLines];
	System.arraycopy(FileLines, 0, FileContents, 0, nLines);
    }

    /** constructor
	  * @param pathAndName path and name of file
	  * @exception IOException
	  */
    public FileHandler(String pathAndName) throws IOException
    {
	this(new File(pathAndName));
    }

    /** constructor
	  * @param path path of file (or null pointer if file is in current
	  *    directory
	  * @param name name of file
	  * @exception IOException
	  */
    public FileHandler(String path, String name) throws IOException
    {
	this(new File(path, name));
    }
	
    /** Constructor.
	  * @param theFile the file
	  * @exception IOException
	  * @exception FileNotFoundException
	  * @exception SecurityException	  
	  */
    public FileHandler(File theFile) 
	throws IOException, FileNotFoundException, SecurityException
    {
	nLines = 0;
	FileContents = new String[30];
	String filNam = theFile.getAbsolutePath();
	FileInputStream fis = new FileInputStream(filNam); 
	DataInputStream din = new DataInputStream(fis);
        BufferedReader br = new BufferedReader(new InputStreamReader(din));
	String linStr; // the line to be read
	//Read File Line By Line
	while ((linStr = br.readLine()) != null){
	    linStr += LS;
	    // Print the content on the console
	    addElement(linStr);
	}
	//Close the input stream
	din.close();
	fis.close();
    }


    /** Appends the text to a file and closes the file again
	  *@param fileName file path and name where the text has to be appended
	  *@param text text to be written in the file
	  *@exception IOException
	  */
    public static void appendToFile(String fileName, String text) throws IOException
    {
	try
	    {
		FileWriter FilWri = new FileWriter(fileName, true);
		FilWri.write(text);
		FilWri.close();
	    }
	catch(IOException  e)
	    {
		String ErrMes =
		    "IOException while writing file: '" 
		    + fileName + "': " + e.getMessage();
		throw new IOException(ErrMes);
	    }
    }


    /** adds an element to the object
     *@param s String to be added
     */
    private void addElement(final String s)
    {
	if (++nLines >= FileContents.length)
	    FileContents[nLines-1] = new String(s);
	{
	    String[] temp = new String[FileContents.length];
	    System.arraycopy(FileContents, 0, temp, 0, temp.length);
	    FileContents = new String[FileContents.length + INCREMENT];
	    System.arraycopy(temp, 0, FileContents, 0, temp.length);
	}
	FileContents[nLines-1] = new String(s);
    }

    /** checks if the byte is a line feed ('\n') or a carriage return ('\r')
	  *@param b the byte value to be checked
	  *@return <CODE>true</CODE> if <CODE>b</CODE> is a line feed or a carriage return
	  */ 
    public static final boolean isEndOfLine(int b)
    {
	char c = (char)b;
	return ( ('\n' == c) || ('\r' == c) ) ? true : false;
    }

    /** replaces all occurences of the String 'find' with 
	  *    the String 'set' (even if 'find' appears several times
	  *    on the same line and/or in the same file)
	  * @param Find String that has to be found and replaced
	  * @param Set String that has to be set at the position of 'Find'
	  * @return <code>true</code> if 'Find' has been found, <code>false</code> otherwise
	  */
    public boolean replaceString(String Find, String Set)
    {	
	boolean r = false;
	// get the single strings out of the array, replace the 
	// 'Find' with 'Set'
	for (int iL = 0; iL < nLines; iL++){
	    if ( (!r) ){
		final String old =  new String(FileContents[iL]);
		FileContents[iL] = FileHandler.replaceAll(Find, Set, FileContents[iL]);
		r =  ( ! FileContents[iL].equals(old) );
	    }
	    else
		FileContents[iL] = FileHandler.replaceAll(Find, Set, FileContents[iL]);
	}
	return r;
    }

    /** Replaces all paths with their canonical paths
     *
     *@param str The original string
     *@return A copy of <tt>str</tt> with all paths replaced by their canonical path
     *@exception IOException If an I/O error occurs, which is possible because the construction of the 
     *                        canonical pathname may require filesystem queries
     */
    public static String[] replacePathsByCanonicalPaths(final String str[], final String userDir)
	throws IOException{
	if ( str == null )
	    return null;
	String[] r = new String[str.length];
	for(int i = 0; i < str.length; i++)
	    r[i] = FileHandler.replacePathsByCanonicalPaths(str[i], userDir);
	return r;
    }

    /** Replaces all paths with their canonical paths
     *
     *@param str The original string
     *@return A copy of <tt>str</tt> with the path replaced by its canonical path
     *@exception IOException If an I/O error occurs, which is possible because the construction of the 
     *                        canonical pathname may require filesystem queries
     */
    public static String replacePathsByCanonicalPaths(final String str, final String userDir)
	throws IOException{
	StringTokenizer st = new StringTokenizer(str, " \"", true);
	String r = "";
	while (st.hasMoreTokens()) {
	    String tok = st.nextToken();
	    if (tok.startsWith(".")){   // This adds, for example, to ./simulate.sh the full path name
		tok = userDir + FS + tok;
	    }
	    File fil = new File(tok);
	    if ( fil.isFile() || fil.isDirectory() )
		r += fil.getCanonicalPath();
	    else
		r += tok;
	} 
	return r;
    }


    /** Adds the canonical path name to the string, unless the string is already a canonical path 
     *  or is an empty character sequence
     *
     *@param str The original strings
     *@return A copy of <tt>str</tt> that denotes a canonical path name that may not exist
     *           on the file system
     *@exception IOException If an I/O error occurs, which is possible because the construction of the 
     *                        canonical pathname may require filesystem queries
     */
    public static String[] addCanonicalPaths(final String str[], final String userDir)
	throws IOException{
	if ( str == null )
	    return null;
	String[] r = new String[str.length];
	for(int i = 0; i < str.length; i++)
	    r[i] = FileHandler.addCanonicalPaths(str[i], userDir);
	return r;
    }


    /** Adds the canonical path name to the string, unless the string is already a canonical path
     *  or is an empty character sequence
     *
     *@param str The original string
     *@return A copy of <tt>str</tt> that denotes a canonical path name that may not exist
     *           on the file system
     *@exception IOException If an I/O error occurs, which is possible because the construction of the 
     *                        canonical pathname may require filesystem queries
     */
    public static String addCanonicalPaths(final String str, final String userDir)
	throws IOException{
	// Return empty characters. This is needed to preserve SavePath entries that are not set by the user
	if ( str.trim().equals("") )
	    return new String(str);

	String r;
	// First, replace all paths by their canonical paths
	String str2 = replacePathsByCanonicalPaths(str, userDir);
	File fil = new File(str2);
	if (fil.isAbsolute())
	    r = new String(str2);
	else
	    r = new String( (new File(userDir + FS + str2)).getCanonicalPath() );
	return r;
    }


    /** replaces all occurences of the String 'find' with 
     *    the String 'set' (even if 'find' appears several times
     *    on the same line)
     * @param find String that has to be found and replaced
     * @param set String that has to be set at the position of 'Find'
     * @param line line on which replacement takes place
     * @return the String after all replacements
     */
    public static String replaceAll(String find, String set, String line)
    {	
	final int finLen = find.length();
	final int setLen = set.length();
	
	int j;
	int k = 0;
	String r = new String(line);
	do{
	    j = r.indexOf(find, k);
	    if (j != -1){  // Find was found
		r = new String(r.substring(0, j)
			       + set + r.substring(j+finLen));
		k = j + setLen;
	    }
	} while (j != -1);
	return r;
    }


    /** gets the number of the line where 'MatchString' occurs the
	  * first time. The file contents is searched for 'MatchString'
	  * beginning at line Number 'StartLine' and towards the end
	  * of the line. The first line has number 0.
	  * @param MatchString String that has to be searched for
	  * @param StartLine number of Line where the search process starts
	  * @return the line number in which 'MatchString' occurs
	  *   the first time or -1 if 'MatchString' was not found. 
	  */
    public int getLineNumberWithString(String MatchString, int StartLine)
    {
	int j;
	int LineNumber = StartLine - 1;

	do
	    {
		j = FileContents[++LineNumber].indexOf(MatchString);
	    }	while (j == -1 && LineNumber < (nLines-1) );
	return (j == -1) ? j : LineNumber;
    }

    /** Deletes directories, even if they are not empty
     *
     *@param path Name of the directory
     *@return <tt>true</tt> if and only if the file or directory is successfully deleted; <tt>false</tt> otherwise 
     *@exception SecurityException If a security manager exists and its SecurityManager.checkDelete(java.lang.String) method denies delete access to the file
     */
    static public boolean deleteDirectory(File path)
	throws SecurityException{
	if( path.exists() ) {
	    File[] files = path.listFiles();
	    for(int i=0; i<files.length; i++) {
		if(files[i].isDirectory()) 
		    deleteDirectory(files[i]);
		else
		    files[i].delete();
	    }
	}
	return( path.delete() );
    }

    /** gets the lowest position of two delimiters in 'Line'.
	  * If only one delimiter is found in 'Line', then the position
	  * of the found delimiter is returned.
	  * If no delimiter is found in 'Line', then -1 is returned.
	  * @param Line Line to search for the delimiters
	  * @param Delimiter1 First delimiter
	  * @param Delimiter2 Second delimiter
	  * @return Lowest position of any delimiter, either 'Delimiter1'
	  *   or 'Delimiter2'
	  */
    static public int getNextDelimiterPosition(
					       String Line, char Delimiter1, char Delimiter2)
    {
	int DelPos1 = Line.indexOf(Delimiter1);
	int DelPos2 = Line.indexOf(Delimiter2);
	if (DelPos1 == -1) return DelPos2;
	if (DelPos2 == -1) return DelPos1;
	return Math.min(DelPos1, DelPos2);
    }      

    /** gets the number of lines
	  * @return number of elements of the trimed Vector
	  */
    public int getNumberOfLines() { return nLines; }

    /** gets the file contents in a String array where each 
	  * entry contains the corresponding file line
	  * @return String array where each 
	  *   entry contains the corresponding file line
	  */
    public String[] getFileContentsString()
    {
	String[] r = new String[nLines];
	System.arraycopy(FileContents, 0, r, 0, nLines);
	return r;
    }

    /** copies file from source to destination
	  * @param sourcePath path of source file (or null pointer if file is in current
	  *    directory)
	  * @param sourceName name of source file
	  * @param destinationPath path of destination file (or null pointer if file is in current
	  *    directory)
	  * @param destinationName name of destination file
	  * @exception IOException
	  */
    public static void copyFile(String sourcePath,      String sourceName,
				String destinationPath, String destinationName)
	throws IOException
    {
	FileHandler f = new FileHandler(sourcePath, sourceName);
	f.writeFile(destinationPath, destinationName);
	return;
    }

    /** creates directories if they do not exist yet
     * @param path path that has to be created
     * @exception Exception if a SecurityException occured
     */
    public static void makeDirectory(String[] path)
	throws Exception
    {
	File dir;

	for (int iF=0; iF < path.length ; iF++){
	    if (!(path[iF].equals(""))){
		dir = new File(path[iF]);
		
		try {
		    if (!dir.exists()){
			if (!dir.mkdirs()){
			    throw new Exception("Cannot create directory '" +
						dir.getCanonicalPath() + "'.");
			}
		    }
		}
		catch (SecurityException e){
		    throw new Exception("SecurityException occured while creating directory '" +
					dir.getCanonicalPath() + "': Message '" + 
					e.getMessage() + "'.");
		}
	    }
	}
    }


    /** writes a String array in a text file
	  * @param text array of text that has to be written into the file
	  * @param path path of file (or null pointer if file is in current
	  *    directory)
	  * @param name name of file
	  */
    public static void writeFile(String[] text, String path, String name)
	throws IOException
    {
	String s = new String();
	for (int i = 0; i < text.length; i++)
	    s += text[i] + LS;
	writeFile(s, path, name);
	return;
    }

	
    /** writes a string to a text file
	  * @param text text that has to be written to the file
	  * @param path path of file (or null pointer if file is in current
	  *    directory)
	  * @param name name of file
	  */
    public static void writeFile(String text, String path, String name)
	throws IOException
    {
	if (path == null) path = ".";
	File TemFil = new File(path);
	if (!TemFil.exists())
	    TemFil.mkdirs();
	String FilNam = (path.equals(".")) ?
	    new String(name) : new String(path + TemFil.separator + name);
	FileWriter FilWri = new FileWriter(FilNam);
	FilWri.write(text);
	FilWri.close();
    }

    /** writes FileContents in a text file
	  * @param pathAndName path and name of file
	  */
    public void writeFile(String pathAndName)
	throws IOException
    {
	String FilNam = new String(pathAndName);
	FileWriter FilWri = new FileWriter(new String(FilNam.trim()));
	for (int i = 0; i < nLines; i++)
	    FilWri.write(FileContents[i], 0, FileContents[i].length());
	FilWri.close();
    }		

    /** writes FileContents in a text file
	  * @param path path of file (or null pointer if file is in current
	  *    directory)
	  * @param name name of file
	  */
    public void writeFile(String path, String name)
	throws IOException
    {
	File TemFil = new File(path);
	if (!TemFil.exists())
	    TemFil.mkdirs();
	String FilNam = TemFil.getAbsolutePath() + TemFil.separator + name;
	this.writeFile(FilNam);
    }

    /** cuts a leading <I>and</I> terminating string from a string.
	  * If the string has no leading <I>and</I> terminating characters, it is
	  * returned unmodified.<br>
	  * <b>Note:</b> the strings are cutted only once, that means a string of
	  * the form <CODE>"""abc""</CODE> is returned as <CODE>""abc"</CODE> if the
	  * prefix and suffix are specified as: <CODE>"</CODE>
	  *@param s the string
	  *@param prefix the leading string
	  *@param suffix the last string
	  *@return the cutted string
	  */
    public static String  cutBeginAndEnd(String s, String prefix, String suffix)
    {
	String sb  = new String(cutBegin(s, prefix));
	String sbe = new String(cutEnd(sb, suffix));
	return ((!s.equals(sb)) && (!sb.equals(sbe))) ? sbe : s;
    }

    /** cuts a suffix from a string.
	  * If the string is not beginning with the specified string, it is
	  * returned unmodified.<br>
	  *@param s the string
	  *@param prefix the prefix
	  *@return the cutted string
	  */
    public static String  cutBegin(String s, String prefix)
    {
	return (s.startsWith(prefix)) ? 
	    new String(s.substring(prefix.length())) : new String(s);
    }

    /** cuts a suffix from a string.
	  * If the string is not ending with the specified string, it is
	  * returned unmodified.<br>
	  *@param s the string
	  *@param suffix the suffix
	  *@return the cutted string
	  */
    public static String  cutEnd(String s, String suffix)
    {
	return (s.endsWith(suffix)) ? 
	    new String(s.substring(0, s.lastIndexOf(suffix))) : new String(s);
    }
	
    /** cuts a line break ('\n' and/or '\r') from the end of the String
	  *@param line the String to be cutted
	  *@return the String without line break
	 */	
    public static final String cutEndLineBreak(String line)
    {
	String r = new String(line);
	if (r.endsWith("\n"))
	    r = new String(r.substring(0, r.length()-1));
	if (r.endsWith("\r"))
	    r = new String(r.substring(0, r.length()-1));
	return r;
    }


    /** the content of the file */
    protected String[] FileContents;
    /** the number of lines in FileContents (occupied element in <CODE>FileContents</CODE>)*/
    protected int nLines;
}








