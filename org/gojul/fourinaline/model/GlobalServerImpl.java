/*
 * GlobalServerImpl.java
 *
 * Created: 12 mai 08
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

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.TreeMap;

/**
 * The <code>GlobalServerImpl</code> is a simple implementation of the
 * global server.<br/>
 * It observes all the instances of <code>GameServer</code> and cleans
 * up game server instances when it is notified to do so.<br/>
 * <br/>
 * This class can't be instanciated directly for security reasons.
 *
 * @author Julien Aubin
 */
public final class GlobalServerImpl implements GlobalServer, Observer
{
	/**
	 * The serial version UID of this game.
	 */
	final static long serialVersionUID = 1L;
	
	/**
	 * The RMI registry instance.
	 */
	private Registry registry;
	
	/**
	 * The map that ties a server name to a server instance.<br/>
	 * The server instances must not be directly put to the RMI registry
	 * otherwise they might be trashed by the garbage collector. A reference
	 * to them must be put somewhere else.
	 */
	private Map<String, GameServer> serverMap;
	
	/**
	 * Constructor.
	 * @param reg the RMI registry the server takes into account.
	 * @throws NullPointerException if <code>reg</code> is null.
	 */
	private GlobalServerImpl(final Registry reg) throws NullPointerException
	{
		if (reg == null)
			throw new NullPointerException();
		
		serverMap = new TreeMap<String, GameServer>();		
		registry = reg;
	}

	/**
	 * @see org.gojul.fourinaline.model.GlobalServer#createGame(java.lang.String)
	 */
	public synchronized void createGame(final String name) throws NullPointerException,
			RemoteException, AlreadyBoundException
	{
		if (name == null)
			throw new NullPointerException();
		
		if (serverMap.containsKey(name))
			throw new AlreadyBoundException();
		
		try
		{
			GameServerImpl gameServer = new GameServerImpl(name);
			gameServer.addObserver(this);	
			
			GameServer stub = (GameServer) UnicastRemoteObject.exportObject(gameServer, 0);
			registry.bind(SINGLE_GAME_SERVER_STUB_PREFIX + name, stub);
		}
		catch (RemoteException e)
		{
			serverMap.remove(name);
			throw e;
		}
		catch (AlreadyBoundException e)
		{
			serverMap.remove(name);
			throw e;
		}
		
	}
	
	/**
	 * @see org.gojul.fourinaline.model.GlobalServer#getGames()
	 */
	public synchronized Set<String> getGames() throws RemoteException
	{
		return Collections.unmodifiableSet(serverMap.keySet());
	}

	/**
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public synchronized void update(final Observable o, final Object arg)
	{
		if (o != null && arg != null && o instanceof GameServer && arg instanceof String)
		{
			if (serverMap.containsKey(arg))
			{
				serverMap.remove(arg);
			
				try
				{
					registry.unbind(SINGLE_GAME_SERVER_STUB_PREFIX + arg);
				}
				catch (Throwable t)
				{
					t.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * The current server instance. Prevents the server instance
	 * to be trashed by the GC.<br/>
	 * See <A href="http://forum.java.sun.com/thread.jspa?threadID=5155806&messageID=9589670">there</A>
	 * for further information.
	 */
	private static GlobalServer serverInstance = null;
	
	public static void main(String[] args) throws Throwable
	{
		if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        
		Registry registry = LocateRegistry.createRegistry(1099);
		
		serverInstance = new GlobalServerImpl(registry);
		
		GlobalServer stub = (GlobalServer) UnicastRemoteObject.exportObject(serverInstance, 0);
		registry.rebind(STUB_NAME, stub);
		
		System.out.println("Game daemon started");
	}
}
