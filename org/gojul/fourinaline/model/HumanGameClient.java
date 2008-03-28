/*
 * HumanGameClient.java
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Observable;
import java.util.Observer;

import org.gojul.fourinaline.model.GameModel.GameModelException;
import org.gojul.fourinaline.model.GameModel.GameStatus;
import org.gojul.fourinaline.model.GameServer.PlayerRegisterException;
import org.gojul.fourinaline.model.GameServer.ServerTicketException;

/**
 * The <code>HumanGameClient</code> class represents a human player
 * game client.<br/>
 * This game client may be shipped with a GUI or a simple user interface,
 * depending on the needs.
 * 
 * @author Julien Aubin
 */
public final class HumanGameClient extends GameClient
{
	
	/**
	 * The serial version UID.
	 */
	final static long serialVersionUID = 1;
	
	/**
	 * The game model.
	 */
	private GameModel currentGameModel;
	
	/**
	 * Constructor.
	 * @param gameServer the game server.
	 * @param playerName the player name.
	 * @throws NullPointerException if any method parameter is null.
	 * @throws PlayerRegisterException if there's an error while registering
	 * the player which has for name <code>playerName</code>.
	 * @throws RemoteException if a remote error occurs while registering the game.
	 * @throws ServerTicketException if no more server ticket is available.
	 */	
	public HumanGameClient(final GameServer gameServer, final String playerName)
		throws NullPointerException, PlayerRegisterException, RemoteException, ServerTicketException
	{
		super(gameServer, playerName);
		currentGameModel = null;
	}
	
	/**
	 * Returns the game model used.
	 * @return the game model used.
	 */
	public synchronized GameModel getGameModel()
	{
		return currentGameModel;
	}

	/**
	 * @see java.lang.Runnable#run()
	 */
	public void run()
	{
		// The thread stops if the server is no longer running,
		// or at the first exception encountered.
		while (isConnectedToServer())
		{
			
			try
			{					
				GameModel gameModel = getServer().getGame(getPlayer().getPlayerMark(), getTicket());
				
				boolean isGameRunning = true;
				
				synchronized(this)
				{					
					// We only notify the observers in case of an update of the game model
					if ((gameModel != null && !gameModel.equals(currentGameModel))
							|| (currentGameModel != null && gameModel == null))
					{
						setChanged();
					}
					
					currentGameModel = gameModel;
					
					// We evaluate the if() condition in a thread safe environment
					// in order to avoid bad stuff with multithread.
					isGameRunning = getServer().isGameRunning() && currentGameModel != null; 
				}
				
				notifyObservers();
				
				// In case the game is running, the process self-blocks until the next turn
				// at line getServer().getGame()
				// Otherwise we need to sleep a bit in order to avoid a high CPU consumption
				if (!isGameRunning)
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

		setChanged();		
		notifyObservers();
	}
	
	/**
	 * Plays at column <code>columnIndex</code>.
	 * @param columnIndex the column index where we want to play.
	 * @throws GameModelException if the play is not valid.
	 * @throws RemoteException if an external error occurs.
	 */
	public synchronized void play(final int columnIndex) throws GameModelException, RemoteException
	{		
		if (currentGameModel != null 
			&& currentGameModel.getGameStatus().equals(GameStatus.CONTINUE_STATUS)
			&& currentGameModel.getCurrentPlayer().equals(getPlayer().getPlayerMark()))
		{
			getServer().play(columnIndex, getPlayer().getPlayerMark(), currentGameModel, getTicket());
			
			// We play locally AFTER having notified the server because we must be
			// sync to the server when notifying it !!!
			currentGameModel.play(columnIndex, getPlayer().getPlayerMark());
			
			setChanged();
			notifyObservers();
		}
	}
	
	/**
	 * The <code>SimpleObserver</code> class is a simple command-line
	 * game observer for debug purposes.
	 * 
	 * @author Julien Aubin
	 */
	private final static class SimpleObserver implements Observer
	{
		
		/**
		 * The input reader.
		 */
		private BufferedReader inputReader;
		
		/**
		 * Constructor.
		 * @throws IOException if an I/O error occurs while initializing
		 * the observer.
		 */
		public SimpleObserver() throws IOException
		{
			inputReader = new BufferedReader(new InputStreamReader(System.in));
		}

		/**
		 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
		 */
		public void update(final Observable o, final Object arg)
		{
			if (o != null && o instanceof HumanGameClient)
			{				
				HumanGameClient client = (HumanGameClient) o;
				
				System.out.println("Current status : ");
												
				System.out.println(client.getGameModel());
				
				try
				{
					if (client.getServer().isGameRunning() && client.getGameModel() != null)
					{
						System.out.println("?");
				
						boolean isPlaySuccessful = false;
				
						while (!isPlaySuccessful)
						{
							try
							{
								int colIndex = Integer.parseInt(inputReader.readLine());					
								client.play(colIndex);
								
								isPlaySuccessful = true;
							}
							catch (Throwable t)
							{
								System.err.println(t.getMessage());
							}
						}
					}
					else
						System.out.println("Game halted.");
				}
				catch (Throwable t)
				{
					t.printStackTrace();
				}
			}
		}
		
	}

	public static void main(String[] args) throws Throwable
	{
		if (GameServerImpl.startDaemon(false))
		{
			Registry registry = LocateRegistry.getRegistry("127.0.0.1");
			
			GameServer gameServer = (GameServer) registry.lookup(GameServer.STUB_NAME);
			
			HumanGameClient playerClient = new HumanGameClient(gameServer, "Julek"); 
			playerClient.addObserver(new SimpleObserver());
			new Thread(playerClient).start();
			new Thread(new AIGameClient(gameServer, "bougo", new DefaultEvalScore(), 4)).start();
			
			gameServer.newGame(playerClient.getTicket());
		}
	}
}
