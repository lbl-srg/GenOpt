package genopt.db;

/** Object representing an ordered map, similar to HashMap, but order is preserved.
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

public class OrderedMap
{
    /** constructor
     */
    public OrderedMap()
    {
	map = new Object[2][0];
	nK = 0;
    }


    /** puts a pair (key,value) in the map
     *@param key key of the pair
     *@param value value of the pair
     */
    public void put(Object key, Object value)
    {
	increaseCapacity();
	map[0][nK-1] = key;
	map[1][nK-1] = value;
    }

    /** increases the capacity of the mapping
     */
    private void increaseCapacity()
    {
	nK++;
	Object[][] map2 = new Object[2][nK];
	System.arraycopy(map[0], 0, map2[0], 0, nK-1);
	System.arraycopy(map[1], 0, map2[1], 0, nK-1);
	map = map2;
    }

    /** returns <CODE>true</CODE> if the Object is a key in the Map, 
     ** <CODE>false</CODE> otherwise
     *@param o Object to be compared against the keys
     *@return <CODE>true</CODE> if Object is already in Map, 
     *        <CODE>false</CODE> otherwise
     */
    public boolean containsKey(Object o)
    {
	for (int i = 0; i < nK; i++)
	    if (map[0][i].equals(o))
		return true;
	return false;
    }

    /** returns <CODE>true</CODE> if the Object is a value in the Map, 
     ** <CODE>false</CODE> otherwise
     *@param o Object to be compared against the values
     *@return <CODE>true</CODE> if Object is already in Map, 
     *        <CODE>false</CODE> otherwise
     */
    public boolean containsValue(Object o)
    {
	for (int i = 0; i < nK; i++)
	    if (map[1][i].equals(o))
		return true;
	return false;
    }

    /** get the keys
     *@return the keys of the Map
     */
    public Object[] getKeys()
    {
	return map[0];
    }

    /** get the values
     *@return the values of the Map
     */
    public Object[] getValues()
    {
	return map[1];
    }

    /** get the <code>i</code>-th value
     *@return the <code>i</code>-th value
     */
    public Object getValue(int i)
    {
	return map[1][i];
    }
    /** get the value of the key
     *@param key the key for which the value will be returned
     *@return the value of the key
     */
    public Object get(Object key)
    {
	for (int i = 0; i < nK; i++)
	    if (map[0][i].equals(key))
		return map[1][i];
	return null;
    }

    /** sets the value of the <code>i</code>-th entry
     *@param i the number of the value
     *@return value the value of the <code>i</code>-th entry
     */
    public Object setValue(int i, Object value){
	return map[1][i] = value;
    }


    /** returns the number of keys in this Map
     *@return he number of keys in this Map
     */
    public int size() { return nK; }

    /** mapping key -> value */
    protected Object[][] map;
    /** the number of keys in the map */
    protected int nK;

    public static void main(String args[])
    {
	OrderedMap om = new OrderedMap();
	om.put("aa", "00");
	om.put("bb", "11");
	om.put("cc", "22");
	System.out.println(  om.containsKey("aa") );
	System.out.println(  om.containsKey("ab") );
	System.out.println(  om.containsValue("aa") );
	System.out.println(  om.containsValue("01") );
	System.out.println(  om.containsValue("22") );
	System.out.println(  om.get("aa") );
	System.out.println(  om.get("aaa") );
	System.out.println(  om.get("cc") );
    }
}






