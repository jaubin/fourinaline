/*
 * GlobalLoginFrame.java
 *
 * Created: 12 mai 08
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
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
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
import org.gojul.fourinaline.model.GlobalServer;
import org.gojul.fourinaline.model.GameModel.PlayerMark;

/**
 * The <code>GlobalLoginFrame</code> class enables the user to login to 
 * a global game server and to select the game mode they want.
 *
 * @author Julien Aubin
 */
public final class GlobalLoginFrame extends JDialog implements ActionListener, ChangeListener
{
	
	/**
	 * The serial version UID.
	 */
	final static long serialVersionUID = 1;
	
	/**
	 * The button of game creation.
	 */
	private JRadioButton createGameRadioButton;
	
	/**
	 * The button of game join.
	 */
	private JRadioButton joinGameRadioButton;
	
	/**
	 * The button group of game creation mode.
	 */
	private ButtonGroup gameCreationModeButtonGroup;
	
	/**
	 * The text field that specifies the name of the created game.
	 */
	private JTextField gameCreationNameTextField;
	
	/**
	 * The game join combo box.
	 */
	private JComboBox gameJoinComboBox;
	
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
	 * The remote server address.
	 */
	private String remoteServerAddress;
	
	/**
	 * The global server to use.
	 */
	private GlobalServer globalServer;
	
	/**
	 * Constructor.<br/>
	 * Does not make the fame visible.
	 * @param serverAddress the name of the global server address.
	 * @throws NullPointerException if <code>serverAddress</code> is null.
	 * @throws RemoteException if a remote error occurs while trying to connect the
	 * game server.
	 * @throws NotBoundException if the server which has for name <code>serverAddress</code>
	 * does not contain the expected server component.
	 */
	public GlobalLoginFrame(final String serverAddress) throws NullPointerException, RemoteException, NotBoundException
	{
		super();
		setSize(500, 200);
		setResizable(false);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setTitle(GUIMessages.MAIN_FRAME_TITLE.toString());
		setLocationRelativeTo(null);
		
		if (serverAddress == null)
			throw new NullPointerException();
		
		remoteServerAddress = serverAddress;
		
		initFrame();
		
		globalServer = getGlobalServer();
		
		// Fullfills the game join combo box with all the existing games.
		Vector<String> v = new Vector<String>(globalServer.getGames());
		gameJoinComboBox.setModel(new DefaultComboBoxModel(v));
	}
	
	
	/**
	 * Inits the frame.
	 */
	private void initFrame()
	{
		getContentPane().setLayout(new GridBagLayout());
		
		initGameCreationPanel();
		initPlayerPanel();
		initBottomPanel();
		
		// Inits the listeners after GUI init in order to avoid NPEs
		createGameRadioButton.addChangeListener(this);
		computerAdversoryRadioButton.addChangeListener(this);
		okButton.addActionListener(this);
		cancelButton.addActionListener(this);
	}
	
	/**
	 * Inits the game creation panel.
	 */
	private void initGameCreationPanel()
	{
		JPanel gameCreationPanel = new JPanel();
		gameCreationPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), GUIMessages.GAME_MESSAGE.toString()));
		GridBagConstraints gameCreationPanelConstraints = new GridBagConstraints();
		gameCreationPanelConstraints.gridx = 0;
		gameCreationPanelConstraints.gridy = 0;
		gameCreationPanelConstraints.fill = GridBagConstraints.BOTH;
		gameCreationPanelConstraints.weightx = 1.0;
		gameCreationPanelConstraints.weighty = 0.5;
		getContentPane().add(gameCreationPanel, gameCreationPanelConstraints);
		
		gameCreationPanel.setLayout(new GridLayout(2, 1));
		
		JPanel createPanel = new JPanel();
		createPanel.setLayout(new GridBagLayout());
		gameCreationPanel.add(createPanel);
		
		createGameRadioButton = new JRadioButton(GUIMessages.CREATE_GAME_MESSAGE.toString());
		GridBagConstraints createGameRadioButtonConstraints = new GridBagConstraints();
		createGameRadioButtonConstraints.gridx = 0;
		createGameRadioButtonConstraints.gridy = 0;
		createPanel.add(createGameRadioButton, createGameRadioButtonConstraints);
		
		gameCreationModeButtonGroup = new ButtonGroup();
		gameCreationModeButtonGroup.add(createGameRadioButton);
		
		gameCreationNameTextField = new JTextField();
		GridBagConstraints gameCreationNameTextFieldConstraints = new GridBagConstraints();
		gameCreationNameTextFieldConstraints.gridx = 1;
		gameCreationNameTextFieldConstraints.gridy = 0;
		gameCreationNameTextFieldConstraints.fill = GridBagConstraints.HORIZONTAL;
		gameCreationNameTextFieldConstraints.weightx = 1.0;
		createPanel.add(gameCreationNameTextField, gameCreationNameTextFieldConstraints);
		
		JPanel joinGamePanel = new JPanel();
		joinGamePanel.setLayout(new GridBagLayout());
		gameCreationPanel.add(joinGamePanel);
		
		joinGameRadioButton = new JRadioButton(GUIMessages.JOIN_GAME_MESSAGE.toString());
		GridBagConstraints joinGameRadioButtonConstraints = new GridBagConstraints();
		joinGameRadioButtonConstraints.gridx = 0;
		joinGameRadioButtonConstraints.gridy = 0;
		joinGamePanel.add(joinGameRadioButton, joinGameRadioButtonConstraints);
		gameCreationModeButtonGroup.add(joinGameRadioButton);
		
		gameJoinComboBox = new JComboBox();
		GridBagConstraints gameJoinComboBoxConstraints = new GridBagConstraints();
		gameJoinComboBoxConstraints.gridx = 1;
		gameJoinComboBoxConstraints.gridy = 0;
		gameJoinComboBoxConstraints.fill = GridBagConstraints.HORIZONTAL;
		gameCreationNameTextFieldConstraints.weightx = 1.0;
		joinGamePanel.add(gameJoinComboBox, gameJoinComboBoxConstraints);
		
		createGameRadioButton.setSelected(true);
		gameCreationNameTextField.setEnabled(createGameRadioButton.isSelected());
		gameJoinComboBox.setEnabled(!createGameRadioButton.isSelected());
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
	 * Returns the instance of global server running on the remote server.
	 * @return the instance of global server running on the remote server.
	 * @throws RemoteException if a remote error occurs while initializing the
	 * server.
	 * @throws NotBoundException if the remote server
	 * does not contain the expected global server.
	 */
	private GlobalServer getGlobalServer() throws RemoteException, NotBoundException
	{
		Registry registry = LocateRegistry.getRegistry(remoteServerAddress);
		return (GlobalServer) registry.lookup(GlobalServer.STUB_NAME);
	}
	
	/**
	 * Action launched when the user clicks the
	 * OK button and wants a game creation.
	 */
	private void createGame()
	{
		String gameName = gameCreationNameTextField.getText();
		
		if (gameName == null || gameName.trim().length() == 0)
		{
			JOptionPane.showMessageDialog(this, GUIMessages.YOU_MUST_SPECIFY_A_GAME_NAME_MESSAGE.toString(), GUIMessages.ERROR_TEXT.toString(), JOptionPane.ERROR_MESSAGE);
			gameCreationNameTextField.requestFocusInWindow();
			return;
		}
		
		GameServer gameServer = null;
		
		try
		{
			globalServer.createGame(gameName);
			
			Registry registry = LocateRegistry.getRegistry(remoteServerAddress);
			gameServer = (GameServer) registry.lookup(GlobalServer.SINGLE_GAME_SERVER_STUB_PREFIX + gameName);
			
		}
		catch (RemoteException e)
		{
			JOptionPane.showMessageDialog(this, GUIMessages.UNABLE_TO_CONNECT_TO_SERVER_MESSAGE + e.getMessage(), GUIMessages.ERROR_TEXT.toString(), JOptionPane.ERROR_MESSAGE);
			return;
		}
		catch (NotBoundException e)
		{
			JOptionPane.showMessageDialog(this, GUIMessages.UNABLE_TO_CONNECT_TO_SERVER_MESSAGE + e.getMessage(), GUIMessages.ERROR_TEXT.toString(), JOptionPane.ERROR_MESSAGE);
			return;
		}
		catch (AlreadyBoundException e)
		{
			JOptionPane.showMessageDialog(this, GUIMessages.THERE_IS_ALREADY_A_GAME_WITH_NAME + gameName, GUIMessages.ERROR_TEXT.toString(), JOptionPane.ERROR_MESSAGE);
			gameCreationNameTextField.requestFocusInWindow();
			return;
		}
		
		dispose();
		
		PlayerSelectionFrame psFrame = null;
		
		if (computerAdversoryRadioButton.isSelected())
		{
			AIGameLevel aiLevel = (AIGameLevel) aiLevelComboBox.getSelectedItem();
			psFrame = new PlayerSelectionFrame(gameServer, aiLevel);
		}
		else
			psFrame = new PlayerSelectionFrame(gameServer, null);
			
		psFrame.setVisible(true);
	}
	
	/**
	 * Action launched when the user clicks the OK button and
	 * wants to join a game.
	 *
	 */
	private void joinGame()
	{
		String gameName = gameJoinComboBox.getSelectedItem().toString();
		
		GameServer gameServer = null;
		
		try
		{
			Registry registry = LocateRegistry.getRegistry(remoteServerAddress);
			gameServer = (GameServer) registry.lookup(GlobalServer.SINGLE_GAME_SERVER_STUB_PREFIX + gameName);
			
			if (gameServer.getPlayers().size() == PlayerMark.getNumberOfPlayerMarks())
			{
				JOptionPane.showMessageDialog(this, GUIMessages.THE_SELECTED_GAME_IS_FULL, GUIMessages.ERROR_TEXT.toString(), JOptionPane.ERROR_MESSAGE);				
				return;
			}
		}
		catch (RemoteException e)
		{
			JOptionPane.showMessageDialog(this, GUIMessages.UNABLE_TO_CONNECT_TO_SERVER_MESSAGE + e.getMessage(), GUIMessages.ERROR_TEXT.toString(), JOptionPane.ERROR_MESSAGE);
			return;
		}
		catch (NotBoundException e)
		{
			JOptionPane.showMessageDialog(this, GUIMessages.THERE_IS_NO_GAME_WITH_NAME + gameName, GUIMessages.ERROR_TEXT.toString(), JOptionPane.ERROR_MESSAGE);
			gameJoinComboBox.requestFocusInWindow();
			return;
		}
		
		// Here we never have to init a player selection frame with any AI.
		new PlayerSelectionFrame(gameServer, null).setVisible(true);
	}
	
	/**
	 * Action launched when the user clicks the OK button.
	 */
	private void okButtonAction()
	{
		if (createGameRadioButton.isSelected())
			createGame();
		else
			joinGame();
	}
	
	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(final ActionEvent e)
	{
		if (e.getSource() == cancelButton)
			dispose();
		else if (e.getSource() == okButton)
		{
			okButtonAction();
		}

	}

	/**
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public void stateChanged(final ChangeEvent e)
	{
		if (e.getSource() == createGameRadioButton)
		{
			boolean isSelected = createGameRadioButton.isSelected();
			gameCreationNameTextField.setEnabled(isSelected);
			gameJoinComboBox.setEnabled(!isSelected);
			computerAdversoryRadioButton.setEnabled(isSelected);
			aiLevelComboBox.setEnabled(isSelected && computerAdversoryRadioButton.isSelected());
			humanAdversoryRadioButton.setEnabled(isSelected);
		}
		else if (e.getSource() == computerAdversoryRadioButton)
		{
			aiLevelComboBox.setEnabled(computerAdversoryRadioButton.isSelected());
		}
		
	}
	
	public static void main(String[] args)
	{
		try
		{
			new GlobalLoginFrame(args[0]).setVisible(true);
		}
		catch (Throwable t)
		{
			JOptionPane.showMessageDialog(null, GUIMessages.UNABLE_TO_CONNECT_TO_SERVER_MESSAGE + t.getMessage(), GUIMessages.ERROR_TEXT.toString(), JOptionPane.ERROR_MESSAGE);
			t.printStackTrace();
		}
	}

}
