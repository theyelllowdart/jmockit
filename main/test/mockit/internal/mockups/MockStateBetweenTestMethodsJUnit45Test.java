/*
 * Copyright (c) 2006-2013 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.mockups;

import static org.junit.Assert.*;
import org.junit.*;

import mockit.*;
import mockit.external.asm4.*;
import mockit.internal.mockups.MockStateBetweenTestMethodsJUnit45Test.*;
import mockit.internal.state.*;

@UsingMocksAndStubs(TheMockClass.class)
public final class MockStateBetweenTestMethodsJUnit45Test
{
   static final class RealClass
   {
      int doSomething() { throw new RuntimeException("Unexpected execution"); }
   }

   static final class TheMockClass extends MockUp<RealClass>
   {
      static final String internalName = Type.getInternalName(TheMockClass.class);
      int i;

      @Mock(invocations = 1)
      int doSomething() { return ++i; }

      static void assertMockState(int expectedInvocationCount)
      {
         MockState mockState = TestRun.getMockClasses().getMockStates().getMockState(internalName, 0);

         assertTrue(mockState.isWithExpectations());
         assertFalse(mockState.isReentrant());
         assertFalse(mockState.isOnReentrantCall());
         assertEquals(expectedInvocationCount, mockState.getTimesInvoked());
      }
   }

   @Test
   public void firstTest()
   {
      TheMockClass.assertMockState(0);
      assertEquals(1, new RealClass().doSomething());
      TheMockClass.assertMockState(1);
   }

   @Test
   public void secondTest()
   {
      TheMockClass.assertMockState(0);
      assertEquals(2, new RealClass().doSomething());
      TheMockClass.assertMockState(1);
   }
}