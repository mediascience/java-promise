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

import org.junit.Test;

import com.msiops.footing.tuple.Tuple;
import com.msiops.ground.promise.Async;
import com.msiops.ground.promise.Promise;
import com.msiops.ground.promise.Promises;

public class WaitForTest {

    @Test
    public void testWaitFor2AsyncFulfilled() {

        final Async<Integer> a1 = Promises.async();
        final Async<Integer> a2 = Promises.async();

        final AtomicReference<Object> actual = new AtomicReference<>();
        Promises.waitFor(a1.promise(), a2.promise()).forEach(actual::set);

        a2.succeed(2);
        assertNull(actual.get());

        a1.succeed(1);
        assertEquals(Tuple.of(1, 2), actual.get());
    }

    @Test
    public void testWaitFor2AsyncMultipleBroken() {

        final AtomicReference<Object> actual = new AtomicReference<>();

        final Exception x = new RuntimeException();
        final Async<Integer> a1 = Promises.async();
        final Async<Integer> a2 = Promises.async();

        Promises.waitFor(a1.promise(), a2.promise()).on(Throwable.class,
                actual::set);

        /*
         * the leftmost promise to break becomes the error in the resulting
         * promise
         */
        assertNull(actual.get());

        /*
         * break second promise, downstream is still incomplete because first is
         * still unknown
         */
        a2.fail(new Exception());
        assertNull(actual.get());

        /*
         * break first promise. The downstream can be broken now.
         */
        a1.fail(x);
        assertEquals(x, actual.get());

    }

    @Test
    public void testWaitFor2AsyncSingleBroken() {

        final Exception x = new RuntimeException();
        final Promise<Integer> p1 = Promises.fulfilled(1);
        final Async<Integer> a2 = Promises.async();

        final AtomicReference<Object> actual = new AtomicReference<>();
        Promises.waitFor(p1, a2.promise()).on(Throwable.class, actual::set);

        assertNull(actual.get());

        a2.fail(x);

        assertEquals(x, actual.get());

    }

    @Test
    public void testWaitFor2DegenerateFulfilled() {

        final Promise<Integer> p1 = Promises.fulfilled(1);
        final Promise<Integer> p2 = Promises.fulfilled(2);

        final AtomicReference<Object> actual = new AtomicReference<>();
        Promises.waitFor(p1, p2).forEach(actual::set);

        assertEquals(Tuple.of(1, 2), actual.get());
    }

    @Test
    public void testWaitFor2DegenerateMultipleBroken() {

        final Exception x = new RuntimeException();
        final Promise<Integer> p1 = Promises.broken(x);
        final Promise<Integer> p2 = Promises.broken(new Exception());

        final AtomicReference<Object> actual = new AtomicReference<>();
        Promises.waitFor(p1, p2).on(Throwable.class, actual::set);

        // leftmost prevails
        assertEquals(x, actual.get());

    }

    @Test
    public void testWaitFor3AsyncFulfilled() {

        final Async<Integer> a1 = Promises.async();
        final Async<Integer> a2 = Promises.async();
        final Async<String> a3 = Promises.async();

        final AtomicReference<Object> actual = new AtomicReference<>();
        Promises.waitFor(a1.promise(), a2.promise(), a3.promise()).forEach(
                actual::set);

        a3.succeed("3");
        assertNull(actual.get());

        a2.succeed(2);
        assertNull(actual.get());

        a1.succeed(1);
        assertEquals(Tuple.of(1, 2, "3"), actual.get());
    }

    @Test
    public void testWaitFor3AsyncMultipleBroken() {

        final AtomicReference<Object> actual = new AtomicReference<>();

        final Exception x = new RuntimeException();
        final Async<Integer> a1 = Promises.async();
        final Async<Integer> a2 = Promises.async();
        final Async<String> a3 = Promises.async();

        Promises.waitFor(a1.promise(), a2.promise(), a3.promise()).on(
                Throwable.class, actual::set);

        /*
         * the leftmost promise to break becomes the error in the resulting
         * promise
         */
        assertNull(actual.get());

        /*
         * break second promise, downstream is still incomplete because first is
         * still unknown
         */
        a2.fail(new Exception());
        assertNull(actual.get());

        /*
         * break first promise. Now the downstream can be broken without waiting
         * for the 3rd.
         */
        a1.fail(x);
        assertEquals(x, actual.get());

    }

    @Test
    public void testWaitFor3AsyncSingleBroken() {

        final Exception x = new RuntimeException();
        final Promise<Integer> p1 = Promises.fulfilled(1);
        final Async<Integer> a2 = Promises.async();
        final Promise<String> p3 = Promises.fulfilled("3");

        final AtomicReference<Object> actual = new AtomicReference<>();
        Promises.waitFor(p1, a2.promise(), p3).on(Throwable.class, actual::set);

        assertNull(actual.get());

        a2.fail(x);

        assertEquals(x, actual.get());

    }

    @Test
    public void testWaitFor3DegenerateFulfilled() {

        final Promise<Integer> p1 = Promises.fulfilled(1);
        final Promise<Integer> p2 = Promises.fulfilled(2);
        final Promise<String> p3 = Promises.fulfilled("3");

        final AtomicReference<Object> actual = new AtomicReference<>();
        Promises.waitFor(p1, p2, p3).forEach(actual::set);

        assertEquals(Tuple.of(1, 2, "3"), actual.get());
    }

    @Test
    public void testWaitFor3DegenerateMultipleBroken() {

        final Exception x = new RuntimeException();
        final Promise<Integer> p1 = Promises.broken(x);
        final Promise<Integer> p2 = Promises.broken(new Exception());
        final Promise<String> p3 = Promises.fulfilled("3");

        final AtomicReference<Object> actual = new AtomicReference<>();
        Promises.waitFor(p1, p2, p3).on(Throwable.class, actual::set);

        assertEquals(x, actual.get());

    }

    @Test
    public void testWaitFor4AsyncFulfilled() {

        final Async<Integer> a1 = Promises.async();
        final Async<Integer> a2 = Promises.async();
        final Async<String> a3 = Promises.async();
        final Async<String> a4 = Promises.async();

        final AtomicReference<Object> actual = new AtomicReference<>();
        Promises.waitFor(a1.promise(), a2.promise(), a3.promise(), a4.promise())
                .forEach(actual::set);

        a4.succeed("4");
        assertNull(actual.get());

        a3.succeed("3");
        assertNull(actual.get());

        a2.succeed(2);
        assertNull(actual.get());

        a1.succeed(1);
        assertEquals(Tuple.of(1, 2, "3", "4"), actual.get());
    }

    @Test
    public void testWaitFor4AsyncMultipleBroken() {

        final AtomicReference<Object> actual = new AtomicReference<>();

        final Exception x = new RuntimeException();
        final Async<Integer> a1 = Promises.async();
        final Async<Integer> a2 = Promises.async();
        final Async<String> a3 = Promises.async();
        final Async<String> a4 = Promises.async();

        Promises.waitFor(a1.promise(), a2.promise(), a3.promise(), a4.promise())
                .on(Throwable.class, actual::set);

        /*
         * the leftmost promise to break becomes the error in the resulting
         * promise
         */
        assertNull(actual.get());

        /*
         * break second promise, downstream is still incomplete because first is
         * still unknown
         */
        a2.fail(new Exception());
        assertNull(actual.get());

        /*
         * break first promise. Now the downstream can be broken without waiting
         * for any others.
         */
        a1.fail(x);
        assertEquals(x, actual.get());

    }

    @Test
    public void testWaitFor4AsyncSingleBroken() {

        final Exception x = new RuntimeException();
        final Promise<Integer> p1 = Promises.fulfilled(1);
        final Async<Integer> a2 = Promises.async();
        final Promise<String> p3 = Promises.fulfilled("3");
        final Promise<String> p4 = Promises.fulfilled("4");

        final AtomicReference<Object> actual = new AtomicReference<>();
        Promises.waitFor(p1, a2.promise(), p3, p4).on(Throwable.class,
                actual::set);

        assertNull(actual.get());

        a2.fail(x);

        assertEquals(x, actual.get());

    }

    @Test
    public void testWaitFor4DegenerateFulfilled() {

        final Promise<Integer> p1 = Promises.fulfilled(1);
        final Promise<Integer> p2 = Promises.fulfilled(2);
        final Promise<String> p3 = Promises.fulfilled("3");
        final Promise<String> p4 = Promises.fulfilled("4");

        final AtomicReference<Object> actual = new AtomicReference<>();
        Promises.waitFor(p1, p2, p3, p4).forEach(actual::set);

        assertEquals(Tuple.of(1, 2, "3", "4"), actual.get());
    }

    @Test
    public void testWaitFor4DegenerateMultipleBroken() {

        final Exception x = new RuntimeException();
        final Promise<Integer> p1 = Promises.broken(x);
        final Promise<Integer> p2 = Promises.broken(new Exception());
        final Promise<String> p3 = Promises.fulfilled("3");
        final Promise<String> p4 = Promises.fulfilled("4");

        final AtomicReference<Object> actual = new AtomicReference<>();
        Promises.waitFor(p1, p2, p3, p4).on(Throwable.class, actual::set);

        /*
         * leftmost broken promise prevails
         */
        assertEquals(x, actual.get());

    }

    @Test
    public void testWaitFor5AsyncFulfilled() {

        final Async<Integer> a1 = Promises.async();
        final Async<Integer> a2 = Promises.async();
        final Async<String> a3 = Promises.async();
        final Async<String> a4 = Promises.async();
        final Async<Integer> a5 = Promises.async();

        final AtomicReference<Object> actual = new AtomicReference<>();
        Promises.waitFor(a1.promise(), a2.promise(), a3.promise(),
                a4.promise(), a5.promise()).forEach(actual::set);

        a5.succeed(5);
        assertNull(actual.get());

        a4.succeed("4");
        assertNull(actual.get());

        a3.succeed("3");
        assertNull(actual.get());

        a2.succeed(2);
        assertNull(actual.get());

        a1.succeed(1);
        assertEquals(Tuple.of(1, 2, "3", "4", 5), actual.get());
    }

    @Test
    public void testWaitFor5AsyncMultipleBroken() {

        final AtomicReference<Object> actual = new AtomicReference<>();

        final Exception x = new RuntimeException();
        final Async<Integer> a1 = Promises.async();
        final Async<Integer> a2 = Promises.async();
        final Async<String> a3 = Promises.async();
        final Async<String> a4 = Promises.async();
        final Async<Integer> a5 = Promises.async();

        Promises.waitFor(a1.promise(), a2.promise(), a3.promise(),
                a4.promise(), a5.promise()).on(Throwable.class, actual::set);

        /*
         * the leftmost promise to break becomes the error in the resulting
         * promise
         */
        assertNull(actual.get());

        /*
         * break second promise, downstream is still incomplete because first is
         * still unknown
         */
        a2.fail(new Exception());
        assertNull(actual.get());

        /*
         * break first promise. Now the downstream can be broken without waiting
         * for any others.
         */
        a1.fail(x);
        assertEquals(x, actual.get());

    }

    @Test
    public void testWaitFor5AsyncSingleBroken() {

        final Exception x = new RuntimeException();
        final Promise<Integer> p1 = Promises.fulfilled(1);
        final Async<Integer> a2 = Promises.async();
        final Promise<String> p3 = Promises.fulfilled("3");
        final Promise<String> p4 = Promises.fulfilled("4");
        final Promise<Integer> p5 = Promises.fulfilled(5);

        final AtomicReference<Object> actual = new AtomicReference<>();
        Promises.waitFor(p1, a2.promise(), p3, p4, p5).on(Throwable.class,
                actual::set);

        assertNull(actual.get());

        a2.fail(x);

        assertEquals(x, actual.get());

    }

    @Test
    public void testWaitFor5DegenerateFulfilled() {

        final Promise<Integer> p1 = Promises.fulfilled(1);
        final Promise<Integer> p2 = Promises.fulfilled(2);
        final Promise<String> p3 = Promises.fulfilled("3");
        final Promise<String> p4 = Promises.fulfilled("4");
        final Promise<Integer> p5 = Promises.fulfilled(5);

        final AtomicReference<Object> actual = new AtomicReference<>();
        Promises.waitFor(p1, p2, p3, p4, p5).forEach(actual::set);

        assertEquals(Tuple.of(1, 2, "3", "4", 5), actual.get());
    }

    @Test
    public void testWaitFor5DegenerateMultipleBroken() {

        final Exception x = new RuntimeException();
        final Promise<Integer> p1 = Promises.broken(x);
        final Promise<Integer> p2 = Promises.broken(new Exception());
        final Promise<String> p3 = Promises.fulfilled("3");
        final Promise<String> p4 = Promises.fulfilled("4");
        final Promise<Integer> p5 = Promises.fulfilled(5);

        final AtomicReference<Object> actual = new AtomicReference<>();
        Promises.waitFor(p1, p2, p3, p4, p5).on(Throwable.class, actual::set);

        /*
         * leftmost broken promise prevails
         */
        assertEquals(x, actual.get());

    }

}
