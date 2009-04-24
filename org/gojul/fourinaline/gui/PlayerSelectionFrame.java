/*
 * PlayerSelectionFrame.java
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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.gojul.fourinaline.model.AIGameClient;
import org.gojul.fourinaline.model.DefaultEvalScore;
import org.gojul.fourinaline.model.GameClient;
import org.gojul.fourinaline.model.GamePlayer;
import org.gojul.fourinaline.model.GameServer;
import org.gojul.fourinaline.model.HumanGameClient;
import org.gojul.fourinaline.model.GameClient.ComputerGameClient;
import org.gojul.fourinaline.model.GameModel.PlayerMark;
import org.gojul.fourinaline.model.GameServer.PlayerRegisterException;
import org.gojul.fourinaline.model.GameServer.ServerTicket;
import org.gojul.fourinaline.model.GameServer.ServerTicketException;

/**
 * The <code>PlayerSelectionFrame</code> is a simple window
 * that makes it possible for a player to register.
 * 
 * @author Julien Aubin
 */
@SuppressWarnings("serial")
final class PlayerSelectionFrame extends JDialog implements Runnable, WindowListener, ActionListener, KeyListener
{
	
	/**
	 * The <code>AIGameLevel</code> class represents the game level the user
	 * has selected.
	 *
	 * @author Julien Aubin
	 */
	final static class AIGameLevel
	{		
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
	 * The server ticket used for authentication.
	 */
	private ServerTicket serverTicket;
	
	/**
	 * Constructor.
	 * @param server the game server.
	 * @param aiGameLevel the AI game level, in case the adversory is an AI,
	 * null otherwise.
	 * @throws NullPointerException if <code>server</code> is null.
	 * @throws ServerTicketException if the connection to the server fails.
	 * @throws RemoteException if the connection to the server fails.
	 */
	public PlayerSelectionFrame(final GameServer server, final AIGameLevel aiGameLevel)
		throws NullPointerException, ServerTicketException, RemoteException
	{
		super();
		setSize(500, 250);
		setResizable(false);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setTitle(GUIMessages.MAIN_FRAME_TITLE.toString());
		setLocationRelativeTo(null);
		addWindowListener(this);
		getContentPane().setLayout(new GridBagLayout());
		
		if (server == null)
			throw new NullPointerException();
		
		gameServer = server;
		serverTicket = gameServer.getTicket();
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
		topPanelConstraints.insets = new Insets(5, 5, 5, 5);
		
		getContentPane().add(topPanel, topPanelConstraints);
		
		topPanel.setLayout(new BorderLayout(5, 5));
		
		JLabel registeredLabel = new JLabel(GUIMessages.LIST_OF_PLAYERS_MESSAGE.toString());
		topPanel.add(registeredLabel, BorderLayout.NORTH);
		
		playerList = new JList();
		topPanel.add(new JScrollPane(playerList), BorderLayout.CENTER);
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
		
		namePanel.setLayout(new GridBagLayout());
		
		JLabel nameLabel = new JLabel("<html>" + GUIMessages.ENTER_THE_NAME_OF_YOUR_PLAYER_MESSAGE.toString() 
				+ "<br>" + GUIMessages.PLAYER_NAME_MUST_BE_DIFFERENT_FROM_THE_OTHER_NAMES_MESSAGE.toString() + "</html>");
		GridBagConstraints nameLabelConstraints = new GridBagConstraints();
		nameLabelConstraints.gridx = 0;
		nameLabelConstraints.gridy = 0;
		nameLabelConstraints.fill = GridBagConstraints.BOTH;
		nameLabelConstraints.weightx = 1.0;
		nameLabelConstraints.insets = new Insets(5, 5, 5, 5);
		namePanel.add(nameLabel, nameLabelConstraints);
		
		playerNameTextField = new JTextField();
		GridBagConstraints playerNameTextFieldConstraints = new GridBagConstraints();
		playerNameTextFieldConstraints.gridx = 0;
		playerNameTextFieldConstraints.gridy = 1;
		playerNameTextFieldConstraints.fill = GridBagConstraints.HORIZONTAL;
		playerNameTextFieldConstraints.insets = new Insets(5, 5, 5, 5);
		playerNameTextFieldConstraints.weightx = 1.0;
		namePanel.add(playerNameTextField, playerNameTextFieldConstraints);
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
				
				final String[] playerNames = new String[currentPlayersLocal.size()];
				int i = 0;
				          						
				for (GamePlayer player: currentPlayersLocal)
				{
					playerNames[i] = player.getName();
					i++;
				}
				
				// Thread safety with Swing
				SwingUtilities.invokeLater(new Runnable()
				{
					/**
					 * @see java.lang.Runnable#run()
					 */
					public void run()
					{
						playerList.setListData(playerNames);
					}
				});						

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
		ComputerGameClient.disconnectLocalComputerClients();
		
		try
		{
			gameServer.releaseTicket(serverTicket);
		}
		catch (Throwable t)
		{
			t.printStackTrace();
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
		
		ServerTicket aiServerTicket = null;
		
		while (continueTryingCreateAIGameClient)
		{
			try
			{
				if (aiServerTicket == null)
					aiServerTicket = gameServer.getTicket();
				
				String playerName = "Computer";
				
				if (computerPlayerIndex != 0)
					playerName += " " + computerPlayerIndex;
				
				GameClient AIclient = new AIGameClient(gameServer, aiServerTicket, GUIMessages.COMPUTER_ADVERSORY_TEXT.toString(), new DefaultEvalScore(), aiPlayerGameLevel.getLevel());
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
			gameClient = new HumanGameClient(gameServer, serverTicket, playerName);
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
				finally
				{
					ComputerGameClient.disconnectLocalComputerClients();
				}
				
				System.exit(0);
			}
			
			// Avoids the program exiting surprisingly.
			timer.stop();
			
			SwingUtilities.invokeLater(new Runnable()
			{
				/**
				 * @see java.lang.Runnable#run()
				 */
				public void run()
				{
					playerSelectionFrame.dispose();
					new MainFrame(gameClient).setVisible(true);
				}
			});
		
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
			finally
			{
				ComputerGameClient.disconnectLocalComputerClients();
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

	/**
	 * @see java.awt.event.WindowListener#windowIconified(java.awt.event.WindowEvent)
	 */
	public void windowIconified(final WindowEvent e)
	{
		// TODO Raccord de méthode auto-généré
		
	}

	/**
	 * @see java.awt.event.WindowListener#windowOpened(java.awt.event.WindowEvent)
	 */
	public void windowOpened(final WindowEvent e)
	{
		// TODO Raccord de méthode auto-généré
		
	}
	
	
}
