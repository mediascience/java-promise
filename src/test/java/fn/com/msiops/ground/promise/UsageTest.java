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

    @Test(expected = NullPointerException.class)
    public void testAsyncFailNullIllegal() {

        Promises.async().fail(null);

    }

    @Test(expected = IllegalStateException.class)
    public void testAsyncFulfillBrokenFails() {
        final Async<Object> a = Promises.async();
        a.succeed(new RuntimeException());
        a.succeed(25);
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

    @Test(expected = NullPointerException.class)
    public void testDegenerateBrokenNullInvalid() {

        Promises.broken(null);

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
}
