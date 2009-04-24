/*
 * MainFrame.java
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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.gojul.fourinaline.gui.GameModelPanel.GameModelPanelEvent;
import org.gojul.fourinaline.gui.GameModelPanel.GameModelPanelListener;
import org.gojul.fourinaline.model.GameModel;
import org.gojul.fourinaline.model.HumanGameClient;
import org.gojul.fourinaline.model.GameClient.ComputerGameClient;
import org.gojul.fourinaline.model.GameModel.GameStatus;
import org.gojul.fourinaline.model.GameModel.PlayerMark;

/**
 * The <code>MainFrame</code> class is the project main frame.
 * 
 * @see org.gojul.fourinaline.gui.GUIMessages
 * 
 * @author Julien Aubin
 */
@SuppressWarnings("serial")
public final class MainFrame extends JFrame implements Observer, WindowListener, GameModelPanelListener
{	
	
	/**
	 * The <code>NewGameAction</code> class starts a new game if possible.
	 * 
	 * @author Julien Aubin
	 */
	@SuppressWarnings("serial")
	private final static class NewGameAction extends AbstractAction
	{ 
		
		/**
		 * The game client used.
		 */
		private MainFrame mainFrame;

		/**
		 * Constructor.
		 * @param frame the main frame used.
		 */
		public NewGameAction(final MainFrame frame)
		{
			super(GUIMessages.NEW_GAME_ACTION_TEXT.toString());
			putValue(MNEMONIC_KEY, KeyEvent.VK_N);
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK));
			
			mainFrame = frame;
			setEnabled(mainFrame.isGameOwner);
		}
		
		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(final ActionEvent e)
		{
			boolean commandResult;
			
			PlayerMark currentPlayerMark = null;
			
			try
			{
				commandResult = mainFrame.gameClient.newGame();
				GameModel currentModel = mainFrame.gameClient.getGameModelImmediately();
				
				// Avoids some possible multithread issues in which a
				// user may have ended a game that has just started now.
				if (currentModel != null)
					currentPlayerMark = currentModel.getCurrentPlayer();
			}
			catch (Throwable t)
			{
				t.printStackTrace();
				commandResult = false;
			}
			
			if (commandResult)
			{
				mainFrame.gameModelPanel.forceUpdate();
				// In case the game has just ended, we must allow the user to recreate
				// a new game.
				setEnabled(currentPlayerMark == null);
				// We enable the end game action if and only if this is up to the player who
				// controls this client to play. This avoids a lot of very tricky issues without 
				// being inconvenient from the user point of view.
				mainFrame.endGameAction.setEnabled(mainFrame.isGameOwner
						&& mainFrame.gameClient.getPlayer().getPlayerMark().equals(currentPlayerMark));
			}
			else
				JOptionPane.showMessageDialog(mainFrame, 
						GUIMessages.IMPOSSIBLE_TO_START_A_NEW_GAME, 
						GUIMessages.ERROR_TEXT.toString(), 
						JOptionPane.ERROR_MESSAGE);
		}
		
	}
	
	/**
	 * The end game action ends the current game if possible.
	 * 
	 * @author Julien Aubin
	 */
	@SuppressWarnings("serial")
	private final static class EndGameAction extends AbstractAction
	{ 
		
		/**
		 * The game client used.
		 */
		private MainFrame mainFrame;

		/**
		 * Constructor.<br/>
		 * Sets the action as disabled.
		 * @param frame the main frame used.
		 */
		public EndGameAction(final MainFrame frame)
		{
			super(GUIMessages.END_GAME_ACTION_TEXT.toString());
			putValue(MNEMONIC_KEY, KeyEvent.VK_E);
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK));
			
			mainFrame = frame;
			setEnabled(false);
		}

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(final ActionEvent e)
		{
			boolean commandResult;
			
			try
			{
				commandResult = mainFrame.gameClient.endGame();
			}
			catch (Throwable t)
			{
				t.printStackTrace();
				commandResult = false;
			}
			
			if (commandResult)
			{
				mainFrame.gameModelPanel.forceUpdate();
				setEnabled(false);
				mainFrame.newGameAction.setEnabled(mainFrame.isGameOwner);
			}
			else
				JOptionPane.showMessageDialog(mainFrame, 
						GUIMessages.IMPOSSIBLE_TO_END_THE_CURRENT_GAME, 
						GUIMessages.ERROR_TEXT.toString(), 
						JOptionPane.ERROR_MESSAGE);
			
		}
		
	}
	
	/**
	 * The quit action.
	 * 
	 * @author Julien Aubin
	 */
	@SuppressWarnings("serial")
	private final static class QuitAction extends AbstractAction
	{	 
		
		/**
		 * The game client used.
		 */
		private MainFrame mainFrame;

		/**
		 * Constructor.<br/>
		 * @param frame the main frame used.
		 */
		public QuitAction(final MainFrame frame)
		{
			super(GUIMessages.QUIT_ACTION_TEXT.toString());
			putValue(MNEMONIC_KEY, KeyEvent.VK_Q);
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK));
			
			mainFrame = frame;
		}

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(final ActionEvent e)
		{
			mainFrame.quit();			
		}
	}
	
	/**
	 * The help about action.
	 * 
	 * @author Julien Aubin
	 */
	@SuppressWarnings("serial")
	private final static class AboutAction extends AbstractAction
	{ 
		
		/**
		 * The game client used.
		 */
		private MainFrame mainFrame;
		
		/**
		 * The about dialog.
		 */
		private AboutDialog aboutDialog;
		
		/**
		 * Constructor.
		 * @param frame the main frame used.
		 */
		public AboutAction(final MainFrame frame)
		{
			super(GUIMessages.ABOUT_ACTION_TEXT.toString());
			
			mainFrame = frame;
			aboutDialog = null;
		}

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(final ActionEvent e)
		{
			if (aboutDialog == null)
				aboutDialog = new AboutDialog(mainFrame);
			
			aboutDialog.setVisible(true);
			
		}
	}

	/**
	 * The game client this frame uses.
	 */
	private HumanGameClient gameClient;
	
	/**
	 * The new game action.
	 */
	private AbstractAction newGameAction;
	
	/**
	 * The end game action.
	 */
	private AbstractAction endGameAction;
	
	/**
	 * Boolean indicating whether the current client is the game owner
	 * or not, i.e. its the first player connected to the server, or not.
	 */
	private boolean isGameOwner;
	
	/**
	 * The game model panel.
	 */
	private GameModelPanel gameModelPanel;
	
	/**
	 * Constructor.<br/>
	 * The constructor makes the frame visible.
	 * @param client the client to consider.
	 * @throws NullPointerException if any of the method parameter is null.
	 */
	public MainFrame(final HumanGameClient client) throws NullPointerException
	{
		super();
		
		if (client == null)
			throw new NullPointerException();
		
		gameClient = client;
		gameClient.addObserver(this);
		
		isGameOwner = client.isGameOwner();
		
		initFrame();
		initMenu();
		
		addWindowListener(this);
		
		setVisible(true);
	}
	
	/**
	 * Inits the frame.
	 */
	private void initFrame()
	{
		setTitle(GUIMessages.MAIN_FRAME_TITLE.toString());
		Dimension size = new Dimension(600, 600); 
		setMinimumSize(size);
		setSize(size);
		setLocationRelativeTo(null);
		// The close operation must be performed manually.
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		
		getContentPane().setLayout(new BorderLayout());
		gameModelPanel = new GameModelPanel(gameClient);
		getContentPane().add(gameModelPanel, BorderLayout.CENTER);
		gameModelPanel.addGameModelPanelListener(this);
	}
	
	/**
	 * Inits the menu.
	 */
	private void initMenu()
	{
		JMenuBar menuBar = new JMenuBar();
		this.setJMenuBar(menuBar);
		
		// File menu
		JMenu fileMenu = new JMenu(GUIMessages.FILE_MENU_TEXT.toString());
		fileMenu.setMnemonic('F');
		menuBar.add(fileMenu);
		
		newGameAction = new NewGameAction(this);
		fileMenu.add(newGameAction);		
		endGameAction = new EndGameAction(this);
		fileMenu.add(endGameAction);		
		fileMenu.addSeparator();
		fileMenu.add(new QuitAction(this));
		
		// Help menu
		JMenu helpMenu = new JMenu(GUIMessages.HELP_MENU_TEXT.toString());
		helpMenu.setMnemonic('H');
		menuBar.add(helpMenu);
		helpMenu.add(new AboutAction(this));
		
	}
	
	/**
	 * Quits the game.
	 */
	private void quit()
	{
		int answerResult = JOptionPane.showConfirmDialog(this, GUIMessages.QUIT_MESSAGE, GUIMessages.MAIN_FRAME_TITLE.toString(), JOptionPane.YES_NO_OPTION);
		
		if (answerResult == JOptionPane.YES_OPTION)
		{
			gameClient.disconnect();
			ComputerGameClient.disconnectLocalComputerClients();
			dispose();
			System.exit(0);
		}
	}
	
	/**
	 * Informs the user that we've disconnected from the server
	 * and quits the program.
	 */
	private void disconnectionFromServer()
	{
		// No gameClient.disconnect() call here since we're already
		// disconnected from the server.
		
		JOptionPane.showMessageDialog(this, GUIMessages.DISCONNECTED_FROM_SERVER_MESSAGE, 
				GUIMessages.ERROR_TEXT.toString(), JOptionPane.ERROR_MESSAGE);
		setVisible(false);
		System.exit(0);
	}
	
	

	/**
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(final Observable o, final Object arg)
	{
		if (o == gameClient)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				/**
				 * @see java.lang.Runnable#run()
				 */
				public void run()
				{
					if (!gameClient.isConnectedToServer())
					{
						disconnectionFromServer();
						return;
					}
					
					GameModel model = gameClient.getGameModel();
					
					
					// Updates the actions if necessary.
					if (model == null || !model.getGameStatus().equals(GameStatus.CONTINUE_STATUS))
					{
						// Well... no risk of redundant enabling
						// with the gameFinished() listener since
						// there's no thread concurrency at this stage.
						// Thus in some conditions the new game action
						// must be enabled while no game has been started
						// nor is running. In these cases the gameFinished()
						// event will never be thrown.
						newGameAction.setEnabled(isGameOwner);
						endGameAction.setEnabled(false);
					}
					// Prevents the user from ending a game when it is not up to it to play.
					// Avoids some very tricky unsolvable multithread conflicts, without being
					// surprising for the users.
					else if (model != null && model.getGameStatus().equals(GameStatus.CONTINUE_STATUS))
					{
						newGameAction.setEnabled(false);				
						endGameAction.setEnabled(isGameOwner && model.getCurrentPlayer().equals(gameClient.getPlayer().getPlayerMark()));
					}
				}
			});
		}
		
	}

	/**
	 * @see org.gojul.fourinaline.gui.GameModelPanel.GameModelPanelListener#gameFinished(org.gojul.fourinaline.gui.GameModelPanel.GameModelPanelEvent)
	 */
	public void gameFinished(final GameModelPanelEvent e)
	{
		if (e.getSource() == gameModelPanel)
		{
			newGameAction.setEnabled(isGameOwner);
			endGameAction.setEnabled(false);
			
			if (isGameOwner)
			{
				if (JOptionPane.showConfirmDialog(this, GUIMessages.PLAY_AGAIN, GUIMessages.MAIN_FRAME_TITLE.toString(), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)
						== JOptionPane.YES_OPTION) {
					// The new game action ignores the action event there...
					newGameAction.actionPerformed(null);
				}
			}
		}
		
	}

	/**
	 * @see java.awt.event.WindowListener#windowActivated(java.awt.event.WindowEvent)
	 */
	public void windowActivated(final WindowEvent e)
	{
		// TODO Raccord de méthode auto-généré
		
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
		
	}

	/**
	 * @see java.awt.event.WindowListener#windowIconified(java.awt.event.WindowEvent)
	 */
	public void windowIconified(final WindowEvent e)
	{
		
	}

	/**
	 * @see java.awt.event.WindowListener#windowOpened(java.awt.event.WindowEvent)
	 */
	public void windowOpened(final WindowEvent e)
	{
		
	}
	
	
}
