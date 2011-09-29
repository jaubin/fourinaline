/*
 * GameClient.java
 *
 * Created: 2008/03/23
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

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Set;

import org.gojul.fourinaline.model.GameServer.PlayerDescriptor;
import org.gojul.fourinaline.model.GameServer.PlayerRegisterException;
import org.gojul.fourinaline.model.GameServer.ServerTicket;
import org.gojul.fourinaline.model.GameServer.ServerTicketException;


/**
 * The <code>GameClient</code> interface is the interface of the
 * client side of the game.<br/>
 * A part of the implementor should run in a thread, waiting for
 * its turn before playing.<br/>
 * The AI game clients must implement the <code>ComputerGameClient</code>
 * class instead of this one.
 * 
 * @see org.gojul.fourinaline.model.GameClient.ComputerGameClient
 * 
 * @author Julien Aubin
 */
public abstract class GameClient extends Observable implements Runnable
{
	
	/**
	 * The serial version UID.
	 */
	final static long serialVersionUID = 1;
	
	/**
	 * The <code>GameServerCaller</code> class calls every two seconds the server
	 * in order to ensure it is running. If this is no longer the case,
	 * notifies the client to stop its thread.
	 * 
	 * @author Julien Aubin
	 */
	private final static class GameServerCaller implements Runnable
	{		
		/**
		 * The call period in milliseconds.
		 */
		private final static int CALL_PERIOD = 10000;
		
		/**
		 * The game client to update.
		 */
		private GameClient gameClient;
		
		/**
		 * The game server to call.
		 */
		private GameServer gameServer;
		
		/**
		 * Constructor.
		 * @param client the client.
		 * @param server the server.
		 */
		public GameServerCaller(final GameClient client, final GameServer server)
		{
			gameClient = client;
			gameServer = server;
		}

		/**
		 * @see java.lang.Runnable#run()
		 */
		public void run()
		{
			try
			{
				while (true)
				{
					gameServer.isGameRunning();
					
					try
					{
						Thread.sleep(CALL_PERIOD);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
			}
			catch (Throwable t)
			{
				gameClient.setConnectedToServer(false);
			}
			
		}
		
		
	}
	
	/**
	 * The server status update period in milliseconds, in case a game is not running.<br/>
	 * This avoid an infinite loop which would be very consuming.
	 */
	protected final static int SERVER_STATUS_UPDATE_PERIOD = 100;

	/**
	 * The game server used.
	 */
	private GameServer gameServer;
	
	/**
	 * The server ticket used.
	 */
	private ServerTicket serverTicket;
	
	/**
	 * The game player used.
	 */
	private GamePlayer gamePlayer;
	
	/**
	 * Boolean indicating whether we're connected to the server or not.
	 */
	private boolean connectedToServer;
	
	/**
	 * Boolean indicating whether the client is the game owner or not.
	 */
	private boolean isGameOwner;
	
	/**
	 * Constructor.
	 * @param server the server.
	 * @param ticket the server ticket.
	 * @param playerName the player name.
	 * @throws NullPointerException if any of the method parameter is null.
	 * @throws RemoteException if a remote error occurs while registering
	 * the player.
	 * @throws ServerTicketException if an error occurs while initializing the client.
	 * @throws PlayerRegisterException if an error occurs while registering the
	 * player represented here.
	 */
	public GameClient(final GameServer server, final ServerTicket ticket, final String playerName)
		throws NullPointerException, ServerTicketException, PlayerRegisterException, RemoteException
	{
		if (server == null || playerName == null || ticket == null)
			throw new NullPointerException();
		
		gameServer = server;
		
		// No risk here on the serverRunning variable, since
		// it is inizialized before any thread can be running.
		connectedToServer = true;
		
		serverTicket = ticket;

		PlayerDescriptor descriptor = gameServer.registerPlayer(playerName, serverTicket); 
		gamePlayer = descriptor.getGamePlayer();
		isGameOwner = descriptor.isGameOwner();
		
		// Here the fact that we launch the thread in the class constructor
		// is not a bug. The thread acts as a daemon that is supposed to
		// stop the client thread if the server is halted.
		new Thread(new GameServerCaller(this, gameServer)).start();
	}
	
	/**
	 * Hook for subclassers in order to add specific handlings
	 * when the game is started.
	 * @throws Throwable if any error occurs in the subclasser method.
	 */
	protected void doNewGame() throws Throwable
	{
		
	}
	
	/**
	 * Launches a new game.
	 * @return true if the new game has been launched successfully,
	 * false elsewhere.
	 * @throws RemoteException if a RMI error occurs while sending
	 * the command to the server.
	 */
	public final boolean newGame() throws RemoteException
	{
		if (!gameServer.isGameRunning())
		{
			try
			{
				gameServer.newGame(serverTicket);
				doNewGame();
				return true;
			}
			catch (RemoteException e)
			{
				throw e;
			}
			catch (Throwable t)
			{
				t.printStackTrace();
			}
		}
		return false;
	}
	
	/**
	 * Hook for subclassers in order to add specific handlings
	 * when the game is ended.
	 * @throws Throwable if any error occurs in the subclasser method.
	 */
	protected void doEndGame() throws Throwable
	{
		
	}
	
	/**
	 * Ends the current game.
	 * @return true if the game has been ended successfully, false elsewhere.
	 * @throws RemoteException if a RMI error occurs while sending
	 * the command to the server.
	 */
	public final boolean endGame() throws RemoteException
	{
		if (gameServer.isGameRunning())
		{
			try
			{
				gameServer.endGame(serverTicket);
				doEndGame();
				return true;
			}
			catch (RemoteException e)
			{
				throw e;
			}
			catch (Throwable t)
			{
				t.printStackTrace();
			}
		}
		return false;
	}
	
	/**
	 * Returns the game model. This method must only be used for
	 * display purposes.
	 * @return the game model.
	 * @throws RemoteException if a remote error occurs while returning
	 * the game.
	 */
	public final GameModel getGameModelImmediately() throws RemoteException
	{
		return gameServer.getGameImmediately();
	}
	
	/**
	 * Returns the set of game players currently connected to the server.
	 * @return the set of game players currently connected to the server.
	 * @throws RemoteException if a RMI error occurs while sending the command
	 * to the server.
	 */
	public final Set<GamePlayer> getPlayers() throws RemoteException
	{
		return gameServer.getPlayers();
	}
	
	/**
	 * Sets the server running status.
	 * @param value the value to set for the server running status.
	 */
	private synchronized void setConnectedToServer(boolean value)
	{
		connectedToServer = value;
	}
	
	/**
	 * Returns true if the client is connected to the server, false elsewhere.<br/>
	 * Subclassers should call this method in order to stop their
	 * thread when needed.
	 * @return true if the server is running, false elsewhere.
	 */
	public final synchronized boolean isConnectedToServer()
	{
		return connectedToServer;
	}
	
	/**
	 * Returns the server ticket.
	 * @return the server ticket.
	 */
	protected final ServerTicket getTicket()
	{
		return serverTicket;
	}
	
	/**
	 * Returns the game server.
	 * @return the game server.
	 */
	protected final GameServer getServer()
	{
		return gameServer;
	}
	
	/**
	 * Returns the player.
	 * @return the player.
	 */
	public final GamePlayer getPlayer()
	{
		return gamePlayer;
	}
	
	/**
	 * Returns true if the current client is the game owner, i.e.
	 * it is the first client to have registered to the server, false
	 * elsewhere.
	 * @return true if the current client is the game owner, i.e.
	 * it is the first client to have registered to the server, false
	 * elsewhere.
	 */
	public final boolean isGameOwner()
	{
		return isGameOwner;
	}
	
	/**
	 * Disconnects from the server.<br/>
	 * This method must be called when the game client
	 * stops running.
	 * @throws RuntimeException if an error occurs while
	 * disconnecting from the server.
	 */
	public final void disconnect() throws RuntimeException
	{
		try
		{
			if (isConnectedToServer())
			{
				// Notifies of a disconnection of the server.				
				setConnectedToServer(false);
				
				gameServer.endGame(serverTicket);
				gameServer.releaseTicket(serverTicket);
			}
		}
		catch (Throwable t)
		{
			throw new RuntimeException(t);
		}
	}

	/**
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable
	{
		// TODO Raccord de méthode auto-généré
		super.finalize();
		
		disconnect();
	}
	
	/**
	 * The <code>ComputerGameClient</code> class is the class which all the
	 * computer players should inherit.<br/>
	 * It implements a mechanism that makes all the computer players disconnect
	 * from the server properly.
	 *
	 * @author Julien Aubin
	 */
	public static abstract class ComputerGameClient extends GameClient
	{		
		/**
		 * The list of instances.
		 */
		private final static List<ComputerGameClient> instanceList = new ArrayList<ComputerGameClient>();
		
		/**
		 * Constructor.
		 * @param server the server.
		 * @param ticket the server ticket.
		 * @param playerName the player name.
		 * @throws NullPointerException if any of the method parameter is null.
		 * @throws RemoteException if a remote error occurs while registering
		 * the player.
		 * @throws ServerTicketException if an error occurs while initializing the client.
		 * @throws PlayerRegisterException if an error occurs while registering the
		 * player represented here.
		 */
		public ComputerGameClient(final GameServer server, final ServerTicket ticket, final String playerName)
			throws NullPointerException, ServerTicketException, PlayerRegisterException, RemoteException
		{
			super(server, ticket, playerName);
			instanceList.add(this);
		}
		
		/**
		 * Disconnects all the local computer clients from the server.<br/>
		 * This method must be called by all the user client when quitting the
		 * game.<br/>
		 * Note that this disconnects only the clients that have been created locally,
		 * not the other ones.
		 */
		public final static void disconnectLocalComputerClients()
		{
			for (ComputerGameClient client: instanceList)
			{
				if (client != null)
				{
					try
					{
						client.disconnect();
					}
					catch (Throwable t)
					{
						t.printStackTrace();
					}
				}
			}
		}
	}
}
