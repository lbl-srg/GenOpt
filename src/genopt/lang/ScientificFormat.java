package genopt.lang;

/** Object for parsing numbers to scientific format.
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
  * @version GenOpt(R) 3.0.0 alpha 3 (November 20, 2008)<P>
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

public class ScientificFormat
{
	/** constructor
	 *@param number the number to be parsed
	 *@param fieldWidth the width of the field
	 *@param digits the number of digits following the decimal point
	  */
	public ScientificFormat(double number, int fieldWidth, int digits)
	{
		num      = number;
		fieWidth = fieldWidth;
		dig      = digits;
	}
		
	/** sets the number of digits following the decimal point
	  *@param n the number of digits following the decimal point
	  */
	public void setDigits(int n) { dig = n; }
		
	/** gets the number of digits following the decimal point
	  *@return the number of digits following the decimal point
	  */
	public int getDigits() { return dig; }		

	/** sets the width of the field (including minus sign, excluding <CODE>E</CODE>)
	  *@param n the width of the field
	  */
	public void setFieldWidth(int n) { fieWidth = n; }
		
	/** gets the width of the field (including minus sign, excluding <CODE>E</CODE>)
	  *@return the width of the field
	  */
	public int getFieldWidth() { return fieWidth; }	

	/** gets the <CODE>double</CODE> representation of the Object
	  *@return the <CODE>double</CODE> representation of the Object
	  */
	public double doubleValue() { return num; }	

	/** gets the <CODE>String</CODE> representation of the Object
	  *@return the <CODE>String</CODE> representation of the Object
	  */
	public String toString()
	{
		return parseToString();
	}
	
	/** parses the the Object to a <CODE>String</CODE>
	  *@return the <CODE>String</CODE> representation of the Object
	  */
	protected String parseToString()
	{
		// parse 0
		if (num == 0)
		{
			String man = " ";
			while(man.length() <= fieWidth)
				man += "0";
			String expSig = "+";
			String expNum = "";
			int expLen    = 0;
			while (expLen < expWidth - 1)
			{
				expNum += "0";
				expLen++;
			}				
			return man + "E" + expSig + expNum;			
		}
		
		// parse any other number

		// mantissa and exponent
		String man = (num < 0) ? "-" : " ";
		if (num < 0)
			num = - num;
		int exp = (int)(Math.log(num)/LG_10);

		man += String.valueOf( (num / Math.pow(10, exp)));
		
		String expSig;
		if (exp < 0)
		{
			expSig = "-";
			exp = -exp;
		}
		else 
		    expSig = "+";

		String expDig = String.valueOf(exp);
		String expNum = "";
		int expLen    = expDig.length();

		while (expLen < expWidth - 1)
		{
			expNum += "0";
			expLen++;
		}
		while(man.length() <= fieWidth)
		    man += "0";
		return man.substring(0, fieWidth) + "E" 
		    + expSig + expNum + expDig;
	}

	/** the number */
	private double num;
	/** the field width */
	private int fieWidth;
	/** the number of digits following the decimal point */
	private int dig;
	/** the exponent width (including sign, excluding <CODE>E</CODE>)*/
	private static int expWidth = 4;
	/** the natural logarithm (base e) of 10 */
	private static double LG_10 = Math.log(10);

         public static void main(String[] args)
        {
	    int fW = 5;
	    int dig = 2;
	    double x = new Double(args[0]).doubleValue();
	    System.out.println( new ScientificFormat(
				x, fW, 
				dig).toString());
	}
    
}







