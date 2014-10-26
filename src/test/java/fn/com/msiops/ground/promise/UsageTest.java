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
package fn.com.msiops.ground.promise;

import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

import com.msiops.ground.promise.Async;
import com.msiops.ground.promise.Promise;

public class UsageTest {

    @Test
    public void testAsyncBroken() {

        final Async<?> a = new Async<Object>();
        final Promise<?> p = a.promise();

        final Exception expected = new Exception();
        a.fail(expected);

        checkBroken(p, expected);

    }

    @Test
    public void testAsyncDeferredError() {

        final Async<Object> a = new Async<>();

        final AtomicReference<Object> emitted = new AtomicReference<>();

        a.promise().on(Throwable.class, x -> {
            emitted.set(x);
        });

        final Exception expected = new Exception();
        a.fail(expected);

        assertEquals(expected, emitted.get());

    }

    @Test
    public void testAsyncDeferredErrorDoesNotEmitValue() {

        final Async<Object> a = new Async<>();

        final AtomicBoolean emitted = new AtomicBoolean();

        a.promise().forEach(o -> {
            emitted.set(true);
        });

        final Exception expected = new Exception();
        a.fail(expected);

        assertFalse(emitted.get());

    }

    @Test
    public void testAsyncDeferredErrorMulti() {

        final Async<Object> a = new Async<>();

        final AtomicReference<Object> emitted1 = new AtomicReference<>();
        final AtomicReference<Object> emitted2 = new AtomicReference<>();

        a.promise().on(Throwable.class, x -> {
            emitted1.set(x);
        });
        a.promise().on(Throwable.class, x -> {
            emitted2.set(x);
        });

        final Exception expected = new Exception();
        a.fail(expected);

        assertEquals(expected, emitted1.get());
        assertEquals(expected, emitted2.get());

    }

    @Test
    public void testAsyncDeferredValue() {

        final Async<Object> a = new Async<>();

        final AtomicReference<Object> emitted = new AtomicReference<>();

        a.promise().forEach(o -> {
            emitted.set(o);
        });

        final Object expected = new Object();
        a.succeed(expected);

        assertEquals(expected, emitted.get());

    }

    @Test
    public void testAsyncDeferredValueDoesNotEmitError() {

        final Async<Object> a = new Async<>();

        final AtomicBoolean emitted = new AtomicBoolean();

        a.promise().on(Throwable.class, x -> {
            emitted.set(true);
        });

        final Object expected = new Object();
        a.succeed(expected);

        assertFalse(emitted.get());

    }

    @Test
    public void testAsyncDeferredValueMulti() {

        final Async<Object> a = new Async<>();

        final AtomicReference<Object> emitted1 = new AtomicReference<>();
        final AtomicReference<Object> emitted2 = new AtomicReference<>();

        a.promise().forEach(o -> {
            emitted1.set(o);
        });
        a.promise().forEach(o -> {
            emitted2.set(o);
        });

        assertNull(emitted1.get());
        assertNull(emitted2.get());

        final Object expected = new Object();
        a.succeed(expected);

        assertEquals(expected, emitted1.get());
        assertEquals(expected, emitted2.get());

    }

    @Test(expected = NullPointerException.class)
    public void testAsyncFailNullIllegal() {

        new Async<Object>().fail(null);

    }

    @Test
    public void testAsyncFulfilled() {

        final Async<Integer> a = new Async<>();
        final Promise<Integer> p = a.promise();

        a.succeed(12);

        checkFulfilled(p, 12);

    }

    @Test
    public void testAsyncIncompleteDoesNotEmitErrorImmediately() {

        final Async<Object> a = new Async<>();

        final AtomicBoolean emitted = new AtomicBoolean();

        a.promise().on(Throwable.class, x -> {
            emitted.set(true);
        });

        assertFalse(emitted.get());

    }

    @Test
    public void testAsyncIncompleteDoesNotEmitValueImmediately() {

        final Async<Object> a = new Async<>();

        final AtomicBoolean emitted = new AtomicBoolean();

        a.promise().forEach(o -> {
            emitted.set(true);
        });

        assertFalse(emitted.get());

    }

    @Test
    public void testDegenerateBroken() {

        final Exception expected = new Exception();

        final Promise<Integer> p = Promise.broken(expected);

        checkBroken(p, expected);
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

    @Test(expected = NullPointerException.class)
    public void testDegenerateBrokenNullInvalid() {

        Promise.broken(null);

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

        checkFulfilled(p, 12);

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
    public void testDegenerateFulfilledNull() {

        final Promise<?> p = Promise.of(null);

        checkFulfilled(p, null);

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

    private void checkBroken(final Promise<?> p, final Throwable expected) {

        final AtomicReference<Object> actual = new AtomicReference<>();

        p.on(Throwable.class, x -> {
            actual.set(x);
        });

        assertEquals(expected, actual.get());

    }

    private void checkFulfilled(final Promise<?> p, final Object expected) {

        final AtomicReference<Object> actual = new AtomicReference<>();
        /*
         * needed because fulfilled value may be null so we need to distinguish
         * between fulfilled null and not being called at all.
         */
        final AtomicBoolean set = new AtomicBoolean();

        p.forEach(o -> {
            actual.set(o);
            set.set(true);
        });

        assertTrue(set.get());
        assertEquals(expected, actual.get());

    }

}
