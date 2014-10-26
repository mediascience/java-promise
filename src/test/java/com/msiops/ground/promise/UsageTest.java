/**
 * Licensed to Media Science International (MSI) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. MSI
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.msiops.ground.promise;

import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

public class UsageTest {

    @Test
    public void testDegenerateBroken() {

        final Exception expected = new Exception();

        final Promise<Integer> p = Promise.broken(expected);

        final AtomicReference<Object> actual = new AtomicReference<>();
        p.on(Exception.class, x -> {
            actual.set(x);
        });
        assertEquals(expected, actual.get());

    }

    @Test
    public void testDegenerateBrokenDoesNotEmitValue() {

        final Promise<?> p = Promise.broken(new Exception());

        final AtomicBoolean actual = new AtomicBoolean();
        p.forEach(o -> {
            actual.set(true);
        });
        assertFalse(actual.get());

    }

    @Test
    public void testDegenerateBrokenSelectException() {

        final Exception expected = new Exception();
        final Promise<Integer> p = Promise.broken(expected);

        final AtomicReference<Object> e = new AtomicReference<>();
        final AtomicReference<Object> rte = new AtomicReference<>();

        p.on(Exception.class, x -> {
            e.set(x);
        });

        p.on(RuntimeException.class, x -> {
            rte.set(x);
        });

        assertEquals(expected, e.get());
        assertNull(rte.get());

    }

    @Test
    public void testDegenerateBrokenThrowsHandlerExceptions() {

        final Promise<Integer> p = Promise.broken(new Exception());

        try {
            p.on(Exception.class, x -> {
                throw new RuntimeException();
            });
            fail("should throw");
        } catch (final RuntimeException x) {
            // OK
        }
    }

    @Test
    public void testDegenerateFulfilled() {

        final Promise<Integer> p = Promise.of(12);

        final AtomicInteger actual = new AtomicInteger();
        p.forEach(i -> {
            actual.set(i);
        });
        assertEquals(12, actual.get());

    }

    @Test
    public void testDegenerateFulfilledDoesNotEmitError() {

        final Promise<?> p = Promise.of(12);

        final AtomicBoolean actual = new AtomicBoolean();
        p.on(Throwable.class, x -> {
            actual.set(true);
        });

        assertFalse(actual.get());

    }

    @Test
    public void testDegenerateFulfilledThrowsHandlerExceptions() {

        final Promise<Integer> p = Promise.of(12);

        try {
            p.forEach(i -> {
                throw new RuntimeException();
            });
            fail("should throw");
        } catch (final RuntimeException x) {
            // OK
        }

    }

}
