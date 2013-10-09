/*
 * Copyright (c) 2006-2013 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.invocation;

import java.util.*;

import mockit.external.asm4.*;
import mockit.internal.*;
import mockit.internal.expectations.argumentMatching.*;
import mockit.internal.state.*;
import mockit.internal.util.*;

public final class InvocationArguments
{
   final String classDesc;
   final String methodNameAndDesc;
   final String genericSignature;
   final String[] exceptions;
   private final ArgumentValuesAndMatchers valuesAndMatchers;
   private RealMethod realMethod;

   InvocationArguments(
      int access, String classDesc, String methodNameAndDesc, String genericSignature, String exceptions, Object[] args)
   {
      this.classDesc = classDesc;
      this.methodNameAndDesc = methodNameAndDesc;
      this.genericSignature = genericSignature;
      this.exceptions = exceptions == null ? null : exceptions.split(" ");
      valuesAndMatchers =
         (access & Opcodes.ACC_VARARGS) == 0 ?
            new ArgumentValuesAndMatchersWithoutVarargs(this, args) :
            new ArgumentValuesAndMatchersWithVarargs(this, args);
   }

   String getClassName() { return classDesc.replace('/', '.'); }

   boolean isForConstructor() { return methodNameAndDesc.charAt(0) == '<'; }

   public Object[] getValues() { return valuesAndMatchers.values; }
   void setValues(Object[] values) { valuesAndMatchers.values = values; }

   public void setValuesWithNoMatchers(Object[] argsToVerify)
   {
      valuesAndMatchers.setValuesWithNoMatchers(argsToVerify);
   }

   public List<ArgumentMatcher> getMatchers() { return valuesAndMatchers.matchers; }
   public void setMatchers(List<ArgumentMatcher> matchers) { valuesAndMatchers.matchers = matchers; }

   public Object[] prepareForVerification(Object[] argsToVerify, List<ArgumentMatcher> matchers)
   {
      return valuesAndMatchers.prepareForVerification(argsToVerify, matchers);
   }

   public boolean isMatch(Object[] replayArgs, Map<Object, Object> instanceMap)
   {
      TestRun.enterNoMockingZone();

      try {
         return valuesAndMatchers.isMatch(replayArgs, instanceMap);
      }
      finally {
         TestRun.exitNoMockingZone();
      }
   }

   public Error assertMatch(Object[] replayArgs, Map<Object, Object> instanceMap, CharSequence errorMessagePrefix)
   {
      return valuesAndMatchers.assertMatch(replayArgs, instanceMap, errorMessagePrefix);
   }

   Error argumentMismatchMessage(int paramIndex, Object expected, Object actual, CharSequence errorMessagePrefix)
   {
      ArgumentMismatch message = new ArgumentMismatch();

      if (errorMessagePrefix != null) {
         message.append(errorMessagePrefix);
         message.append('\n');
      }

      message.append("Parameter ");

      String parameterName = ParameterNames.getName(classDesc, methodNameAndDesc, paramIndex);

      if (parameterName == null) {
         message.append(paramIndex);
      }
      else {
         message.appendFormatted(parameterName);
      }

      message.append(" of ").append(new MethodFormatter(classDesc, methodNameAndDesc).toString());
      message.append(" expected ").appendFormatted(expected);

      if (!message.isFinished()) {
         message.append(", got ").appendFormatted(actual);
      }

      return new UnexpectedInvocation(message.toString());
   }

   @Override
   public String toString()
   {
      MethodFormatter methodFormatter = new MethodFormatter(classDesc, methodNameAndDesc);
      return valuesAndMatchers.toString(methodFormatter);
   }

   public boolean hasEquivalentMatchers(InvocationArguments other)
   {
      return valuesAndMatchers.hasEquivalentMatchers(other.valuesAndMatchers);
   }

   RealMethod getRealMethod()
   {
      if (realMethod == null) {
         realMethod = new RealMethod(getClassName(), methodNameAndDesc);
      }

      return realMethod;
   }
}
