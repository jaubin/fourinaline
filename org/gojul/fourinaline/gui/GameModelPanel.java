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
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.EventObject;
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
import javax.swing.event.EventListenerList;

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
	 * The <code>GameModelPanelEvent</code> class represents the
	 * events launched by the game model panel.
	 *
	 * @author Julien Aubin
	 */
	public final static class GameModelPanelEvent extends EventObject
	{
		/**
		 * Constructor.
		 * @param source the event source.
		 */
		public GameModelPanelEvent(final Object source) {
			super(source);
		}
	}
	
	/**
	 * The <code>GameModelPanelListener</code> interface handles
	 * the events of a game model panel instance.
	 *
	 * @author Julien Aubin
	 */
	public static interface GameModelPanelListener extends EventListener 
	{
		/**
		 * Notify the listeners of the game model panel that a game
		 * has just been finished.
		 * @param e the launched game model panel event.
		 */
		public void gameFinished(final GameModelPanelEvent e);
	}
	
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
		@SuppressWarnings("unused")
		public final static PlayerColorRepresentation PLAYER_A_REPRESENTATION = new PlayerColorRepresentation(PlayerMark.PLAYER_A_MARK, Color.YELLOW);
		
		/**
		 * The player B icon representation.
		 */
		@SuppressWarnings("unused")
		public final static PlayerColorRepresentation PLAYER_B_REPRESENTATION = new PlayerColorRepresentation(PlayerMark.PLAYER_B_MARK, Color.RED);
	}
	
	/**
	 * The <code>GamePlayerPanel</code> represents the data
	 * of a game player.
	 * 
	 * @author Julien Aubin
	 */
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
	private final static class GameModelDrawPanel extends JPanel implements MouseListener
	{	
		/**
		 * The cell border width.
		 */
		private final static int CELL_BORDER_WIDTH = 3;
		
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
		 * The game player marks.<br>
		 * We use an array which actually mirrors
		 * the game model in order to be able to
		 * play animations easily.
		 */
		private PlayerMark[][] playerMarks;
		
		/**
		 * Boolean indicating that the animation
		 * is running.<br/>
		 * When the animation is running, the user
		 * cannot click the panel.
		 */
		private boolean animationRunning;
		
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
			playerMarks = new PlayerMark[gameModel.getRowCount()][gameModel.getColCount()];
			animationRunning = false;
			
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
			int cellHeightDraw = cellHeight - CELL_BORDER_WIDTH;
			int cellWidthDraw = cellWidth - CELL_BORDER_WIDTH;
			
			for (int i = 0, len = playerMarks.length; i < len; i++)
			{
				for (int j = 0, len2 = playerMarks[i].length; j < len2; j++)
				{
					int cellOrigX = cellWidth * j + CELL_BORDER_WIDTH;
					int cellOrigY = cellHeight * i + CELL_BORDER_WIDTH;
					int cellDestX = cellOrigX + cellWidthDraw;
					int cellDestY = cellOrigY + cellHeightDraw;
					
					Paint currentGradient = Color.WHITE;
					
					PlayerMark mark = playerMarks[i][j];
					
					if (mark != null)
						currentGradient = PlayerColorRepresentation.valueOf(mark).getPlayerPaint(cellOrigX, cellOrigY, cellDestX, cellDestY);
					
					g2d.setPaint(currentGradient);
					g2d.fillOval(cellOrigX, cellOrigY, cellWidthDraw, cellHeightDraw);
				}
			}
			
			// Marks the last inserted chip differently
			// in case the game is running.
			if (gameModel.getGameStatus().equals(GameStatus.CONTINUE_STATUS) && lastInsertedCell != null)
			{
				// The circle mark width.
				final int CIRCLE_WIDTH = 3;
				// The circle color : a kind of green.
				final Color CIRCLE_COLOR = new Color(0, 255, 100);
				
				int j = lastInsertedCell.getColIndex();
				int i = lastInsertedCell.getRowIndex();
				g2d.setColor(CIRCLE_COLOR);
				
				g2d.fillOval(cellWidth * j + CELL_BORDER_WIDTH, cellHeight * i + CELL_BORDER_WIDTH, cellWidthDraw, cellHeightDraw);
				
				PlayerMark mark = playerMarks[i][j];
				g2d.setPaint(PlayerColorRepresentation.valueOf(mark).getPlayerPaint(cellWidth * j + CELL_BORDER_WIDTH, cellHeight * i + CELL_BORDER_WIDTH, 
						cellWidth * (j + 1) - CELL_BORDER_WIDTH, cellHeight * (i + 1) - CELL_BORDER_WIDTH));
				
				g2d.fillOval(cellWidth * j + CIRCLE_WIDTH + CELL_BORDER_WIDTH, cellHeight * i + CIRCLE_WIDTH + CELL_BORDER_WIDTH, cellWidthDraw - 2 * CIRCLE_WIDTH, cellHeightDraw - 2 * CIRCLE_WIDTH);
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
		 * In case the game model <code>model</code> contains only one chip, return
		 * its coordinates. Otherwise return null.
		 * @param model the model to test.
		 * @return the coordinate of the cell which contains the chip if applicable,
		 * null otherwise.
		 */
		private synchronized CellCoord getChipIfOnlyOneInserted(final GameModel model)
		{
			CellCoord result = null;
			int nbCellsOccupied = 0;
			
			for (int i = 0; i < model.getRowCount() && nbCellsOccupied <= 1; i++)
			{
				for (int j = 0; j < model.getColCount() && nbCellsOccupied <= 1; j++)
				{
					if (model.getCell(i, j) != null)
					{
						if (nbCellsOccupied == 0)
						{
							result = new CellCoord(i, j);
						}
						else
						{
							result = null;
						}
						
						nbCellsOccupied++;
					}
				}
			}
			
			return result;
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
						{
							// Here the new model is not a successor of the old one, so 
							// we do not return any information.
							return null;
						}
					}
				}
			}
			
			// In some conditions in a multiplayer environment, one of the client
			// may only get the model once the other has already played. In that
			// case we have to check that if there's only one chip inserted,
			// and if this is the case we return it since no difference between
			// the two models could be detected.
			if (result == null)
				result = getChipIfOnlyOneInserted(model);
			
			return result;
		}
		
		/**
		 * Display the animation of the chip dropping in the game.
		 * @param model the game model for which we want to display the animation.
		 * @param last the last inserted chip coordinates in the model.
		 */
		private synchronized void displayAnimation(final GameModel model, final CellCoord last)
		{
			// We synchronize this method in order to prevent the animation of
			// two cells dropping at the same time...
			animationRunning = true;
			
			int cellHeight = getCellHeight();
			int cellWidth = getCellWidth();
			
			PlayerMark mark = model.getCell(last);
			int rowIndex = last.getRowIndex();
			int colIndex = last.getColIndex();
			playerMarks[rowIndex][colIndex] = null;
			
			int xLeft = colIndex * cellWidth;
			
			for (int i = 0; i <= rowIndex; i++) 
			{
				int yTop = 0;
				int cellToDrawOnYAxis = 1;
				
				if (i > 0) {
					yTop = (i - 1) * cellHeight;
					cellToDrawOnYAxis = 2;
					playerMarks[i - 1][colIndex] = null;
				}
				playerMarks[i][colIndex] = mark;
				
				// Speed optimization : we only paint the updated part of the panel
				// Works notably better on OpenJDK.
				paintImmediately(new Rectangle(xLeft, yTop, cellWidth, cellToDrawOnYAxis * cellHeight));
				
				try
				{
					wait(30);
				}
				catch (Throwable t)
				{
					t.printStackTrace();
				}
			}
			
			animationRunning = false;
		}
		
		/**
		 * Updates the model this panel paints.
		 * @param model the model to update.
		 * @throws NullPointerException if {@code model} is null
		 */
		public synchronized void updateModel(final GameModel model)
			throws NullPointerException
		{
			// Here the method must be synchronized since the
			// component may be updated while it's being painted,
			// which would lead to some very bad issues.
			
			if (model == null)
				throw new NullPointerException();
			
			CellCoord lastBeforeRefresh = lastInsertedCell;
			
			// We do not set up the last inserted cell coordinates
			// now as we do not want to have a circled element before
			// it is dropped.
			lastInsertedCell = null;
			
			CellCoord last = getLastInsertedChip(model);
			
			for (int i = 0, len1 = playerMarks.length; i < len1; i++)
			{
				for (int j = 0, len2 = playerMarks[i].length; j < len2; j++)
				{
					playerMarks[i][j] = model.getCell(i, j);
				}
			}
			
			int cellWidth = getCellWidth();
			int cellHeight = getCellHeight();
			
			// Play a small animation in order to drop chips.
			// if there's a drop to display.
			if (last != null) 
			{
				displayAnimation(model, last);
				
				// We delete the circle of the last inserted cell when possible.
				if (lastBeforeRefresh != null)
				{
					paintImmediately(new Rectangle(lastBeforeRefresh.getColIndex() * cellWidth, 
							lastBeforeRefresh.getRowIndex() * cellHeight,
							cellWidth, cellHeight));
				}
			} 
			
			// Now that the animation is successful, we can copy
			// the parameters so that it is taken into account by
			// the paint() method.
			lastInsertedCell = last;
			gameModel = model;
			
			// OpenJDK speed improvement : we only paint the last inserted chip when
			// this is possible... 
			if (last != null && gameModel != null && gameModel.getGameStatus().equals(GameStatus.CONTINUE_STATUS))
			{				
				paintImmediately(new Rectangle(last.getColIndex() * cellWidth, last.getRowIndex() * cellHeight,
						cellWidth, cellHeight));
			}
			else 
			{
				repaint();
			}
		}

		/**
		 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
		 */
		public synchronized void mouseClicked(final MouseEvent e)
		{
			// Here the method must be synchronized since the
			// component may be updated while it's being painted,
			// which would lead to some very bad issues.
			
			// Thus we don't want the user to be able to click somewhere
			// while the animation is running
			if (gameModel.getCurrentPlayer().equals(gameClient.getPlayer().getPlayerMark())
					&& gameModel.getGameStatus().equals(GameStatus.CONTINUE_STATUS)
					&& !animationRunning)
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
	 * The event listener list.
	 */
	private EventListenerList eventListenerList;
	
	/**
	 * Constructor.
	 * @param client the game client to consider.
	 * @throws NullPointerException if <code>client</code> is null.
	 */
	public GameModelPanel(final HumanGameClient client) throws NullPointerException
	{
		super();
		
		eventListenerList = new EventListenerList();
		
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
		updateMainPanel(null);
		updatePlayerPanel();
	}
	
	/**
	 * Add <code>l</code> to the list of listeners of this game model
	 * panel.
	 * @param l the listener to add.
	 * @throws NullPointerException if <code>l</code> is null.
	 */
	public void addGameModelPanelListener(final GameModelPanelListener l) 
		throws NullPointerException
	{
		if (l == null)
			throw new NullPointerException();
		
		eventListenerList.add(GameModelPanelListener.class, l);
	}
	
	/**
	 * Remove <code>l</code> from the list of listeners of this game
	 * model panel.
	 * @param l the listener to remove.
	 * @throws NullPointerException if <code>l</code> is null.
	 */
	public void removeGameModelPanelListener(final GameModelPanelListener l)
		throws NullPointerException
	{
		if (l == null)
			throw new NullPointerException();
		
		eventListenerList.remove(GameModelPanelListener.class, l);
	}
	
	/**
	 * Notify the listeners that the
	 * game has been finished.
	 */
	private void fireGameFinished()
	{
		GameModelPanelListener[] listeners = eventListenerList.getListeners(GameModelPanelListener.class);
		GameModelPanelEvent event = new GameModelPanelEvent(this);
		
		for (GameModelPanelListener l: listeners)
			l.gameFinished(event);
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
	 * @param newGameModel the game model with which the update
	 * must be done, or null if the game model must be fetched
	 * using the <code>getGameModelImmediately</code> method.
	 */
	private synchronized void updateMainPanel(final GameModel newGameModel)
	{
		// This method is synchronized since it may be called by the client
		// thread or by the GUI.
		
		GameModel gameModel = newGameModel;
		
		if (newGameModel == null)
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
		
		if (gameModel == null)
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
			
			// We can display the current turn without any risk
			// as this has no impact on the user.
			// Thus it is important to have it up to date
			// even if the game has been updated immediately.
			// Thus we must do it before launching the game status
			// events to avoid some bad interaction due to the fact
			// that the clients may force the panel update, which
			// is the source of most of the UI problems of the game.
			if (localGameModel.getGameStatus().equals(GameStatus.CONTINUE_STATUS))
			{
				statusLabel.setText(GUIMessages.CURRENT_TURN_MESSAGE + getPlayerName(mark));
			}
			
			// This double-check of the game model update ensures that a
			// has-won message is not sent twice... 
			// This can occur sometimes in some race conditions.
			// Thus game status may only be updated by the client thread, not
			// the forceUpdate() method.
			if (bUpdatedGame && newGameModel != null)
			{
				// Updates the panel according to the game state.
				if (localGameModel.getGameStatus().equals(GameStatus.TIE_STATUS))
				{
					JOptionPane.showMessageDialog(this, GUIMessages.TIE_GAME_MESSAGE);
					fireGameFinished();
				}
				else if (localGameModel.getGameStatus().equals(GameStatus.WON_STATUS))
				{			
					JOptionPane.showMessageDialog(this, getPlayerName(mark) + GUIMessages.HAS_WON_MESSAGE);
					fireGameFinished();
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
		updateMainPanel(null);
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
				// We work on a local copy of the game model in order to avoid
				// some side effects due to the speed of some games.
				// In some cases the panel would not even notify that something
				// has happened.
				GameModel model = gameClient.getGameModel();
				final GameModel localModel = model != null ? new GameModel(model): null;
				
				SwingUtilities.invokeLater(new Runnable()
				{
					/**
					 * @see java.lang.Runnable#run()
					 */
					public void run()
					{
						updatePlayerPanel();				
						updateMainPanel(localModel);
					}
				});

			}
		}
		
	}
	
}
