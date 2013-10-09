/*
 * Copyright (c) 2006-2013 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package org.jdesktop.animation.transitions;

import java.awt.*;
import java.awt.image.*;
import javax.swing.*;

import org.jdesktop.animation.timing.*;

import org.junit.*;
import static org.junit.Assert.*;

import mockit.*;

public final class ScreenTransitionExecutionTest
{
   @Tested ScreenTransition transition;

   @Injectable JComponent container;
   @Injectable TransitionTarget target;
   @Injectable Animator animator;

   @Mocked AnimationManager manager;
   @Mocked AnimationLayer animationLayer;
   @Mocked("()") @Capturing(maxInstances = 1) TimingTarget timingTarget;

   @Before
   public void setUp()
   {
      // The ScreenTransition constructor calls animator.addTarget(...), so we specify
      // the desired behavior for that here (otherwise, the real method would run).
      new NonStrictExpectations() {{ animator.addTarget((TimingTarget) any); }};
   }

   @Test
   public void endTransition()
   {
      new NonStrictExpectations() {
         @Mocked JRootPane rootPane;
         @Mocked Component savedGlassPane;

         {
            container.getRootPane(); result = rootPane;
            setField(transition, savedGlassPane);
         }
      };

      timingTarget.end();

      new Verifications() {{
         animationLayer.setVisible(false);
         container.setVisible(true);
         manager.reset(animator);
      }};
   }

   @Test
   public void beginTransition(final Graphics2D g2D)
   {
      new NonStrictExpectations() {
         final int width = 200;
         final int height = 150;
         @Mocked JRootPane rootPane;
         @Mocked BufferedImage transitionImage;

         {
            container.getWidth(); result = width;
            container.getHeight(); result = height;

            container.createImage(width, height); result = transitionImage;

            container.getRootPane(); result = rootPane;
            transitionImage.getGraphics(); result = g2D;
         }
      };

      timingTarget.begin();
      assertNotNull(transition.getTransitionImage());

      new VerificationsInOrder() {{
         manager.setupStart();
         animationLayer.setupBackground(container);
         target.setupNextScreen();
         manager.setupEnd();
         manager.init(animator);
         manager.paint(g2D);
      }};
   }
}
