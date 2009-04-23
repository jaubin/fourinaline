/*
 * FourInALine.java
 *
 * Created: 9 mars 08
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

import java.io.File;
import java.net.URL;

import org.gojul.fourinaline.gui.LoginFrame;
import org.gojul.fourinaline.model.GameServerImpl;

/**
 * The program launcher, from the user point of view.
 *
 * @author Julien Aubin
 */
public class FourInALine
{
	
	/**
	 * Private constructor.<br/>
	 * Prevents the class from being instanciated.
	 *
	 */
	private FourInALine()
	{
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Throwable
	{
		
		
		if (args.length > 0)
		{
			if (args[0].equalsIgnoreCase("-server"))
			{
				GameServerImpl.startDaemon();
				
				while (true)
				{
					Thread.sleep(5000);
				}
			}				
			else
			{
				URL jarURL = FourInALine.class.getProtectionDomain().getCodeSource().getLocation();
				File locationFile = new File(jarURL.toURI());
				
				System.err.println("USAGE : java " + (locationFile.isFile() ? "-jar " + locationFile.getName() : FourInALine.class.getName()) + " [-server]");
				System.err.println("Use the -server flag if you want to start the server mode without starting the GUI.");
			}
		}	
		else 
		{
			new LoginFrame().setVisible(true);
		}
	}

}
