package genopt.gui;
import javax.swing.*;
import javax.swing.event.*;
import genopt.*;
import java.awt.*;
import java.awt.event.*;

/** Object for adding or removing items to a list.
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

public class ListSelector extends JPanel
    implements ActionListener
{
	/** System dependent line separator */
	private final static String LS = System.getProperty("line.separator");
	
	public ListSelector(Object[] choose, Object[] selected)
	{
		// the lists
		choIte = new JList();
		selIte = new JList();
		choIte.setPrototypeCellValue("00000000000000000");
		selIte.setPrototypeCellValue("00000000000000000");

		choEnt = new DefaultListModel();
		selEnt = new DefaultListModel();
		for (int i = 0; i < choose.length; i++)
			choEnt.addElement(choose[i]);
		for (int i = 0; i < selected.length; i++)
			selEnt.addElement(selected[i]);
		choIte.setModel(choEnt);
		selIte.setModel(selEnt);

		// the buttons
		addBut = new JButton("add ->");
		addBut.addActionListener(this);
		
		remBut = new JButton("<- remove");
		remBut.addActionListener(this);
		
		GridLayout butPanLay = new GridLayout(2, 1);
		butPanLay.setVgap(10);
		JPanel butPan = new JPanel(butPanLay);
		butPan.add(addBut);
		butPan.add(remBut);					
		
		// add lists and buttons to the JPanel
		// the Layout
		add(new JScrollPane(choIte), BorderLayout.WEST);
		add(butPan, BorderLayout.CENTER);
		add(new JScrollPane(selIte), BorderLayout.EAST);
	}
	
	public void actionPerformed(ActionEvent evt)
	{
	String arg = evt.getActionCommand();
	if (arg.equals("add ->"))
		addPressed();
	else if (arg.equals("<- remove"))
		removePressed();
	else
	    System.err.println("Error: Event '" + arg + "' not implemented yet.");
	repaint();
	}
	
	/** gets the selected items
	  *@return the selected items
	  */
	public Object[] getSelectedItems()
	{
		ListModel lm = selIte.getModel();
		int n = lm.getSize();
		Object[] r = new Object[n];
		for (int i = 0; i < n; i++)
			r[i] = lm.getElementAt(i);
		return r;
	}
	/** updates the list after the "add" or release button was pressed
	 */	
	protected void addPressed()
	{
		addOrRemovePressed(choEnt, selEnt, choIte);
		selIte.setModel(selEnt);
		choIte.setModel(choEnt);
	}	
	
	/** updates the list after the "removed" button was pressed
	  */
	protected void removePressed()
	{
		addOrRemovePressed(selEnt, choEnt, selIte);
		selIte.setModel(selEnt);
		choIte.setModel(choEnt);
	}

	/** updates the list after the "add" or release button was pressed
	  */
	protected void addOrRemovePressed(DefaultListModel source, DefaultListModel sink, JList markedList)
	{
		int[] selInd = markedList.getSelectedIndices();
		int nCha = selInd.length; // number of changes
		// update the source list
		for (int i = 0; i < nCha; i++)
			sink.addElement(source.remove(selInd[i]-i));
	}
	
	/** list with choose entries */
	private JList choIte;
	/** list with select entries */
	private JList selIte;
	/** the choose entries */
	private DefaultListModel choEnt;
	/** the selected entries */
	private DefaultListModel selEnt;	
	/** add button */
	private JButton addBut;
	/** remove button */
	private JButton remBut;

	public static void main(String[] args)
	{
		JFrame f = new JFrame();

		int N = 20;
		String[] cho = new String[N];
		for(int i = 0; i < N; i++)
			cho[i] = "item " + i;
		String[] sel = {"abc", "def"};
		ListSelector ls = new ListSelector(cho, sel);
		f.getContentPane().add(ls);
		f.pack();
		f.setVisible(true);
		System.exit(0);
	}

}	







