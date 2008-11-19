package genopt.algorithm.util.math;

/** Mathematical functions for binary numbers.<P>
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
  * <LI>the Swiss National Energy Binaryd (NEFF), and
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

public class Binary{

    /** The number of bits used to present binary numbers */
    private final static int BIT_SIZE = 32;

    /** Empty constructor.
     *
     * This class has only <CODE>static</CODE> methods and fields.
     */
    private Binary(){}

    /** Gets the L2 norm.
     *
     *@param i an integer array
     *@param j an integer array
     *@return the L2 norm
     *@exception IllegalArgumentException if the arguments have different length
     */
    public static final long getL2Norm(int[] i, int[] j){
	if ( i.length != j.length )
	    throw new IllegalArgumentException("Arguments have different length.");
	long r = 0;
	for (int k = 0; k < i.length; k++){
	    final long diff = i[k] - j[k];
	    if ( diff > 0 )
		r += diff;
	    else if ( diff < 0 )
		r -= diff;
	}
	return r;
    }

    /** Returns a <CODE>char[]</CODE> representation of the long 
     * argument as an unsigned integer in base 2.
     *@param i a <CODE>long</CODE> to be converted to a <CODE>char[]</CODE>.
     *@return the <CODE>char[]</CODE> representation of the 
     * unsigned <CODE>long</CODE> value represented by the argument 
     * in binary (base 2).
     */
    public static char[] toBinaryChar(long i){
	return (Long.toBinaryString(i)).toCharArray();
    }

    /** Returns an <CODE>int[]</CODE> representation of the long 
     * argument as an unsigned integer in base 2.
     *@param i a <CODE>long</CODE> to be converted to an <CODE>int[]</CODE>.
     *@return the <CODE>int[]</CODE> representation of the 
     * unsigned <CODE>long</CODE> value represented by the argument 
     * in binary (base 2).
     */
    public static int[] toBinaryInt(long i){
	return toBinaryInt( toBinaryChar(i) );
    }

    /** Returns an <CODE>int[]</CODE> representation of the long 
     * argument as an unsigned integer in base 2.
     *@param i a <CODE>long</CODE> to be converted to an <CODE>int[]</CODE>.
     *@param length number of elements of the returned array
     *@return the <CODE>int[]</CODE> representation of the 
     * unsigned <CODE>long</CODE> value represented by the argument 
     * in binary (base 2).
     *@exception IllegalArgumentException if <CODE>length</CODE> is
     *  not long enough to represent the argument as a binary integer array
     */
    public static int[] toBinaryInt(long i, int length){
	return toBinaryInt( increaseLength( toBinaryChar(i), length ) );
    }

    /** Gets the length of the binary string required to represent
     * the argument.
     *
     *@param n argument whose required length will be computed.
     *@return the length of the binary string required to represent
     * the argument.
     *@exception IllegalArgumentException  if <CODE>n < 0</CODE>
     */
    public static int getStringLength(long n)
    throws IllegalArgumentException{
	return toBinaryChar( getGrayCode(n) ).length;
    }

    /** Returns a <CODE>String</CODE> representation of the argument
     * that contains <CODE>0</CODE> or <CODE>1</CODE>.
     *
     *@param intArr an <CODE>int[]</CODE> array containing <CODE>0</CODE> or <CODE>1</CODE>
     *@return the <CODE>String</CODE> representation of the argument
     *@exception IllegalArgumentException if the argument contains non-binary
     *           numbers
     */
    static private final String binaryToString(final int[] intArr)
    throws IllegalArgumentException{
	char[] r = new char[intArr.length];
	for(int i = 0; i < intArr.length; i++)
	    switch (intArr[i]){
	    case 0:
		r[i] = '0';
		break;
	    case 1:
		r[i] = '1';
		break;
	    default:
		throw new IllegalArgumentException("Argument contains non-binary numbers.");
	    }
	return new String(r);
    }

    /** Returns a <CODE>String</CODE> representation of the argument
     * that contains <CODE>0</CODE> or <CODE>1</CODE>.
     *
     *@param intArr an <CODE>int[]</CODE> array containing <CODE>0</CODE> or <CODE>1</CODE>
     *@param length the length of the string array that will be returned
     *@return the <CODE>String</CODE> representation of the argument
     *@exception IllegalArgumentException if the argument contains non-binary
     *           numbers
     */
    static private final String binaryToString(final int[] intArr,
					       int length)
    throws IllegalArgumentException{
	char[] r = new char[intArr.length];
	for(int i = 0; i < intArr.length; i++)
	    switch (intArr[i]){
	    case 0:
		r[i] = '0';
		break;
	    case 1:
		r[i] = '1';
		break;
	    default:
		throw new IllegalArgumentException("Argument contains non-binary numbers.");
	    }
	return new String( increaseLength(r, length) );
    }

    /** Gets the Gray code.
     *
     *@param n non-negative argument whose length will be computed
     *@return the Gray code
     *@exception IllegalArgumentException if <CODE>n < 0</CODE>
     */
    static public long getGrayCode(final long n)
	throws IllegalArgumentException{
	if ( n < 0 )
	    throw new IllegalArgumentException("Only non-negative values allowed. Received '" + 
					  n + "'.");
	return ieor(n, n/2);
    }

    /** Converts a <CODE>char</CODE> array with binary values to
     *  a binary <CODE>int</CODE> array.
     *
     *@param c a <CODE>char</CODE> array with <CODE>0</CODE> and <CODE>1</CODE>
     *         elements
     *@return <CODE>c</CODE> converted to an <CODE>int</CODE> array
     *@exception IllegalArgumentException if <CODE>c</CODE> is not a binary array
     */
    static private int[] toBinaryInt(char[] c){
	int[] r = new int[c.length];
	for(int i = 0; i < c.length; i++)
	    switch (c[i]){
	    case '0':
		r[i] = 0;
		break;
	    case '1':
		r[i] = 1;
		break;
	    default:
		throw new IllegalArgumentException("Cannot convert '" + 
						   new String(c) + "'.");
	    }
	return r;
    }
    
    /** Converts a binary <CODE>int</CODE> array to 
     * the long number it presents.
     *
     *@param intArray a binary <CODE>int</CODE> array
     *@return the long number that is presented by the argument
     */
    static private long binaryToLong(int[] intArray){
	long r = 0;
	long mul = 1;
	for(int i = intArray.length-1; i >= 0; i--){
	    if ( i != intArray.length-1)
		mul *= 2;
	    assert ( intArray[i] == 0 || intArray[i] == 1 );
	    r += ( intArray[i] * mul );
	}
	return r;
    }

    /** Converts a binary <CODE>char</CODE> array to 
     * the long number it presents.
     *
     *@param charArray a binary <CODE>int</CODE> array
     *@return the long number that is presented by the argument
     *@exception IllegalArgumentException if <CODE>c</CODE> is not a binary array
     */
    static private long binaryToLong(char[] charArray)
	throws IllegalArgumentException{
	return binaryToLong( toBinaryInt( charArray ) );
    }
    
    /** Gets the inverse of the Gray code of a binary <CODE>int</CODE> array.
     *
     * <CODE>gray</CODE> is first encoded in a <CODE>long</CODE> value,
     * and then the inverse Gray coding of the <CODE>long</CODE> is
     * computed and returned.
     *
     *@param gray an <CODE>int[]</CODE> argument with binary numbers
     *@return the inverse of <CODE>gray</CODE>, using inverse
     *        Gray coding
     */
    static public long getInverseGrayCode(final int[] gray){
	return getInverseGrayCode( binaryToLong(gray) );
    }

    /** Gets the inverse of the Gray code.
     *
     *@param gray a number in Gray code
     *@return the inverse of <CODE>gray</CODE>, using inverse
     *        Gray coding
     */
    static public long getInverseGrayCode(final long gray){
	int ish = -1;
	long r = gray;
	boolean finished;
	do{
	    final long idiv = ishft(r, ish);
	    r = ieor(r, idiv);
	    finished = ( ( idiv <= 1 ) || ( ish == -BIT_SIZE ) );
	    ish += ish;
	}while( ! finished );
	return r;
    }

    /** Increases the length of a <CODE>char</CODE> array by
     *  setting additional elements with 0.
     * 
     *  If <CODE> n=binStr.length < length </CODE>, then
     *  the return argument has <CODE>0</CODE> at the 
     *  elements <CODE>0</CODE> to <CODE>lenth-n</CODE>.
     *
     *@param binStr the binary string string to be extended
     *@param length the length of the returned binary string
     *@return <CODE>binStr</CODE> with increased length
     *@exception IllegalArgumentException if <CODE>binStr.length > length</CODE>
     */
    static public final char[] increaseLength(final char[] binStr, 
					      final int length)
    throws IllegalArgumentException{
	if (binStr.length > length) 
	    throw new IllegalArgumentException("binStr is too long. binStr.length = " + 
					       binStr.length +", length =" +
					       length);
	
	final int offSet = (length - binStr.length);
	if (offSet == 0)
	    return binStr;
	else{
	    char[] r = new char[length];
	    
	    for(int i = 0; i < offSet; i++)
		r[i] = '0';
	    for(int i = offSet; i < length; i++)
		r[i] = binStr[i-offSet];
	    return r;
	}
    }
    
    /** Computes the value of a bitwise exclusive or between the arguments.
     *
     *@param i first argument
     *@param j second argument
     *@return the value of a bitwise exclusive or between the arguments
     */
    static public final long ieor(final long i, final long j){
	// make sure that bi is the longer char array than bj
	char[] bi = (Long.toBinaryString( i )).toCharArray();
	char[] bj = (Long.toBinaryString( j )).toCharArray();
	if (bi.length > bj.length)
	    bj = increaseLength(bj, bi.length );
	else if (bj.length > bi.length)
	    bi = increaseLength(bi, bj.length );
	final int len = bi.length;	
	for(int k = 0; k < len; k++){
	    bi[k] = ( bi[k] == bj[k] ) ? '0' : '1';
	}
	return binaryToLong( bi );
    }

    /** Computes the value obtained by shifting the bits of <CODE>i</CODE>
     *  by <CODE>shift</CODE> positions.
     *
     * If <CODE>shift</CODE> is positive, the shift is to the left, 
     * otherwise to the right.
     * Bits shifted off the left or right are lost and zero bits are 
     * shifted in from the opposite end.
     *
     *@param i value whose bits will be shifted
     *@param shift number of positions that the bits will be shifted
     *@return the value of <CODE>i</CODE> after shifting the bits
     */
    static public final long ishft(final long i, final int shift){
	char[] bs = increaseLength((Long.toBinaryString(i)).toCharArray(),
				   BIT_SIZE);
	if ( shift > 0){ // shift to the left
	    for(int k = 0; k < bs.length; k++){
		final int src = k+shift;
		bs[k] = ( src < bs.length ) ? bs[src] : '0';
	    }
	}
	else{ // shift to the right
	    for(int k = bs.length-1; k >= 0; k--){
		final int src = k+shift;
		bs[k] = ( src >= 0 ) ? bs[src] : '0';
	    }
	}
	final String str = new String(bs);
	final long r = Long.parseLong(str, 2);
	return r;
    }

    public static void main(String[] args){
	final long iMin = 0;
	final long iMax = 10000+iMin;
	final int sLen = getStringLength(iMax-1);
	int[] bPrev = new int[sLen];
	for(long i = iMin; i < iMax; i++){
	    System.out.print(" i = " + i);
	    long gray = getGrayCode( i );
	    System.out.print( "\t Gray = " + String.valueOf( gray ));
	    
	    long rev = getInverseGrayCode( gray );
	    if ( rev != i ){
		System.err.println("Wrong result!");
		System.exit(1);
	    }
	    System.out.print("\t; inverse = " + rev);

	    int[] bi = toBinaryInt( gray, sLen );
	    if ( i != iMin) {
		if ( getL2Norm(bi, bPrev) != 1 ){
		    System.err.println("Wrong hamming distance!");
		    System.exit(1);
		}
	    }
	    bPrev = bi;
	    System.out.println("\t; bin = " + binaryToString(bi, sLen) ); 
	}
	//	    System.out.println("****************************************************************");
    }
}
