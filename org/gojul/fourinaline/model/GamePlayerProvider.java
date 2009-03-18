/*
 * GamePlayerProvider.java
 *
 * Created: 28 oct. 08
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

import java.util.Set;

import org.gojul.fourinaline.model.GameModel.PlayerMark;

/**
 * The <code>GamePlayerProvider</code> interface provides the
 * game players used by a game server implementation.
 *
 * @author Julien Aubin
 */
public interface GamePlayerProvider
{
	/**
	 * Returns the game player which has for name <code>name</code>.
	 * @param name the player name.
	 * @param playerMark the player mark.
	 * @return the game player which has for name <code>name</code> and for mark <code>playerMark</code>.
	 * @throws NullPointerException if any of the method parameter is null.
	 * @throws RuntimeException if the player with name <code>name</code>
	 * could not be retrieved.
	 */
	public GamePlayer getGamePlayer(final String name, final PlayerMark playerMark) throws NullPointerException, RuntimeException;
	
	/**
	 * Stores in statistics the game won by <code>winner</code>. This
	 * method should be called by the game server every time a game
	 * has ended.
	 * @param winner the game winner.
	 * @param gamePlayers the set of game players who attended to the game. 
	 * @throws NullPointerException if <code>gamePlayers</code> is null.
	 * @throws RuntimeException if an error occured while storing the mae.
	 */
	public void storeGame(final GamePlayer winner, final Set<GamePlayer> gamePlayers) throws NullPointerException, RuntimeException;
	
	/**
	 * Releases the player which has for name <code>name</code> for the current game.
	 * If the player who has for name <code>name</code> does not play to any current
	 * game, then they should be considered as logged off.
	 * @param name the player to release.
	 * @throws NullPointerException if <code>name</code> is not null.
	 * @throws RuntimeException if an error occurs while releasing the game player
	 * which has for name <code>name</code>.
	 */
	public void releasePlayer(final String name) throws NullPointerException, RuntimeException;
}

/**
 * The <code>DefaultGamePlayerProvider</code> class provides a default implementation
 * of the <code>GamePlayerProvider</code> interface used on a server environment.
 *
 * @author Julien Aubin
 */
final class DefaultGamePlayerProvider implements GamePlayerProvider
{

	/**
	 * @see org.gojul.fourinaline.model.GamePlayerProvider#getGamePlayer(java.lang.String, org.gojul.fourinaline.model.GameModel.PlayerMark)
	 */
	public GamePlayer getGamePlayer(final String name, final PlayerMark playerMark) throws NullPointerException, RuntimeException
	{
		if (name == null || playerMark == null)
			throw new NullPointerException();
		
		return new GamePlayerImpl(name, playerMark);
	}

	/**
	 * @see org.gojul.fourinaline.model.GamePlayerProvider#storeGame(org.gojul.fourinaline.model.GamePlayer, java.util.Set)
	 */
	public void storeGame(final GamePlayer winner, final Set<GamePlayer> gamePlayers) throws NullPointerException, RuntimeException
	{
		if (gamePlayers == null)
			throw new NullPointerException();
	}

	/**
	 * @see org.gojul.fourinaline.model.GamePlayerProvider#releasePlayer(java.lang.String)
	 */
	public void releasePlayer(final String name) throws NullPointerException, RuntimeException
	{
		if (name == null)
			throw new NullPointerException();
	}

	
	
}
