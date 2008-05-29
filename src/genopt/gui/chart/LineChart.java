package genopt.gui.chart;
import genopt.lang.ScientificFormat;
import java.awt.*;
import java.awt.font.*;
import javax.swing.JComponent;

/** Object for displaying an online line chart.
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
  * @version GenOpt(R) 2.1.0 (May 29, 2008)<P>
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

public class LineChart extends JComponent{

    /** Natural logarithmus of 10 */
    private static final double LN10 = StrictMath.log(10.);


    public static final int INTEGER = 0;
    public static final int SCIENTIFIC = 1;
    public static final int NUMBER_OF_MINOR_Y_GRIDLINES = 20;

    /** Constructor.
     */
    public LineChart()
    {
	super();

        foTi = new Font("Helvetica", Font.BOLD,  16);
	foSuTi = foTi.deriveFont(Font.BOLD,  12);
        foLa   = foTi.deriveFont(Font.BOLD, 12);
	setTitle("");
        
        xAxiLab = new String("");
        xNumFor = SCIENTIFIC;
        // the inset around everything
        ins = 20;
	// the number of series
	nSer = 0;

	chartInitialized = false;
    }

    /** sets the number format for the x-axis label
     *@param format the number format
     */
    public final void setXNumberFormat(final int format)
    {
	xNumFor = format;
    }

    /** sets the sub title
     */
    public final void setSubTitle()
    {
	subTitLay = new TextLayout("Legend", foSuTi, frc);
    }

    /** sets the title
     *@param title the title of the chart
     */
    public final void setTitle(final String title)
    {
	titLab = new String(title);
    }

    /** sets the x-axis label
     *@param label the x-axis label
     */
    public final void setXAxisLabel(final String label)
    {
	xAxiLab = new String(label);
    }
        
    /** clears all data series */
    public final void removeAllSeries()
    {
	nSer = 0;
	serie = null;
    }
        
    /** adds a data serie to the chart
     *@param ds the data serie
     *@exception if the name of the serie is not unique
     */
    public final void add(final DataSerie ds)
	throws IllegalArgumentException {
	int nSerM1 = nSer;
	nSer++;
	
	if (nSer > 1){
	    // check whether name is unique
	    for(int i = 0 ; i < nSerM1; i++)
		if (serie[i].getName().equals(ds.getName()))
		    throw new 
			IllegalArgumentException("DataSerie '" + 
						 ds.getName() + 
						 "' name is not unique.");
	    // expand arrays
	    DataSerie[] temp = new DataSerie[nSerM1];
	    System.arraycopy(serie, 0, temp, 0, nSerM1);
	    serie = new DataSerie[nSer];                    
	    System.arraycopy(temp, 0, serie, 0, nSerM1);
	    double[] tempD = new double[nSerM1];
	    // y0
	    System.arraycopy(y0, 0, tempD, 0, nSerM1);
	    y0 = new double[nSer];                  
	    System.arraycopy(tempD, 0, y0, 0, nSerM1);
	    // y1
	    System.arraycopy(y1, 0, tempD, 0, nSerM1);
	    y1 = new double[nSer];                  
	    System.arraycopy(tempD, 0, y1, 0, nSerM1);
	    // y0Last
	    System.arraycopy(y0Last, 0, tempD, 0, nSerM1);
	    y0Last = new double[nSer];                      
	    System.arraycopy(tempD, 0, y0Last, 0, nSerM1);
	    // y1Last
	    System.arraycopy(y1Last, 0, tempD, 0, nSerM1);
	    y1Last = new double[nSer];                      
	    System.arraycopy(tempD, 0, y1Last, 0, nSerM1);
	    // yAxiLenVal
	    System.arraycopy(yAxiLenVal, 0, tempD, 0, nSerM1);
	    yAxiLenVal = new double[nSer];
	    System.arraycopy(tempD, 0, yAxiLenVal, 0, nSerM1);
	    // insYLab
	    int[] tempI = new int[nSerM1];
	    System.arraycopy(insYLab, 0, tempI, 0, nSerM1);
	    insYLab = new int[nSer];
	    System.arraycopy(tempI, 0, insYLab, 0, nSerM1);

	    // insNameWest
	    System.arraycopy(insNameWest, 0, tempI, 0, nSerM1);
	    insNameWest = new int[nSer];
	    System.arraycopy(tempI, 0, insNameWest, 0, nSerM1);
	    // insNameNorth
	    System.arraycopy(insNameNorth, 0, tempI, 0, nSerM1);
	    insNameNorth = new int[nSer];
	    System.arraycopy(tempI, 0, insNameNorth, 0, nSerM1);
	}
	else
	    {
		serie        = new DataSerie[nSer];
		y0           = new double[nSer];
		y1           = new double[nSer];
		y0Last       = new double[nSer];
		y1Last       = new double[nSer];
		yAxiLenVal   = new double[nSer];
		insYLab      = new int[nSer];
		insNameWest  = new int[nSer];
		insNameNorth = new int[nSer];  
	    }
	serie[nSerM1] = ds;
	int nCol = nSerM1; 
	while(nCol >= NUMBER_OF_COLORS)
	    nCol -= NUMBER_OF_COLORS;
	serie[nSerM1].setColor(new Color(STANDARD_COLOR[nCol].getRGB()));               
    }

    /** sets new values of a serie. The serie is identified by its name
     *@param ds the data serie

     *@exception IllegalArgumentException if the serie is not registered yet
     */
    public final void setSerie(final DataSerie ds)
	throws IllegalArgumentException
    {
	// distinguish the serie if multi-series are implemented
	if (nSer == 0)
	    throw new IllegalArgumentException(
					       "DataSerie '" + ds.getName() + "' not set yet.");
	int i = 0;

	while(! serie[i].getName().equals(ds.getName()) )
	    {
		i++;
		if (i == nSer)
		    throw new IllegalArgumentException(
						       "DataSerie '" + ds.getName() + "' not set yet.");
	    }
	serie[i] = ds;

	int nCol = i;
	while(nCol >= NUMBER_OF_COLORS)
	    nCol -= NUMBER_OF_COLORS;

	serie[i].setColor(new Color(
				    STANDARD_COLOR[nCol].getRGB()));
    }

    /** gets an axis label
     *@param x the number to be parsed
     */
    protected final String getAxisLabel(final double x)
    {
	final double xAbs = StrictMath.abs(x);
	if ((xAbs >= 0.00009 && xAbs < 100001 )|| xAbs == 0)
	    {	// cut zero if number is, i.e., 1.0
		if (StrictMath.abs( (float)StrictMath.round(xAbs) - (float)xAbs )
		    < 10*Float.MIN_VALUE)
		    return Integer.toString((int)x);
		else
		    return Float.toString((float)x);
	    }
	else
	    return new ScientificFormat(x, MANTISSA_LENGTH, 
					NUMBER_OF_DIGITS).toString();        
    }
    
    /** gets the label ticks
     *@param xMin the minimum data point of the axis
     *@param xMax the maximum data point of the axis
     */
    public final static double[] getTicks(final double xMin, 
					  final double xMax)
    {
	double[] r = new double[2];
	// check whether both are equal
	if ((xMax-xMin) < (Double.MIN_VALUE * 10))
	    {
		r[0] = xMin;
		r[1] = xMax;
		return r;
	    }
	double xMinShi, xMaxShi, shi;
	// shift everything into positiv
	if (xMin < 0)
            {
		shi = StrictMath.pow(10, (int)(StrictMath.log(-xMin)/LG_10)+1);
                xMinShi = xMin + shi;
                xMaxShi = xMax + shi;
	    }
	else
            {
		shi = 0;
		xMinShi = xMin;
		xMaxShi = xMax;
            }               
            
	double dX = xMaxShi - xMinShi;
	int expDX = (int)(StrictMath.log(dX)/LG_10);
	// normalize dX (set it between 0 and 1)
	float dXNor     = (float)(dX / StrictMath.pow(10, expDX));
	double dXScaNor;
	if (dXNor <= 0.2)
	    dXScaNor = 0.2;
	else if (dXNor <= 0.5)
	    dXScaNor = 0.5;
	else if (dXNor <= 1)
	    dXScaNor = 1;
	else if (dXNor <= 2)
	    dXScaNor = 2;
	else if (dXNor <= 5)
	    dXScaNor = 5;
	else
	    dXScaNor = 10;
                        
	double dXSca = dXScaNor * StrictMath.pow(10, expDX);
                
	// determine r
	if (xMin >= 0)
	    r[0] = StrictMath.floor(xMin / dXSca) * dXSca;
	else
	    r[0] = StrictMath.floor(xMin / dXSca) * dXSca;

	r[1] = r[0] + dXSca;

	// double the interval if xMax is bigger then r[1]
	if (xMax > r[1])
	    r[1] += dXSca;
                
	return r;
    }

    /** gets the x axis labels
     */
    protected final String[] getXAxisLabel()
    {
	final int n = xAxiTic.length;
	String[] r = new String[n];
	if (xNumFor == INTEGER)
	    for (int i = 0; i < n; i++)
		r[i] = Integer.toString((int)StrictMath.rint(xAxiTic[i]));
	else
	    for (int i = 0; i < n; i++)
		r[i] = new ScientificFormat(xAxiTic[i], MANTISSA_LENGTH, 
					    NUMBER_OF_DIGITS).toString();
	return r;
    }

    /** gets the x maximum label
     */
    protected final void computeXAxisTicks(){
	assert serie != null : "serie = 'null'";
	// estimate lenght of x axis
	x0 = serie[0].getXMin();
	x1 = serie[0].getXMax();
	xAxiLen = x1-x0;

	// number of labels
	int nMaxLab = 10;

	int iLog = (int)  StrictMath.floor( StrictMath.log(x1) / LN10 );
	double incr = StrictMath.pow(10., iLog);

	int nLabel = (int) StrictMath.ceil(x1/incr) + 1;
	if ( nLabel == 1 ) nLabel = 2;
	xAxiTic = new double[nLabel];
	xAxiTic[0] = 0;
	for (int i = 1; i < nLabel; i++){
	    xAxiTic[i] = xAxiTic[i-1] + incr;
	}
	// set lenght of x axis
	x0      = xAxiTic[0];
	x1      = xAxiTic[nLabel-1];
	xAxiLen = x1-x0;
	storeScale();
    }

    /** updates the y min values and the y axis length (in displayed units)
     */
    protected final void updateYScale()
    {
	double[] t = new double[2];
	for (int i = 0; i < nSer; i++){
	    t = getTicks(serie[i].getYMin(), serie[i].getYMax());
	    y0[i] = t[0];
	    y1[i] = t[1];
	    yAxiLenVal[i] = y1[i] - y0[i];
	}
    }

    private int getLabelStringWidth(final String label)
    {
	TextLayout tl = new TextLayout(label, foLa, frc);
	return (int)(tl.getAdvance());
    }

    /** updates the insets around the chart
     */
    protected final void updateInsets()
    {
	int i = 0;
	insYLab[i] = ins;

	for (i= 1; i < nSer; i++) {
	    insYLab[i] = insYLab[i-1] + (int)(0.25 * ins + 
					      StrictMath.max(getLabelStringWidth(getAxisLabel((y0[i-1]+y1[i-1])/2)),
						       StrictMath.max(
								getLabelStringWidth(getAxisLabel(y0[i-1])),
								getLabelStringWidth(getAxisLabel(y1[i-1])))));
	}
	
	insChaWest = insYLab[i-1] + (int)(0.25 * ins + 
					  StrictMath.max(getLabelStringWidth(getAxisLabel((y0[i-1]+y1[i-1])/2)),
						   StrictMath.max(
							    getLabelStringWidth(getAxisLabel(y0[i-1])),
							    getLabelStringWidth(getAxisLabel(y1[i-1])))));
	
	// legend
	int[] sW = new int[nSer];
	for (i = 0; i < nSer; i++)
	    sW[i] = getLabelStringWidth(serie[i].getName());
	int dSLast = 0;
	for (i = 0; i < nSer; i++)
	    {
		if (i < 2)
		    insNameWest[i] = 0;
		else
		    {
			insNameWest[i] = insNameWest[i-2] + dSLast;
		    }
		if (i > 0 && !isEven(i))
		    dSLast = (int)(StrictMath.max(sW[i-1], sW[i]) + 0.25 * ins);
	    }
	// shift it to its place
	if (nSer > 1)
	    dSLast = isEven(nSer) ? (int)(StrictMath.max(sW[nSer-1], sW[nSer-2])) :
	    sW[nSer-1];
	else
	    dSLast = sW[0];
                 
	final int dYLegend = insNameWest[nSer-1] + dSLast;
	final int y0Legend = super.getSize().width - ins - dYLegend;
				// title height
	final int evenNorth = (int)(titLay.getAscent() + titLay.getLeading()) * 2;

	final int oddNorth  = evenNorth + 2 * labAsc;
                        
	for (i = 0; i < nSer; i++) {
	    insNameWest[i] += y0Legend;
	    insNameNorth[i] = (isEven(i)) ? evenNorth : oddNorth;
	}

        // the y offset of the upper chart boundary
	insChaNorth = (nSer == 1) ? insNameNorth[0] : insNameNorth[1];
	insChaNorth = insChaNorth + (int)(0.25 * ins);
    }
    /** checks whether argument is an even number or an odd number
     *@param i the argument to be tested
     *@return <CODE>true</CODE> if i is even, <CODE>false</CODE> otherwise
     */
    protected final static boolean isEven(final int i) { return ((i / 2) * 2 == i); }


    /** gets the maximal number of data in all series
     *@return the maximal number of data in all series
     */
    protected final int getMaxPoints(){
	int maxVal = -1;
	for (int i = 0; i < nSer; i++){
	    if (maxVal < serie[i].getNumberOfValues())
		maxVal = serie[i].getNumberOfValues();
	}
	return maxVal;
    }

    /** paints the chart (or the message if chart cannot be initialize yet)
     *@param g the Graphics Object
     */
    public void paint(Graphics g)
    {
	Graphics2D g2 = (Graphics2D)g;
	frc = g2.getFontRenderContext();
	titLay = new TextLayout(titLab, foTi, frc);
	labAsc = (int)(new TextLayout("ABC", 
				      foLa, frc).getAscent());			

	final int maxVal = getMaxPoints();
	
	g.setColor(Color.black);

	if (maxVal >= MINPOI)
	    drawWholeChart(g2);
	else
	    initialize(g2);
    }
    /** checks if the chart requires a rescaling
     *@return <CODE>true</CODE> if the chart requires a rescaling
     *        <CODE>false</CODE> otherwise
     */
    private final boolean requireRescale()
    {
	final double c = 1E-5;
	if (StrictMath.abs(x1 - x1Last) > c * (x1-x0))
	    return true;
	for (int i = 0; i < nSer; i++)
	    {
		if (StrictMath.abs(y0[i] - y0Last[i]) > c * (y1[i]-y0[i]))
		    return true;
		if (StrictMath.abs(y1[i] - y1Last[i]) > c * (y1[i]-y0[i]))
		    return true;
	    }
	if ( ( x1 != x1Last ) || (x0 != x0Last) )
	    return true;
	return false;
    }
        
    /** renders the new data points without repainting the whole chart
     */
    public final void renderNewPoints()
    {
	// check whether any data serie is set and whether the chart has
	// been initialized
	if (serie == null){
	    return;
	}
	computeXAxisTicks();
	updateYScale();

	if (requireRescale())
	    repaint();
	else if ( chartInitialized ){
	    Graphics g = super.getGraphics();
	    final Color col = new Color(g.getColor().getRGB());
	    safeDrawChartLines(g);
	    g.setColor(col);
	}
    }

    /** draws the lines of the chart
     *@param g reference to graphics object
     */
    private void safeDrawChartLines(Graphics g){
	g.translate(insChaWest, insChaNorth);
	final double facX = ((double)chartDX) / xAxiLen ;

	try{ // sometimes, an array out of bound exception is thrown 
	     //  at DataSerie.getX
	    for (int i = 0; i < nSer; i++){
		//	int n = serie[i].getNumberOfValues();
                double[] xPDDou = serie[i].getX();
		double[] yPDDou = serie[i].getY();
		int[] xPD = new int[xPDDou.length];
		int[] yPD = new int[yPDDou.length];       

		for (int j = 0; j < xPDDou.length; j++) 
		    xPD[j] = (int)( (xPDDou[j] - x0) * facX);

		for (int j = 0; j < yPDDou.length; j++) 
		    yPD[j] = (int) ((1 - ((yPDDou[j] - y0[i]) / yAxiLenVal[i])) * chartDY);

		g.setColor(serie[i].getColor());
		g.drawPolyline(xPD, yPD, xPD.length);
	    }
	}
	catch(ArrayIndexOutOfBoundsException e){
	    assert null != null : 
		"Catched ArrayIndexOutOfBoundsException. Continue...";

	    if ( genopt.GenOpt.DEBUG == true ){
		System.err.println("");
		System.err.println("##################");
		e.printStackTrace();
		System.err.println("##################");
	    }
	}
	g.translate(-insChaWest, -insChaNorth);
    }

    /** draws the chart title
     *@param g the Graphics Object
     */
    protected void drawTitle(Graphics2D g)
    {
	final Font f = g.getFont();
	g.setFont(foTi);
	titLay.draw(g, ins, titLay.getAscent() + titLay.getLeading());
	g.setFont(f);
    }
        
    /** draws the chart label
     *@param g the Graphics Object
     */
    protected final void drawLegend(Graphics g){
	final Font f = g.getFont();
	final Color c = g.getColor();

	g.setFont(foLa);

	// x axis ticks
	final String[] s = getXAxisLabel();
	final int nXLab = s.length;
	// draw labels
	for (int i = 0; i < nXLab; i++){
	    final double xOffSet = insChaWest + getXPointOffset(xAxiTic[i]) - 
		getLabelStringWidth(s[i]) / 2;
	    g.drawString(s[i], (int)xOffSet, insXLabNorth);
	}

	// x axis label
	g.drawString(xAxiLab, insChaWest + (chartDX - getLabelStringWidth(xAxiLab)) / 2,
		     insXLabNorth + 2 * labAsc);     


	// y axis label and legend
	for (int i = 0; i < nSer; i++) {
	    g.setColor(serie[i].getColor());
	    g.drawString(getAxisLabel(y0[i]), insYLab[i], insY0LabNorth);
	    g.drawString(getAxisLabel((y0[i]+ y1[i]) / 2), insYLab[i], insYHalfLabNorth);
	    g.drawString(getAxisLabel(y1[i]), insYLab[i], insY1LabNorth);
	    g.drawString(serie[i].getName(), insNameWest[i], insNameNorth[i]);
	}
	g.setColor(Color.black);
	g.setFont(foSuTi);
	g.drawString("Legend",
		     insNameWest[0] - (int)(subTitLay.getAdvance()) - (int)(0.5* ins),
		     insNameNorth[0]);
	g.setColor(c);
	g.setFont(f);
    }
        
    /** draws the box around the chart
     *@param g the Graphics Object
     */    
    protected final void drawBox(Graphics g)
    {
	final Color c = g.getColor();
	g.setColor(Color.white);
	g.fillRect(insChaWest, insChaNorth, chartDX, chartDY);
	g.setColor(c);
	g.drawRect(insChaWest, insChaNorth, chartDX, chartDY);
	final int x1 = insChaWest + chartDX;
	int y  = insChaNorth + chartDY / 2;
	final int yMin = insChaNorth + chartDY;

	g.drawLine(insChaWest, y, x1, y);
	g.setColor(Color.lightGray);

	// y grid lines
	final int iYMaj = NUMBER_OF_MINOR_Y_GRIDLINES / 2;

	for (int i = 1; i < NUMBER_OF_MINOR_Y_GRIDLINES; i++) {
	    if (i != iYMaj){
		y = insChaNorth + i * chartDY / NUMBER_OF_MINOR_Y_GRIDLINES;
		g.drawLine(insChaWest, y, x1, y);                               
	    }
	}

	// x grid lines
	if (xAxiLen >= 10){ // make sub intervals
	    final double incr = (xAxiTic[1] - xAxiTic[0]) / 10;
	    g.setColor(Color.lightGray);
	    for (int j = 1; j < 10; j++){
		final int xSub = (int)
		    ( insChaWest + getXPointOffset( xAxiTic[0] + (double)j *
						    incr ) );
		if (j == 5){
		    g.setColor(c);
		    g.drawLine(xSub, yMin, xSub, insChaNorth);
		    g.setColor(Color.lightGray);
		}
		else
		    g.drawLine(xSub, yMin, xSub, insChaNorth);
	    }
	}
	g.setColor(c);
	for (int i = 1; i < xAxiTic.length-1; i++) {
	    final int x = (int)( insChaWest + getXPointOffset(xAxiTic[i]) );
	    g.drawLine(x, yMin, x, insChaNorth);
	    if (xAxiLen >= 10){ // make sub intervals
		final double incr = (xAxiTic[i+1] - xAxiTic[i]) / 10;
		g.setColor(Color.lightGray);
		for (int j = 1; j < 10; j++){
		    final int xSub = (int)
			( insChaWest + getXPointOffset( xAxiTic[i] + (double)j *
							incr ) );
		if (j == 5){
		    g.setColor(c);
		    g.drawLine(xSub, yMin, xSub, insChaNorth);
		    g.setColor(Color.lightGray);
		}
		else
		    g.drawLine(xSub, yMin, xSub, insChaNorth);
		}
	    }
	g.setColor(c);
	}
    }


    /** gets the offset of an x-value, measured in pixels
     * from the west border of the chart surrounding box
     *@param x the x-value (not in pixels)
     *@return the offset
     */    
    protected final int getXPointOffset(final double x)
    {
	return (int)( (x - x0) * chartDX / xAxiLen );
    }

    /** gets the offset of an y-value, measured in pixels
     * from the north border of the chart surrounding box
     *@param y the function value f(x)

     *@param i the number of the data serie to which the point belongs to
     *@return the offset
     */
    protected final int getYPointOffset(final double y, final int i)
    {
	return (int)((1 - ((y - y0[i]) / yAxiLenVal[i])) * chartDY);
    }

    /** stores the current values of the x and y scale
     */
    private final void storeScale()
    {
	// x axis
	x0Last = x0;
	x1Last = x1;
	// y axis
	System.arraycopy(y0, 0, y0Last, 0, nSer);
	System.arraycopy(y1, 0, y1Last, 0, nSer);
    }
    /** draws the whole chart
     *@param g the Graphics Object
     */
    protected final void drawWholeChart(Graphics2D g){
	setSubTitle();
	updateYScale();
	updateInsets();
        
	// chart area
	final Dimension dim = super.getSize();
	chartDX = dim.width - ins - insChaWest;
	if (chartDX < 0)
	    chartDX = 0;            

	chartDY = dim.height - ins - insChaNorth - labAsc * 4;
	if (chartDY < 0)
	    chartDY = 0;

	insY0LabNorth = insChaNorth + chartDY + labAsc / 2;
	insY1LabNorth = insChaNorth + labAsc / 2;
	insYHalfLabNorth = (insY0LabNorth + insY1LabNorth ) / 2;
	insXLabNorth = insChaNorth + chartDY + labAsc * 3;
	final Color col = new Color(g.getColor().getRGB());

	// draw the title and labels
	drawTitle(g);
	drawLegend(g);
	drawBox(g);
	safeDrawChartLines(g);
	g.setColor(col);
	chartInitialized = true;
	return;
    }

    /** initializes the space of the chart (used if no data are available yet)
     *@param g the Graphics Object
     */
    protected void initialize(Graphics g)
    {
	chartInitialized = false;
	g.setFont(foLa);
	g.drawString("No data series", 20, 20);
	g.drawString("available to draw chart.", 20, 40);
	return;
    }


    protected boolean chartInitialized;
    /** the data serie */
    protected DataSerie[] serie;
    /** number of data series in the chart */
    protected int nSer;
    /** maximum number of data serie that can be displayed at once */
    protected static final int NSERMAX = 10;

    /** FontRenderContext of Graphics2D */
    protected FontRenderContext frc;

    /** title label */
    protected String titLab;
    /** x-axis label */
    protected String xAxiLab;
    /** x-axis ticks */
    protected double[] xAxiTic;
    /** x-axis minimum in displayed units (not pixels) */
    protected double x0;
    /** x-axis maximum in displayed units (not pixels) */
    protected double x1;
    /** x-axis minimum of the last rendering in displayed units (not pixels) */
    protected double x0Last;
    /** x-axis maximum of the last rendering in displayed units (not pixels) */
    protected double x1Last;

    /** y-axis lenght in displayed units (not pixels) */
    protected double[] yAxiLenVal;
    /** y-axis minimum in displayed units (not pixels) */
    protected double[] y0;
    /** y-axis maximum in displayed units (not pixels) */
    protected double[] y1;
    /** y-axis minimum of the last rendering in displayed units (not pixels) */
    protected double[] y0Last;
    /** y-axis maximum of the last rendering in displayed units (not pixels) */
    protected double[] y1Last;

    /** title Font */
    protected Font foTi;
    /** title TextLayout */
    protected TextLayout titLay;
    /** sub title TextLayout */
    protected TextLayout subTitLay;
    /** sub title Font */
    protected Font foSuTi;
    /** label Font */
    protected Font foLa;
    /** ascent of label */
    protected int labAsc;
    /** inset around everything */
    protected int ins;
    /** inset of the chart on the west side */
    protected int insChaWest;
    /** inset of the chart on the north side */
    protected int insChaNorth;
    /** inset of x-axis label on the north side */
    protected int insXLabNorth;

    /** inset of the y0 label on the north side */
    protected int insY0LabNorth;
    /** inset of the y half label on the north side */
    protected int insYHalfLabNorth; 
    /** inset of the y1 label on the north side */
    protected int insY1LabNorth;
    /** inset of each y-axis label */
    protected int[] insYLab;
    /** inset of each serie name on the north side */
    protected int[] insNameNorth;   
    /** inset of each serie name on the west side */
    protected int[] insNameWest;
    /** number format of x-axis label */
    protected int xNumFor;
    /** x extension of chart */
    protected int chartDX;  
    /** y extension of the chart */
    protected int chartDY;
    
    /** number of points for which the graph is drawn */
    protected final int MINPOI = 3;
        
    /** lenght of mantissa field */
    protected static int MANTISSA_LENGTH = 5;
    /** number of digits after decimal point */
    protected static int NUMBER_OF_DIGITS = 2;
    /** the natural logarithm (base e) of 10 */
    private static double LG_10 = StrictMath.log(10);
    /** the standard color to be assigned */
    private static final Color[] STANDARD_COLOR = new Color[]
    {Color.red, Color.blue, Color.green.darker(), Color.black,
     Color.magenta, Color.cyan, Color.green, Color.cyan.darker().darker(),
     Color.orange.darker(), Color.cyan.darker()};

    /** the number of available colors */
    private static final int NUMBER_OF_COLORS = STANDARD_COLOR.length;
    /** increment after how many simulations the whole 
	graph is rescaled */
    private int xUpdInc;
        
    /** x-axis lenght in x units */
    private double xAxiLen; 
        
    /*      public static void main(String[] args)
	    {
            LineChart lc = new LineChart();
            DataSerie ds = new DataSerie("abc", "def");
	    ds.setPoints(new double[] {0, 1}, new double[] {0, 1});
	    ds.setPoints(new double[] {0, 1}, new double[] {0, 1});
	    lc.setSerie(ds);
	    lc.setSerie(ds);
	    lc.show();
            //    double[] t = new double[2];
            //    t = getTicks(new Double(args[0]).doubleValue(), new Double(args[1]).doubleValue());
	    //              System.out.println(t[0] + "\t" + t[1]);
	    System.out.println("\nfinished");
	    return;
	    }
    */
}







