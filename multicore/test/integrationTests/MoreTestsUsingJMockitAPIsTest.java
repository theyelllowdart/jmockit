/*
 * Copyright (c) 2006-2013 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package integrationTests;

import java.awt.Toolkit;

import org.junit.*;

import mockit.*;

@UsingMocksAndStubs(Toolkit.class)
public final class MoreTestsUsingJMockitAPIsTest
{
   @Test
   public void verifyThatAWTToolkitIsStubbedOut()
   {
      assert Toolkit.getDefaultToolkit() == null;
   }

   public static class A
   {
      public void doSomething() { throw new RuntimeException("should not execute"); }
      int getValue() { return -1; }
   }

   @Mocked A mock;

   @Test
   public void usingTheExpectationsAPI()
   {
      new NonStrictExpectations() {{ mock.getValue(); result = 123; }};

      assert mock.getValue() == 123;
      mock.doSomething();
   }

   @Test
   public void usingTheVerificationsAPI()
   {
      mock.doSomething();

      new FullVerifications() {{ mock.doSomething(); times = 1; }};
   }
}
