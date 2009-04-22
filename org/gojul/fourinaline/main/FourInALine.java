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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
	 * Compute the first file name which does not exist from the file
	 * name <code>fileName</code>. Return <code>fileName</code> if no
	 * file with name <code>fileName</code> exists.
	 * @param fileName the input absolute file name.
	 * @return the first file name which does not exist from the file
	 * name <code>fileName</code>.
	 */
	private static String computeAvailableFileName(final String fileName) 
	{
		String result = fileName;
		
		if (new File(result).exists())
		{
			int i = 1;
			String prefix = result;
			while (new File(prefix + i).exists()) 
			{
				i++;
			}
			result = prefix + i;
		}
		
		return result;
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
		
		outputFile = computeAvailableFileName(outputFile);
		
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
		finally
		{
			try
			{
				if (br != null)
					br.close();
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
	 * Copy the file <code>fSource</code> to the file <code>fDest</code>. Note that this
	 * method will fail in case the directory to which <code>fDest</code> belongs does not
	 * exist.
	 * @param fSource the source file.
	 * @param fDest the destination file.
	 * @throws IOException if an I/O error occurs while performing the copy.
	 */
	private static void copyFile(final File fSource, final File fDest) throws IOException 
	{
		// We perform the copy by blocks of 32 MBs
		final int BLOCK_SIZE = 32 * 1024 * 1024;
		InputStream is = null;
		OutputStream os = null;
		
		try 
		{
			long length = fSource.length();			
			
			is = new BufferedInputStream(new FileInputStream(fSource));
			os = new BufferedOutputStream(new FileOutputStream(fDest));
			
			long nbIter = length / BLOCK_SIZE;
			byte[] data = new byte[BLOCK_SIZE];
			
			// In case the file is bigger than 32 MB...
			for (long i = 0; i < nbIter; i++) 
			{
				is.read(data);
				os.write(data);
			}
			
			int remainingData = (int) (length % BLOCK_SIZE);
			is.read(data, 0, remainingData);
			os.write(data, 0, remainingData);
		}
		finally
		{
			try 
			{
				if (is != null)
				{
					is.close();
				}
			}
			finally
			{
				if (os != null)
				{
					os.close();
				}
			}
		}
	}
	
	/**
	 * Copy the file <code>f</code> which is the JAR file of the Four in a line
	 * game to a location which does not contain spaces.
	 * @param f the file which represents the application JAR file.
	 * @return the destination file.
	 * @throws IOException if an I/O error occurs while copying the file.
	 */
	private final static File copyJarFileToSafeLocation(final File f) throws IOException 
	{
		
		String outputFile = System.getProperty("java.io.tmpdir") + File.separator + "fourinaline.jar";
		if (new File(outputFile).exists()) 
		{
			int i = 1;
			String outputFilePrefix = outputFile;
			while (new File(outputFilePrefix + i).exists()) 
			{
				i++;
			}
			outputFile = outputFilePrefix + i;
		}
		
		outputFile = computeAvailableFileName(outputFile);
		
		File fResult = new File(outputFile);
		fResult.deleteOnExit();
		
		copyFile(f, fResult);
		
		return fResult;
	}
    
	/**
	 * Runs the command <code>command</code>.
	 * @param command the command to run.
	 * @throws NullPointerException if <code>command</code> is null.
	 * @throws Throwable if any error occurs while running the command.
	 */
	public final static void execCommand(final String[] command) throws NullPointerException, Throwable
	{
		if (command == null) 
		{
			throw new NullPointerException();
		}
		
		StringBuilder commandDisplay = new StringBuilder();
		
		for (int i = 0, length = command.length; i < length; i++) 
		{
			commandDisplay.append(command[i]);
			
			if (i < length - 1) 
			{
				commandDisplay.append(" ");
			}
		}
		
		System.out.println("Running command : " + commandDisplay);
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
		// See http://www.velocityreviews.com/forums/t147526-how-to-get-jar-file-name.html
		URL jarURL = FourInALine.class.getProtectionDomain().getCodeSource().getLocation();
		
		// Since RMI does not like spaces in path names, we copy the JAR file to a path
		// which does not contain spaces, i.e. the temp dir which seems to be safe both
		// on Linux and Windows in term of spaces...
		File fTest = new File(jarURL.toURI());
		if (fTest.getAbsolutePath().indexOf(" ") != -1) {
			jarURL = copyJarFileToSafeLocation(fTest).toURI().toURL();
		}
		
		String[] command = {"java", "-cp", jarURL.getFile(), "-Djava.security.policy=" + rmiPolicyFileName, "-Djava.rmi.server.codebase=file:" + jarURL.getFile(), "org.gojul.fourinaline.main.Main"};
		
		if (args.length > 0)
		{
			if (args[0].equalsIgnoreCase("-server"))
				command = new String[]{"java", "-cp", jarURL.getFile(), "-Djava.security.policy=" + rmiPolicyFileName, " -Djava.rmi.server.codebase=file:" + jarURL.getFile(), "org.gojul.fourinaline.main.MainServer"};
			else
			{
				System.out.println("USAGE : java -jar " + JAR_FILE_NAME + " [-server]");
				System.out.println("Use the -server flag if you want to start the server mode without starting the GUI.");
			}
		}	
		
		execCommand(command); 
	}

}
