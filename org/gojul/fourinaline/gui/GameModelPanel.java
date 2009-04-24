/*
 * GameModelPanel.java
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
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

import org.gojul.fourinaline.model.GameModel;
import org.gojul.fourinaline.model.GamePlayer;
import org.gojul.fourinaline.model.HumanGameClient;
import org.gojul.fourinaline.model.GameModel.CellCoord;
import org.gojul.fourinaline.model.GameModel.GameModelException;
import org.gojul.fourinaline.model.GameModel.GameStatus;
import org.gojul.fourinaline.model.GameModel.PlayerMark;

/**
 * The <code>GameModelPanel</code> class represents the
 * state of the current game.
 * 
 * @author Julien Aubin
 */
@SuppressWarnings("serial")
public final class GameModelPanel extends JPanel implements Observer
{
	
	/**
	 * The player color representation for a given player mark.
	 * 
	 * @author Julien Aubin
	 */
	private final static class PlayerColorRepresentation
	{
		/**
		 * The list of all the player color representations available.
		 */
		private final static List<PlayerColorRepresentation> playerIcons = new ArrayList<PlayerColorRepresentation>();
		
		/**
		 * The value of a dark color channel.
		 */
		private final static int DARK_COLOR_CHANNEL_VALUE = 100;
		
		/**
		 * The player mark to consider.
		 */
		private PlayerMark playerMark;
		
		/**
		 * The player panel icon.
		 */
		private Color playerColor;
		
		/**
		 * Constructor.
		 * @param mark the player mark to consider.
		 * @param color the player color.
		 */
		private PlayerColorRepresentation(final PlayerMark mark, final Color color)
		{
			playerMark = mark;
			playerColor = color;
			playerIcons.add(this);
		}
		
		/**
		 * Returns the player color.
		 * @return the player color.
		 */
		public Color getPlayerColor()
		{
			return playerColor;
		}
		
		/**
		 * Returns the player gradient color.
		 * @param x1 the gradient origin along the x axis.
		 * @param y1 the gradient origin along the y axis.
		 * @param x2 the gradient destination along the x axis.
		 * @param y2 the gradient destination along the y axis.
		 * @return the player gradient color.
		 */
		public Paint getPlayerPaint(final int x1, final int y1, final int x2, final int y2)
		{
			return new GradientPaint(x1, y1, playerColor, x2, y2, 
					// The color here is a darker version of the player color.
					new Color(Math.min(playerColor.getRed(), DARK_COLOR_CHANNEL_VALUE), 
					Math.min(playerColor.getGreen(), DARK_COLOR_CHANNEL_VALUE), 
					Math.min(playerColor.getBlue(), DARK_COLOR_CHANNEL_VALUE)), false);
		}
		
		/**
		 * Returns the player icon representation which has for mark <code>mark</code>.
		 * @param mark the mark to consider.
		 * @return the player icon representation which has for mark <code>mark</code>.
		 */
		public static PlayerColorRepresentation valueOf(final PlayerMark mark)
		{
			
			for (PlayerColorRepresentation repr: playerIcons)
				if (repr.playerMark.equals(mark))
					return repr;
				
			return null;
		}
		
		/**
		 * The player A icon representation.
		 */
		public final static PlayerColorRepresentation PLAYER_A_REPRESENTATION = new PlayerColorRepresentation(PlayerMark.PLAYER_A_MARK, Color.YELLOW);
		
		/**
		 * The player B icon representation.
		 */
		public final static PlayerColorRepresentation PLAYER_B_REPRESENTATION = new PlayerColorRepresentation(PlayerMark.PLAYER_B_MARK, Color.RED);
	}
	
	/**
	 * The <code>GamePlayerPanel</code> represents the data
	 * of a game player.
	 * 
	 * @author Julien Aubin
	 */
	@SuppressWarnings("serial")
	private final static class GamePlayerPanel extends JPanel
	{				
		/**
		 * The panel size.
		 */
		private final static Dimension PANEL_SIZE = new Dimension(150, 50);
		
		/**
		 * The icon panel size.
		 */
		private final static Dimension ICON_PANEL_SIZE = new Dimension(50, 50);
		
		/**
		 * The value of a dark color channel.
		 */
		private final static int DARK_COLOR_CHANNEL_VALUE = 100;
		
		/**
		 * The game player to consider.
		 */
		private GamePlayer gamePlayer;
		
		/**
		 * The icon panel.
		 */
		private JPanel iconPanel;
		
		/**
		 * Constructor
		 * @param player the player to consider.
		 * @throws NullPointerException if <code>player</code> is null.
		 */
		public GamePlayerPanel(final GamePlayer player) throws NullPointerException
		{
			super();
			
			if (player == null)
				throw new NullPointerException();
			
			gamePlayer = player;
			
			setMinimumSize(PANEL_SIZE);
			setPreferredSize(PANEL_SIZE);
			setMaximumSize(PANEL_SIZE);
			
			initPanel();
		}
		
		/**
		 * Inits the panel.
		 */
		private void initPanel()
		{
			setLayout(new BorderLayout(5, 5));
			
			// Icon
			final Color color = PlayerColorRepresentation.valueOf(gamePlayer.getPlayerMark())
				.getPlayerColor();
			iconPanel = new JPanel()
			{
				/**
				 * The serial version UID.
				 */
				final static long serialVersionUID = 1;
				
				/**
				 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
				 */
				@Override
				protected final void paintComponent(final Graphics g)
				{
					super.paintComponent(g);
					
					Graphics2D g2d = (Graphics2D) g;
					
					g2d.setPaint(new GradientPaint(0, 0, color, getWidth(), getHeight(), 
							// The color here is a darker version of the player color.
							new Color(Math.min(color.getRed(), DARK_COLOR_CHANNEL_VALUE), 
							Math.min(color.getGreen(), DARK_COLOR_CHANNEL_VALUE), 
							Math.min(color.getBlue(), DARK_COLOR_CHANNEL_VALUE)), false));					
					g2d.fillOval(0, 0, getWidth(), getHeight());
				}
			};
			
			iconPanel.setMinimumSize(ICON_PANEL_SIZE);
			iconPanel.setMaximumSize(ICON_PANEL_SIZE);
			iconPanel.setPreferredSize(ICON_PANEL_SIZE);
			
			add(iconPanel, BorderLayout.WEST);
			
			// Text
			JLabel label = new JLabel("<html><font face=\"arial\" size=3><b>" + gamePlayer.getName() + "</b></font><br>"
					+ "<font face=\"courier\" size=2>" + GUIMessages.SCORE_MESSAGE + gamePlayer.getScore() + "</font></html>");
						
			add(label, BorderLayout.CENTER);
				
		}
	}
	
	/**
	 * The <code>GameModelDrawPanel</code> draws a game model to the screen.
	 * 
	 * @author Julien Aubin
	 */
	@SuppressWarnings("serial")
	private final static class GameModelDrawPanel extends JPanel implements MouseListener
	{		
		/**
		 * The game model to consider.
		 */
		private GameModel gameModel;
		
		/**
		 * The game client to consider.
		 */
		private HumanGameClient gameClient;
		
		/**
		 * The last inserted cell coordinates.
		 */
		private CellCoord lastInsertedCell;
		
		/**
		 * Constructor.
		 * @param model the game model to draw.
		 * @throws NullPointerException if any of the method parameter is null.
		 */
		public GameModelDrawPanel(final GameModel model, final HumanGameClient client) throws NullPointerException
		{
			super();
			
			if (model == null)
				throw new NullPointerException();
			
			gameModel = model;
			gameClient = client;
			lastInsertedCell = null;
			
			addMouseListener(this);
		}
		
		/**
		 * Returns the height of a game cell.
		 * @return the height of a game cell.
		 */
		private synchronized int getCellHeight()
		{
			return getHeight() / gameModel.getRowCount();
		}
		
		/**
		 * Returns the width of a game cell.
		 * @return the width of a game cell.
		 */
		private synchronized int getCellWidth()
		{
			return getWidth() / gameModel.getColCount();
		}

		/**
		 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
		 */
		@Override
		protected synchronized void paintComponent(final Graphics g)
		{
			// Here the method must be synchronized since the
			// component may be updated while it's being painted,
			// which would lead to some very bad issues.
			
			super.paintComponent(g);
			
			Graphics2D g2d = (Graphics2D) g;
			
			// Background.
			g2d.setPaint(new GradientPaint(0, 0, Color.BLUE, getWidth(), getHeight(), new Color(0, 0, 100), false));
			g2d.fillRect(0, 0, getWidth(), getHeight());
			
			int cellHeight = getCellHeight();
			int cellWidth = getCellWidth();
			
			for (int i = 0; i < gameModel.getRowCount(); i++)
			{
				for (int j = 0; j < gameModel.getColCount(); j++)
				{
					int cellOrigX = cellWidth * j;
					int cellOrigY = cellHeight * i;
					int cellDestX = cellOrigX + cellWidth;
					int cellDestY = cellOrigY + cellHeight;
					
					Paint currentGradient = Color.WHITE;
					
					PlayerMark mark = gameModel.getCell(i, j);
					
					if (mark != null)
						currentGradient = PlayerColorRepresentation.valueOf(mark).getPlayerPaint(cellOrigX, cellOrigY, cellDestX, cellDestY);
					
					g2d.setPaint(currentGradient);
					g2d.fillOval(cellOrigX, cellOrigY, cellWidth, cellHeight);
				}
			}
			
			// Marks the last inserted chip differently
			// in case the game is running.
			if (gameModel.getGameStatus().equals(GameStatus.CONTINUE_STATUS) && lastInsertedCell != null)
			{
				// The circle mark width.
				final int CIRCLE_WIDTH = 5;
				// The circle color : a kind of green.
				final Color CIRCLE_COLOR = new Color(0, 255, 100);
				
				int j = lastInsertedCell.getColIndex();
				int i = lastInsertedCell.getRowIndex();
				g2d.setColor(CIRCLE_COLOR);
				
				g2d.fillOval(cellWidth * j, cellHeight * i, cellWidth, cellHeight);
				
				PlayerMark mark = gameModel.getCell(i, j);
				g2d.setPaint(PlayerColorRepresentation.valueOf(mark).getPlayerPaint(cellWidth * j, cellHeight * i, cellWidth * (j + 1), cellHeight * (i + 1)));
				
				g2d.fillOval(cellWidth * j + CIRCLE_WIDTH, cellHeight * i + CIRCLE_WIDTH, cellWidth - 2 * CIRCLE_WIDTH, cellHeight - 2 * CIRCLE_WIDTH);
			}
			
			if (gameModel.getGameStatus().equals(GameStatus.WON_STATUS))
			{
				List<CellCoord> winningLine = gameModel.getWinLine();
				
				assert winningLine != null: "The winning line at this stage must not be null.";
				
				int lastElementIndex = winningLine.size() - 1;
				Color winColor = Color.GREEN;
				
				// The cells are all contiguous, so drawing the line consists in drawing
				// the line from the first list element to the last list element.
				int firstCellHeight = winningLine.get(0).getRowIndex() * cellHeight + cellHeight / 2;
				int firstCellWidth = winningLine.get(0).getColIndex() * cellWidth + cellWidth / 2;
				
				int lastCellHeight = winningLine.get(lastElementIndex).getRowIndex() * cellHeight + cellHeight / 2;
				int lastCellWidth = winningLine.get(lastElementIndex).getColIndex() * cellWidth + cellWidth / 2;
				
				// The line width, in pixels
				final int HALF_LINE_WIDTH = 5;
				
				// Computes the line polygone
				int[] xCoords = null; 
				int[] yCoords = null; 
				
				// We must correctly display the line in all cases.
				
				// Same Y value for both extremities
				if (firstCellHeight == lastCellHeight)
				{
					xCoords = new int[]{firstCellWidth, lastCellWidth, lastCellWidth, firstCellWidth};
					yCoords = new int[]{firstCellHeight - HALF_LINE_WIDTH, lastCellHeight - HALF_LINE_WIDTH, lastCellHeight + HALF_LINE_WIDTH, firstCellHeight + HALF_LINE_WIDTH};
				}
				// Other standard case
				else
				{
					xCoords = new int[]{firstCellWidth - HALF_LINE_WIDTH, lastCellWidth - HALF_LINE_WIDTH, lastCellWidth + HALF_LINE_WIDTH, firstCellWidth + HALF_LINE_WIDTH};
					yCoords = new int[]{firstCellHeight, lastCellHeight, lastCellHeight, firstCellHeight};
				}
				
				g.setColor(winColor);
				g.fillPolygon(xCoords, yCoords, xCoords.length);
			}
		}
		
		/**
		 * Returns the coordinates of the last inserted chip, in case <code>model</code>
		 * is the current game model plus one inserted chip.<br/>
		 * Otherwise returns null.
		 * @param model the model from which we want to get an evolution.
		 * @return the coordinates of the last inserted chip, in case <code>model</code>
		 * is the current game model plus one inserted chip.
		 */
		private synchronized CellCoord getLastInsertedChip(final GameModel model)
		{
			CellCoord result = null;
			
			// In case the dimensions of model are not the same as the dimensions
			// of gameModel, returns null.
			if (model.getRowCount() != gameModel.getRowCount()
					|| model.getColCount() != gameModel.getColCount())
				return result;
			
			// There must be only one different cell between model and gameModel
			// and this must be a newly played cell.
			for (int i = 0; i < model.getRowCount(); i++)
			{
				for (int j = 0; j < model.getColCount(); j++)
				{
					if (model.getCell(i, j) != null && !model.getCell(i, j).equals(gameModel.getCell(i, j)))
					{
						if (result == null)
							result = new CellCoord(i, j);
						else
							// Here the new model is not a successor of the old one, so we do not
							// return any information.
							return null;
					}
				}
			}
			
			return result;
		}
		
		/**
		 * Updates the model this panel paints.
		 * @param model the model to update.
		 * @throws NullPointerException if 
		 */
		public synchronized void updateModel(final GameModel model)
			throws NullPointerException
		{
			// Here the method must be synchronized since the
			// component may be updated while it's being painted,
			// which would lead to some very bad issues.
			
			if (model == null)
				throw new NullPointerException();
			
			lastInsertedCell = getLastInsertedChip(model);
			gameModel = model;
			
			validate();
			repaint();
		}

		/**
		 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
		 */
		public synchronized void mouseClicked(final MouseEvent e)
		{
			// Here the method must be synchronized since the
			// component may be updated while it's being painted,
			// which would lead to some very bad issues.
			
			if (gameModel.getCurrentPlayer().equals(gameClient.getPlayer().getPlayerMark())
					&& gameModel.getGameStatus().equals(GameStatus.CONTINUE_STATUS))
			{
				int cellWidth = getCellWidth();
				
				int colIndex = e.getX() / cellWidth;
				
				try
				{
					gameClient.play(colIndex);
				}
				catch (GameModelException ex)
				{
					JOptionPane.showMessageDialog(this, GUIMessages.YOU_CANNOT_PLAY_THERE_MESSAGE, GUIMessages.ERROR_TEXT.toString(), JOptionPane.ERROR_MESSAGE);
				}
				catch (Throwable t)
				{
					t.printStackTrace();
				}
			}
		}

		/**
		 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
		 */
		public void mouseEntered(final MouseEvent e)
		{
			// TODO Raccord de méthode auto-généré
			
		}

		/**
		 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
		 */
		public void mouseExited(final MouseEvent e)
		{
			// TODO Raccord de méthode auto-généré
			
		}

		/**
		 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
		 */
		public void mousePressed(final MouseEvent e)
		{
			// TODO Raccord de méthode auto-généré
			
		}

		/**
		 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
		 */
		public void mouseReleased(final MouseEvent e)
		{
			// TODO Raccord de méthode auto-généré
			
		}
		
		
	}
	
	/**
	 * The game client this panel observes.
	 */
	private HumanGameClient gameClient;
	
	/**
	 * The local game model.
	 */
	private GameModel localGameModel;
	
	/**
	 * The game panel.
	 */
	private GameModelDrawPanel gameModelDrawPanel;
	
	/**
	 * The main panel, which contains both the game panel and the toolbar.
	 */
	private JPanel mainPanel;
	
	/**
	 * The current set of players.
	 */
	private Set<GamePlayer> currentPlayers;
	
	/**
	 * The player panel.
	 */
	private JPanel playerPanel;
	
	/**
	 * The game status label.
	 */
	private JLabel statusLabel;
	
	/**
	 * Constructor.
	 * @param client the game client to consider.
	 * @throws NullPointerException if <code>client</code> is null.
	 */
	public GameModelPanel(final HumanGameClient client) throws NullPointerException
	{
		super();
		
		if (client == null)
			throw new NullPointerException();
		
		setLayout(new GridBagLayout());
		
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BorderLayout(5, 5));
		GridBagConstraints contentPanelConstraints = new GridBagConstraints();
		contentPanelConstraints.gridx = 0;
		contentPanelConstraints.gridy = 0;
		contentPanelConstraints.fill = GridBagConstraints.BOTH;
		contentPanelConstraints.weightx = 1.0;
		contentPanelConstraints.weighty = 1.0;
		contentPanelConstraints.insets = new Insets(5, 5, 5, 5);
		add(contentPanel, contentPanelConstraints);
		
		gameClient = client;		
		gameClient.addObserver(this);
		
		// Inits the main panel.
		mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		contentPanel.add(mainPanel, BorderLayout.CENTER);		
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BorderLayout(5, 5));
		contentPanel.add(bottomPanel, BorderLayout.SOUTH);
		
		// Inits the player panel.
		currentPlayers = new LinkedHashSet<GamePlayer>();		
		playerPanel = new JPanel();		
		playerPanel.setLayout(new GridLayout(1, PlayerMark.getNumberOfPlayerMarks(), 5, 5));
		bottomPanel.add(playerPanel, BorderLayout.CENTER);
		
		// Inits the status label
		statusLabel = new JLabel();
		statusLabel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		GridBagConstraints statusLabelConstraints = new GridBagConstraints();
		statusLabelConstraints.gridx = 0;
		statusLabelConstraints.gridy = 1;
		statusLabelConstraints.fill = GridBagConstraints.HORIZONTAL;
		statusLabelConstraints.weightx = 1.0;
		add(statusLabel, statusLabelConstraints);
		
		// Updates the panel after having initialized the interface.
		updateMainPanel(true);
		updatePlayerPanel();
	}
	
	/**
	 * Inits the fulfilled main panel, with a real game model.
	 * @param gameModel the game model to consider.
	 */
	private void initFulfilledMainPanel(final GameModel gameModel)
	{
		assert gameModel != null: "The game model must not be null !";
		
		mainPanel.removeAll();
		
		gameModelDrawPanel = new GameModelDrawPanel(gameModel, gameClient);
		mainPanel.add(gameModelDrawPanel, BorderLayout.CENTER);		
		
	}
	
	/**
	 * Returns the name of the player which has for mark <code>mark</code>.
	 * @param mark the player mark.
	 * @return the name of the player which has for mark <code>mark</code>.
	 */
	private synchronized String getPlayerName(final PlayerMark mark)
	{
		Iterator<GamePlayer> it = currentPlayers.iterator();
		
		while (it.hasNext())
		{
			GamePlayer player = it.next();
			
			if (player.getPlayerMark().equals(mark))
				return player.getName();
		}
		
		return "";
	}
	
	/**
	 * Updates the main panel.
	 * @param forceUpdate true if the update must be forced, false
	 * elsewhere.
	 */
	private synchronized void updateMainPanel(final boolean forceUpdate)
	{
		// This method is synchronized since it may be called by the client
		// thread or by the GUI.
		
		GameModel gameModel = null;
		
		if (forceUpdate)
		{
			try
			{
				gameModel = gameClient.getGameModelImmediately();
			}
			catch (RemoteException e)
			{
				e.printStackTrace();
				gameModel = gameClient.getGameModel();
			}
		}
		else
			gameModel = gameClient.getGameModel();
		
		if (gameModel == null)
		{			
			if (localGameModel != null || forceUpdate)
			{
				mainPanel.removeAll();
			
				JLabel label = new JLabel(GUIMessages.NO_GAME_RUNNING_MESSAGE.toString());
				label.setHorizontalAlignment(JLabel.CENTER);
				label.setVerticalAlignment(JLabel.CENTER);
				label.setOpaque(true);
				label.setBackground(Color.BLUE);
				label.setForeground(Color.WHITE);
				
				gameModelDrawPanel = null;
			
				mainPanel.add(label, BorderLayout.CENTER);
				
				statusLabel.setText(GUIMessages.NO_GAME_RUNNING_MESSAGE.toString());
			}
			
			localGameModel = null;
		}
		else
		{
			if (localGameModel == null)
			{
				initFulfilledMainPanel(gameModel);
			}
			
			boolean bUpdatedGame = !gameModel.equals(localGameModel);
			
			localGameModel = new GameModel(gameModel);
			
			gameModelDrawPanel.updateModel(localGameModel);
			
			PlayerMark mark = localGameModel.getCurrentPlayer();
			
			// This double-check of the game model update ensures that a
			// has-won message is not sent twice...
			// This may occur in case the update is forced and then a "normal"
			// update occurs...
			if (bUpdatedGame)
			{
				// Updates the panel according to the game state.
				if (localGameModel.getGameStatus().equals(GameStatus.TIE_STATUS))
				{
					JOptionPane.showMessageDialog(this, GUIMessages.TIE_GAME_MESSAGE);
				}
				else if (localGameModel.getGameStatus().equals(GameStatus.WON_STATUS))
				{			
					JOptionPane.showMessageDialog(this, getPlayerName(mark) + GUIMessages.HAS_WON_MESSAGE);
				}
				else if (localGameModel.getGameStatus().equals(GameStatus.CONTINUE_STATUS))
				{
					statusLabel.setText(GUIMessages.CURRENT_TURN_MESSAGE + getPlayerName(mark));
				}
			}
		}
		
		mainPanel.validate();
		mainPanel.repaint();
		
	}
	
	/**
	 * Updates the player panel if this is needed.
	 */
	private synchronized void updatePlayerPanel()
	{	
		// This method is synchronized since it may be called by the client
		// thread or by the GUI.
		
		Set<GamePlayer> players = new LinkedHashSet<GamePlayer>();
		
		try
		{
			players.addAll(gameClient.getPlayers());
		}
		// Should never occur there.
		catch (Throwable t)
		{
			t.printStackTrace();
		}
		
		if (! players.isEmpty() && !players.equals(currentPlayers))
		{
			currentPlayers = players;
			playerPanel.removeAll();
			
			for (GamePlayer player: currentPlayers)
				playerPanel.add(new GamePlayerPanel(player));
			
			playerPanel.validate();
			playerPanel.repaint();
		}
	}
	
	/**
	 * Forces a display update.<br/>
	 * This method is synchronized because the resources it needs are shared
	 * among several threads.
	 */
	synchronized final void forceUpdate()
	{
		updatePlayerPanel();
		updateMainPanel(true);
	}

	/**
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(final Observable o, final Object arg)
	{
		if (o == gameClient)
		{
			if (!gameClient.isConnectedToServer())
				return;
			
			// The panel is shared among the client thread and the current swing thread.
			synchronized(this)
			{
				SwingUtilities.invokeLater(new Runnable()
				{
					/**
					 * @see java.lang.Runnable#run()
					 */
					public void run()
					{
						updatePlayerPanel();				
						updateMainPanel(false);
					}
				});

			}
		}
		
	}
	
}
