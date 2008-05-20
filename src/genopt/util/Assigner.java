package genopt.util;

/** Object to substitute references to variables by its numerical values.
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

public class Assigner
{
	/** System dependent line separator */
	private final static String LS = System.getProperty("line.separator");

	/** assigns to all keyWords its value by eliminating all references to a keyword with its value
	  * @param keyWords the keyWords
	  * @param values the values of the keywords
	  * @return the values where no value has any reference to a keyWord
	  * @exception IllegalArgumentException
	  */
	public static String[] assign(String[] keyWords, String[] values)
		 throws IllegalArgumentException, RuntimeException{
	    final int n = keyWords.length;
	    if ( n != values.length )
		throw new RuntimeException("Assigner.assign(): Arguments must have equal length.");
	    if ( n == 0 ) 
		return values;
	    int counter = 0;
	    final int counterMax = n*n*n*10;
	    int[][] p = new int[n][n];
	    boolean[] a = new boolean[n]; // assigned flag
	    
	    p = getReferences(keyWords, values);
	    checkValidInput(p);
	    
	    a = getAssigned(p);
	    
	    while (!isAllTrue(a)){
		updateValues(keyWords, values, p);
		p = getReferences(keyWords, values);
		a = getAssigned(p);
		if (counter > counterMax){
		    String em = new String("Cannot sort variables.");
		    throw new IllegalArgumentException(em);
		}
		else
		    counter++;
	    }
	    return values;
	}
    
	/** checks for valid input syntax (cycles and references to itself are not allowed)
	  * @exception IllegalArgumentException if variables cannot be assigned or if a reference
	  *            to itself occurs
	  */
	private static void checkValidInput(int[][] p) throws IllegalArgumentException
	{
	    int n = p[0].length;
	    for(int i = 0; i < n; i++)
		{
		    for (int j = 0; j < n; j++)
			{
			    if (p[i][j] != -1)
				{	// check for cycles
				    for(int k = 0; k < n; k++)
					{
					    if (p[ p[i][j] ][k] == i)
						{
						    String em = new String("Implicit variable reference. " +
									   "Cannot sort variables.");
						    throw new IllegalArgumentException(em);
						}
					}
				    // check for reference to itself
				    if (p[i][j] == i)
					{
					    String em = new String("Variable refers to itself." + LS +
								   "Cannot sort variables.");
					    throw new IllegalArgumentException(em);
					}
				}
			}
		}
	}

	/** updates the values by eliminating the keyWord references
	  * @param keyWords the keyWords
	  * @param values   the values of the keywords
	  * @param p NxN array which has a number in each element that points to the keyWord which
	  *        is referenced. If there is no reference, then the element is equal to <CODE>-1</CODE>
	  */
	private static void updateValues(String[] keyWords, String[] values, int[][] p)
	{
	    final int n = keyWords.length;
	    int st;
	    for(int i = 0; i < n; i++){
		for (int j = 0; j < n; j++){
		    if (p[i][j] != -1){
			st = values[ p[i][j] ].indexOf(keyWords[i]);
			if (st != -1){
			    values[p[i][j]] = new String(values[p[i][j]].substring(0, st) +
							 values[i] + values[p[i][j]].substring(st + keyWords[i].length()));
			}
		    }
		}
	    }
	}

	/** gets an array which contains <CODE>true</CODE> at the i-th element if
	  * the 0-th element of the i-th row of p is equal to <CODE>-1</CODE>
	  * @param p NxN array which has a <CODE>-1</CODE> as the first element of the i-th column
	  *          if there is no further assignement to the i-th variable
	  * @return array which contain <CODE>true</CODE> at the i-th element if the i-th element
	  *         is not refered anywhere, <CODE>false</CODE>otherwise
	  */
	private static boolean[] getAssigned(int[][] p)
	{
		int n = p.length;
		boolean[] r = new boolean[n];
		
		for (int i = 0; i < n; i++)
		{
			if (p[i][0] == -1)
				//variable not referenced
				r[i] = true;
			else
				r[i] = false;
		}
		return r;
	}

	/** checks whether all values of <CODE>b</CODE> are true
	  * @param b the array to be checked
	  * @return <CODE>true</CODE> if all elements are true,
	  *         <CODE>false</CODE> otherwise
	  */
	private static boolean isAllTrue(boolean[] b)
	{
		for (int i = 0; i < b.length; i++)
			if (!b[i])
				return false;
		return true;
	}

	/** gets all references to a <CODE>keyWords</CODE>
	  * @param keyWords the keyWords
	  * @param values   the values of the keywords
	  * @return a NxN matrix that contains in its i-th column all references to the
	  *         i-th <CODE>keyWords</CODE> in <CODE>values</CODE>
	  */
	private static int[][] getReferences(String[] keyWords, String[] values)
	{
		int n = keyWords.length;
		int[][] r = new int[n][n];
		int i, j, k;
		boolean stay;
		for (i = 0; i < n; i++)
			for (j = 0; j < n; j++)
			r[i][j] = -1;

		for (i = 0; i < n; i++)
		{
			for (j = 0; j < n; j++)
			{
				if (values[j].indexOf(keyWords[i]) != -1)
				{	// values[j] contains a reference to keyWords[i]
					k = 0;
					stay = true;
					do
					{
						if (r[i][k] == -1)
						{
							r[i][k] = j;  // define reference
							stay = false;
						}
						k++;
					} while(stay && k < n);
				}
			}
		}
		return r;
	}

/*	public static void main(String[] args)
	{
		int n = 5;
		String[] kw = new String[n];
		String[] val = new String[n];
		kw[0] = "a";  val[0] = "b";
		kw[1] = "b";  val[1] = "c";
		kw[2] = "c";  val[2] = "d";
		kw[3] = "d";  val[3] = "e";
		kw[4] = "e";  val[4] = "a";

		for (int i = 0; i < n; i++)
			System.out.println(kw[i] + " = " + val[i]);

		val = assign(kw, val);

		System.out.println();
		for (int i = 0; i < n; i++)
			System.out.println(kw[i] + " = " + val[i]);

	}
	*/
}







