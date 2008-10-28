/*
 * GlobalServer.java
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
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

/**
 * The <code>GlobalServer</code> interface is the interface
 * for all the global servers, i.e. the servers that are designed
 * to run on a webserver in order to handle several games.<br/>
 * Note that all the single game servers stored on this registry
 * have a name prefixed with the constant <code>SINGLE_GAME_SERVER_STUB_PREFIX</code>.
 *
 * @author Julien Aubin
 */
public interface GlobalServer extends Remote
{
	/**
	 * The global server stub name.
	 */
	public final static String STUB_NAME = "FourInALineGlobalServer";
	
	/**
	 * The prefix of a single game server.<br/>
	 * When looking up a game server on the rmi registry, a client must
	 * know that the server name is prefixed with this prefix.
	 */
	public final static String SINGLE_GAME_SERVER_STUB_PREFIX = "FourInALine_Server_";

	/**
	 * Creates the game with name <code>name</code>.
	 * @param name the name of the game to create.
	 * @throws NullPointerException if <code>name</code> is null.
	 * @throws RemoteException if a remote error occurs while creating the game.
	 * @throws AlreadyBoundException if there's another game with name <code>name</code>.
	 */
	public void createGame(final String name) throws NullPointerException, RemoteException, AlreadyBoundException;
	
	/**
	 * Returns the list of running games.
	 * @return the list of running games.
	 * @throws RemoteException if a remote error while returning the list of running games.
	 */
	public Set<String> getGames() throws RemoteException;
}
