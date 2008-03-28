/*
 * GameServer.java
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
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.SecureRandom;
import java.util.Random;
import java.util.Set;

import org.gojul.fourinaline.model.GameModel.GameModelException;
import org.gojul.fourinaline.model.GameModel.PlayerMark;

/**
 * The <code>GameServer</code> interface is the server
 * part of the four in a line game.<br/>
 * It has been designed to be used with RMI, and the clients
 * use it as an RMI server. However it is very easy to adapt the
 * main programs and GUI to that the server is no longer used as an RMI
 * server but as a local server instead.
 * 
 * @author Julien Aubin
 */
public interface GameServer extends Remote, Serializable
{
	
	/**
	 * The server stub name.
	 */
	public final static String STUB_NAME = "FourInALine";
	
	/**
	 * The server ticket.<br/>
	 * This is a small security mechanism that prevents clients
	 * from hacking a current game.<br/>
	 * This class just extends the object class, without anything
	 * further.
	 * 
	 * @author Julien Aubin
	 */
	public final static class ServerTicket implements Serializable
	{
		/**
		 * The ticket random value distributor.
		 */
		private final static Random ticketRandom = new SecureRandom();
		
		/**
		 * The serial version UID.
		 */
		static final long serialVersionUID = 1;
		
		/**
		 * The ticket value.
		 */
		private long ticketValue;
		
		/**
		 * Constructor.<br/>
		 * The ticket value is generated randomly in order to avoid the probability
		 * of having two equal tickets.
		 */
		protected ServerTicket()
		{
			ticketValue = ticketRandom.nextLong();
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(final Object obj)
		{
			if (obj != null && obj instanceof ServerTicket)
				return ((ServerTicket) obj).ticketValue == ticketValue;
			else
				return false;
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode()
		{			
			return (int) ticketValue;
		}
		
	}
	
	/**
	 * Exception thrown when an invalid ticket is used in order to send orders
	 * to the server.
	 * 
	 * @author Julien Aubin
	 */
	public final static class ServerTicketException extends RuntimeException
	{
		/**
		 * The serial version UID.
		 */
		final static long serialVersionUID = 1;
		
		/**
		 * Constructor.
		 * @param message the message to display.
		 */
		protected ServerTicketException(final String message)
		{
			super(message);
		}
	}
	
	/**
	 * Exception thrown when there are error dealing with player registering
	 * and unregistering.
	 * 
	 * @author Julien Aubin
	 */
	public final static class PlayerRegisterException extends Exception
	{
		/**
		 * The serial version UID.
		 */
		final static long serialVersionUID = 1;
		
		/**
		 * Constructor.
		 * @param message the message to display.
		 */
		protected PlayerRegisterException(final String message)
		{
			super(message);
		}
	}
	
	/**
	 * The <code>PlayerDescriptor</code> class describes a game player
	 * and provides some additional information about it.
	 *
	 * @author Julien Aubin
	 */
	public final static class PlayerDescriptor implements Serializable
	{
		/**
		 * The serial version UID.
		 */
		final static long serialVersionUID = 1;
		
		/**
		 * The game player.
		 */
		private GamePlayer gamePlayer;
		
		/**
		 * Boolean set to true if the game player of this descriptor
		 * is the first player who has connected, false elsewhere.
		 */
		private boolean isGameOwner;
		
		/**
		 * Constructor.
		 * @param player the game player.
		 * @param isOwner true if the game player of this descriptor
		 * is the first player who has connected, false elsewhere.
		 * @throws NullPointerException if any of the method parameter is null.
		 */
		protected PlayerDescriptor(final GamePlayer player, final boolean isOwner)
			throws NullPointerException
		{
			if (player == null)
				throw new NullPointerException();
			
			gamePlayer = player;
			isGameOwner = isOwner;
		}

		/**
		 * Returns the game player.
		 * @return the game player.
		 */
		public GamePlayer getGamePlayer()
		{
			return gamePlayer;
		}

		/**
		 * Returns true if the game player of this descriptor
		 * is the first player who has connected, false elsewhere.
		 * @return true if the game player of this descriptor
		 * is the first player who has connected, false elsewhere.
		 */
		public boolean isGameOwner()
		{
			return isGameOwner;
		}
		
		
	}
	
	/**
	 * Returns a server ticket used in order to send orders.
	 * @return a server ticket used in order to send orders.
	 * @throws ServerTicketException if there's no more ticket available.
	 * @throws RemoteException if a RMI error occurs while releasing
	 * the ticket.
	 */
	public ServerTicket getTicket() throws ServerTicketException, RemoteException;
	
	/**
	 * Release a server ticket so that it can be used by other
	 * clients.
	 * @param serverTicket the server ticket to release.
	 * @throws ServerTicketException if <code>serverTicket</code> does not
	 * belong to the list of tickets currently in use.
	 * @throws RemoteException if a RMI error occurs while releasing
	 * the ticket.
	 * @throws NullPointerException if <code>serverTicket</code> is null.
	 */
	public void releaseTicket(final ServerTicket serverTicket) throws ServerTicketException, RemoteException,
		NullPointerException;
	
	/**
	 * Creates a new game.
	 * @param serverTicket the ticket of the calling client.
	 * @throws NullPointerException if any of the method
	 * parameter is null.
	 * @throws ServerTicketException if <code>serverTicket</code>
	 * is not valid.
	 * @throws RuntimeException if there are not enough players
	 * in order to create a new game.
	 * @throws RemoteException if a RMI exception occurs.
	 */
	public void newGame(final ServerTicket serverTicket) throws NullPointerException, ServerTicketException, RuntimeException, RemoteException;
	
	/**
	 * Ends the current game.
	 * @param serverTicket the ticket of the calling client.
	 * @throws NullPointerException if any of the method
	 * parameter is null.
	 * @throws ServerTicketException if <code>serverTicket</code>
	 * is not valid.
	 * @throws RemoteException if a RMI exception occurs.
	 */
	public void endGame(final ServerTicket serverTicket) throws NullPointerException, ServerTicketException, RemoteException;
	
	/**
	 * Returns true if the game is running, false elsewhere.
	 * @return true if the game is running, false elsewhere.
	 * @throws RemoteException if a RMI exception occurs.
	 */
	public boolean isGameRunning() throws RemoteException;
	
	/**
	 * Returns all the game players. The order in which the
	 * players are returned is always the same.
	 * @return all the game players.
	 * @throws RemoteException if a remote exception occurs
	 * while getting the game players.
	 */
	public Set<GamePlayer> getPlayers() throws RemoteException;
	
	/**
	 * Returns the player which has for name <code>playerName</code>,
	 * or creates it if the player does not exist.
	 * @param playerName the player name.
	 * @param serverTicket the ticket of the calling client.
	 * @return the descriptor of the player which has for name <code>playerName</code>,
	 * or creates it if the player does not exist.
	 * @throws NullPointerException if any of the method parameter is null.
	 * @throws ServerTicketException if <code>serverTicket</code>
	 * is not valid.
	 * @throws PlayerRegisterException if there are already too many players,
	 * or if there's already a player with name <code>playerName</code>.
	 * @throws RemoteException if a remote exception occurs while registering
	 * the player which has for name <code>playerName</code>.
	 * @throws RuntimeException if an unexpected error occurs.
	 */
	public PlayerDescriptor registerPlayer(final String playerName, final ServerTicket serverTicket)
		throws NullPointerException, ServerTicketException, PlayerRegisterException, RemoteException,
		RuntimeException;
	
	/**
	 * Unregisters from the server the player which has for name
	 * <code>playerName</code>, and ends the current game if any.
	 * @param playerName the player name.
	 * @param serverTicket the ticket of the
	 * @throws NullPointerException if any of the method parameter is null.
	 * @throws ServerTicketException if <code>serverTicket</code>
	 * is not valid.
	 * @throws PlayerRegisterException if <code>playerName</code> is not the name
	 * of a known player, and there are already too many players.
	 * @throws RemoteException if a remote exception occurs while registering
	 * the player which has for name <code>playerName</code>.
	 */
	public void unregisterPlayer(final String playerName, final ServerTicket serverTicket)
		throws NullPointerException, ServerTicketException, PlayerRegisterException, RemoteException;

	/**
	 * Returns a copy of the current game for display purposes, or null if there's no current game.<br/>
	 * This method must not be used by client in order to wait for their turn ! They
	 * must use the <code>getGame</code> method instead.
	 * @return a copy of the current game for display purposes.
	 * @throws RemoteException if a remote exception occurs while returning
	 * the current game. 
	 */
	public GameModel getGameImmediately() throws ServerTicketException, RemoteException;
	
	/**
	 * Returns a copy of the current game, or null if there's no current game.
	 * This method is synchronous and only
	 * returns the game when it's up to the player which has for mark
	 * <code>playerMark</code> to play.<br/>
	 * The game returned is the game model shared among all the clients
	 * and the server, not a copy of the server internal game model.
	 * @param playerMark the mark of the player which requests the game.
	 * @param serverTicket the ticket of the calling client.
	 * @return the current game, or null if there's no current game.
	 * @throws NullPointerException if any of the method parameter is null.
	 * @throws ServerTicketException if <code>serverTicket</code>
	 * is not valid.
	 * @throws RuntimeException if an unexpected error occurs.
	 * @throws RemoteException if a remote exception occurs while returning
	 * the current game.
	 */
	public GameModel getGame(final PlayerMark playerMark, final ServerTicket serverTicket) throws NullPointerException, ServerTicketException, RuntimeException, RemoteException;
	
	/**
	 * Notifies the game server of play at the column <code>colIndex</code>.
	 * Gives the hand to the next player if the play is successful.
	 * @param colIndex the index of the column that is played.
	 * @param playerMark the mark of the player which requests the game.
	 * @param clientGameModel the game model on the client side. This game model
	 * must be equal to the server game model otherwise the play is ignored.
	 * @param serverTicket the ticket of the calling client.
	 * @throws GameModelException if the attempted play at the
	 * column number <code>colIndex</code> is not valid.
	 * @throws ServerTicketException if <code>serverTicket</code>
	 * is not valid.
	 * @throws NullPointerException if any of the method parameter is null.
	 * @throws RemoteException if a RMI error occurs while updating
	 * the game.
	 */
	public void play(final int colIndex, final PlayerMark playerMark, final GameModel clientGameModel, final ServerTicket serverTicket) throws NullPointerException, RemoteException, ServerTicketException, GameModelException;
}
