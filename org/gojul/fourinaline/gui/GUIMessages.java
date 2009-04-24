/*
 * GUIMessages.java
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

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * The <code>GUIFrameMessages</code> class contains
 * the messages of the GUI, localized in English.<br/>
 * The messages can be easily translated to another language
 * using a property file named :<br/>
 * <code>i10n/fourInALine_&lt;lang&gt;_&lt;COUNTRY&gt;.properties</code><br/>
 * Don't forget in this property file that specific characters like
 * spaces, colons and parentheses must be backslashed.
 * 
 * @author Julien Aubin
 */
public final class GUIMessages implements Serializable
{
	
	/**
	 * The serial version UID.
	 */
	final static long serialVersionUID = 1;
	
	/**
	 * The <code>DummyResourceBundle</code> class always returns what
	 * it has as an input. This is a fallback bundle in case there's
	 * no translation file available.
	 * 
	 * @author julien
	 */
	private final static class DummyResourceBundle extends ResourceBundle
	{

		/**
		 * @see java.util.ResourceBundle#getKeys()
		 */
		@Override
		public Enumeration<String> getKeys()
		{
			// TODO Raccord de méthode auto-généré
			return null;
		}

		
		/**
		 * @see java.util.ResourceBundle#handleGetObject(java.lang.String)
		 */
		@Override
		protected Object handleGetObject(final String key)
		{
			return key;
		}

	}

	
	/**
	 * The translation file name.
	 */
	public final static String TRANSLATION_FILE_NAME = "i10n/fourInALine";
	
	/**
	 * The localization handler used for the translation.
	 */
	private static ResourceBundle localizationHandler = null;
	
	static
	{
		try
		{
			localizationHandler = ResourceBundle.getBundle(TRANSLATION_FILE_NAME, Locale.getDefault());
		}
		catch (MissingResourceException e)
		{
			e.printStackTrace();
			localizationHandler = new DummyResourceBundle();
		}
	}
	
	/**
	 * The message label.
	 */
	private String messageLabel;

	/**
	 * Constructor.<br/>
	 * WARNING : since this constructor is private, no test is performed
	 * on it.
	 * @param label the message label.
	 */
	private GUIMessages(final String label)
	{
		messageLabel = label;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		String result = null;
		
		try
		{
			result = localizationHandler.getString(messageLabel);
		}
		catch (MissingResourceException e)
		{
			e.printStackTrace();
		}
		
		if (result == null)
			result = messageLabel;
		
		return result;
	}



	/**
	 * The main frame title.<br/>
	 * Value : &quot;Four in a line&quot;.
	 */
	public final static GUIMessages MAIN_FRAME_TITLE = new GUIMessages("Four in a line");
	
	/**
	 * The new game action text.<br/>
	 * Value : &quot;New game&quot;.
	 */
	public final static GUIMessages NEW_GAME_ACTION_TEXT = new GUIMessages("New game");
	
	/**
	 * The end game action text.<br/>
	 * Value : &quot;End game&quot;.
	 */
	public final static GUIMessages END_GAME_ACTION_TEXT = new GUIMessages("End game");
	
	/**
	 * The quit action text.<br/>
	 * Value : &quot;Quit&quot;.
	 */
	public final static GUIMessages QUIT_ACTION_TEXT = new GUIMessages("Quit");
	
	/**
	 * The error text.<br/>
	 * Value : &quot;Error&quot;
	 */
	public final static GUIMessages ERROR_TEXT = new GUIMessages("Error");
	
	/**
	 * The impossible to start a new game text.<br/>
	 * Value : &quot;Impossible to start a new game.&quot;.
	 */
	public final static GUIMessages IMPOSSIBLE_TO_START_A_NEW_GAME = new GUIMessages("Impossible to start a new game.");
	
	/**
	 * The impossible to end the current game text.<br/>
	 * Value : &quot;Impossible to end the current game.&quot;.
	 */
	public final static GUIMessages IMPOSSIBLE_TO_END_THE_CURRENT_GAME = new GUIMessages("Impossible to end the current game.");
	
	/**
	 * The file menu text.<br/>
	 * Value : &quot;File&quot;
	 */
	public final static GUIMessages FILE_MENU_TEXT = new GUIMessages("File");
	
	/**
	 * The help menu text.<br/>
	 * Value : &quot;Help&quot;
	 */
	public final static GUIMessages HELP_MENU_TEXT = new GUIMessages("Help");
	
	/**
	 * The quit message.<br/>
	 * Value : &quot;Do you really want to quit the game ?&quot;
	 */
	public final static GUIMessages QUIT_MESSAGE = new GUIMessages("Do you really want to quit the game ?");
	
	/**
	 * The disconnected from server message.<br/>
	 * Value : &quot;Disconnected from server.\nThe game will terminate.&quot;
	 */
	public final static GUIMessages DISCONNECTED_FROM_SERVER_MESSAGE = new GUIMessages("Disconnected from server.\nThe game will terminate.");
	
	/**
	 * The score message.<br/>
	 * Value : &quot;Score: &quot;
	 */
	public final static GUIMessages SCORE_MESSAGE = new GUIMessages("Score: ");
	
	/**
	 * The no game running message.<br/>
	 * Value : &quot;No game running.&quot;
	 */
	public final static GUIMessages NO_GAME_RUNNING_MESSAGE = new GUIMessages("No game running");
	
	/**
	 * The you can't play there message.<br/>
	 * Value : &quot;You cannot play there.&quot;
	 */
	public final static GUIMessages YOU_CANNOT_PLAY_THERE_MESSAGE = new GUIMessages("You cannot play there.");
	
	/**
	 * The tie game message.<br/>
	 * Value : &quot;Tie game.&quot;
	 */
	public final static GUIMessages TIE_GAME_MESSAGE = new GUIMessages("Tie game.");
	
	/**
	 * The has won message.<br/>
	 * Value : &quot; has won the game !&quot;
	 */
	public final static GUIMessages HAS_WON_MESSAGE = new GUIMessages(" has won the game !");
	
	/**
	 * The about frame title.<br/>
	 * Value : &quot;About&quot;
	 */
	public final static GUIMessages ABOUT_FRAME_TITLE = new GUIMessages("About");
	
	/**
	 * The about action text.<br/>
	 * Value : &quot;About...&quot;
	 */
	public final static GUIMessages ABOUT_ACTION_TEXT = new GUIMessages("About...");
	
	/**
	 * The copyright information.<br/>
	 * Value : &quot;Copyright (c) 2008 Julien Aubin.&quot;
	 */
	public final static GUIMessages COPYRIGHT_INFO = new GUIMessages("Copyright (c) 2008 Julien Aubin.");
	
	/**
	 * The software license information.<br/>
	 * Value : &quot;This software is distributed under the terms of the GNU General Public License version 2 and any later version.&quot;
	 */
	public final static GUIMessages LICENSE_INFO = new GUIMessages("This software is distributed under the terms of the GNU General Public License version 2 and any later version.");
	
	/**
	 * The Keith Pomakis acknoledgement.<br/>
	 * Value : &quot;Special thanks to Keith Pomakis for his work around Four in a line AI.&quot;
	 */
	public final static GUIMessages POMAKIS_ACKNOWLEDGEMENT = new GUIMessages("Special thanks to Keith Pomakis for his work around Four in a line AI.");
	
	/**
	 * The OK text.<br/>
	 * Value : &quot;OK&quot;
	 */
	public final static GUIMessages OK_TEXT = new GUIMessages("OK");
	
	/**
	 * The Cancel text.<br/>
	 * Value : &quot;Cancel&quot;
	 */
	public final static GUIMessages CANCEL_TEXT = new GUIMessages("Cancel");
	
	/**
	 * The Host text.<br/>
	 * Value : &quot;Host&quot;
	 */
	public final static GUIMessages HOST_TEXT = new GUIMessages("Host");
	
	/**
	 * The local game text.<br/>
	 * Value : &quot;Local game&quot;
	 */
	public final static GUIMessages LOCAL_GAME_TEXT = new GUIMessages("Local game");
	
	/**
	 * The remote host text.<br/>
	 * Value : &quot;Remote server (enter host name or IP) : &quot;
	 */
	public final static GUIMessages REMOTE_SERVER_TEXT = new GUIMessages("Remote server (enter host name or IP) : ");
	
	/**
	 * The adversory text.<br/>
	 * Value : &quot;Adversory&quot;
	 */
	public final static GUIMessages ADVERSORY_TEXT = new GUIMessages("Adversory");
	
	/**
	 * The computer adversory text.<br/>
	 * Value : &quot;Computer&quot;
	 */
	public final static GUIMessages COMPUTER_ADVERSORY_TEXT = new GUIMessages("Computer");
	
	/**
	 * The human adversory text.<br/>
	 * Value : &quot;Human&quot;
	 */
	public final static GUIMessages HUMAN_ADVERSORY_TEXT = new GUIMessages("Human");
	
	/**
	 * The unable to start server message.<br/>
	 * Value : &quot;Unable to start server.&quot;
	 */
	public final static GUIMessages UNABLE_TO_START_SERVER_MESSAGE = new GUIMessages("Unable to start server.");
	
	/**
	 * The you must select a server message.<br/>
	 * Value : &quot;You must select a server.&quot;
	 */
	public final static GUIMessages YOU_MUST_SELECT_A_SERVER_MESSAGE = new GUIMessages("You must select a server.");
	
	/**
	 * The unable to connect to server message.<br/>
	 * Value : &quot;Unable to connect to server. Error message returned from server: &quot;
	 */
	public final static GUIMessages UNABLE_TO_CONNECT_TO_SERVER_MESSAGE = new GUIMessages("Unable to connect to server. Error message returned from server: ");
	
	/**
	 * The lost connection to server message.<br/>
	 * Value : &quot;Lost connection to server. The program will halt.&quot;
	 */
	public final static GUIMessages LOST_CONNECTION_TO_SERVER_MESSAGE = new GUIMessages("Lost connection to server. The program will halt.");
	
	/**
	 * The list of players currently registered message.<br/>
	 * Value : &quot;List of players currently registered: &quot;
	 */
	public final static GUIMessages LIST_OF_PLAYERS_MESSAGE = new GUIMessages("List of players currently registered: ");
	
	/**
	 * The enter the name of your player message.<br/>
	 * Value : &quot;Enter the name of your player.&quot;
	 */
	public final static GUIMessages ENTER_THE_NAME_OF_YOUR_PLAYER_MESSAGE = new GUIMessages("Enter the name of your player.");
	
	/**
	 * The player name must be different from the other names message.<br/>
	 * Value : &quot;This name must be different from the other player names.&quot;
	 */
	public final static GUIMessages PLAYER_NAME_MUST_BE_DIFFERENT_FROM_THE_OTHER_NAMES_MESSAGE = new GUIMessages("This name must be different from the other player names.");
	
	/**
	 * The you must specify a player name message.<br/>
	 * Value : &quot;You must specify a player name.&quot;
	 */
	public final static GUIMessages YOU_MUST_SPECIFY_A_PLAYER_NAME_MESSAGE = new GUIMessages("You must specify a player name.");
	
	/**
	 * The failed to register message.<br/>
	 * Value : &quot;Failed to register to the server. Error message: &quot;
	 */
	public final static GUIMessages FAILED_TO_REGISTER_MESSAGE = new GUIMessages("Failed to register to the server. Error message: ");
	
	/**
	 * The no player available message.<br/>
	 * Value : &quot;Impossible to register: no more player available.&quot;
	 */
	public final static GUIMessages NO_PLAYER_AVAILABLE_MESSAGE = new GUIMessages("Impossible to register: no more player available.");
	
	/**
	 * The unable to create AI player message.<br/>
	 * Value : &quot;Unable to create an AI player because there's no more player left. Playing against a human player.&quot;
	 */
	public final static GUIMessages UNABLE_TO_CREATE_AI_PLAYER = new GUIMessages("Unable to create an AI player because there's no more player left. Playing against a human player.");
	
	/**
	 * The current turn message.<br/>
	 * Value : &quot;Current turn: &quot;
	 */
	public final static GUIMessages CURRENT_TURN_MESSAGE = new GUIMessages("Current turn: ");
	
	/**
	 * The week AI level message.<br/>
	 * Value : &quot;Weak (Fast)&quot;
	 */
	public final static GUIMessages WEAK_AI_LEVEL_MESSAGE = new GUIMessages("Weak (Fast)");
	
	/**
	 * The intermediate AI level message.<br/>
	 * Value : &quot;Intermediate&quot;
	 */
	public final static GUIMessages INTERMEDIATE_AI_LEVEL_MESSAGE = new GUIMessages("Intermediate");
	
	/**
	 * The strong AI level message.<br/>
	 * Value : &quot;Strong (Slow)&quot;
	 */
	public final static GUIMessages STRONG_AI_LEVEL_MESSAGE = new GUIMessages("Strong (Slow)");
	
	/**
	 * The game message.<br/>
	 * Value : &quot;Game&quot;
	 */
	public final static GUIMessages GAME_MESSAGE = new GUIMessages("Game");
	
	/**
	 * The create game message.<br/>
	 * Value : &quot;Create a new game&quot;
	 */
	public final static GUIMessages CREATE_GAME_MESSAGE = new GUIMessages("Create a new game");
	
	/**
	 * The join game message.<br/>
	 * Value : &quot;Join an existing game&quot;
	 */
	public final static GUIMessages JOIN_GAME_MESSAGE = new GUIMessages("Join an existing game");
	
	/**
	 * The you must specify a game name message.<br/>
	 * Value : &quot;You must specify a game name&quot;
	 */
	public final static GUIMessages YOU_MUST_SPECIFY_A_GAME_NAME_MESSAGE = new GUIMessages("You must specify a game name.");
	
	/**
	 * The there's already a game with name message.<br/>
	 * Value : &quot;There's already a game with name &quot;
	 */
	public final static GUIMessages THERE_IS_ALREADY_A_GAME_WITH_NAME = new GUIMessages("There's already a game with name ");
	
	/**
	 * The there's no game with name message.<br/>
	 * Value : &quot;There's no game with name &quot;
	 */
	public final static GUIMessages THERE_IS_NO_GAME_WITH_NAME = new GUIMessages("There's no game with name ");
	
	/**
	 * The the selected game is full message.<br/>
	 * Value : &quot;Impossible to register a player to the selected game since it is already full.&quot;
	 */
	public final static GUIMessages THE_SELECTED_GAME_IS_FULL = new GUIMessages("Impossible to register a player to the selected game since it is already full.");
	
	/**
	 * The play again message.<br/>
	 * Value : &quot;Play again ?&quot;
	 */
	public final static GUIMessages PLAY_AGAIN = new GUIMessages("Play again ?");
}
