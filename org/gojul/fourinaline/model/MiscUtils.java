/*
 * MiscUtils.java
 *
 * Created: 23 avr. 09
 *
 * Copyright (C) 2009 Julien Aubin
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * The <code>MiscUtils</code> class contains various utilities
 * for servers.
 *
 * @author Julien Aubin
 */
public final class MiscUtils
{
	/**
	 * The line separator we use.
	 */
	public final static String LINE_SEP = System.getProperty("line.separator");
	
	/**
	 * Private constructor.<br/>
	 * Prevents the class from being instanciated.
	 */
	private MiscUtils() {
		
	}
	
	/**
	 * Return the version of the software.
	 * @return the version of the software.
	 */
	public static String getVersion() 
	{
		final String DEVELOPMENT_VERSION = "Development";
		final String VERSION_TAG = "Version";
		
		try
		{
			URL jarURL = MiscUtils.class.getProtectionDomain().getCodeSource().getLocation();
			File locationFile = new File(jarURL.toURI());
			
			if (locationFile.isFile())
			{
				JarFile jarFile = new JarFile(locationFile);
				Manifest manifest = jarFile.getManifest();
				return manifest.getMainAttributes().getValue(VERSION_TAG);
			}
			else
			{
				return DEVELOPMENT_VERSION;
			}
		}
		catch (Throwable t)
		{
			t.printStackTrace();
			return DEVELOPMENT_VERSION;
		}
	}
	
	/**
	 * Read the text contained in the input stream <code>is</code>
	 * and return the text it contains.<br/>
	 * Close <code>is</code> at the end of the read.
	 * @param is the input stream to read.
	 * @return the text contained in <code>is</code>.
	 * @throws NullPointerException if <code>is</code> is null.
	 * @throws IOException if an I/O error occurs while reading <code>is</code>.
	 */
	public static String readTextStream(final InputStream is) throws NullPointerException, IOException
	{
		if (is == null)
			throw new NullPointerException();
		
		StringBuilder result = new StringBuilder();
		
		BufferedReader br = null;
		
		try
		{
			br = new BufferedReader(new InputStreamReader(is));
			
			String line = br.readLine();
			
			while (line != null)
			{
				result.append(line);
				
				line = br.readLine();
				if (line != null)
					result.append(LINE_SEP);
			}
		}
		finally
		{
			if (br != null)
				br.close();
		}
		
		return result.toString();
	}

	/**
	 * Copy the file <code>fSource</code> to the file <code>fDest</code>. Note that
	 * if the directory which contains <code>fDest</code> does not exist this method
	 * will attempt to create it.
	 * @param fSource the source file.
	 * @param fDest the destination file.
	 * @throws NullPointerException if any of the method parameter is null.
	 * @throws IOException if an I/O error occurs while performing the copy.
	 */
	public static void copyFile(final File fSource, final File fDest) throws NullPointerException, IOException 
	{
		if (fSource == null || fDest == null)
			throw new NullPointerException();
		
		File fDestDir = fDest.getParentFile();
		if (fDestDir != null)
		{
			if (fDestDir.exists())
			{
				if (fDestDir.isFile())
					throw new FileNotFoundException("The destination directory " + fDestDir + " is not a directory !");
			}
			else if (!fDestDir.mkdirs())
			{
				throw new FileNotFoundException("Unable to create the destination directory " + fDestDir);
			}
		}
		
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
	 * Copies the RMI policy resource file to the temp directory and returns it
	 * as the RMI policy file to use by the command to launch.
	 * @return the RMI policy file to use by the command to launch.
	 * @throws IOException if an I/O error occurs while copying the RMI policy file.
	 */
	private final static String initRMIPolicy() throws IOException
	{
		final String RMI_POLICY_FILE_NAME = "/rmipolicy.policy";
		
		BufferedReader br = null;
		BufferedWriter bw = null;
		
		String outputFile = System.getProperty("java.io.tmpdir") + File.separator + "fourinaline.rmi.policy";
		
		outputFile = computeAvailableFileName(outputFile);
		
		try
		{
			br = new BufferedReader(new InputStreamReader(MiscUtils.class.getResourceAsStream(RMI_POLICY_FILE_NAME)));
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
	 * Compute the first file name which does not exist from the file
	 * name <code>fileName</code>. Return <code>fileName</code> if no
	 * file with name <code>fileName</code> exists.
	 * @param fileName the input absolute file name.
	 * @return the first file name which does not exist from the file
	 * name <code>fileName</code>.
	 */
	private final static String computeAvailableFileName(final String fileName) 
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
	 * Init an RMI registry at port <code>port</code>
	 * and return it.
	 * @param port the RMI registry port.
	 * @return the initialized RMI registry.
	 * @throws RuntimeException in case an error occurs while  
	 * initializing the RMI server.
	 */
	public final static Registry initRMIServer(final int port) throws RuntimeException {
		
		try 
		{
			final Properties currentProperties = new Properties(System.getProperties());
			
			final String rmiPolicyFileName = initRMIPolicy();
			
			URL jarURL = MiscUtils.class.getProtectionDomain().getCodeSource().getLocation();
			File locationFile = new File(jarURL.toURI());
			if (locationFile.isFile() && locationFile.getAbsolutePath().indexOf(' ') != -1) {
				jarURL = copyJarFileToSafeLocation(locationFile).toURI().toURL();
			}
			final String rmiFileName = jarURL.getFile();
			
			return AccessController.doPrivileged(new PrivilegedAction<Registry>() {
	
				/**
				 * @see java.security.PrivilegedAction#run()
				 */
				public Registry run()
				{
					// Swaps the properties to initialize the RMI registry
					System.setProperty("java.security.policy", rmiPolicyFileName);
					System.setProperty("java.rmi.server.codebase", "file:" + rmiFileName);
					
					// Then swaps the security manager...
					SecurityManager manager = System.getSecurityManager();					
					System.setSecurityManager(new SecurityManager());
					
					Registry registry = null;
					
					// Creates the registry
					try 
					{
						registry = LocateRegistry.createRegistry(port);
					}
					catch (RemoteException e) 
					{
						throw new RuntimeException(e);
					}
					finally 
					{
						// Finally restores everything as it was before...
						System.setProperties(currentProperties);
						System.setSecurityManager(manager);
					}
					return registry;
				}
				
			});
		}
		catch (Throwable t) 
		{
			throw new RuntimeException(t);
		}
	}
}
