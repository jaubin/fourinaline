/*
 * GameServerImpl.java
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Observable;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import javax.swing.Timer;

import org.gojul.fourinaline.model.GameModel.GameModelException;
import org.gojul.fourinaline.model.GameModel.GameStatus;
import org.gojul.fourinaline.model.GameModel.PlayerMark;

/**
 * The <code>GameServerImpl</code> class is a simple implementation
 * of the game server.<br/>
 * This implementation is completely synchronous, i.e. it does not need
 * to use the callback pattern. Each client gets the game when it's up
 * to them to play.<br/>
 * <br/>
 * This server extends the <code>Observable</code> class in order
 * 
 * @author Julien Aubin
 */
public final class GameServerImpl extends Observable implements GameServer, ActionListener
{
	
	/**
	 * The game model used.
	 */
	private GameModel gameModel;
	
	/**
	 * The map, that ties a player name to a player.
	 */
	private Map<String, GamePlayer> players;
	
	/**
	 * The name of the game owner.
	 */
	private String gameOwnerPlayerName;
	
	/**
	 * The map, that ties a player mark to a semaphore.
	 */
	private Map<PlayerMark, Semaphore> playerMarkSemaphores;
	
	/**
	 * The set of unused server tickets.
	 */
	private Set<ServerTicket> unusedTickets;
	
	/**
	 * The map that ties a server ticket to a player name.
	 */
	private Map<ServerTicket, String> usedTickets;
	
	/**
	 * The set of used player marks.<br/>
	 * The first mark found as unused is assigned to the next player.
	 */
	private Set<PlayerMark> usedPlayerMarks;
	
	/**
	 * Boolean that indicates whether the score is updated for
	 * the current game or not.
	 */
	private boolean isScoreUpdated;
	
	/**
	 * Boolean indicating whether we're in debug mode or not.
	 */
	private boolean debugMode;
	
	/**
	 * The server name, in case the server is running on a multi-server instance.
	 */
	private String serverName;
	
	/**
	 * The game player provider used to provide game players from the server.
	 */
	private GamePlayerProvider gamePlayerProvider;
	
	/**
	 * The timer that notifies the global server repository that the game
	 * must be ended. It is reset every time a player plays.<br/>
	 * It has a 30 minute delay.
	 */
	private Timer timeoutTimer;
	
	/**
	 * Constructor.
	 */
	public GameServerImpl()
	{
		this(null, false, new DefaultGamePlayerProvider());
	}
	
	/**
	 * Constructor.
	 * @param name the server name. This parameter may be null if the server
	 * is running as a single game server, i.e. not belonging to a global game
	 * server.
	 * @param playerProvider the game player provider used to deliver players to the
	 * server.
	 * @throws NullPointerException if <code>playerProvider</code> is null.
	 */
	public GameServerImpl(final String name, final GamePlayerProvider playerProvider)
		throws NullPointerException
	{
		this(name, false, playerProvider);
	}
	
	/**
	 * Constructor.
	 * @param name the server name. This parameter may be null if the server
	 * is running as a single game server, i.e. not belonging to a global game
	 * server.
	 * @param debug true if the server must be started in debug mode,
	 * false elsewhere.
	 * @param playerProvider the game player provider used to store the game.
	 * @throws NullPointerException if <code>playerProvider</code> is null.
	 */
	private GameServerImpl(final String name, final boolean debug, final GamePlayerProvider playerProvider)
		throws NullPointerException
	{
		if (playerProvider == null)
			throw new NullPointerException();
		
		debugMode = debug;
		gamePlayerProvider = playerProvider;
		serverName = name;
		
		// The timer has a 30 minute delay.
		timeoutTimer = new Timer(30 * 60 * 1000, this);
		
		players = new LinkedHashMap<String, GamePlayer>();
		// We use a map there that is thread safe.
		playerMarkSemaphores = new ConcurrentHashMap<PlayerMark, Semaphore>();
		
		unusedTickets = new HashSet<ServerTicket>();
		usedTickets = new HashMap<ServerTicket, String>();
		usedPlayerMarks = new HashSet<PlayerMark>();
		gameOwnerPlayerName = null;
		
		Iterator<PlayerMark> it = PlayerMark.getPlayerIterator();
		
		while (it.hasNext())
		{
			PlayerMark playerMark = it.next();			
			unusedTickets.add(new ServerTicket());
			playerMarkSemaphores.put(playerMark, new Semaphore(0));
		}
	}
	
	/**
	 * In case this server is used in global mode, informs the
	 * server repository that this server instance must be deleted
	 * as it is no longer used.
	 */
	private void releaseServer()
	{
		// Here only the player who are not disconnected
		// are released from the game player provider.
		// The other ones have already been released...
		for (String playerName: players.keySet())
			gamePlayerProvider.releasePlayer(playerName);
		
		setChanged();
		notifyObservers(serverName);
	}
	
	/**
	 * Releases all the currently blocked processes.
	 */
	private synchronized void releaseSemaphores()
	{		
		for (Semaphore s: playerMarkSemaphores.values())
		{
			// Here it is safe to release all the semaphores
			// with a huge number of permits since this method
			// is called at the end of a game.
			// The semaphores are then reset on the next game.
			// This avoids interblocking processes with the getGame()
			// method in case the game end just occurs between a client
			// has tested that the game is running and then sleeps.
			// See the getGame() method for further information.
			s.release(150);
		}
	}
	
	/**
	 * @see org.gojul.fourinaline.model.GameServer#endGame(org.gojul.fourinaline.model.GameServer.ServerTicket)
	 */
	public synchronized void endGame(final ServerTicket serverTicket) throws NullPointerException, ServerTicketException, RemoteException
	{
		checkTicket(serverTicket);
		
		gameModel = null;
		
		releaseSemaphores();
	}

	/**
	 * @see org.gojul.fourinaline.model.GameServer#getTicket()
	 */
	public synchronized ServerTicket getTicket() throws ServerTicketException, RemoteException
	{		
		if (unusedTickets.isEmpty())
			throw new ServerTicketException("No more ticket available");
		
		ServerTicket ticket = unusedTickets.iterator().next();
		
		unusedTickets.remove(ticket);
		usedTickets.put(ticket, null);
		
		return ticket;
	}

	/**
	 * @see org.gojul.fourinaline.model.GameServer#releaseTicket(org.gojul.fourinaline.model.GameServer.ServerTicket)
	 */
	public synchronized void releaseTicket(final ServerTicket serverTicket) throws ServerTicketException, RemoteException, NullPointerException
	{
		checkTicket(serverTicket);	
		
		String playerName = usedTickets.remove(serverTicket);
		
		if (playerName != null)
			unregisterPlayer(playerName);
		
		unusedTickets.add(serverTicket);
		
		// Notifies the global server that the game must be ended.
		// This is the case if there's no more player or if the game owner has left.
		if (usedTickets.isEmpty() || playerName != null && playerName.equals(gameOwnerPlayerName))
			releaseServer();
	}

	/**
	 * Checks that the ticket <code>serverTicket</code> is valid, and if so resets
	 * the time out timer.
	 * @param serverTicket the server ticket to test.
	 * @throws NullPointerException if <code>serverTicket</code> is null.
	 * @throws ServerTicketException if <code>serverTicket</code> is not valid.
	 */
	private synchronized void checkTicket(final ServerTicket serverTicket) throws NullPointerException, ServerTicketException
	{		
		if (serverTicket == null)
			throw new NullPointerException();
		
		if (! usedTickets.containsKey(serverTicket))
			throw new ServerTicketException("Invalid server ticket");
		
		timeoutTimer.restart();
	}

	
	/**
	 * @see org.gojul.fourinaline.model.GameServer#getGame(org.gojul.fourinaline.model.GameModel.PlayerMark, org.gojul.fourinaline.model.GameServer.ServerTicket)
	 */
	public GameModel getGame(final PlayerMark playerMark, final ServerTicket serverTicket) throws NullPointerException, RuntimeException, RemoteException, ServerTicketException
	{
		checkTicket(serverTicket);
		
		// We want to avoid the risk of acquiring a bad semaphore reference so
		// we get it in a synchronized way.
		Semaphore s = null;
		
		// In order to avoid interblocking processes,
		// we synchronize only critical sections here.
		synchronized(this)		
		{
			if (!isGameRunning())
			{
				if (gameModel != null)
				{
					if (debugMode)
					{
						System.err.println("Not running.");
						System.err.println(gameModel);
						System.err.println("---");
					}
					
					return new GameModel(gameModel);
				}
				else
					return null;
			}
			else
				s = playerMarkSemaphores.get(playerMark);
		}

		// The code here ensures that every client has only the game
		// when it's up to them to play.
		// In case the thread commutation happens between the if() clause
		// and a end game occurs, the semaphores are all release with a huge
		// number of permits in order to avoid interblocking processes
		// at time a game is ended. This is safe since semaphores are reset
		// at each game. The semaphore reference is ensured since it's gotten
		// in a synchronized way and that the acquired semaphore is a semaphore.
		// from the current game, not the next one.
		// This method is not that clean but there's no other possible way
		// to deal with the issue.
		try
		{
			s.acquire();
		}
		catch (InterruptedException e)
		{
			throw new RuntimeException(e);
		}
			
		// In order to avoid interblocking processes,
		// we synchronize only critical sections here.
		synchronized(this)
		{
			// The game model may have become null if a user called the endGame()
			// method.
			if (gameModel != null)
			{
				if (debugMode)
				{
					System.err.println("Running. Player : " + gameModel.getCurrentPlayer());
					System.err.println(gameModel);
					System.err.println("----");
				}
					
				return new GameModel(gameModel);
			}
			else
				return null;
		}
	}
	
	
	/**
	 * @see org.gojul.fourinaline.model.GameServer#play(int, org.gojul.fourinaline.model.GameModel.PlayerMark, org.gojul.fourinaline.model.GameModel, org.gojul.fourinaline.model.GameServer.ServerTicket)
	 */
	public synchronized void play(final int colIndex, final PlayerMark playerMark, final GameModel clientGameModel, final ServerTicket serverTicket) throws NullPointerException, RemoteException, ServerTicketException, GameModelException
	{
		// Here we must synchronize the play() method because it has no risk
		// of interblocking threads but on the contrary it may release too
		// many semaphore permits.
		
		checkTicket(serverTicket);
		
		if (playerMark == null)
			throw new NullPointerException();
		
		// By default, we consider the game is not running.
		boolean isGameRunning = false;
		

		// In some weird case, the client game model may not be equal
		// to the current game model, especially when the previous game
		// has been stopped by a client.
		if (gameModel != null && gameModel.equals(clientGameModel))
		{
			gameModel.play(colIndex, playerMark);
			isGameRunning = isGameRunning();
		}
		else
			return;
		
		// In case the game is still running, we release
		// the next player.
		if (isGameRunning)
		{
			playerMarkSemaphores.get(PlayerMark.getNextMark(playerMark)).release();
		}
		else
		{
			// No risk here, since there's only one thread running at this stage.
			if (!isScoreUpdated)
			{
				isScoreUpdated = true;
				Set<GamePlayer> gamePlayers = new HashSet<GamePlayer>(players.values());
				GamePlayer winner = null;
				
				// Increments by one the score of the latest player if
				// he's won.
				if (gameModel != null && gameModel.getGameStatus() == GameStatus.WON_STATUS)
				{
					Iterator<GamePlayer> it = gamePlayers.iterator();
						
					boolean winnerFound = false;
					
					while (it.hasNext() && !winnerFound)
					{
						GamePlayer player = it.next();
						
						if (player.getPlayerMark().equals(playerMark))
						{
							player.incrementScore();
							winnerFound = true;
							winner = player;
						}
					}
				}
				
				gamePlayerProvider.storeGame(winner, gamePlayers);
			}
			
			releaseSemaphores();
			
		}
		
	}

	/**
	 * @see org.gojul.fourinaline.model.GameServer#registerPlayer(String, org.gojul.fourinaline.model.GameServer.ServerTicket)
	 */
	public synchronized PlayerDescriptor registerPlayer(final String playerName, final ServerTicket serverTicket) throws NullPointerException,
		PlayerRegisterException, RemoteException, ServerTicketException, RuntimeException
	{
		checkTicket(serverTicket);
		
		if (playerName == null)
			throw new NullPointerException();
		
		if (isGameRunning())
			throw new RuntimeException("There's already a running game.");
		
		String name = usedTickets.get(serverTicket);
		
		if (name != null)
			throw new PlayerRegisterException("The ticket with which you attempt to register a player has already been used.");
		
		GamePlayer result = players.get(playerName);
		
		if (result == null)
		{
			// Looks for the next free mark and assigns it to the player.
			PlayerMark mark = null;
			
			Iterator<PlayerMark> it = PlayerMark.getPlayerIterator();
			
			while (it.hasNext() && mark == null)
			{
				PlayerMark markTest = it.next();
				
				if (!usedPlayerMarks.contains(markTest))
				{
					mark = markTest;
					usedPlayerMarks.add(markTest);
				}
			}
			
			if (mark == null)
				throw new RuntimeException("Unable to add another player.");
			
			result = gamePlayerProvider.getGamePlayer(playerName, mark);
			
			players.put(playerName, result);
		}
		else
			throw new PlayerRegisterException("There's already a player with name " + playerName);
		
		usedTickets.put(serverTicket, playerName);
		
		// Assigns the game owner.
		boolean isGameOwner = gameOwnerPlayerName == null;
		if (isGameOwner)
			gameOwnerPlayerName = playerName;
		
		return new PlayerDescriptor(new UnmodifiableGamePlayer(result), isGameOwner);
	}
	
	/**
	 * Unregisters from the server the player which has for name
	 * <code>playerName</code>, and ends the current game if any.
	 * @param playerName the player name.
	 * @throws NullPointerException if any of the method parameter is null.
	 */
	private synchronized void unregisterPlayer(final String playerName) throws NullPointerException
	{		
		if (playerName == null)
			throw new NullPointerException();
		
		GamePlayer p = players.remove(playerName);
		usedPlayerMarks.remove(p.getPlayerMark());
		gamePlayerProvider.releasePlayer(playerName);
	}

	/**
	 * @see org.gojul.fourinaline.model.GameServer#getGameImmediately()
	 */
	public synchronized GameModel getGameImmediately() throws RemoteException
	{
		if (gameModel == null)
			return null;
		else
			return new GameModel(gameModel);
	}

	/**
	 * @see org.gojul.fourinaline.model.GameServer#getPlayers()
	 */
	public synchronized Set<GamePlayer> getPlayers() throws RemoteException
	{			
		Set<GamePlayer> result = new LinkedHashSet<GamePlayer>();
		
		for (GamePlayer player: players.values())
			result.add(new UnmodifiableGamePlayer(player));
		
		return result;
	}

	/**
	 * @see org.gojul.fourinaline.model.GameServer#isGameRunning()
	 */
	public synchronized boolean isGameRunning() throws RemoteException
	{	
		return gameModel != null && gameModel.getGameStatus().equals(GameStatus.CONTINUE_STATUS);
	}

	
	/**
	 * @see org.gojul.fourinaline.model.GameServer#newGame(org.gojul.fourinaline.model.GameServer.ServerTicket)
	 */
	public synchronized void newGame(final ServerTicket serverTicket) throws NullPointerException, RuntimeException, RemoteException, ServerTicketException
	{
		checkTicket(serverTicket);
		
		if (usedPlayerMarks.size() < PlayerMark.getNumberOfPlayerMarks())
			throw new RuntimeException("Not all the players have been registered !");
		
		if (isGameRunning())
			throw new RuntimeException("The game is already running !");
		
		gameModel = new GameModel();
		
		// The semaphores are reset for each game.
		playerMarkSemaphores.put(PlayerMark.PLAYER_A_MARK, new Semaphore(0));
		playerMarkSemaphores.put(PlayerMark.PLAYER_B_MARK, new Semaphore(0));
		
		playerMarkSemaphores.get(gameModel.getCurrentPlayer()).release();
		isScoreUpdated = false;

		if (debugMode)
			System.out.println("Game running !");
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		if (gameModel != null)
			return gameModel.toString();
		else
			return "No game running";
	}
	
	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(final ActionEvent e)
	{
		releaseServer();		
	}

	/**
	 * The game server instance is kept locally, in order to
	 * avoid system GCs at RMI startup.<br/>
	 * See <A href="http://forum.java.sun.com/thread.jspa?threadID=5155806&messageID=9589670">there</A>
	 * for further information.
	 */
	private static GameServer serverInstance = null;
	
	/**
	 * Starts the server daemon.
	 * @param debugMode true if we're in debug mode, false elsewhere.
	 * @return the server daemon.
	 */
	public final static boolean startDaemon(final boolean debugMode)
	{
		if (System.getSecurityManager() == null) 
		{
			System.setSecurityManager(new SecurityManager());
		}
		try 
		{
			serverInstance = new GameServerImpl("local", debugMode, new DefaultGamePlayerProvider());
            
			GameServer stub = (GameServer) UnicastRemoteObject.exportObject(serverInstance, 0);
            
			Registry registry = LocateRegistry.createRegistry(1099);
			registry.rebind(STUB_NAME, stub);
            
			System.out.println("Game daemon started !");
            
			return true;
		}
		catch (Throwable t)
		{
			System.err.println("Error while starting daemon : ");
        	
			t.printStackTrace();
        	
			return false;
		}
	}

	/**
	 * Starts the game server daemon.
	 * @return true if the game server daemon is started, false elsewhere.
	 */
	public final static boolean startDaemon()
	{
		return startDaemon(false);
	}
	
	public static void main(String[] args) throws Throwable
	{
		if (startDaemon(true))
		{
			Registry registry = LocateRegistry.getRegistry("127.0.0.1");
			
			GameServer gameServer = (GameServer) registry.lookup(STUB_NAME);
			
			GameClient firstClient = new AIGameClient(gameServer, gameServer.getTicket(), "bougo", new DefaultEvalScore(), 4); 
			
			new Thread(firstClient).start();
			new Thread(new AIGameClient(gameServer, gameServer.getTicket(), "bougoéland", new DefaultEvalScore(), 4)).start();
			
			gameServer.newGame(firstClient.getTicket());
			
			while (gameServer.isGameRunning());
		}		
	}
}
