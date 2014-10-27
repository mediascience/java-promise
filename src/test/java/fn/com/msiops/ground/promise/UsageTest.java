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
    public void testAsyncBokenMap() {

        final Async<Integer> a = new Async<>();
        final Promise<?> p = a.promise().map(i -> 2 * i);

        final Exception expected = new Exception();

        a.fail(expected);

        checkBroken(p, expected);

    }

    @Test(expected = IllegalStateException.class)
    public void testAsyncBreakBrokenFails() {
        final Async<Object> a = new Async<>();
        a.succeed(new RuntimeException());
        a.succeed(new RuntimeException());
    }

    @Test(expected = IllegalStateException.class)
    public void testAsyncBreakFulfilledFails() {
        final Async<Object> a = new Async<>();
        a.succeed(25);
        a.succeed(new RuntimeException());
    }

    @Test
    public void testAsyncBroken() {

        final Async<?> a = new Async<Object>();
        final Promise<?> p = a.promise();

        final Exception expected = new Exception();
        a.fail(expected);

        checkBroken(p, expected);

    }

    @Test
    public void testAsyncBrokenDefer() {

        final Async<Integer> a = new Async<>();
        final Promise<?> p = a.promise().defer(() -> Promise.of("HI"));

        checkNotComplete(p);

        a.fail(new Exception());

        checkFulfilled(p, "HI");
    }

    @Test
    public void testAsyncBrokenDeferBroken() {

        final Async<Integer> outer = new Async<>();
        final Async<String> inner = new Async<>();

        final Promise<?> p = outer.promise().defer(() -> inner.promise());

        outer.fail(new Error());

        checkNotComplete(p);

        final Exception expected = new Exception();
        inner.fail(expected);

        checkBroken(p, expected);

    }

    @Test
    public void testAsyncBrokenDeferFulfilled() {

        final Async<Integer> outer = new Async<>();
        final Async<String> inner = new Async<>();

        final Promise<?> p = outer.promise().defer(() -> inner.promise());

        outer.fail(new Exception());

        checkNotComplete(p);

        inner.succeed("Hi.");

        checkFulfilled(p, "Hi.");

    }

    @Test
    public void testAsyncBrokenFlatMap() {

        final Async<Integer> a = new Async<>();
        final Promise<Integer> p = a.promise().flatMap(i -> Promise.of(2 * i));

        final Exception expected = new Exception();
        a.fail(expected);

        checkBroken(p, expected);

    }

    @Test
    public void testAsyncBrokenRecoverBroken() {

        final Async<Integer> a = new Async<>();
        final Async<String> inner = new Async<>();
        final Promise<?> p = a.promise().recover(Throwable.class,
                x -> inner.promise());

        a.fail(new Exception());

        checkNotComplete(p);

        final Exception expected = new Exception();
        inner.fail(expected);

        checkBroken(p, expected);
    }

    @Test
    public void testAsyncBrokenRecoverFulfilled() {

        final Async<Integer> a = new Async<>();
        final Async<String> inner = new Async<>();
        final Promise<?> p = a.promise().recover(Throwable.class,
                x -> inner.promise());

        a.fail(new Exception());

        checkNotComplete(p);

        inner.succeed("Hi.");

        checkFulfilled(p, "Hi.");
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

        assertNull(emitted1.get());
        assertNull(emitted2.get());

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
    public void testAsyncFlatMapFulfilledBroken() {

        final Async<Integer> aouter = new Async<>();
        final Async<String> ainner = new Async<>();

        final Promise<?> p = aouter.promise().flatMap(i -> ainner.promise());

        final Exception expected = new Exception();

        aouter.succeed(7);
        ainner.fail(expected);

        checkBroken(p, expected);

    }

    @Test
    public void testAsyncFlatMapFulfilledFulfilled() {

        final Async<Integer> aouter = new Async<>();
        final Async<String> ainner = new Async<>();

        final Promise<?> p = aouter.promise().flatMap(i -> ainner.promise());

        aouter.succeed(7);
        ainner.succeed("hi");

        checkFulfilled(p, "hi");
    }

    @Test(expected = IllegalStateException.class)
    public void testAsyncFulfillBrokenFails() {
        final Async<Object> a = new Async<>();
        a.succeed(new RuntimeException());
        a.succeed(25);
    }

    @Test
    public void testAsyncFulfilled() {

        final Async<Integer> a = new Async<>();
        final Promise<Integer> p = a.promise();

        a.succeed(12);

        checkFulfilled(p, 12);

    }

    @Test
    public void testAsyncFulfilledDefer() {

        final Async<Integer> a = new Async<>();
        final Promise<?> p = a.promise().defer(() -> Promise.of("HI"));

        checkNotComplete(p);

        a.succeed(12);

        checkFulfilled(p, "HI");

    }

    @Test
    public void testAsyncFulfilledDeferBroken() {

        final Async<Integer> outer = new Async<>();
        final Async<String> inner = new Async<>();

        final Promise<?> p = outer.promise().defer(() -> inner.promise());

        outer.succeed(12);

        checkNotComplete(p);

        final Exception expected = new Exception();
        inner.fail(expected);

        checkBroken(p, expected);

    }

    @Test
    public void testAsyncFulfilledDeferFulfilled() {

        final Async<Integer> outer = new Async<>();
        final Async<String> inner = new Async<>();

        final Promise<?> p = outer.promise().defer(() -> inner.promise());

        outer.succeed(12);

        checkNotComplete(p);

        inner.succeed("Hi.");

        checkFulfilled(p, "Hi.");

    }

    @Test
    public void testAsyncFulfilledFlatMap() {

        final Async<Integer> a = new Async<>();
        final Promise<Integer> p = a.promise().flatMap(i -> Promise.of(2 * i));

        a.succeed(12);

        checkFulfilled(p, 24);

    }

    @Test(expected = IllegalStateException.class)
    public void testAsyncFulfillFulfilledFails() {

        final Async<Object> a = new Async<>();
        a.succeed(15);
        a.succeed(25);

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
    public void testAsyncMap() {

        final Async<Integer> a = new Async<>();
        final Promise<Integer> p = a.promise();

        final Promise<?> m = p.map(i -> 2 * i).map(String::valueOf);

        final AtomicReference<Object> actual = new AtomicReference<>();
        m.forEach(o -> actual.set(o));

        assertNull(actual.get());

        a.succeed(12);

        assertEquals("24", actual.get());

    }

    @Test
    public void testDegenerateBrokenDefer() {

        final Promise<?> p = Promise.broken(new Exception()).defer(
                () -> Promise.of("HI"));

        checkFulfilled(p, "HI");
    }

    @Test
    public void testDegenerateBrokenFlatMap() {

        final Exception expected = new Exception();
        final Promise<Integer> p = Promise.<Integer> broken(expected).flatMap(
                i -> Promise.of(2 * i));

        checkBroken(p, expected);

    }

    @Test
    public void testDegenerateBrokenMap() {

        final Exception expected = new Exception();
        final Promise<?> p = Promise.<Integer> broken(expected).map(i -> 2 * i)
                .map(String::valueOf);

        checkBroken(p, expected);

    }

    @Test(expected = NullPointerException.class)
    public void testDegenerateBrokenNullInvalid() {

        Promise.broken(null);

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
    public void testDegenerateFlatMapFulfilledBroken() {

        final Async<String> ainner = new Async<>();

        final Promise<?> p = Promise.of(12).flatMap(i -> ainner.promise());

        final Exception expected = new Exception();

        ainner.fail(expected);

        checkBroken(p, expected);

    }

    @Test
    public void testDegenerateFlatMapFulfilledFulfilled() {

        final Async<String> ainner = new Async<>();

        final Promise<?> p = Promise.of(12).flatMap(i -> ainner.promise());

        ainner.succeed("hi");

        checkFulfilled(p, "hi");
    }

    @Test
    public void testDegenerateFulfilledDefer() {

        final Promise<?> p = Promise.of(12).defer(() -> Promise.of("HI"));

        checkFulfilled(p, "HI");

    }

    @Test
    public void testDegenerateFulfilledFlatMap() {

        final Promise<Integer> p = Promise.of(12);

        final Promise<Integer> m = p.flatMap(i -> Promise.of(2 * i));

        checkFulfilled(m, 24);

    }

    @Test
    public void testDegenerateFulfilledMap() {

        final Promise<Integer> p = Promise.of(12);

        final Promise<?> m = p.map(i -> 2 * i).map(String::valueOf);

        checkFulfilled(m, "24");

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

    @Test
    public void testDegenerateRecoverBroken() {

        final Exception expected = new RuntimeException();
        final Promise<?> p = Promise.broken(new Exception()).recover(
                Throwable.class, ix -> Promise.broken(expected));

        checkBroken(p, expected);

    }

    @Test
    public void testDegenerateRecoverFulfilled() {

        final Promise<?> p = Promise.broken(new Exception()).recover(
                Throwable.class, ix -> Promise.of(12));

        checkFulfilled(p, 12);

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

    private void checkNotComplete(final Promise<?> p) {

        final AtomicBoolean set = new AtomicBoolean();

        p.forEach(o -> {
            set.set(true);
        });

        assertFalse(set.get());

    }

}
