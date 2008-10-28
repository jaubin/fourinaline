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
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

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
		setSize(700, 200);
		setLocationRelativeTo(relativeWindow);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setAlwaysOnTop(true);
		setResizable(false);
		setTitle(GUIMessages.ABOUT_FRAME_TITLE.toString());
		
		getContentPane().setLayout(new BorderLayout());
		
		JPanel textPanel = new JPanel();
		textPanel.setLayout(new GridLayout(4, 1));
		getContentPane().add(textPanel, BorderLayout.CENTER);
		
		textPanel.add(new JLabel(GUIMessages.MAIN_FRAME_TITLE.toString()));
		textPanel.add(new JLabel(GUIMessages.COPYRIGHT_INFO.toString()));
		textPanel.add(new JLabel(GUIMessages.LICENSE_INFO.toString()));
		textPanel.add(new JLabel(GUIMessages.POMAKIS_ACKNOWLEDGEMENT.toString()));
		
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
