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

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import org.junit.Test;

import com.msiops.ground.either.Either;
import com.msiops.ground.promise.Async;
import com.msiops.ground.promise.Promise;
import com.msiops.ground.promise.Promises;

public class UsageTest {

    @Test(expected = IllegalStateException.class)
    public void testAsyncBreakBrokenFails() {
        final Async<Object> a = Promises.async();
        a.succeed(new RuntimeException());
        a.succeed(new RuntimeException());
    }

    @Test(expected = IllegalStateException.class)
    public void testAsyncBreakFulfilledFails() {
        final Async<Object> a = Promises.async();
        a.succeed(25);
        a.succeed(new RuntimeException());
    }

    @Test
    public void testAsyncBrokenFromEither() {

        final Async<Integer> a = Promises.async();
        final AtomicReference<Object> actual = new AtomicReference<>();
        a.promise().on(Throwable.class, actual::set);

        assertNull(actual.get());

        final Exception x = new RuntimeException();
        a.complete(Either.right(x));

        assertEquals(x, actual.get());
    }

    @Test(expected = NullPointerException.class)
    public void testAsyncFailNullIllegal() {

        Promises.async().fail(null);

    }

    @Test(expected = NullPointerException.class)
    public void testAsyncFromNullEitherIllegal() {

        Promises.async().complete(null);

    }

    @Test(expected = IllegalStateException.class)
    public void testAsyncFulfillBrokenFails() {
        final Async<Object> a = Promises.async();
        a.succeed(new RuntimeException());
        a.succeed(25);
    }

    @Test
    public void testAsyncFulfilledFromEither() {

        final Async<Integer> a = Promises.async();
        final AtomicReference<Object> actual = new AtomicReference<>();
        a.promise().forEach(actual::set);

        assertNull(actual.get());

        a.complete(Either.left(12));

        assertEquals(12, actual.get());
    }

    @Test(expected = IllegalStateException.class)
    public void testAsyncFulfillFulfilledFails() {

        final Async<Object> a = Promises.async();
        a.succeed(15);
        a.succeed(25);

    }

    @Test(expected = NullPointerException.class)
    public void testAsyncSucceedNullIllegal() {

        Promises.async().succeed(null);

    }

    @Test
    public void testDegenerateBrokenFromEither() {

        final Exception x = new RuntimeException();

        final AtomicReference<Object> actual = new AtomicReference<>();
        Promises.of(Either.right(x)).on(Throwable.class, actual::set);

        assertEquals(x, actual.get());
    }

    @Test(expected = NullPointerException.class)
    public void testDegenerateBrokenNullInvalid() {

        Promises.broken(null);

    }

    @Test(expected = NullPointerException.class)
    public void testDegenerateFromNullEitherIllegal() {

        Promises.of(null);

    }

    @Test
    public void testDegenerateFulfilledFromEither() {

        final AtomicReference<Object> actual = new AtomicReference<>();
        Promises.of(Either.left(12)).forEach(actual::set);

        assertEquals(12, actual.get());
    }

    @Test(expected = NullPointerException.class)
    public void testDegenerateFulfilledNullInvalid() {

        Promises.fulfilled(null);

    }

    @Test
    public void testLiftedOnAsyncBroken() {

        final Function<Promise<Integer>, Promise<String>> lifted = Promises
                .lift(i -> String.valueOf(i * 2));

        final Async<Integer> a = Promises.async();

        final Exception x = new RuntimeException();
        final AtomicReference<Object> actual = new AtomicReference<>();
        lifted.apply(a.promise()).on(Throwable.class, actual::set);

        assertNull(actual.get());

        a.fail(x);

        assertEquals(x, actual.get());

    }

    @Test
    public void testLiftedOnAsyncFulfilled() {

        final Function<Promise<Integer>, Promise<String>> lifted = Promises
                .lift(i -> String.valueOf(i * 2));

        final Async<Integer> a = Promises.async();

        final AtomicReference<String> actual = new AtomicReference<>();
        lifted.apply(a.promise()).forEach(actual::set);

        assertNull(actual.get());

        a.succeed(12);

        assertEquals("24", actual.get());

    }

    @Test
    public void testLiftedOnDegenerateBroken() {

        final Function<Promise<Integer>, Promise<String>> lifted = Promises
                .lift(i -> String.valueOf(i * 2));

        final Exception x = new RuntimeException();
        final AtomicReference<Object> actual = new AtomicReference<>();
        lifted.apply(Promises.broken(x)).on(Throwable.class, actual::set);

        assertEquals(x, actual.get());

    }

    @Test
    public void testLiftedOnDegenerateFulfilled() {

        final Function<Promise<Integer>, Promise<String>> lifted = Promises
                .lift(i -> String.valueOf(i * 2));

        final AtomicReference<String> actual = new AtomicReference<>();
        lifted.apply(Promises.fulfilled(12)).forEach(actual::set);

        assertEquals("24", actual.get());

    }

    @Test
    public void testWaitForDegenerateBroken() {

        final Exception x = new RuntimeException();
        final Promise<Integer> p1 = Promises.broken(x);
        final Promise<Integer> p2 = Promises.broken(new Exception());
        final Promise<String> p3 = Promises.fulfilled("3");

        final AtomicReference<Object> actual = new AtomicReference<>();
        Promises.waitFor(p1, p2, p3).on(Throwable.class, actual::set);

        assertEquals(x, actual.get());

    }

    @Test
    public void testWaitForDegenerateFulfilled() {

        final Promise<Integer> p1 = Promises.fulfilled(1);
        final Promise<Integer> p2 = Promises.broken(new RuntimeException());
        final Promise<String> p3 = Promises.fulfilled("3");

        final AtomicReference<Object> actual = new AtomicReference<>();
        Promises.waitFor(p1, p2, p3).forEach(actual::set);

        assertEquals(1, actual.get());

    }

}
