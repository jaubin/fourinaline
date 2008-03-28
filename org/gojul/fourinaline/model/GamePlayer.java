/*
 * GamePlayer.java
 *
 * Created: 2008/02/23
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

import java.io.Serializable;

import org.gojul.fourinaline.model.GameModel.PlayerMark;

/**
 * The <code>GamePlayer</code> interface represents a player of
 * the four in a line game.
 * 
 * @author Julien Aubin
 */
public interface GamePlayer extends Serializable
{
	/**
	 * Increments the player score by one.
	 * @throws UnsupportedOperationException if the operation is not supported.
	 */
	public void incrementScore() throws UnsupportedOperationException;

	/**
	 * Returns the player name.
	 * @return the player name.
	 */
	public String getName();
	
	/**
	 * Returns the player mark.
	 * @return the player mark.
	 */
	public PlayerMark getPlayerMark();
	
	/**
	 * Returns the player score.
	 * @return the player score.
	 */
	public int getScore();
}

/**
 * The <code>GamePlayerImpl</code> class is the
 * default implementation of the <code>GamePlayer</code>
 * class.
 * 
 * @author Julien Aubin
 */
final class GamePlayerImpl implements GamePlayer
{
	/**
	 * The serial version UID.
	 */
	final static long serialVersionUID = 1;
	
	/**
	 * The player name.
	 */
	private String playerName;
	
	/**
	 * The player mark.
	 */
	private PlayerMark playerMark;
	
	/**
	 * The player score.
	 */
	private int score;
	
	/**
	 * Constructor.
	 * @param name the player name.
	 * @param mark the player mark.
	 * @throws NullPointerException if any of the method parameter is null.
	 */
	public GamePlayerImpl(final String name, final PlayerMark mark)
		throws NullPointerException
	{
		if (name == null || mark == null)
			throw new NullPointerException();
		
		playerName = name;
		playerMark = mark;
		score = 0;
	}
	
	
	/**
	 * @see org.gojul.fourinaline.model.GamePlayer#incrementScore()
	 */
	public final void incrementScore() throws UnsupportedOperationException
	{
		score++;
	}
	
	
	/**
	 * @see org.gojul.fourinaline.model.GamePlayer#getName()
	 */
	public final String getName()
	{
		return playerName;
	}
	
	
	/**
	 * @see org.gojul.fourinaline.model.GamePlayer#getPlayerMark()
	 */
	public final PlayerMark getPlayerMark()
	{
		return playerMark;
	}

	/**
	 * @see org.gojul.fourinaline.model.GamePlayer#getScore()
	 */
	public int getScore()
	{
		return score;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj)
	{
		if (obj !=  null && obj instanceof GamePlayer)
		{
			GamePlayer gpTest = (GamePlayer) obj;
			
			return gpTest.getPlayerMark().equals(playerMark)
				&& gpTest.getName().equals(playerName);
		}
		else
			return false;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{		
		return super.hashCode();
	}


	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return new StringBuffer("Name: ").append(playerName).append(" - mar : ").append(playerMark)
		   .append(" - score: ").append(score).toString();
	}
	
	
}

/**
 * The <code>UnmodifiableGamePlayer</code> class is a decorator
 * over a standard player that makes it immutable.
 * 
 * @author Julien Aubin
 */
final class UnmodifiableGamePlayer implements GamePlayer
{
	/**
	 * The serial version UID.
	 */
	final static long serialVersionUID = 1;

	/**
	 * The decorated game player.
	 */
	private GamePlayer gamePlayer;
	
	/**
	 * Constructor.
	 * @param player the player to decorate.
	 * @throws NullPointerException if any of the method parameter is null.
	 */
	public UnmodifiableGamePlayer(final GamePlayer player) throws NullPointerException
	{
		if (player == null)
			throw new NullPointerException();
		
		gamePlayer = player;
	}
	
	/**
	 * @see org.gojul.fourinaline.model.GamePlayer#getScore()
	 */
	public int getScore()
	{
		return gamePlayer.getScore();
	}

	/**
	 * @see org.gojul.fourinaline.model.GamePlayer#getName()
	 */
	public String getName()
	{		
		return gamePlayer.getName();
	}

	/**
	 * @see org.gojul.fourinaline.model.GamePlayer#getPlayerMark()
	 */
	public PlayerMark getPlayerMark()
	{
		return gamePlayer.getPlayerMark();
	}

	/**
	 * @see org.gojul.fourinaline.model.GamePlayer#incrementScore()
	 */
	public void incrementScore() throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException();		
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj)
	{
		return gamePlayer.equals(obj);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return gamePlayer.hashCode();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		// TODO Raccord de méthode auto-généré
		return gamePlayer.toString();
	}
	
	
}
