/*
 * Copyright 2023 The Fury Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.fury.builder;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import io.fury.TestUtils;
import io.fury.test.bean.Foo;
import io.fury.test.bean.Struct;
import io.fury.type.Descriptor;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.Data;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AccessorHelperTest {

  @Data
  public static class A {
    protected String f1;
    String f2;
  }

  @Test
  public void genCode() {
    AccessorHelper.genCode(A.class);
  }

  @Test
  public void defineAccessorClass() throws Exception {
    assertTrue(AccessorHelper.defineAccessorClass(A.class));
    Class<?> accessorClass = AccessorHelper.getAccessorClass(A.class);
    assertEquals(accessorClass.getClassLoader(), A.class.getClassLoader());
    A a = new A();
    a.f1 = "str";
    a.f2 = "str";
    Method f1 = accessorClass.getDeclaredMethod("f1", A.class);
    Method f2 = accessorClass.getDeclaredMethod("f2", A.class);
    assertEquals(f1.invoke(null, a), a.f1);
    assertEquals(f2.invoke(null, a), a.f2);
    assertTrue(AccessorHelper.defineAccessor(A.class.getDeclaredField("f1")));
    assertTrue(AccessorHelper.defineAccessor(A.class.getDeclaredMethod("getF1")));
    assertSame(AccessorHelper.getAccessorClass(A.class), accessorClass);
  }

  @Test
  public void defineAccessorClassInDefaultPackage() {
    Class<?> testAccessorClass = Struct.createStructClass("TestAccessorDefaultPackage", 1);
    assertTrue(AccessorHelper.defineAccessorClass(testAccessorClass));
    Class<?> cls = AccessorHelper.getAccessorClass(testAccessorClass);
    assertEquals(cls.getClassLoader(), testAccessorClass.getClassLoader());
  }

  @Test(timeOut = 60000)
  public void testAccessorClassGC() {
    WeakReference<Class<?>> accessorClassRef = generateAccessorForGC();
    Descriptor.clearDescriptorCache();
    TestUtils.triggerOOMForSoftGC(
        () -> {
          System.out.printf("Wait cls %s gc.\n", accessorClassRef.get());
          return accessorClassRef.get() != null;
        });
    Assert.assertNull(accessorClassRef.get());
  }

  private WeakReference<Class<?>> generateAccessorForGC() {
    Class<?> testAccessorClass = Struct.createStructClass("TestAccessor", 1);
    assertTrue(AccessorHelper.defineAccessorClass(testAccessorClass));
    Class<?> cls = AccessorHelper.getAccessorClass(testAccessorClass);
    assertEquals(cls.getClassLoader(), testAccessorClass.getClassLoader());
    return new WeakReference<>(cls);
  }

  @Test
  public void defineAccessorClassConcurrent() throws InterruptedException {
    ExecutorService executorService = Executors.newFixedThreadPool(10);
    AtomicBoolean hasException = new AtomicBoolean(false);
    for (int i = 0; i < 1000; i++) {
      executorService.execute(
          () -> {
            try {
              assertTrue(AccessorHelper.defineAccessorClass(A.class));
              assertTrue(AccessorHelper.defineAccessorClass(Foo.class));
            } catch (Exception e) {
              hasException.set(true);
            }
          });
    }
    executorService.shutdown();
    assertTrue(executorService.awaitTermination(30, TimeUnit.SECONDS));
    assertFalse(hasException.get());
  }
}
