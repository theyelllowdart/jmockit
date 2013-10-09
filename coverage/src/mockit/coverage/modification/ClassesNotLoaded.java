/*
 * Copyright (c) 2006-2013 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage.modification;

import java.io.*;
import java.security.*;

/**
 * Finds and loads all classes that should also be measured, but were not loaded until now.
 */
public final class ClassesNotLoaded
{
   private final ClassModification classModification;
   private ProtectionDomain protectionDomain;
   private int firstPosAfterParentDir;

   public ClassesNotLoaded(ClassModification classModification) { this.classModification = classModification; }

   public void gatherCoverageData()
   {
      for (ProtectionDomain pd : classModification.protectionDomains) {
         File classPathEntry = new File(pd.getCodeSource().getLocation().getPath());

         if (!classPathEntry.getPath().endsWith(".jar")) {
            protectionDomain = pd;
            firstPosAfterParentDir = classPathEntry.getPath().length() + 1;
            loadAdditionalClasses(classPathEntry);
         }
      }
   }

   private void loadAdditionalClasses(File classPathEntry)
   {
      File[] filesInDir = classPathEntry.listFiles();

      if (filesInDir != null) {
         for (File fileInDir : filesInDir) {
            if (fileInDir.isDirectory()) {
               loadAdditionalClasses(fileInDir);
            }
            else {
               loadAdditionalClass(fileInDir.getPath());
            }
         }
      }
   }

   private void loadAdditionalClass(String filePath)
   {
      int p = filePath.lastIndexOf(".class");

      if (p > 0) {
         String relativePath = filePath.substring(firstPosAfterParentDir, p);
         String className = relativePath.replace(File.separatorChar, '.');

         if (classModification.isToBeConsideredForCoverage(className, protectionDomain)) {
            loadClass(className);
         }
      }
   }

   private void loadClass(String className)
   {
      try {
         Class.forName(className, false, protectionDomain.getClassLoader());
      }
      catch (ClassNotFoundException ignore) {}
      catch (NoClassDefFoundError ignored) {}
   }
}
