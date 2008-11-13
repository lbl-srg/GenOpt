package genopt.util;
import genopt.io.Token;
import java.io.*;
import java.util.*;

/** Object to read and store user preference.
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

public class Preference
{
	/** valid keywords */
	private static final String[][] MAP = {
		{"pane.layout",  "split.horizontal"},
		{"frame.width",  "800"},
		{"frame.height", "500"},
		{"frame.x",      "0"},
		{"frame.y",      "0"},
		{"file.ini.startUp", System.getProperty("user.home")},
		{"simulation.result.separator", ":;,"},
		{"debug",      "false"}};


	/** constructor
	  *@param preferenceFile the Preference file
	  */
	public Preference(File preferenceFile)
	{
		file = preferenceFile;
		list = new TreeMap<String, String>();
		setDefault();
		
		try
		{
			if (!file.exists())
			{
				File par = file.getParentFile();
				par.mkdirs();
				file.createNewFile();
			}
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);

			StreamTokenizer st = new StreamTokenizer(br);
			st.resetSyntax();
			st.wordChars(0, 255); // all chars
			st.ordinaryChar('=');
			st.ordinaryChar(';');
			st.ordinaryChar('\n');
			st.ordinaryChar('\r');
			st.quoteChar('\"');
			st.eolIsSignificant(false);
			int type;
			String key = "";
			type = st.nextToken();
			while (type  != StreamTokenizer.TT_EOF)
			{
				st.pushBack();
				Token.skipJavaComments(st);
				type = st.nextToken();
				if (type == StreamTokenizer.TT_WORD)
				{
					key = st.sval;
					if ((type = st.nextToken()) == '=')
					    if ((type = st.nextToken()) != StreamTokenizer.TT_EOF){
						list.put(key, st.sval);
					    }
				}
				// move to ';' or TT_EOF, whatever comes first
				st.pushBack();
				Token.skipJavaComments(st);
				type = st.nextToken();
				while(!(type == StreamTokenizer.TT_EOF || type == StreamTokenizer.TT_WORD))
				{
					type = st.nextToken();
				}
			}
			br.close();
		}
		catch (FileNotFoundException e) { }
		catch (IOException e) { }
	}

	/** Sets default values.
	  */
	private void setDefault()
	{
		for (int i = 0; i < MAP.length; i++)
			list.put(MAP[i][0], MAP[i][1]);
	}

    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for this key, the old
     * value is replaced.
     *
     * @param key key with which the specified value is to be associated.
     * @param value value to be associated with the specified key.
     * 
     * @return previous value associated with specified key, or <tt>null</tt>
     *	       if there was no mapping for key.  A <tt>null</tt> return can
     *	       also indicate that the map previously associated <tt>null</tt>
     *	       with the specified key.
     */
    public String put(String key, String value)
	{
	    return list.put(key, value);
	}
    /**
     * Returns the value to which this map maps the specified key.  Returns
     * <tt>null</tt> if the map contains no mapping for this key.  A return
     * value of <tt>null</tt> does not <i>necessarily</i> indicate that the
     * map contains no mapping for the key; it's also possible that the map
     * explicitly maps the key to <tt>null</tt>.  The <tt>containsKey</tt>
     * operation may be used to distinguish these two cases.
     *
     * @param key key whose associated value is to be returned.
     * @return the value to which this map maps the specified key, or
     *	       <tt>null</tt> if the map contains no mapping for the key.
     */
    public Object get(Object key) { return list.get(key); }

    /** Removes all mappings
     */
    public void clear() { list.clear(); }

	/** Writes the Preference file */
	public void write()
	{		
		String LS = System.getProperty("line.separator");		
		Object val;
		String endln = "\";" + LS; // we do need the \" for simulation.result.separator !
		String st = "// GenOpt Preference, GenOpt Version: " + genopt.GenOpt.VERSION + LS;
		st += "// " + new Date().toString() + LS;
		for (int i = 0; i < MAP.length; i++)
		{
			val = list.get(MAP[i][0]);
			if (val != null){
				st += MAP[i][0] + "=\"" + (String)val + endln;
			}
		}

		try
		{
			FileWriter fr = new FileWriter(file);
			fr.write(st, 0, st.length());
			fr.close();
		}
		catch (IOException e) { }
	}
	/** list with settings */
        protected TreeMap<String, String> list;
	/** Preference file */
	protected File file;

	public static void main(String[] args)
	{
		if (args.length == 0) {
				System.err.println("missing input filename");
				System.exit(1);
		}
		Preference pr = new Preference(new File(args[0]));
		if (args.length > 2) pr.put(args[1], args[2]);
		pr.write();
	}
}
