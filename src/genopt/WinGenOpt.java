package genopt;

import genopt.io.*;
import genopt.gui.*;
import genopt.gui.chart.*;
import genopt.db.*;
import genopt.util.*;
import genopt.algorithm.util.math.Point;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.System;
import java.util.Properties;
import java.net.URL;

/** Object for optimizing an objective function computed by
  * a simulation program.
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
  * <h3>Copyright Notice</h3>
  *
  * GenOpt Copyright (c) 1998-2008, The Regents of the University of 
  * California, through Lawrence Berkeley National Laboratory (subject 
  * to receipt of any required approvals from the U.S. Dept. of Energy).  
  * All rights reserved.<p>
  *
  * If you have questions about your rights to use or distribute this software, 
  * please contact Berkeley Lab's Technology Transfer Department at TTD@lbl.gov.
  * <p>
  * 
  * NOTICE.  This software was developed under partial funding from the U.S. 
  * Department of Energy.  As such, the U.S. Government has been granted for 
  * itself and others acting on its behalf a paid-up, nonexclusive, 
  * irrevocable, worldwide license in the Software to reproduce, prepare 
  * derivative works, and perform publicly and display publicly. Beginning 
  * five (5) years after the date permission to assert copyright is obtained 
  * from the U.S. Department of Energy, and subject to any subsequent 
  * five (5) year renewals, the U.S. Government is granted for itself and 
  * others acting on its behalf a paid-up, nonexclusive, irrevocable, 
  * worldwide license in the Software to reproduce, prepare derivative 
  * works, distribute copies to the public, perform publicly and 
  * display publicly, and to permit others to do so.
  * 
  * <h3>License agreement</h3>
  *
  * GenOpt Copyright (c) 1998-2008, The Regents of the University of
  * California, through Lawrence Berkeley National Laboratory (subject 
  * to receipt of any required approvals from the U.S. Dept. of Energy).  
  * All rights reserved.
  * <p>
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions are met:
  * <p>
  * (1) Redistributions of source code must retain the above copyright notice, 
  * this list of conditions and the following disclaimer.
  * <p>
  * (2) Redistributions in binary form must reproduce the above copyright 
  * notice, this list of conditions and the following disclaimer in the 
  * documentation and/or other materials provided with the distribution.
  * <p>
  * (3) Neither the name of the University of California, Lawrence Berkeley 
  * National Laboratory, U.S. Dept. of Energy nor the names of its 
  * contributors may be used to endorse or promote products derived from 
  * this software without specific prior written permission.
  * <p>
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
  * <p>
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
  *
  * @author <A HREF="mailto:MWetter@lbl.gov">Michael Wetter</A>
  *
  * @version GenOpt(R) 3.0.0 beta 2 (February 23, 2009)<P>
  */
	
public class WinGenOpt extends JFrame
    implements  ActionListener, ItemListener, WindowListener

{
    /** System dependent line separator */
    private final static String LS = System.getProperty("line.separator");

    private final static String SEELOGFILEFORERROR = new String(
								"GenOpt terminated with error." + LS + "See logfile for detailed information.");
   
    /** Button Constants */
    protected final static int BUTTONSTART=0;
    protected final static int BUTTONSTOP=1;
    protected final static int BUTTONCHART=2;
    protected final static int BUTTONSPLIT=3;

    /** Constructor for WinGenOpt 
     */
    public WinGenOpt()
    {
	setupProperties();
	GenOpt.DEBUG = isDebug();
	optRuns = false;
	panel = new JPanel();
	setupWindow();
	setupMenuBar();
	setupToolBar();
	setupTextArea();
	setupLineChart();

	String or = (String)pref.get("pane.layout");
	if (or.equals("split.vertical"))
	    setupVerticalSplitPane();
	else
	    setupHorizontalSplitPane();
	// print warning if in debug mode
	if (GenOpt.DEBUG) append(GenOpt.DEBUG_WARNING);
    }
    /** sets up the properties */
    private void setupProperties()
    {
	String home = System.getProperty("user.home");
	String fs   = System.getProperty("file.separator");
	File file = new File(home + fs + ".genopt"
		+ GenOpt.VERSION_NUMBER + GenOpt.VERSION_ID + fs + "properties.txt");
	pref = new Preference(file);
    }

    /** sets up the window
	 */
    private void setupWindow()
    {
	addWindowListener(this);
	int w, h, x, y;
	try
	    {
		w = Integer.parseInt((String)pref.get("frame.width"));
		h = Integer.parseInt((String)pref.get("frame.height"));
		x = Integer.parseInt((String)pref.get("frame.x"));
		y = Integer.parseInt((String)pref.get("frame.y"));
	    }
	catch (NumberFormatException e)
	    {
		w = 800; h = 500; x = 0; y = 0;
	    }
	setSize(w, h);
	setTitle("WinGenOpt");
    	setLocation(x, y);
	setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    }

    /** sets up the menu bar
	 */
    private void setupMenuBar()
    {
    	JMenuBar mbar = new JMenuBar();
    	// File menu
    	fileMenu = new JMenu("File");
	fileMenu.setMnemonic(KeyEvent.VK_F);
    	fileMenu_Start = new JMenuItem("Start...", KeyEvent.VK_A);
    	fileMenu_Start.addActionListener(this);
    	fileMenu.add(fileMenu_Start);
    	fileMenu_Stop = new JMenuItem("Stop...", KeyEvent.VK_O);
    	fileMenu_Stop.addActionListener(this);
	fileMenu_Stop.setEnabled(false);
    	fileMenu.add(fileMenu_Stop);
    	fileMenu.addSeparator();
    	JMenuItem fme = new JMenuItem("Exit", KeyEvent.VK_X);
    	fme.addActionListener(this);
    	fileMenu.add(fme);
    	mbar.add(fileMenu);
	// Chart menu
	chartMenu = new JMenu("Chart");
	chartMenu.setMnemonic(KeyEvent.VK_C);
	chartMenu_Change = new JMenuItem("Change data series...", KeyEvent.VK_C);
	chartMenu_Change.addActionListener(this);
	chartMenu.add(chartMenu_Change);
	mbar.add(chartMenu);
	chartMenu_Change.setEnabled(false);
	// Window menu
	windowMenu = new JMenu("Window");
	windowMenu.setMnemonic(KeyEvent.VK_W);
	mbar.add(windowMenu);

	JRadioButtonMenuItem[] wmi = {new JRadioButtonMenuItem("Horizontal Split View"),
				      new JRadioButtonMenuItem("Vertical Split View")};
	wmi[0].setMnemonic(KeyEvent.VK_H);
	wmi[1].setMnemonic(KeyEvent.VK_V);

	String or = (String)pref.get("pane.layout");
	if (or.equals("split.vertical"))
	    wmi[1].setSelected(true);
	else
	    wmi[0].setSelected(true);
	ButtonGroup wmbg = new ButtonGroup();
	for (int i = 0; i < wmi.length; i++)
	    {
		wmi[i].addActionListener(this);
		windowMenu.add(wmi[i]);
		wmbg.add(wmi[i]);
	    }
	//	windowMenu.add(new JSeparator());

	// About menu
	aboutMenu = new JMenu("About");
	aboutMenu.setMnemonic(KeyEvent.VK_A);
	JMenuItem wma = new JMenuItem("About GenOpt", KeyEvent.VK_A);
	wma.addActionListener(this);
	aboutMenu.add(wma);
	mbar.add(aboutMenu);
	// Set the menu bar
	setJMenuBar(mbar);
    }

    /** sets up the tool bar
	 */
    private void setupToolBar()
    {
    	toolBar = new JToolBar();
	String[] imgLoc = {"genopt/img/start.gif", "genopt/img/stop.gif", "genopt/img/chart.gif", "genopt/img/split.gif"};
	String[] imgHel = {"Start optimization", "Stop optimization", "Chart", "Split view"};
	String[] imgAlt = {"Start", "Stop", "Chart", "Split"};
	int[] keyEve = {KeyEvent.VK_A,  KeyEvent.VK_O, KeyEvent.VK_C, KeyEvent.VK_P};
	int nImg = imgLoc.length;
	button = new JButton[nImg];
     	URL[] iconURL = new URL[nImg];
	// get the path of the images if anywhere found in the CLASSPATH
	for (int iBut = 0; iBut < nImg; iBut++)
		iconURL[iBut] = ClassLoader.getSystemResource(imgLoc[iBut]);
	// --- load the JButtons ---
	for (int iBut = 0; iBut < nImg; iBut++)
	    {
		// load either just a button with a String or the image
		if (iconURL[iBut] == null)
		    button[iBut] = new JButton(imgAlt[iBut]);
		else
		    button[iBut] = new JButton(new ImageIcon(iconURL[iBut], imgHel[iBut]));

		button[iBut].setMnemonic(keyEve[iBut]);
		button[iBut].setToolTipText(imgHel[iBut]);
		toolBar.add(button[iBut]);
		if (iBut == BUTTONSTOP)
		    toolBar.add(new JToolBar.Separator());
	    }

	button[BUTTONSTART].addActionListener(new ActionListener()
	    {
		public void actionPerformed(ActionEvent e)
		{
		    startGenOpt();
		}
	    });
	button[BUTTONSTOP].addActionListener(new ActionListener()
	    {
		public void actionPerformed(ActionEvent e)
		{
		    stopOptimization();
		}
	    });
	button[BUTTONSTOP].setEnabled(false);
	button[BUTTONCHART].addActionListener(new ActionListener()
	    {
		public void actionPerformed(ActionEvent e)
		{
		    changeDataSeries();
		}
	    });
	button[BUTTONCHART].setEnabled(false);

	button[BUTTONSPLIT].addActionListener(new ActionListener()
	    {
		public void actionPerformed(ActionEvent e)
		{
		    if (splPanHor)
			setupVerticalSplitPane();
		    else
			setupHorizontalSplitPane();
		}
	    });
       // Disabling floating
       toolBar.setFloatable(false);
    }

    /** sets up the text area
	 */
    private void setupTextArea()
    {
	ta = new FlowTextArea(new PlainDocument());
    	ta.setEditable(false);
    	ta.setFont(new Font("Monospaced", Font.PLAIN, 12));
    	ta.setOpaque(false);
	scTa = new JScrollPane(ta);
	scTa.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	scTa.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
	scTa.setMinimumSize(new Dimension(100,50));
	taVp = new JViewport();
	taVp.add(scTa);
    }

    /** sets up the line chart
	 */
    private void setupLineChart()
    {
	resetDiagramProperties();
	lc = new LineChart();
	lc.setTitle("Result overview");
	lc.setXAxisLabel("result number");
	lc.setXNumberFormat(LineChart.INTEGER);
	lc.setMinimumSize(new Dimension(100,50));
    }

    /** sets up the horizontal split pane
	 */
    private void setupHorizontalSplitPane()
    {
	splPan = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scTa, lc);
	splPanHor = true;
	setupGeneralSplitPane();
    }

    /** sets up the vertical split pane
	 */
    private void setupVerticalSplitPane()
    {
	splPan = new JSplitPane(JSplitPane.VERTICAL_SPLIT, lc, scTa);
	splPanHor = false;
	setupGeneralSplitPane();
    }

    /** sets up the general setting of the split pane
	  */
    private void setupGeneralSplitPane()
    {
	splPan.setContinuousLayout(false);
	splPan.setOneTouchExpandable(true);
	buildContainer();
    }

    /** builds the container */
    private void buildContainer()
    {
	Container c = super.getContentPane();
	c.removeAll();
	c.add("North", toolBar);
	c.add("Center", splPan);
	super.validate();
	// the divider location has to be set after the validate() call
	// see Java Bug Id 4182558
	splPan.setDividerLocation(300);
	//splPan.setDividerLocation(0.3);
    }

    /** reset the diagram properties
	  */
    private void resetDiagramProperties() {
	disOrd = null;
	nDisSer = 6;
    }

    /** sets the order in which the series are displayed
	  */
    protected void changeDataSeries() {
	int i;
	go.sleepGenOpt(true);
	int nSer     = resMan.getDimensionF() +  
	    resMan.getDimensionContinuous() + resMan.getDimensionDiscrete();
	int nNotDis  = nSer - nDisSer;
	String[] cho = new String[nNotDis];
	String[] sel = new String[nDisSer];
	int[] notDis = new int[nNotDis];
	boolean[] set = new boolean[nSer];
	for(i = 0; i < nSer; i++)
	    set[i] = false;
	// set the selected entries
	for(i = 0; i < nDisSer; i++) {
	    sel[i] = se[disOrd[i]].getName();
	    set[disOrd[i]] = true;
	}
	// set the choose entries
	int j = 0;
	for(i = 0; i < nSer; i++) {
	    if (!set[i])
		cho[j++] = se[i].getName();
	}
	ListSelector lisSel = new ListSelector(cho, sel);
	JOptionPane pane = new JOptionPane(lisSel,
					   JOptionPane.PLAIN_MESSAGE,
					   JOptionPane.OK_CANCEL_OPTION);
	
	JDialog dialog = pane.createDialog(this, "Set displayed data series");
	dialog.setResizable(false);
	dialog.setVisible(true);
	Object selectedValue = pane.getValue();

	if (((Integer)selectedValue).intValue() == JOptionPane.OK_OPTION)
	    {	// user pressed "OK"
		Object[] selIteObj = lisSel.getSelectedItems();

		String[] selIte = new String[selIteObj.length];
		for (i = 0; i < selIteObj.length; i++)
		    selIte[i] = (String)selIteObj[i];
		nDisSer = selIte.length;
		disOrd = new int[nDisSer];
		for(i = 0; i < nDisSer; i++)
		    {
			j = 0;
			while(! selIte[i].equals(se[j].getName()) )
			    j++;
			disOrd[i] = j;
		    }
		lc.removeAllSeries();
		for (i = 0; i < nDisSer; i++)
		    lc.add(se[disOrd[i]]);
		updateChart();
	    }
	go.sleepGenOpt(false);
    }

    /** initializes the DataSerie
	  *@param res reference to the result manager
	  */
    public void initializeSeries(ResultManager res) {
	int i;
	resMan = res;

	lc.removeAllSeries(); // to delete the entries of a former run
	resetDiagramProperties();

	int nF = resMan.getDimensionF();
	int nCon = resMan.getDimensionContinuous();
	int nDis = resMan.getDimensionDiscrete();

	se = new DataSerie[nF+nCon+nDis];

	String[] nameF = resMan.getNameF();
	String[] nameX = resMan.getNameContinuousAndDiscrete();

	for (i = 0; i < nF; i++)
	    se[i] = new DataSerie(nameF[i], nameF[i]);
	for (int j = 0; j < (nCon+nDis); j++, i++)
	    se[i] = new DataSerie(nameX[j], nameX[j]);

	  if (disOrd == null) {
	    nDisSer = ( (nF+nCon+nDis) <= nDisSer) ? (nF+nCon+nDis) : nDisSer;
	    disOrd = new int[nDisSer];
	    for (i = 0; i < nDisSer; i++)
		disOrd[i] = i;
	}

	for (i = 0; i < nDisSer; i++){
	    lc.add(se[disOrd[i]]);
	}

	button[BUTTONCHART].setEnabled(true);
	chartMenu_Change.setEnabled(true);
    }

    /** sets a new optimization trial
	  */
    public void setNewTrial() {
	updateChart();
    }

    /** updates the online chart
	  */
    protected void updateChart() {
	try{

	    final int nVal = resMan.getNumberOfAllResults();
	    if ( nVal == 0 )
		return;
	    
	    final int nF = resMan.getDimensionF();
	    final int nCon = resMan.getDimensionContinuous();
	    
	    double[] x = new double[nVal];
	    for (int i = 0; i < nVal; )
		x[i] = ++i; // the number of iteration

	    // update the series that are displayed
	    Point[] pt = resMan.getAllPoint(nVal);
	    double[] temp = new double[nVal];
	    
	    for (int i = 0; i < nDisSer; i++) {
		int j = disOrd[i];
		if (j < nF) { // process function values
		    for (int k = 0; k < nVal; k++)
			temp[k] = pt[k].getF(j);
		    se[j].setPoints(x, temp);
		}
	    else if ( j < nF+nCon ) { // process continuous parameters
		for (int k = 0; k < nVal; k++)
		    temp[k] = pt[k].getX(j-nF);
		se[j].setPoints(x, temp);
	    }
		else { // process discrete parameters
		    for (int k = 0; k < nVal; k++)
			temp[k] = go.disPar[j-nF-nCon].getValueDouble( pt[k].getIndex(j-nF-nCon) );
		    //		    temp[k] =  (double)( pt[k].getIndex(j-nF-nCon) );
		    se[j].setPoints(x, temp);
		}
		lc.setSerie(se[j]);
	    }
	    
	    if (nDisSer != 0)
		lc.renderNewPoints();
	    else
		lc.repaint();
	}
	catch(ArrayIndexOutOfBoundsException e){
	    if (GenOpt.DEBUG) printError("Catched ArrayIndexOutOfBoundsException while updating online chart. Continue..." + LS);
	}
	
	catch(NullPointerException e){
	    if (GenOpt.DEBUG) printError("Catched NullPointerException while updating online chart. Continue..." + LS);
	}
	/*	catch(Exception e){
		if (GenOpt.DEBUG) printError("Catched Exception while updating online chart. Continue..." + LS);
		}*/
	return;
    }

    /** appends text to the text field
	  * @param text the text to be appended
	  */
    public void append(String text){
	ta.append(text);
	// this brings the GUI always in front: show();
	// repaint() works, but not ta.repaint(). We need
	// repaint(), otherwise, ta is not repainted.
	repaint();
    }

    /** performs the ActionEvent
      * @param evt the action event
      */
    public void actionPerformed(ActionEvent evt)
    {
	JMenuItem c = (JMenuItem)evt.getSource();
	String arg = c.getText();
	if (arg.equals("Start..."))
	    startGenOpt();
	else if (arg.equals("Stop..."))
	    stopOptimization();
	else if (arg.equals("Exit"))
	    exitWinGenOpt();
	else if (arg.equals("Change data series..."))
	    changeDataSeries();
	else if (arg.equals("Horizontal Split View"))
	    setupHorizontalSplitPane();
	else if (arg.equals("Vertical Split View"))
	    setupVerticalSplitPane();
	else if (arg.equals("About GenOpt"))
	    showAboutGenOpt();
	else
	    printError("Error: Menu '" + arg + "' not implemented yet.");
    }

    /** performs the ItemEvent if the item state changed
      * @param evt the item event
      */
    public void itemStateChanged(ItemEvent evt) {
	JCheckBoxMenuItem c = (JCheckBoxMenuItem)evt.getSource();
    	System.out.print(c.getText() + " ");
	if (!c.getState())
	    System.out.print("de");
	System.out.println("selected");
    }
    
    /** Starts GenOpt.
     *
     * This method opens an file chooser to let the user select the optimization 
     * initialization file. Then, it calls {@link #startGenOpt(File)} to start
     * the optimization.
     */
    protected void startGenOpt() {
	optRuns = true;
	//construct file dialog
	JFileChooser fc = GenOpt.getInitializationFileChooser( getIniStartUpFile() );
	
	if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION){
	    // user did not choose any file
	    optRuns = false;
	    return;
	}
	else {
	    button[BUTTONSTART].setEnabled(false);
	    fileMenu_Start.setEnabled(false);
	}
	
	ta.setText("");
	File iniFil = fc.getSelectedFile();
	startGenOpt(iniFil);
    }

    /** Starts GenOpt
     *
     * @param iniFil optimization initialization file.
     */
    protected void startGenOpt(File iniFil){
	try{
	    InputFormatException inpForExc = new InputFormatException();
	    go = new GenOpt(iniFil.getPath(), inpForExc, this);
	    // register ini file
	    File optIniFil = go.getOptimizationIniFile();
	    if (optIniFil != null)
		pref.put("file.ini.startUp", optIniFil.getPath());

	    // check for error during initialization
	    if (inpForExc.getNumberOfErrors() > 0)
		throw inpForExc;

	    button[BUTTONSTART].setEnabled(false);
	    fileMenu_Start.setEnabled(false);
	    go.start();
	    button[BUTTONSTOP].setEnabled(true);
	    fileMenu_Stop.setEnabled(true);
	}
	catch(Throwable t) { handleThrowable(t); }
    }

    /** handles an exception which was thrown by GenOpt
     *@param t the thrown exception
     */
    private void handleThrowable(Throwable t) {
	if (go == null){
	    if (GenOpt.DEBUG) GenOpt.printStackTrace(t);
	    printError(t.getClass().getName() + ": " + LS +
		       t.getMessage() );
	}
	else {
	    if (GenOpt.DEBUG) GenOpt.printStackTrace(t);
	    printError(t.getClass().getName() + ": " + LS +
		       t.getMessage() + LS + LS + SEELOGFILEFORERROR);
	    go.writeLogFile(t.getMessage());
	}
	finalizeOptimization();
    }
    
    
    /** exits WinGenOpt
     */
    protected void exitWinGenOpt() {
	int exiVal;
	if (optRuns)
	    exiVal = stopOptimization();
	else
	    exiVal = JOptionPane.OK_OPTION;
	
	if (exiVal == JOptionPane.OK_OPTION){ 
	    // close WinGenOpt
	    // set up preferences
	    if (splPanHor)
		pref.put("pane.layout", "split.horizontal");
	    else
		pref.put("pane.layout", "split.vertical");
	    pref.put("frame.width",  Integer.toString(getWidth()));
	    pref.put("frame.height", Integer.toString(getHeight()));
	    pref.put("frame.x"     , Integer.toString(getX()));
	    pref.put("frame.y"     , Integer.toString(getY()));
	    pref.write();
	    
	    dispose();
	    System.exit(0);
	}
	else
	    go.sleepGenOpt(false);
    }
    
    /** displays a message box to stop GenOpt, and if the user selects <CODE>OK</CODE>, tells
     * <CODE>GenOpt</CODE> to stop the optimization
     *@return <CODE>0</CODE> if OK has been selected, <CODE>1</CODE> otherwise
     */
    protected int stopOptimization() {
	go.sleepGenOpt(true);
	
	String mes = "Optimization is running.\n" +
	    "Do you really want to stop it?";
	
	JOptionPane pane = new JOptionPane(mes,
					   JOptionPane.WARNING_MESSAGE,
					   JOptionPane.YES_NO_OPTION);
	
	JDialog dialog = pane.createDialog(this, "Stop Optimization");
	dialog.setResizable(false);
	dialog.setVisible(true);
	Object selectedValue = pane.getValue();
	
	int exiVal =  (selectedValue instanceof Integer) ?
	    ((Integer)selectedValue).intValue() : JOptionPane.CLOSED_OPTION;
	
	if (exiVal == JOptionPane.OK_OPTION) {	// stop the optimization
	    go.sleepGenOpt(false);
	    go.stopOptimization();
	    go.SimSta.destroyProcess();
	    //	wait for GenOpt to finish
	    try{
		go.join();
	    }
	    catch(InterruptedException e){
		handleThrowable(e);
	    }
	    finalizeOptimization();
	}
	
	go.sleepGenOpt(false);
	return exiVal;
    }
    
    /** displays the about window
	  */
    protected void showAboutGenOpt() {
	String message = GenOpt.RUNHEADER;
		
	int k = 0;
	int j = message.indexOf(LS);
	int finLen = LS.length();
	while(j > -1){
	    message = message.substring(0, j)
		+ "\n" + message.substring(j+finLen);
	    k = j + 1;
	    j = message.indexOf(LS, k);
	}
	
	JOptionPane pane = new JOptionPane(message,
					   JOptionPane.INFORMATION_MESSAGE,
					   JOptionPane.DEFAULT_OPTION);
	
	JDialog dialog = pane.createDialog(this, "About GenOpt");
	dialog.setResizable(false);
	dialog.setVisible(true);
    }

    /** prints an error message to the output device
     * @param text the text to be printed
     */
    public void printError(String text){
        append(text);
	if (GenOpt.DEBUG) System.err.println(text);
    }
    
    /** finalizes optimization
     */
    public void finalizeOptimization() {
	optRuns = false;
	fileMenu_Start.setEnabled(true);
	fileMenu_Stop.setEnabled(false);
	button[BUTTONSTART].setEnabled(true);
	button[BUTTONSTOP].setEnabled(false);
	validate();
    }
    
    /** gets the initialization startup file
     *@return the initialization file startup file
     */
    protected File getIniStartUpFile() {
	return new File((String)pref.get("file.ini.startUp"));
    }
    
    /** gets the separator, as specified in properties.txt
     *@return the separator
     */
    public String getSeparator(){
	return (String)pref.get("simulation.result.separator");
    }


   /** checks whether we should run in debug mode
     *@return <CODE>true</CODE> if we run in debug mode, <CODE>false</CODE> otherwise
     */
    public boolean isDebug(){
	String deb = (String)pref.get("debug");
	return deb.equals("true");
    }

    /* WindowListener */
    public void windowClosed(WindowEvent e) { exitWinGenOpt(); }
    public void windowIconified(WindowEvent e) {}
    public void windowOpened(WindowEvent e) {}
    public void windowClosing(WindowEvent e) { exitWinGenOpt(); }
    public void windowDeiconified(WindowEvent e) {}
    public void windowActivated(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}

    /** GenOpt Object */
    protected GenOpt go;
    /** Panel */
    protected JPanel panel;
    /** scroll pane for the text area */
    protected JScrollPane scTa;
    /** text area displaying online result (shell piping) */
    protected FlowTextArea ta;
    /** Viewport of the text area */
    protected JViewport taVp;
    /** File menu */
    protected JMenu fileMenu;
    /** File menu -> Start */
    protected JMenuItem fileMenu_Start;
    /** File menu -> Stop */
    protected JMenuItem fileMenu_Stop;
    /** Toolbar */
    protected JToolBar toolBar;
    /** Start button */
    protected JButton[] button;
    /** Chart menu */
    protected JMenu chartMenu;
    /** Chart menu -> Change */
    protected JMenuItem chartMenu_Change;
    /** Windows menu */
    protected JMenu windowMenu;
    /** About menu */
    protected JMenu aboutMenu;
    /** Tabbed Pane */
    protected JTabbedPane tabPan;
    /** Split Pane */
    protected JSplitPane splPan;
    /** LineChart */
    protected LineChart lc;
    /** DataSeries */
    protected DataSerie[] se;
    /** number of displayed data series */
    protected int nDisSer;
    /** order in which data series are displayed */
    protected int[] disOrd;
    /** reference to ResultManager */
    protected ResultManager resMan;
    /** flag whether an optimization is currently in progress */
    protected boolean optRuns;
    /** user preference */
    private Preference pref;
    /** flag whether split pane is horizontal or vertical */
    protected boolean splPanHor;

    /** The main routine */
    public static void main(String[] args){
    	if (GenOpt.DEBUG) System.err.println(GenOpt.DEBUG_WARNING);
	try{
	    WinGenOpt w = new WinGenOpt();
	    w.setVisible(true);
	    // test if first argument is a file, then assume it is 
	    // the initialization file
	    if ( args.length != 0 ){
		File iniFil = new File(args[0]);
		if (iniFil.canRead())
		    w.startGenOpt(iniFil);
	    }
	}
	catch(Throwable t){
	    t.printStackTrace();
	    System.exit(1);
	}
    }
}
