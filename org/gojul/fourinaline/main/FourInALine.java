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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * The program launcher, from the user point of view.
 *
 * @author Julien Aubin
 */
public class FourInALine
{
	
	/**
	 * The <code>StreamGlobber</code> class redirects the output
	 * from a process launched from the JVM to the standard output.<br/>
	 * This class is derived from the works from Michael C. Daconta.
	 * 
	 * See <A href="http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html?page=4">there</A>
	 * for further reference.
	 *
	 * @author Julien Aubin
	 */
	final static class StreamGobbler extends Thread
	{
		/**
		 * The inputstream from the process.
		 */
		private InputStream is;
	    
		/**
		 * Constructor.
		 * @param is the inputstream from the process.
		 * @throws NullPointerException if <code>is</code> is null.
		 */
		StreamGobbler(final InputStream is) throws NullPointerException
		{
			if (is == null)
				throw new NullPointerException();
	    	
			this.is = is;
		}
	    
	    
		/**
		 * @see java.lang.Thread#run()
		 */
		public void run()
		{
			try
			{	                
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line=null;
				while ( (line = br.readLine()) != null)
				{
					System.out.println(line);    
				}
			} 
			catch (IOException ioe)
			{
				ioe.printStackTrace();  
			}
		}
	}
	
	/**
	 * Copies the RMI policy resource file to the temp directory and returns it
	 * as the RMI policy file to use by the command to launch.
	 * @return the RMI policy file to use by the command to launch.
	 * @throws IOException if an I/O error occurs while copying the RMI policy file.
	 */
	private static String initRMIPolicy() throws IOException
	{
		final String RMI_POLICY_FILE_NAME = "/rmipolicy.policy";
		
		BufferedReader br = null;
		BufferedWriter bw = null;
		
		String outputFile = System.getProperty("java.io.tmpdir") + File.separator + "fourinaline.rmi.policy";
		
		try
		{
			br = new BufferedReader(new InputStreamReader(FourInALine.class.getResourceAsStream(RMI_POLICY_FILE_NAME)));
			bw = new BufferedWriter(new FileWriter(outputFile));
			
			String line = br.readLine();
			
			while (line != null)
			{
				bw.write(line);
				
				line = br.readLine();
				
				if (line != null)
					bw.newLine();
			}
		}
		catch (IOException e)
		{
			throw e;
		}
		finally
		{
			try
			{
				if (br != null)
					br.close();
			}
			catch (IOException e)
			{
				throw e;
			}
			finally
			{
				if (bw != null)
					bw.close();
			}
		}
		
		new File(outputFile).deleteOnExit();
		
		return outputFile;
	}
    
	/**
	 * Runs the command <code>command</code>.
	 * @param command the command to run.
	 * @throws Throwable if any error occurs while running the command.
	 */
	public final static void execCommand(final String command) throws Throwable
	{
		System.out.println("Running command : " + command);
		Process proc = Runtime.getRuntime().exec(command);
		
		// any error message?
		StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream());            
        
		// any output?
		StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream());
            
		// kick them off
		errorGobbler.start();
		outputGobbler.start();
                                
		// any error???
		int exitVal = proc.waitFor();
		System.out.println("Exit Value: " + exitVal); 
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Throwable
	{
		final String JAR_FILE_NAME = "fourinaline.jar";
		
		// See http://forum.java.sun.com/thread.jspa?threadID=5153353&messageID=9577541
		String rmiPolicyFileName = initRMIPolicy();
		URL jarURL = FourInALine.class.getResource("/" + JAR_FILE_NAME);
		
		String command = "java -cp " + jarURL.getFile() + " -Djava.security.policy=" + rmiPolicyFileName + " -Djava.rmi.server.codebase=file:" + jarURL.getFile() + " org.gojul.fourinaline.main.Main";
		
		if (args.length > 0)
		{
			if (args[0].equalsIgnoreCase("-server"))
				command = "java -cp " + jarURL.getFile() + " -Djava.security.policy=" + rmiPolicyFileName + " -Djava.rmi.server.codebase=file:" + jarURL.getFile() + " org.gojul.fourinaline.main.MainServer";
			else
			{
				System.out.println("USAGE : java -jar " + JAR_FILE_NAME + " [-server]");
				System.out.println("Use the -server flag if you want to start the server mode without starting the GUI.");
			}
		}	
		
		execCommand(command); 
	}

}
