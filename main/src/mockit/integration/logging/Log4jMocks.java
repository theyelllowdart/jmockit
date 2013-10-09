/*
 * Copyright (c) 2006-2013 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.integration.logging;

import org.apache.log4j.*;
import org.apache.log4j.spi.*;

import mockit.*;

/**
 * A mock class containing mocks and stubs for the Log4j API.
 * <p/>
 * When a test class is annotated as {@code @UsingMocksAndStubs(Log4jMocks.class)}, all production code touched by the
 * tests in that class will receive mock {@code Logger} instances instead of real ones, when one of the factory methods
 * in class {@code org.apache.log4j.Logger} is called.
 * <p/>
 * <a href="http://jmockit.googlecode.com/svn/trunk/www/tutorial/UsingMocksAndStubs.html">Tutorial</a>
 */
public final class Log4jMocks extends MockUp<Logger>
{
   private static final Logger MOCK_LOGGER = new RootLogger(Level.OFF);

   Log4jMocks() { new Hierarchy(MOCK_LOGGER).setThreshold(Level.OFF); }

   /**
    * Returns a singleton mock {@code Logger} instance, whose methods do nothing.
    */
   @Mock public static Logger getLogger(String name) { return MOCK_LOGGER; }

   /**
    * Returns a singleton mock {@code Logger} instance, whose methods do nothing.
    */
   @Mock public static Logger getLogger(Class<?> clazz) { return MOCK_LOGGER; }

   /**
    * Returns a singleton mock {@code Logger} instance, whose methods do nothing.
    */
   @Mock public static Logger getRootLogger() { return MOCK_LOGGER; }

   /**
    * Returns a singleton mock {@code Logger} instance, whose methods do nothing.
    */
   @Mock public static Logger getLogger(String name, LoggerFactory lf) { return MOCK_LOGGER; }

   @Mock public static void trace(Object message) {}
   @Mock public static void trace(Object message, Throwable t) {}
   @Mock public static boolean isTraceEnabled() { return false; }
}
