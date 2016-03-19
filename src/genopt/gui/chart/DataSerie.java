package genopt.gui.chart;

import java.awt.Color;

/** Object representing a data serie for a chart.
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

public class DataSerie
{
    /**@param serieLabel the titel label
     *@param yAxisLabel the y-axis label
     */
    public DataSerie(String serieLabel, String yAxisLabel)
    {
    	name   = new String(serieLabel);
    	yLab   = new String(yAxisLabel);
    	color = Color.black;
		
	xMin = Double.POSITIVE_INFINITY;
	xMax = Double.NEGATIVE_INFINITY;
    	yMin = Double.POSITIVE_INFINITY;
    	yMax = Double.NEGATIVE_INFINITY;	
    }

    /** sets the color of the serie
     *@param c color of the serie
     */
    public final void setColor(final Color c) { color = c; }

    /** verifies the validity of x and y
     *@param x the x-values
     *@param y the y-values
     *@exception IllegalArgumentException if x and y have different field width
     */
    protected final void verify(final double[] x, final double[] y)
	throws IllegalArgumentException
    {
	if (x.length != y.length)
	    throw new IllegalArgumentException(
					       "arguments x and y have different field widths");
	return;
    }
	
    /** sets new data for x and y
     *@param x the x-values
     *@param y the y-values
     *@exception IllegalArgumentException if x and y have different field width
     */
    public final void setPoints(final double[] x, 
				final double[] y)
	throws IllegalArgumentException
    {
	// check validity
	verify(x, y);
	nVal = x.length;
	   
	// update fields
	xVal = new double[nVal];
	yVal = new double[nVal];
	System.arraycopy(x, 0, xVal, 0, nVal);
	System.arraycopy(y, 0, yVal, 0, nVal);
	    
	setXExtrema();
	setYExtrema();
	setYAxisLength(yMin, yMax);
    }

    /** gets x minimum
     *@return x minimum
     */
    public final double getXMin() { return xMin; }

    /** gets x maximum
     *@return x maximum
     */
    public final double getXMax() { return xMax; }

    /** gets y minimum
     *@return y minimum
     */
    public final double getYMin() { return yMin; }

    /** gets y maximum
     *@return y maximum
     */
    public final double getYMax() { return yMax; }

    /** gets the y axis length
     *@return the y axis length
     */
    public final double getYAxisLength() { return yAxiLen; }

    /** sets the x extrema
     */
    protected final void setXExtrema()
    {
	xMin = new Double(Double.POSITIVE_INFINITY).doubleValue();
	xMax = new Double(Double.NEGATIVE_INFINITY).doubleValue();
	for (int i = 0; i < nVal; i++) {
	    if (xMin > xVal[i])
		xMin = xVal[i];
	    if (xMax < xVal[i])
		xMax = xVal[i];		    
	}
    }	
	
    /** sets the y extrema
     */
    protected final void setYExtrema()
    {
	yMin = new Double(Double.POSITIVE_INFINITY).doubleValue();
	yMax = new Double(Double.NEGATIVE_INFINITY).doubleValue();
	
	for (int i = 0; i < nVal; i++){
	    if (yMin > yVal[i])
		yMin = yVal[i];
	    if (yMax < yVal[i])
		yMax = yVal[i];		    
	}
    }

    /** gets the name of the serie
     *@return the name of the serie
     */
    public final String getName() { return name; }

    /** gets the y-axis label
     *@return the y-axis label
     */
    public final String getYLabel() { return yLab; }

    /** gets the number of values
     */
    public final int getNumberOfValues() { return nVal; }
	
    /** gets the x values
     *@return the x values (not as a reference)
     */
    public final double[] getX()
    {
	double[] r = new double[nVal];
	System.arraycopy(xVal, 0, r, 0, nVal);
	return r;
    }

    /** gets the y values
     *@return the y values (not as a reference)
     */
    public final double[] getY(){
	double[] r = new double[nVal];
	System.arraycopy(yVal, 0, r, 0, nVal);
	return r;
    }

    /** gets the x[i] value
     *@return the x[i] value
     */
    public final double getX(final int i)	{ return xVal[i]; }

    /** gets the y[i] value
     *@return the y[i] value
     */
    public final double getY(final int i) { return yVal[i]; }

    /** gets the color of the serie
     *@return the color of the serie
     */
    public final Color getColor() { return new Color(color.getRGB()); }


    /** sets the y-axis length
     *@param yMin the minimum x-value
     *@param yMax the maximum x-value
     */
    protected final void setYAxisLength(final double yMin, 
					final double yMax){
	yAxiLen = yMax - yMin;
    }

    /** the number of known values (=xVal.length) */
    private int nVal;
    /** the x values */
    private double[] xVal;
    /** the y values */
    private double[] yVal;	
    /** minimum x value  */
    private double xMin;
    /** maximum x value  */
    private double xMax;	
    /** minimum y value  */
    private double yMin;
    /** maximum y value  */
    private double yMax;

    /** y-axis lenght in y units */
    private double yAxiLen;	

    /** name of serie */
    private String name;
    /** y-axis label */
    private String yLab;

    /** color of the line */
    private Color color;
}
