/*
 * Copyright (c) 2006-2013 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.startup;

import java.io.*;
import java.net.*;
import java.security.*;
import java.util.regex.*;

final class AgentInitialization
{
   private static final Pattern JAR_REGEX = Pattern.compile(".*jmockit[-.\\d]*.jar");

   boolean initializeAccordingToJDKVersion()
   {
      String jarFilePath = discoverPathToJarFile();

      if (Startup.jdk6OrLater) {
         return new JDK6AgentLoader(jarFilePath).loadAgent();
      }
      else if ("1.5".equals(Startup.javaSpecVersion)) {
         throw new IllegalStateException(
            "JMockit has not been initialized. Check that your Java 5 VM has been started with the -javaagent:" +
            jarFilePath + " command line option.");
      }
      else {
         throw new IllegalStateException("JMockit requires a Java 5+ VM");
      }
   }

   private String discoverPathToJarFile()
   {
      String jarFilePath = findPathToJarFileFromClasspath();

      if (jarFilePath == null) {
         // This can fail for a remote URL, so it is used as a fallback only:
         jarFilePath = getPathToJarFileContainingThisClass();
      }

      if (jarFilePath != null) {
         return jarFilePath;
      }

      throw new IllegalStateException(
         "No jar file with name ending in \"jmockit.jar\" or \"jmockit-nnn.jar\" (where \"nnn\" is a version number) " +
         "found in the classpath");
   }

   private String findPathToJarFileFromClasspath()
   {
      String[] classPath = System.getProperty("java.class.path").split(File.pathSeparator);

      for (String cpEntry : classPath) {
         if (JAR_REGEX.matcher(cpEntry).matches()) {
            return cpEntry;
         }
      }

      return null;
   }

   private String getPathToJarFileContainingThisClass()
   {
      CodeSource codeSource = AgentInitialization.class.getProtectionDomain().getCodeSource();

      if (codeSource == null) {
         return null;
      }

      URL location = codeSource.getLocation();
      String locationPath = location.getPath();
      File jarFile;

      if (locationPath.endsWith("/main/classes/")) {
         jarFile = findLocalJarOrZipFileFromLocationOfCurrentClassFile(locationPath);
      }
      else {
         jarFile = findJarFileContainingCurrentClass(location);
      }

      return jarFile.isFile() ? jarFile.getPath() : null;
   }

   private File findLocalJarOrZipFileFromLocationOfCurrentClassFile(String locationPath)
   {
      File localJarFile = new File(locationPath.replace("main/classes/", "jmockit.jar"));

      if (localJarFile.exists()) {
         return localJarFile;
      }

      return new File(locationPath.replace("classes/", "META-INF.zip"));
   }

   private File findJarFileContainingCurrentClass(URL location)
   {
      try {
         // URI is needed to deal with spaces and non-ASCII characters.
         URI jarFileURI = location.toURI();
         return new File(jarFileURI);
      }
      catch (URISyntaxException e) {
         throw new RuntimeException(e);
      }
   }
}
