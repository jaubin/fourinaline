/*
 * LoginFrame.java
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

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.gojul.fourinaline.gui.PlayerSelectionFrame.AIGameLevel;
import org.gojul.fourinaline.model.GameServer;
import org.gojul.fourinaline.model.GameServerImpl;

/**
 * The login frame makes it possible for the user to select
 * the game type they want.
 * 
 * @author Julien Aubin
 */
public final class LoginFrame extends JDialog implements ChangeListener, ActionListener
{

	/**
	 * The serial version UID.
	 */
	final static long serialVersionUID = 1;
	
	/**
	 * The localhost radio button.
	 */
	private JRadioButton localHostRadioButton;
	
	/**
	 * The remote host text field.
	 */
	private JTextField remoteHostTextField;
	
	/**
	 * The host button group.
	 */
	private ButtonGroup hostButtonGroup;
	
	/**
	 * The computer adversory button.
	 */
	private JRadioButton computerAdversoryRadioButton;
	
	/**
	 * The human adversory button.
	 */
	private JRadioButton humanAdversoryRadioButton;
	
	/**
	 * The adversory button group.
	 */
	private ButtonGroup adversoryButtonGroup;
	
	/**
	 * The AI level combo box.
	 */
	private JComboBox aiLevelComboBox;
	
	/**
	 * The OK button.
	 */
	private JButton okButton;
	
	/**
	 * The cancel button.
	 */
	private JButton cancelButton;
	
	/**
	 * Constructor.<br/>
	 * Does not make the frame visible.
	 */
	public LoginFrame()
	{
		super();
		setSize(500, 200);
		setResizable(false);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setTitle(GUIMessages.MAIN_FRAME_TITLE.toString());
		setLocationRelativeTo(null);
		
		initFrame();
	}
	
	/**
	 * Inits the frame.
	 */
	private void initFrame()
	{
		getContentPane().setLayout(new GridBagLayout());
		
		initHostPanel();
		initPlayerPanel();
		initBottomPanel();
		
		// Inits the change listeners after initializing the panels in order
		// to avoid NPE.
		localHostRadioButton.addChangeListener(this);
		computerAdversoryRadioButton.addChangeListener(this);
		okButton.addActionListener(this);
		cancelButton.addActionListener(this);
	}
	
	/**
	 * Inits the host panel.
	 */
	private void initHostPanel()
	{
		JPanel hostPanel = new JPanel();
		hostPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), GUIMessages.HOST_TEXT.toString()));
		GridBagConstraints hostPanelConstraints = new GridBagConstraints();
		hostPanelConstraints.gridx = 0;
		hostPanelConstraints.gridy = 0;
		hostPanelConstraints.fill = GridBagConstraints.BOTH;
		hostPanelConstraints.weightx = 1.0;
		hostPanelConstraints.weighty = 0.5;
		getContentPane().add(hostPanel, hostPanelConstraints);
		
		hostPanel.setLayout(new GridLayout(2, 1));
		
		localHostRadioButton = new JRadioButton(GUIMessages.LOCAL_GAME_TEXT.toString());
		hostPanel.add(localHostRadioButton);
		
		hostButtonGroup = new ButtonGroup();
		hostButtonGroup.add(localHostRadioButton);
		
		JPanel remoteHostPanel = new JPanel();
		remoteHostPanel.setLayout(new GridBagLayout());
		hostPanel.add(remoteHostPanel);
		
		JRadioButton remoteHostRadioButton = new JRadioButton(GUIMessages.REMOTE_SERVER_TEXT.toString());
		GridBagConstraints remoteHostRadioButtonConstraints = new GridBagConstraints();
		remoteHostRadioButtonConstraints.gridx = 0;
		remoteHostRadioButtonConstraints.gridy = 0;
		remoteHostRadioButtonConstraints.fill = GridBagConstraints.BOTH;
		hostButtonGroup.add(remoteHostRadioButton);
		remoteHostPanel.add(remoteHostRadioButton, remoteHostRadioButtonConstraints);
		
		remoteHostTextField = new JTextField();
		GridBagConstraints remoteHostTextFieldConstraints = new GridBagConstraints();
		remoteHostTextFieldConstraints.gridx = 1;
		remoteHostTextFieldConstraints.gridy = 0;
		remoteHostTextFieldConstraints.fill = GridBagConstraints.BOTH;
		remoteHostTextFieldConstraints.weightx = 1.0;
		remoteHostPanel.add(remoteHostTextField, remoteHostTextFieldConstraints);
		
		// By default, the localhost radio button is always selected.
		localHostRadioButton.setSelected(true);
		remoteHostTextField.setEnabled(!localHostRadioButton.isSelected());
	}
	
	/**
	 * Inits the player panel.
	 */
	private void initPlayerPanel()
	{
		JPanel adversoryPanel = new JPanel();
		adversoryPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), GUIMessages.ADVERSORY_TEXT.toString()));
		GridBagConstraints adversoryPanelConstraints = new GridBagConstraints();
		adversoryPanelConstraints.gridx = 0;
		adversoryPanelConstraints.gridy = 1;
		adversoryPanelConstraints.fill = GridBagConstraints.BOTH;
		adversoryPanelConstraints.weightx = 1.0;
		adversoryPanelConstraints.weighty = 0.5;
		getContentPane().add(adversoryPanel, adversoryPanelConstraints);
		
		adversoryPanel.setLayout(new GridLayout(1, 2));
		
		adversoryButtonGroup = new ButtonGroup();
		
		// Computer info
		JPanel computerAdversoryPanel = new JPanel();
		adversoryPanel.add(computerAdversoryPanel);
		computerAdversoryPanel.setLayout(new FlowLayout());
		
		computerAdversoryRadioButton = new JRadioButton(GUIMessages.COMPUTER_ADVERSORY_TEXT.toString());
		computerAdversoryPanel.add(computerAdversoryRadioButton);	
		adversoryButtonGroup.add(computerAdversoryRadioButton);
		
		aiLevelComboBox = new JComboBox(AIGameLevel.getAvailableLevels());
		computerAdversoryPanel.add(aiLevelComboBox);
		aiLevelComboBox.setEditable(false);
		aiLevelComboBox.setSelectedItem(AIGameLevel.WEAK_AI_GAME_LEVEL);
		
		// Human info
		humanAdversoryRadioButton = new JRadioButton(GUIMessages.HUMAN_ADVERSORY_TEXT.toString());
		adversoryPanel.add(humanAdversoryRadioButton);
		adversoryButtonGroup.add(humanAdversoryRadioButton);
		
		computerAdversoryRadioButton.setSelected(true);
	}
	
	/**
	 * Inits the bottom panel.
	 */
	private void initBottomPanel()
	{
		JPanel bottomPanel = new JPanel();
		GridBagConstraints bottomPanelConstraints = new GridBagConstraints();
		bottomPanelConstraints.gridx = 0;
		bottomPanelConstraints.gridy = 2;
		bottomPanelConstraints.weightx = 1.0;
		bottomPanelConstraints.fill = GridBagConstraints.BOTH;
		
		add(bottomPanel, bottomPanelConstraints);
		
		bottomPanel.setLayout(new FlowLayout());
		
		okButton = new JButton(GUIMessages.OK_TEXT.toString());
		bottomPanel.add(okButton);
		
		cancelButton = new JButton(GUIMessages.CANCEL_TEXT.toString());
		bottomPanel.add(cancelButton);
	}

	/**
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public void stateChanged(final ChangeEvent e)
	{
		if (e.getSource() == localHostRadioButton)
		{
			boolean isLocalHostSelected = localHostRadioButton.isSelected();
			
			remoteHostTextField.setEnabled(!isLocalHostSelected);
		}
		else if (e.getSource() == computerAdversoryRadioButton)
		{
			aiLevelComboBox.setEnabled(computerAdversoryRadioButton.isSelected());
		}
		
	}
	
	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(final ActionEvent e)
	{
		if (e.getSource() == cancelButton)
		{
			dispose();
		}
		else if (e.getSource() == okButton)
		{
			String serverHost = "";
			
			if (localHostRadioButton.isSelected())
			{
				serverHost = "127.0.0.1";
				
				if (!GameServerImpl.startDaemon())
				{
					JOptionPane.showMessageDialog(this, GUIMessages.UNABLE_TO_START_SERVER_MESSAGE.toString(), GUIMessages.ERROR_TEXT.toString(), JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			else
			{
				serverHost = remoteHostTextField.getText();
				
				if (serverHost == null || serverHost.trim().length() == 0)
				{
					JOptionPane.showMessageDialog(this, GUIMessages.YOU_MUST_SELECT_A_SERVER_MESSAGE.toString(), GUIMessages.ERROR_TEXT.toString(), JOptionPane.ERROR_MESSAGE);
					remoteHostTextField.requestFocusInWindow();
					return;
				}
				
				serverHost = serverHost.trim();
			}
			
			GameServer gameServer = null;
			
			try
			{
				Registry registry = LocateRegistry.getRegistry(serverHost);
				gameServer = (GameServer) registry.lookup(GameServer.STUB_NAME);
			}
			catch (Throwable t)
			{
				JOptionPane.showMessageDialog(this, GUIMessages.UNABLE_TO_CONNECT_TO_SERVER_MESSAGE + t.getMessage(), GUIMessages.ERROR_TEXT.toString(), JOptionPane.ERROR_MESSAGE);
				t.printStackTrace();
				return;
			}
			
			dispose();
			
			PlayerSelectionFrame psFrame = null;
			
			// If the user decided to join an existing game, it is harmless
			// here to indicate that we want to create an AI player. This will
			// do nothing.
			if (computerAdversoryRadioButton.isSelected())
			{
				AIGameLevel aiLevel = (AIGameLevel) aiLevelComboBox.getSelectedItem();
				psFrame = new PlayerSelectionFrame(gameServer, aiLevel);
			}
			else
				psFrame = new PlayerSelectionFrame(gameServer, null);
				
			psFrame.setVisible(true);
		}
	}

	public static void main(String[] args)
	{
		new LoginFrame().setVisible(true);
	}
}
