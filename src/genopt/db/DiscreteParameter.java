package genopt.db;

import genopt.db.IndependentParameter;
import java.lang.StrictMath;

/** Object that represents a discrete parameter of
  * the optimization process.
  *
  * <h2> Meaning of field <CODE>discreteType</CODE>: </h2>
  * <pre> 0: string
  * 1: integer
  * 2: double</pre>
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
  * GenOpt Copyright (c) 1998-2011, The Regents of the University of
  * California, through Lawrence Berkeley National Laboratory (subject 
  * to receipt of any required approvals from the U.S. Dept. of Energy).  
  * All rights reserved.
  *
  * @author <A HREF="mailto:MWetter@lbl.gov">Michael Wetter</A>
  *
  * @version GenOpt(R) 3.1.0 (November 30, 2011)<P>
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

public class DiscreteParameter extends IndependentParameter
{
    /** constructor. Does nothing
     */
    public DiscreteParameter() { super(); }
    
    /** Constructor for a discrete variable.
     * @param VariableName  Name of the free parameter as specified in the
     *                      command file
     * @param InitialValue  Index of initial value of free parameter
     * @param Values           Set of admissible values
     */
    public DiscreteParameter(String VariableName, int InitialValue, String[] Values) {
	super(VariableName);
	index	         = InitialValue;
	dimension        = Values.length;
	values = new String[dimension];
	valuesNum = new double[dimension];
	discreteType = 1; // set to integer, may be overwritten below
	for(int i=0; i < dimension; i++){
	    values[i] = new String(Values[i]);
	    if (discreteType != 0){
		try { 
		    valuesNum[i] = Double.parseDouble( values[i] );
		}
		catch(NumberFormatException nfe){
		    // parsing failed, hence we have a string and no numerical value
		    discreteType = 0; // indicates string
		    valuesNum = null;
		}
	    }
	}
	// check whether all elements are integers
	if (discreteType != 0){
	    for(int i=0; i < dimension; i++){
		if ( valuesNum[i] != StrictMath.rint( valuesNum[i] ) )
		    discreteType = 2; // have a double
	    }
	}
    }

    /** Clone
     */
    protected Object clone() {
	String[] Values = new String[dimension];
	for(int i=0; i < dimension; i++)
	    Values[i] = new String(values[i]);
	if (name != null)
	    return new DiscreteParameter(new String(name), index, values);
	else
	    return new DiscreteParameter(null, index, values);
    }
    
    /** Gets the index that points to the currently selected value.
     * @return index that points to the currently selected value.
     */
    public final int getIndex() { return index; }

    /** Gets the number of elements
     * @return the number of elements
     */
    public final int length() { return dimension; }

    /** Gets the string representation of the currently selected value.
     * @return the string representation of the currently selected value.
     */
    public final String getValueString(){ return new String(values[index]) ; }

    /** Gets the string representation of the element with index <CODE>ind</CODE>.
     * @param ind index of the element
     * @return the string representation of the element with index <CODE>ind</CODE>
     */
    public final String getValueString(int ind){ return new String(values[ind]) ; }


    /** Gets the double representation of the currently selected value.<BR>
     * If the variable represents discrete numerical values, then the double
     * value of the currently selected value is returned.<BR>
     * If the variable represents different string values, then the currently
     * selected index is returned.
     * @return the double representation of the currently selected value.
     */
    public final double getValueDouble(){
	if (discreteType == 0) // string
	    return (double)index;
	else // integer or double
	    return valuesNum[index];
    }

    /** Gets the double representation of the element with index <CODE>ind</CODE>.<BR>
     * If the variable represents discrete numerical values, then the double
     * value of the currently selected value is returned.<BR>
     * If the variable represents different string values, then the argument
     * <CODE>ind</CODE> is returned.
     * @param ind index of the element
     * @return the double representation of the element with index <CODE>ind</CODE>
     */
    public final double getValueDouble(int ind){
	if (discreteType == 0) // string
	    return (double)(ind+1); // add 1 to make internal 0-based to 1-based index
	else // integer or double
	    return valuesNum[ind];
    }

    /** Gets the discrete <CODE>discreteType</CODE>, as specified in DiscreteParameter.
     * @return <CODE>discreteType</CODE><br> 
     * 0: string<BR>
     * 1: integer<BR>
     * 2: double</pre>
     */
    public int getDiscreteType(){ return discreteType; }
    

    /** set of admissible values */
    protected String[] values;
    /** set of admissible values.<BR>
     <CODE>valuesNum</CODE> is the double representation of <CODE>values</CODE>*/
    protected double[] valuesNum;
    /** number of admissible elements */
    protected int dimension;
    /** flag for native type of elements in <CODE>values</CODE><BR>
	   0: string,<BR>
	   1: integer, <BR>
	   2: double */
    protected int discreteType;
    /** current value of the parameter */
    protected int index;
}
