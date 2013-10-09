/*
 * Copyright (c) 2006-2013 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import java.util.*;
import java.util.concurrent.*;

import org.junit.*;

import static org.junit.Assert.*;

public final class CapturingInstancesTest
{
   public interface Service1 { int doSomething(); }
   static final class Service1Impl implements Service1 { public int doSomething() { return 1; } }

   public static final class TestedUnit
   {
      private final Service1 service1 = new Service1Impl();
      private final Service1 service2 = new Service1() { public int doSomething() { return 2; } };
      Observable observable;

      public int businessOperation(final boolean b)
      {
         new Callable()
         {
            public Object call() { throw new IllegalStateException(); }
         }.call();

         observable = new Observable() {{
            if (b) {
               throw new IllegalArgumentException();
            }
         }};

         return service1.doSomething() + service2.doSomething();
      }
   }

   @Capturing(maxInstances = 2) Service1 service;

   @Test
   public void captureServiceInstancesCreatedByTestedConstructor()
   {
      Service1 initialMockService = service;

      new TestedUnit();

      assertNotSame(initialMockService, service);
      assertFalse(service instanceof Service1Impl);
   }

   @Test
   public void captureAllInternallyCreatedInstances(@Capturing final Callable<?> callable) throws Exception
   {
      new NonStrictExpectations() {
         @Capturing Observable observable;

         {
            service.doSomething(); returns(3, 4);
         }
      };

      TestedUnit unit = new TestedUnit();
      int result = unit.businessOperation(true);
      assertEquals(4, unit.service1.doSomething());
      assertEquals(4, unit.service2.doSomething());

      assertNotNull(unit.observable);
      assertEquals(7, result);

      new Verifications() {{ callable.call(); }};
   }

   public interface Service2 { int doSomething(); }
   static final class Service2Impl implements Service2 { public int doSomething() { return 2; } }

   @Test
   public void recordStrictExpectationsForNextTwoInstancesToBeCreatedUsingMockFields()
   {
      new Expectations() {
         @Capturing(maxInstances = 1) Service2 s1;
         @Capturing(maxInstances = 1) Service2 s2;

         {
            s1.doSomething(); result = 11;
            s2.doSomething(); result = 22;
         }
      };

      assertEquals(11, new Service2Impl().doSomething());
      assertEquals(22, new Service2Impl().doSomething());
   }

   @Test
   public void recordStrictExpectationsForNextTwoInstancesToBeCreatedUsingMockParameters(
      @Capturing(maxInstances = 1) final Service2 s1, @Capturing(maxInstances = 1) final Service2 s2)
   {
      new Expectations() {{
         s1.doSomething(); result = 11;
         s2.doSomething(); returns(22, 33);
      }};

      assertEquals(11, new Service2Impl().doSomething());
      Service2Impl s = new Service2Impl();
      assertEquals(22, s.doSomething());
      assertEquals(33, s.doSomething());
   }

   @Test
   public void recordExpectationsForNextTwoInstancesToBeCreatedUsingNonStrictMockFields()
   {
      new NonStrictExpectations() {
         @Capturing(maxInstances = 1) Service2 s1;
         @Capturing(maxInstances = 1) Service2 s2;

         {
            s1.doSomething(); result = 11;
            s2.doSomething(); result = 22;
         }
      };

      Service2Impl s1 = new Service2Impl();
      Service2Impl s2 = new Service2Impl();
      assertEquals(22, s2.doSomething());
      assertEquals(11, s1.doSomething());
      assertEquals(11, s1.doSomething());
      assertEquals(22, s2.doSomething());
      assertEquals(11, s1.doSomething());
   }

   @Test
   public void recordNonStrictExpectationsForNextTwoInstancesToBeCreatedUsingMockFields()
   {
      new NonStrictExpectations() {
         @Capturing(maxInstances = 1) Service2 s1;
         @Capturing(maxInstances = 1) Service2 s2;

         {
            s1.doSomething(); result = 11;
            s2.doSomething(); result = 22;
         }
      };

      assertEquals(11, new Service2Impl().doSomething());
      assertEquals(22, new Service2Impl().doSomething());
   }

   @Test
   public void recordExpectationsForNextTwoInstancesOfTwoDifferentImplementingClasses()
   {
      new NonStrictExpectations() {
         @Capturing(maxInstances = 1) Service2 s1;
         @Capturing(maxInstances = 1) Service2 s2;

         {
            s1.doSomething(); result = 1;
            s2.doSomething(); result = 2;
         }
      };

      Service2 s1 = new Service2() { public int doSomething() { return -1; } };
      assertEquals(1, s1.doSomething());

      Service2 s2 = new Service2() { public int doSomething() { return -2; } };
      assertEquals(2, s2.doSomething());
   }

   @Test
   public void recordExpectationsForTwoConsecutiveSetsOfFutureInstances()
   {
      new NonStrictExpectations() {
         @Capturing(maxInstances = 2) Service2 s1;
         @Capturing(maxInstances = 3) Service2 s2;

         {
            s1.doSomething(); result = 1;
            s2.doSomething(); result = 2;
         }
      };

      // First set of instances, matching the expectation on "s1":
      Service2 s1 = new Service2Impl();
      Service2 s2 = new Service2Impl();
      assertEquals(1, s1.doSomething());
      assertEquals(1, s2.doSomething());

      // Second set of instances, matching the expectation on "s2":
      Service2 s3 = new Service2Impl();
      Service2 s4 = new Service2Impl();
      Service2 s5 = new Service2Impl();
      assertEquals(2, s3.doSomething());
      assertEquals(2, s4.doSomething());
      assertEquals(2, s5.doSomething());

      // Third set of instances, not matching any expectation:
      Service2 s6 = new Service2Impl();
      assertEquals(0, s6.doSomething());
   }

   @Test
   public void recordExpectationsOnMethodSpecifiedInPartialMockingFilter()
   {
      new NonStrictExpectations() {
         @Capturing(maxInstances = 1) @Mocked({"()", "doSomething"}) Service2 s1;
         @Capturing(maxInstances = 1) Service2 s2;

         {
            s1.doSomething(); result = 1;
            s2.doSomething(); result = 2;
         }
      };

      Service2 first = new Service2Impl();
      assertEquals(1, first.doSomething());

      Service2 second = new Service2Impl();
      assertEquals(2, second.doSomething());
   }

   @Test
   public void recordExpectationsForNextTwoInstancesToBeCreatedUsingMockParameters(
      @Capturing(maxInstances = 1) final Service2 s1, @Capturing(maxInstances = 1) final Service2 s2)
   {
      new NonStrictExpectations() {{
         s2.doSomething(); result = 22;
         s1.doSomething(); result = 11;
      }};

      Service2Impl cs1 = new Service2Impl();
      assertEquals(11, cs1.doSomething());

      Service2Impl cs2 = new Service2Impl();
      assertEquals(22, cs2.doSomething());
      assertEquals(11, cs1.doSomething());
      assertEquals(22, cs2.doSomething());
   }

   static class Base { boolean doSomething() { return false; } }
   static final class Derived1 extends Base {}
   static final class Derived2 extends Base {}

   @Test
   public void verifyExpectationsOnlyOnFirstOfTwoCapturedInstances()
   {
      new NonStrictExpectations() {
         @Capturing(maxInstances = 1) Base b;

         {
            b.doSomething(); result = true; times = 1;
         }
      };

      assertTrue(new Derived1().doSomething());
      assertFalse(new Derived2().doSomething());
   }

   @Test
   public void verifyExpectationsOnlyOnOneOfTwoSubclassesForAnyNumberOfCapturedInstances()
   {
      new NonStrictExpectations() {
         @Capturing Derived1 anyCapture;

         {
            new Derived1(); minTimes = 1;
            anyCapture.doSomething(); result = true; times = 3;
         }
      };

      assertTrue(new Derived1().doSomething());
      assertFalse(new Derived2().doSomething());
      Derived1 d1b = new Derived1();
      assertTrue(d1b.doSomething());
      assertTrue(d1b.doSomething());
   }

   @Test
   public void verifyExpectationsOnlyOnOneOfTwoSubclassesForTwoCapturedInstances()
   {
      new NonStrictExpectations() {
         @Capturing(maxInstances = 1) Derived1 firstCapture;
         @Capturing(maxInstances = 1) Derived1 secondCapture;

         {
            new Derived1(); times = 2;
            firstCapture.doSomething(); result = true; times = 1;
            secondCapture.doSomething(); result = true; times = 1;
         }
      };

      assertTrue(new Derived1().doSomething());
      assertFalse(new Derived2().doSomething());
      assertTrue(new Derived1().doSomething());
   }
}