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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.gojul.fourinaline.model.AIGameClient;
import org.gojul.fourinaline.model.DefaultEvalScore;
import org.gojul.fourinaline.model.GameClient;
import org.gojul.fourinaline.model.GamePlayer;
import org.gojul.fourinaline.model.GameServer;
import org.gojul.fourinaline.model.GameServerImpl;
import org.gojul.fourinaline.model.HumanGameClient;
import org.gojul.fourinaline.model.GameModel.PlayerMark;
import org.gojul.fourinaline.model.GameServer.PlayerRegisterException;
import org.gojul.fourinaline.model.GameServer.ServerTicketException;

/**
 * The login frame makes it possible for the user to select
 * the game type they want.
 * 
 * @author Julien Aubin
 */
public final class LoginFrame extends JDialog implements ChangeListener, ActionListener
{
	
	/**
	 * The <code>AIGameLevel</code> class represents the game level the user
	 * has selected.
	 *
	 * @author Julien Aubin
	 */
	private final static class AIGameLevel implements Serializable
	{
		/**
		 * The serial version UID.
		 */
		final static long serialVersionUID = 1;
		
		/**
		 * The list of game levels.
		 */
		private final static List<AIGameLevel> gameLevelList = new ArrayList<AIGameLevel>();
		
		/**
		 * The level display message.
		 */
		private GUIMessages levelDisplayMessage;
		
		/**
		 * The deepness level.
		 */
		private int deepnessLevel;
		
		/**
		 * Constructor.
		 * @param displayMessage the message to display to the user.
		 * @param level the AI level.
		 */
		private AIGameLevel(final GUIMessages displayMessage, final int level)
		{
			levelDisplayMessage = displayMessage;
			deepnessLevel = level;
			gameLevelList.add(this);
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{			
			return levelDisplayMessage.toString();
		}
		
		/**
		 * Returns the level.
		 * @return the level.
		 */
		public int getLevel()
		{
			return deepnessLevel;
		}
		
		/**
		 * Returns the list of available levels.
		 * @return the list of available levels.
		 */
		public final static AIGameLevel[] getAvailableLevels()
		{
			AIGameLevel[] result = new AIGameLevel[gameLevelList.size()];			
			gameLevelList.toArray(result);			
			return result;
		}
		
		/**
		 * The weak AI game level.
		 */
		public final static AIGameLevel WEAK_AI_GAME_LEVEL = new AIGameLevel(GUIMessages.WEAK_AI_LEVEL_MESSAGE, 4);
		
		/**
		 * The intermediate AI game level.
		 */
		public final static AIGameLevel INTERMEDIATE_AI_GAME_LEVEL = new AIGameLevel(GUIMessages.INTERMEDIATE_AI_LEVEL_MESSAGE, 5);
		
		/**
		 * The strong AI game level.
		 */
		public final static AIGameLevel STRONG_AI_GAME_LEVEL = new AIGameLevel(GUIMessages.STRONG_AI_LEVEL_MESSAGE, 6);
	}
	
	/**
	 * The <code>PlayerSelectionFrame</code> is a simple window
	 * that makes it possible for a player to register.
	 * 
	 * @author Julien Aubin
	 */
	private final static class PlayerSelectionFrame extends JDialog implements Runnable, WindowListener, ActionListener, KeyListener
	{

		/**
		 * The serial version UID.
		 */
		final static long serialVersionUID = 1;
		
		/**
		 * The game server.
		 */
		private GameServer gameServer;
		
		/**
		 * Boolean that indicates that the player list update is running.
		 */
		private boolean isPlayerListUpdateRunning;
		
		/**
		 * The player list.
		 */
		private JList playerList;
		
		/**
		 * The player name text field.
		 */
		private JTextField playerNameTextField;
		
		/**
		 * The OK button.
		 */
		private JButton okButton;
		
		/**
		 * The cancel button.
		 */
		private JButton cancelButton;
		
		/**
		 * The AI player game level, if applicable, or null
		 * if this is not the case.
		 */
		private AIGameLevel aiPlayerGameLevel;
		
		/**
		 * Constructor.
		 * @param server the game server.
		 * @param aiGameLevel the AI game level, in case the adversory is an AI,
		 * null otherwise.
		 * @throws NullPointerException if <code>server</code> is null.
		 */
		public PlayerSelectionFrame(final GameServer server, final AIGameLevel aiGameLevel)
			throws NullPointerException
		{
			super();
			setSize(500, 200);
			setResizable(false);
			setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			setTitle(GUIMessages.MAIN_FRAME_TITLE.toString());
			setLocationRelativeTo(null);
			addWindowListener(this);
			getContentPane().setLayout(new GridBagLayout());
			
			if (server == null)
				throw new NullPointerException();
			
			gameServer = server;
			isPlayerListUpdateRunning = true;
			aiPlayerGameLevel = aiGameLevel;
			
			initTopPanel();
			initNamePanel();
			initBottomPanel();
			
			// The thread here is started at initialization time
			// in order to update regularly the player list.
			new Thread(this).start();
		}
		
		/**
		 * Inits the top panel, which contains the player list.
		 */
		private void initTopPanel()
		{
			JPanel topPanel = new JPanel();
			GridBagConstraints topPanelConstraints = new GridBagConstraints();
			topPanelConstraints.gridx = 0;
			topPanelConstraints.gridy = 0;
			topPanelConstraints.fill = GridBagConstraints.BOTH;
			topPanelConstraints.weightx = 1.0;
			topPanelConstraints.weighty = 1.0;
			
			getContentPane().add(topPanel, topPanelConstraints);
			
			topPanel.setLayout(new BorderLayout());
			
			JLabel registeredLabel = new JLabel(GUIMessages.LIST_OF_PLAYERS_MESSAGE.toString());
			topPanel.add(registeredLabel, BorderLayout.NORTH);
			
			playerList = new JList();
			topPanel.add(playerList, BorderLayout.CENTER);
		}
		
		/**
		 * Inits the name panel.
		 */
		private void initNamePanel()
		{
			JPanel namePanel = new JPanel();
			GridBagConstraints namePanelConstraints = new GridBagConstraints();
			namePanelConstraints.gridx = 0;
			namePanelConstraints.gridy = 1;
			namePanelConstraints.fill = GridBagConstraints.BOTH;
			namePanelConstraints.weightx = 1.0;
			
			getContentPane().add(namePanel, namePanelConstraints);
			
			namePanel.setLayout(new GridLayout(3, 1));
			
			JLabel nameLabel = new JLabel(GUIMessages.ENTER_THE_NAME_OF_YOUR_PLAYER_MESSAGE.toString());
			namePanel.add(nameLabel);
			
			JLabel nameLabel2 = new JLabel(GUIMessages.PLAYER_NAME_MUST_BE_DIFFERENT_FROM_THE_OTHER_NAMES_MESSAGE.toString());
			namePanel.add(nameLabel2);
			
			playerNameTextField = new JTextField();
			namePanel.add(playerNameTextField);
			playerNameTextField.addKeyListener(this);
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
			bottomPanelConstraints.fill = GridBagConstraints.BOTH;
			bottomPanelConstraints.weightx = 1.0;
			
			getContentPane().add(bottomPanel, bottomPanelConstraints);
			
			bottomPanel.setLayout(new FlowLayout());
			
			okButton = new JButton(GUIMessages.OK_TEXT.toString());
			okButton.addActionListener(this);
			bottomPanel.add(okButton);
			
			cancelButton = new JButton(GUIMessages.CANCEL_TEXT.toString());
			cancelButton.addActionListener(this);
			bottomPanel.add(cancelButton);
		}
		
		/**
		 * @see java.lang.Runnable#run()
		 */
		public void run()
		{
			// This thread updates regularly the player list.
			boolean isRunning = false;
			Set<GamePlayer> currentPlayersLocal = new LinkedHashSet<GamePlayer>();
			
			synchronized(this)
			{
				isRunning = isPlayerListUpdateRunning;
			}
			
			while (isRunning)
			{
				
				Set<GamePlayer> currentPlayersServer = null;
				
				try
				{
					currentPlayersServer = new LinkedHashSet<GamePlayer>(gameServer.getPlayers());
				}
				catch (RemoteException e)
				{
					e.printStackTrace();
					JOptionPane.showMessageDialog(this, GUIMessages.LOST_CONNECTION_TO_SERVER_MESSAGE, GUIMessages.ERROR_TEXT.toString(), JOptionPane.ERROR_MESSAGE);
					quit();
				}
				
				if (currentPlayersServer != null && !currentPlayersServer.equals(currentPlayersLocal))
				{
					currentPlayersLocal = currentPlayersServer;
					
					String[] playerNames = new String[currentPlayersLocal.size()];
					int i = 0;
					          						
					for (GamePlayer player: currentPlayersLocal)
					{
						playerNames[i] = player.getName();
						i++;
					}
					
					// Avoids unnecessary synchronizations and update in order to lower the
					// CPU consumption.
					synchronized(this)
					{
						playerList.setListData(playerNames);						
					}
				}
				
				try
				{
					// Sleeps some time in order to avoid synchronization issues.
					Thread.sleep(100);
				}
				catch (Throwable t)
				{
					t.printStackTrace();
				}
				
				synchronized(this)
				{
					isRunning = isPlayerListUpdateRunning;
				}
			}
			
		}

		/**
		 * Quits the program.
		 */
		private void quit()
		{
			synchronized(this)
			{
				isPlayerListUpdateRunning = false;
			}
			System.exit(0);
		}
		
		/**
		 * Creates an AI game client if this is necessary.<br/>
		 * Note that this creation may fail if two human clients
		 * connect to the server before the AI game client could be created.
		 */
		private void createAIGameClientIfNecessary()
		{
			assert aiPlayerGameLevel != null: "At this stage, the AI player game level must not be null.";
			
			boolean continueTryingCreateAIGameClient = true;
			int computerPlayerIndex = 0;
			
			while (continueTryingCreateAIGameClient)
			{
				try
				{
					String playerName = "Computer";
					
					if (computerPlayerIndex != 0)
						playerName += " " + computerPlayerIndex;
					
					// The "4" number is a magic number that defines the game difficulty.
					// TODO : make it possible for the user to define a custom difficulty level.
					
					GameClient AIclient = new AIGameClient(gameServer, GUIMessages.COMPUTER_ADVERSORY_TEXT.toString(), new DefaultEvalScore(), aiPlayerGameLevel.getLevel());
					new Thread(AIclient).start();
					continueTryingCreateAIGameClient = false;
				}
				catch (PlayerRegisterException e)
				{
					e.printStackTrace();
					computerPlayerIndex++;
				}
				catch (RemoteException e)
				{
					JOptionPane.showMessageDialog(this, GUIMessages.DISCONNECTED_FROM_SERVER_MESSAGE, GUIMessages.ERROR_TEXT.toString(), JOptionPane.ERROR_MESSAGE);
					quit();
				}
				catch (ServerTicketException ex)
				{
					JOptionPane.showMessageDialog(this, GUIMessages.NO_PLAYER_AVAILABLE_MESSAGE, GUIMessages.ERROR_TEXT.toString(), JOptionPane.ERROR_MESSAGE);
					continueTryingCreateAIGameClient = false;
				}
			}
		}
		
		/**
		 * Launches the game, or connects
		 * to it.<br/>
		 * In case there's an error while launching the game,
		 * halts the program.
		 */
		private void launchGame()
		{
			String playerName = playerNameTextField.getText();
			
			if (playerName == null || playerName.trim().length() == 0)
			{
				JOptionPane.showMessageDialog(this, GUIMessages.YOU_MUST_SPECIFY_A_PLAYER_NAME_MESSAGE, GUIMessages.ERROR_TEXT.toString(), JOptionPane.ERROR_MESSAGE);
				playerNameTextField.requestFocusInWindow();					
				return;
			}
			
			playerName = playerName.trim();
			
			HumanGameClient gameClient = null;
			
			try
			{
				gameClient = new HumanGameClient(gameServer, playerName);
			}
			catch (PlayerRegisterException ex)
			{
				JOptionPane.showMessageDialog(this, GUIMessages.FAILED_TO_REGISTER_MESSAGE, GUIMessages.ERROR_TEXT.toString(), JOptionPane.ERROR_MESSAGE);
				return;
			}
			catch (RemoteException ex)
			{
				JOptionPane.showMessageDialog(this, GUIMessages.DISCONNECTED_FROM_SERVER_MESSAGE, GUIMessages.ERROR_TEXT.toString(), JOptionPane.ERROR_MESSAGE);
				quit();
			}
			catch (ServerTicketException ex)
			{
				JOptionPane.showMessageDialog(this, GUIMessages.NO_PLAYER_AVAILABLE_MESSAGE, GUIMessages.ERROR_TEXT.toString(), JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			synchronized(this)
			{
				isPlayerListUpdateRunning = false;
			}
			
			if (aiPlayerGameLevel != null)
				createAIGameClientIfNecessary();
			
			okButton.setEnabled(false);
			cancelButton.setEnabled(false);
			playerNameTextField.setEnabled(false);
			setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
			
			new Thread(new WaitThread(this, gameClient)).start();
		}

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(final ActionEvent e)
		{
			if (e.getSource() == okButton)
			{
				launchGame();
			}
			else if (e.getSource() == cancelButton)
			{
				quit();
			}
		}
		
		/**
		 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
		 */
		public void keyPressed(final KeyEvent e)
		{
			
		}

		/**
		 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
		 */
		public void keyReleased(final KeyEvent e)
		{
			
		}

		/**
		 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
		 */
		public void keyTyped(final KeyEvent e)
		{
			if (e.getSource() == playerNameTextField)
			{
				if (e.getKeyChar() == KeyEvent.VK_ENTER)
				{
					e.consume();
					launchGame();
				}
			}
		}

		/**
		 * The wait thread is a small thread that disables the player selection
		 * frame until all the players have been logged.
		 *
		 * @author Julien Aubin
		 */
		private final static class WaitThread implements Runnable, ActionListener
		{
			
			/**
			 * The server query period, in order to avoid a query overload.
			 */
			private final static int PLAYER_QUERY_PERIOD = 100;
			
			/**
			 * The time out period : 2 minutes.
			 */
			private final static int TIME_OUT_PERIOD = 2 * 60 * 1000;
			
			/**
			 * The player selection frame.
			 */
			private PlayerSelectionFrame playerSelectionFrame;
			
			/**
			 * The game client.
			 */
			private HumanGameClient gameClient;
			
			/**
			 * The time out timer.
			 */
			private Timer timer;
			
			/**
			 * Constructor.
			 * @param frame the player selection frame.
			 * @param client the human game client.
			 * @throws NullPointerException if any of the method parameter is null.
			 */
			public WaitThread(final PlayerSelectionFrame frame, final HumanGameClient client) throws NullPointerException
			{
				if (frame == null || client == null)
					throw new NullPointerException();
				
				playerSelectionFrame = frame;
				gameClient = client;
				
				timer = new Timer(TIME_OUT_PERIOD, this);
				timer.start();
			}

			/**
			 * @see java.lang.Runnable#run()
			 */
			public void run()
			{
				GameServer gameServer = playerSelectionFrame.gameServer;
				
				try
				{
					while (gameServer.getPlayers().size() < PlayerMark.getNumberOfPlayerMarks())
					{
						// We wait a small amount of time there in order to avoid overloading
						// the server with requests.
						try
						{
							Thread.sleep(PLAYER_QUERY_PERIOD);
						}
						catch (InterruptedException e)
						{
							
						}
					}
				}
				catch (RemoteException e)
				{					
					e.printStackTrace();
					JOptionPane.showMessageDialog(playerSelectionFrame, GUIMessages.DISCONNECTED_FROM_SERVER_MESSAGE, GUIMessages.ERROR_TEXT.toString(), JOptionPane.ERROR_MESSAGE);
					
					// Disconnects the client  in order to void server blocking.
					try
					{
						gameClient.disconnect();
					}
					catch (RuntimeException ex)
					{
						ex.printStackTrace();
					}
					
					System.exit(0);
				}
				
				// Avoids the program exiting surprisingly.
				timer.stop();
				
				playerSelectionFrame.dispose();
				
				new MainFrame(gameClient).setVisible(true);
			
				// Starts the client thread AFTER the main frame in order to ensure
				// to capture all the update events.
				new Thread(gameClient).start();
				
			}

			/**
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			public void actionPerformed(final ActionEvent e)
			{
				JOptionPane.showMessageDialog(playerSelectionFrame, GUIMessages.DISCONNECTED_FROM_SERVER_MESSAGE, GUIMessages.ERROR_TEXT.toString(), JOptionPane.ERROR_MESSAGE);
				
				// Disconnects the client  in order to void server blocking.
				try
				{
					gameClient.disconnect();
				}
				catch (RuntimeException ex)
				{
					ex.printStackTrace();
				}
				
				System.exit(0);
			}
			
			
			
		}

		/**
		 * @see java.awt.event.WindowListener#windowActivated(java.awt.event.WindowEvent)
		 */
		public void windowActivated(final WindowEvent e)
		{
			
		}

		/**
		 * @see java.awt.event.WindowListener#windowClosed(java.awt.event.WindowEvent)
		 */
		public void windowClosed(final WindowEvent e)
		{
			
		}

		/**
		 * @see java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
		 */
		public void windowClosing(final WindowEvent e)
		{
			// Does nothing if the default close operation is not : Do nothing one close.
			if (getDefaultCloseOperation() != DO_NOTHING_ON_CLOSE)
				quit();			
		}

		/**
		 * @see java.awt.event.WindowListener#windowDeactivated(java.awt.event.WindowEvent)
		 */
		public void windowDeactivated(final WindowEvent e)
		{
						
		}

		/**
		 * @see java.awt.event.WindowListener#windowDeiconified(java.awt.event.WindowEvent)
		 */
		public void windowDeiconified(final WindowEvent e)
		{
			// TODO Raccord de méthode auto-généré
			
		}

		/* (non-Javadoc)
		 * @see java.awt.event.WindowListener#windowIconified(java.awt.event.WindowEvent)
		 */
		public void windowIconified(WindowEvent e)
		{
			// TODO Raccord de méthode auto-généré
			
		}

		/* (non-Javadoc)
		 * @see java.awt.event.WindowListener#windowOpened(java.awt.event.WindowEvent)
		 */
		public void windowOpened(WindowEvent e)
		{
			// TODO Raccord de méthode auto-généré
			
		}
		
		
	}

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
	 * Constructor.
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
		computerAdversoryRadioButton.addChangeListener(this);
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
