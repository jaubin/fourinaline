/*
 * AboutDialog.java
 *
 * Created: 2008/03/07
 *
 * Copyright (C) 2008 Julien Aubin
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.gojul.fourinaline.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import org.gojul.fourinaline.model.MiscUtils;

/**
 * The <code>AboutDialog</code> frame displays copyright information about
 * the program.
 * 
 * @author Julien Aubin
 */
@SuppressWarnings("serial")
public final class AboutDialog extends JDialog implements ActionListener
{

	/**
	 * Constructor.
	 * @param relativeWindow the window to which
	 * the position of this frame is relative.
	 */
	public AboutDialog(final Window relativeWindow)
	{
		super();
		setSize(700, 220);
		setLocationRelativeTo(relativeWindow);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setAlwaysOnTop(true);
		setResizable(false);
		setTitle(GUIMessages.ABOUT_FRAME_TITLE.toString());
		
		getContentPane().setLayout(new BorderLayout(5, 5));
		
		JTabbedPane tabbedPane = new JTabbedPane();
		getContentPane().add(tabbedPane, BorderLayout.CENTER);
		
		JPanel aboutPanel = new JPanel();
		aboutPanel.setOpaque(true);
		aboutPanel.setBackground(Color.WHITE);
		aboutPanel.setLayout(new GridBagLayout());
		tabbedPane.addTab(GUIMessages.ABOUT_FRAME_TITLE.toString(), aboutPanel);
		
		JLabel aboutLabel = new JLabel("<html><h2>" + GUIMessages.MAIN_FRAME_TITLE
				+ "</h2><br>" + "<font face=\"arial\" size=2>" + GUIMessages.VERSION.toString() + " " + MiscUtils.getVersion()
				+ "<br>" + GUIMessages.COPYRIGHT_INFO.toString() 
				+ "<br>" + GUIMessages.LICENSE_INFO.toString()
				+ "<br>" + GUIMessages.POMAKIS_ACKNOWLEDGEMENT + "</font></html>");
		GridBagConstraints labelConstraints = new GridBagConstraints();
		labelConstraints.fill = GridBagConstraints.BOTH;
		labelConstraints.weightx = 1.0;
		labelConstraints.weighty = 1.0;
		labelConstraints.gridx = 0;
		labelConstraints.gridy = 0;
		labelConstraints.insets = new Insets(0, 5, 0, 5);
		aboutPanel.add(aboutLabel, labelConstraints);
		
		try
		{
			// No problem here : the stream is properly closed in the readTextStream method.
			String licenseText = MiscUtils.readTextStream(this.getClass().getResourceAsStream("/license.txt"));
			
			JPanel licensePanel = new JPanel();
			licensePanel.setLayout(new GridBagLayout());
			tabbedPane.addTab(GUIMessages.LICENSE.toString(), licensePanel);
			
			JTextArea licenseTextArea = new JTextArea(licenseText);
			licenseTextArea.setEditable(false);
			// No word wrap there : the license is a properly formatted text file.
			
			GridBagConstraints licenseTextAreaConstraints = new GridBagConstraints();
			licenseTextAreaConstraints.fill = GridBagConstraints.BOTH;
			licenseTextAreaConstraints.weightx = 1.0;
			licenseTextAreaConstraints.weighty = 1.0;
			licenseTextAreaConstraints.gridx = 0;
			licenseTextAreaConstraints.gridy = 0;
			licenseTextAreaConstraints.insets = new Insets(5, 5, 5, 5);
			licensePanel.add(new JScrollPane(licenseTextArea), licenseTextAreaConstraints);
			
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
		
		JButton okButton = new JButton(GUIMessages.OK_TEXT.toString());
		okButton.addActionListener(this);
		getContentPane().add(okButton, BorderLayout.SOUTH);
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(final ActionEvent e)
	{
		dispose();		
	}
	
	
}
