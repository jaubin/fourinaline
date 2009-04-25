/*
 * AIGameClient.java
 *
 * Created: 2008/02/24
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
package org.gojul.fourinaline.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;

import org.gojul.fourinaline.model.GameClient.ComputerGameClient;
import org.gojul.fourinaline.model.GameModel.GameModelException;
import org.gojul.fourinaline.model.GameModel.GameStatus;
import org.gojul.fourinaline.model.GameModel.PlayerMark;
import org.gojul.fourinaline.model.GameServer.PlayerRegisterException;
import org.gojul.fourinaline.model.GameServer.ServerTicket;
import org.gojul.fourinaline.model.GameServer.ServerTicketException;

/**
 * The <code>AIGameClient</code> part of the game represents any AI player.
 * It uses an alpha beta algorithm in order to look for the best play possible
 * at every turn.<br/>
 * The evaluation function can be customized to your own needs, you just have
 * to implement the <code>EvalScore</code> interface and update the UI.
 * 
 * @see org.gojul.fourinaline.model.AIGameClient.EvalScore
 * 
 * @author Julien Aubin.
 */
public final class AIGameClient extends ComputerGameClient
{	
	/**
	 * The <code>EvalScore</code> class is the interface for
	 * all the game evaluation algorithms.
	 * 
	 * @author Julien Aubin
	 */
	public static interface EvalScore extends Serializable
	{		
		
		/**
		 * Returns the score of the game model <code>gameModel</code>
		 * for the player mark <code>playerMark</code>.
		 * @param gameModel the game model to consider.
		 * @param playerMark the player mark to consider.
		 * @return the score of the game model <code>gameModel</code>
		 * for the player mark <code>playerMark</code>.
		 * @throws NullPointerException if any of the method parameter
		 * is null.
		 */
		public int evaluate(final GameModel gameModel, final PlayerMark playerMark)
			throws NullPointerException;
	}
	
	/**
	 * The alpha beta algorithm.
	 */
	private AlphaBeta alphaBeta;
	
	/**
	 * Constructor.
	 * @param server the game server.
	 * @param ticket the server ticket.
	 * @param playerName the player name.
	 * @param evalScore the game evaluation function.
	 * @param deepness the search deepness.
	 * @throws IllegalArgumentException if <code>deepness</code> is smaller than 0.
	 * @throws NullPointerException if any of the method parameter is null.
	 * @throws PlayerRegisterException if there's an error while registering
	 * the player which has for name <code>playerName</code>.
	 * @throws ServerTicketException if no more server ticket is available.
	 * @throws RemoteException if a remote error occurs while registering the game.
	 */
	public AIGameClient(final GameServer server, final ServerTicket ticket, final String playerName, final EvalScore evalScore, final int deepness)
		throws NullPointerException, IllegalArgumentException, PlayerRegisterException, RemoteException, ServerTicketException
	{
		super(server, ticket, playerName);
		
		if (evalScore == null)
			throw new NullPointerException();
		
		if (deepness < 0)
			throw new IllegalArgumentException("Invalid search deepness : " + deepness);
		
		alphaBeta = new AlphaBeta(evalScore, deepness);
	}
	
	/**
	 * @see java.lang.Runnable#run()
	 */
	public void run()
	{
		PlayerMark playerMark = getPlayer().getPlayerMark();
		
		// The thread stops if the server is no longer running,
		// or at the first exception encountered.
		while (isConnectedToServer())
		{
			
			try
			{
				GameModel gameModel = getServer().getGame(getPlayer().getPlayerMark(), getTicket());
				
				// No problem here : the AI waits until a new game has been launched
				// when the previous game is over and checks that it's up to it to play
				// in order to avoid bad issues due to control instructions like start game
				// and end game.
				if (getServer().isGameRunning() && gameModel != null && gameModel.getCurrentPlayer().equals(playerMark))
				{		
					try
					{
						int columnIndex = alphaBeta.getColumnIndex(gameModel, playerMark); 
						getServer().play(columnIndex, playerMark, gameModel, getTicket());
					}
					// Avoids some tricky case in which a new game has been started
					// while the AI player was playing.
					catch (GameModelException e)
					{
						e.printStackTrace();
					}
				}
				else
				{
					// The sleep process is here to avoid the use of an infinite
					// loop which would consume a lot of CPU.
					try
					{
						Thread.sleep(SERVER_STATUS_UPDATE_PERIOD);
					}
					catch (InterruptedException e)
					{
						throw new RuntimeException(e.getMessage());
					}
				}
			}
			catch (RemoteException e)
			{
				// Avoids blocking the server by having too many dead client.
				disconnect();
				throw new RuntimeException(e.getMessage());
			}
			
		}
		
		// Avoids blocking the server by having too many dead client.
		disconnect();
	}

	/**
	 * An implementation of the alpha-beta algorithm for our purpose.
	 * This implementation makes it possible to use any user-developed evaluation
	 * algorithm that can be better than the one provided.<br/>
	 * This algorithm implements a caching mechanism to improve the performance
	 * of the AI player.
	 * 
	 * @author Julien Aubin
	 */
	private final static class AlphaBeta implements Serializable
	{
		/**
		 * The serial version UID.
		 */
		final static long serialVersionUID = 1;
		
		/**
		 * The cache initial capacity.
		 */
		private final static int CACHE_INITIAL_CAPACITY = 5000;
		
		/**
		 * The evaluation function.
		 */
		private EvalScore evalScore;
		
		/**
		 * The search deepness.
		 */
		private int deepness;
		
		/**
		 * The score cache.
		 */
		private transient Map<String, Integer> scoreCache;
		
		/**
		 * Constructor.
		 * @param evalScoreFunction the evaluation function used.
		 * @param deepnessSearch the search deepness.
		 */
		public AlphaBeta(final EvalScore evalScoreFunction, final int deepnessSearch)
		{
			evalScore = evalScoreFunction;
			deepness = deepnessSearch;
			scoreCache = new WeakHashMap<String, Integer>(CACHE_INITIAL_CAPACITY);
		}
		
		/**
		 * Returns the index of the column to play, or -1 if there's no
		 * more playable column.
		 * @param gameModel the game model to consider.
		 * @param playerMark the player mark to consider.
		 * @return the index of the column to play, or -1 if there's no
		 * more playable column.
		 */
		public int getColumnIndex(final GameModel gameModel, final PlayerMark playerMark)
		{			
			Collection<Integer> possibleColumns = gameModel.getListOfPlayableColumns();
			
			int bestColumn = -1;
			int bestScore = -Integer.MAX_VALUE;
			
			for (Integer colIndex: possibleColumns)
			{
				GameModel tempModel = new GameModel(gameModel);
				tempModel.play(colIndex.intValue(), playerMark);
				String key = tempModel.toUniqueKey();
				int currentScore = 0;
				
				Integer currentScoreInt = scoreCache.get(key);
				
				if (currentScoreInt != null)
				{
					currentScore = currentScoreInt.intValue();
				}
				else
				{
					// We build the key before performing the alpha-beta evaluation
					// becuase tempModel is mutable.
					currentScore = alphaBeta(tempModel, playerMark, Integer.MIN_VALUE, -bestScore, 0);
					scoreCache.put(key, Integer.valueOf(currentScore));
				}				
				
				if (currentScore > bestScore)
				{
					bestScore = currentScore;
					bestColumn = colIndex;
				}
			}
			
			return bestColumn;
		}
		
		/**
		 * Deserializes the AI game client in case of serialization.
		 * @param in the input stream responsible of deserialization.
		 * @throws IOException if an I/O error occurs while deserializing.
		 * @throws ClassNotFoundException in case a class to be deserialized
		 * is not found.
		 */
		private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException
		{
			in.defaultReadObject();
			scoreCache = new WeakHashMap<String, Integer>(CACHE_INITIAL_CAPACITY);
		}

		
		/**
		 * Performs an alpha-beta algorithm over the game model <code>gameModel</code>,
		 * with current player <code>playerMark</code>.
		 * @param gameModel the game model to consider. 
		 * @param playerMark the player mark to consider.
		 * @param alpha the alpha value.
		 * @param beta the beta value.
		 * @param currentDeepness the deepness in the alpha-beta tree.
		 * @return the score of each possibility of the alpha beta model.
		 */
		private int alphaBeta(final GameModel gameModel, final PlayerMark playerMark, final int alpha, final int beta, final int currentDeepness)
		{		
			// Game won by the player.
			if (gameModel.getGameStatus() == GameStatus.WON_STATUS)
			{
				return Integer.MAX_VALUE - currentDeepness;
			}
			// Tie game.
			else if (gameModel.getGameStatus() == GameStatus.TIE_STATUS)
				return 0;
			// Maximum deepness.
			else if (currentDeepness >= deepness)
				return evalScore.evaluate(gameModel, playerMark);
			else
			{
				int bestScore = Integer.MIN_VALUE;
				
				PlayerMark tempMark = PlayerMark.getNextMark(playerMark);
				
				int alphaEval = alpha;
				
				Collection<Integer> possiblePlays = gameModel.getListOfPlayableColumns();
				
				for (Integer colIndex: possiblePlays)
				{
					GameModel tempModel = new GameModel(gameModel);					
					tempModel.play(colIndex.intValue(), tempMark);
					String key = tempModel.toUniqueKey();
					
					int currentScore = 0;
					
					Integer currentScoreInt = scoreCache.get(key);
					
					if (currentScoreInt != null)
					{
						currentScore = currentScoreInt.intValue();
					}
					else
					{
						// We build the key before performing the alpha-beta evaluation
						// becuase tempModel is mutable.
						currentScore = alphaBeta(tempModel, tempMark, -beta, -alphaEval, currentDeepness + 1);
						scoreCache.put(key, Integer.valueOf(currentScore));
					}
					
					if (currentScore > bestScore)
					{
						bestScore = currentScore;
						
						if (bestScore > alphaEval)
						{
							alphaEval = bestScore;
							
							if (alphaEval > beta)
							{
								// What is good for the other player is bad for this one.
								return -bestScore;
							}
						}
					}
				}
				
				// What is good for the other player is bad for this one.
				return -bestScore;
			}
		}
		
		public static void main(String[] args)
		{
			GameModel gameModel = new GameModel();
			gameModel.play(3, gameModel.getCurrentPlayer());
			gameModel.play(3, gameModel.getCurrentPlayer());
			gameModel.play(4, gameModel.getCurrentPlayer());
			System.out.println(gameModel);
			
			AlphaBeta alphaBeta = new AlphaBeta(new DefaultEvalScore(), 4);
			
			PlayerMark mark = gameModel.getCurrentPlayer();
			
			System.out.println(alphaBeta.getColumnIndex(gameModel, mark));
		}
	}
}
