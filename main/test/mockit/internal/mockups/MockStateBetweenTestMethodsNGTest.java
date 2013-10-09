/*
 * Copyright (c) 2006-2013 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.mockups;

import org.testng.annotations.*;
import static org.testng.Assert.*;

import mockit.*;
import mockit.internal.mockups.MockStateBetweenTestMethodsJUnit45Test.*;

@Test
@UsingMocksAndStubs(TheMockClass.class)
public final class MockStateBetweenTestMethodsNGTest
{
   public void firstTest()
   {
      TheMockClass.assertMockState(0);
      assertEquals(new RealClass().doSomething(), 1);
      TheMockClass.assertMockState(1);
   }

   public void secondTest()
   {
      TheMockClass.assertMockState(0);
      assertEquals(new RealClass().doSomething(), 2);
      TheMockClass.assertMockState(1);
   }
}