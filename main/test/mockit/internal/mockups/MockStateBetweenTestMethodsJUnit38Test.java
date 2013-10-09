/*
 * Copyright (c) 2006-2013 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.mockups;

import junit.framework.*;

import mockit.*;
import mockit.internal.mockups.MockStateBetweenTestMethodsJUnit45Test.*;

@UsingMocksAndStubs(TheMockClass.class)
public final class MockStateBetweenTestMethodsJUnit38Test extends TestCase
{
   public void testFirst()
   {
      TheMockClass.assertMockState(0);
      assertEquals(1, new RealClass().doSomething());
      TheMockClass.assertMockState(1);
   }

   public void testSecond()
   {
      TheMockClass.assertMockState(0);
      assertEquals(2, new RealClass().doSomething());
      TheMockClass.assertMockState(1);
   }
}