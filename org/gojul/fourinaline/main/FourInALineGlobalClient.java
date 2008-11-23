/*
 * FourInALineGlobalClient.java
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
package org.gojul.fourinaline.main;

import javax.swing.UIManager;

import org.gojul.fourinaline.gui.GlobalLoginFrame;

/**
 * The global client main frame.
 *
 * @author Julien Aubin
 */
public final class FourInALineGlobalClient
{
	public static void main(String[] args) throws Throwable
	{
		if (args.length != 1)
		{
			System.err.println("USAGE: java FourInALineGlobalClient <server IP or name>");
			return;
		}
		
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Throwable t)
		{
			t.printStackTrace();
			// WA for problems that may occur under some badly shaped platforms, as Debian Lenny
			// which seems to have a problem with the Java desktop integration...
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		}
		
		new GlobalLoginFrame(args[0]).setVisible(true);
	}
}
