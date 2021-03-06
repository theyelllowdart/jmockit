/*
 * Copyright (c) 2006-2013 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import static org.junit.Assert.*;
import org.junit.*;

public final class FinalMockFieldsTest
{
   static final class Collaborator
   {
      Collaborator() {}
      Collaborator(boolean b) { if (!b) throw new IllegalArgumentException(); }
      int getValue() { return -1; }
      void doSomething() {}
   }

   static final class AnotherCollaborator
   {
      int getValue() { return -1; }
      void doSomething() {}
   }

   @Injectable final Collaborator mock = new Collaborator();
   @Mocked final AnotherCollaborator mock2 = new AnotherCollaborator();

   @Test
   public void recordExpectationsOnInjectableFinalMockField()
   {
      new Expectations() {{
         mock.getValue(); result = 12;
         mock.doSomething(); times = 0;
      }};

      assertEquals(12, mock.getValue());
   }

   @Test
   public void recordNonStrictExpectationsOnFinalMockField()
   {
      AnotherCollaborator collaborator = new AnotherCollaborator();

      new NonStrictExpectations() {{
         mock2.doSomething(); times = 1;
      }};

      collaborator.doSomething();
      assertEquals(0, collaborator.getValue());
   }

   @Test
   public void recordExpectationsOnInjectableFinalLocalMockField()
   {
      final Collaborator[] collaborators = new Collaborator[1];

      new NonStrictExpectations() {
         @Injectable final Collaborator mock3 = new Collaborator();

         {
            collaborators[0] = mock3;
            mock3.doSomething(); times = 1;
         }
      };

      collaborators[0].doSomething();
   }

   static final class YetAnotherCollaborator
   {
      YetAnotherCollaborator() {}
      YetAnotherCollaborator(boolean b) { if (!b) throw new IllegalArgumentException(); }
      int getValue() { return -1; }
      void doSomething() {}
      static int doSomethingStatic() { return -2; }
   }

   @Test
   public void recordExpectationsOnNonStrictFinalLocalMockField()
   {
      YetAnotherCollaborator collaborator = new YetAnotherCollaborator();

      new NonStrictExpectations() {
         @Mocked final YetAnotherCollaborator mock3 = new YetAnotherCollaborator();

         {
            mock3.doSomething(); times = 1;
         }
      };

      collaborator.doSomething();
      assertEquals(0, collaborator.getValue());
   }

   @Mocked final ProcessBuilder mockProcessBuilder = null;

   @Test
   public void recordExpectationsOnConstructorOfFinalMockField() throws Exception
   {
      new NonStrictExpectations() {{
         new ProcessBuilder("test"); times = 1;
      }};

      assertNull(new ProcessBuilder("test").start());
   }

   @Test
   public void recordExpectationsOnStaticMethodAndConstructorOfFinalLocalMockField()
   {
      new Expectations() {
         @Mocked final YetAnotherCollaborator unused = null;

         {
            new YetAnotherCollaborator(true); result = new RuntimeException();
            YetAnotherCollaborator.doSomethingStatic(); result = 123;
         }
      };

      try {
         new YetAnotherCollaborator(true);
         fail();
      }
      catch (RuntimeException ignore) {}

      assertEquals(123, YetAnotherCollaborator.doSomethingStatic());
   }
}
