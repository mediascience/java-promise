/**
 * Licensed under the Apache License, Version 2.0 (the "License") under
 * one or more contributor license agreements. See the NOTICE file
 * distributed with this work for information regarding copyright
 * ownership. You may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fn.com.msiops.ground.promise;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CancellationException;
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
        a.fail(new RuntimeException());
        a.fail(new RuntimeException());
    }

    @Test(expected = IllegalStateException.class)
    public void testAsyncBreakCanceledFails() {
        final Async<Object> a = Promises.async();
        a.cancel();
        a.fail(new RuntimeException());
    }

    @Test(expected = IllegalStateException.class)
    public void testAsyncBreakFulfilledFails() {
        final Async<Object> a = Promises.async();
        a.succeed(25);
        a.fail(new RuntimeException());
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

    @Test(expected = IllegalStateException.class)
    public void testAsyncCancelBrokenFails() {
        final Async<Object> a = Promises.async();
        a.fail(new RuntimeException());
        a.cancel();
    }

    @Test(expected = IllegalStateException.class)
    public void testAsyncCancelFulfilledFails() {
        final Async<Object> a = Promises.async();
        a.fail(new RuntimeException());
        a.succeed(25);
    }

    public void testAsyncCancelled() {

        final AtomicReference<Object> cap = new AtomicReference<>();

        final Async<?> a = Promises.async();
        a.cancel();

        assertTrue(cap.get() instanceof CancellationException);

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

    @Test(expected = IllegalStateException.class)
    public void testAsyncFulfillCanceledFails() {
        final Async<Object> a = Promises.async();
        a.succeed(25);
        a.fail(new RuntimeException());
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

    @Test(expected = IllegalStateException.class)
    public void testBreakWatchedIllegal() {

        final Async<Object> a = Promises.async();
        a.watch(Promises.fulfilled(999).toBlocking());
        a.fail(new RuntimeException());

    }

    @Test(expected = IllegalStateException.class)
    public void testCancelWatchedIllegal() {

        final Async<Object> a = Promises.async();
        a.watch(Promises.fulfilled(999).toBlocking());
        a.cancel();

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

    @Test
    public void testDegenerateCanceled() {

        final AtomicReference<Object> cap = new AtomicReference<>();

        Promises.canceled().on(CancellationException.class, cap::set);
        assertTrue(cap.get() instanceof CancellationException);

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

    @Test(expected = IllegalStateException.class)
    public void testFulfillWatchedIllegal() {

        final Async<Object> a = Promises.async();
        a.watch(Promises.fulfilled(999).toBlocking());
        a.succeed(21);

    }

    @Test
    public void testJoinAsyncBroken() {

        final AtomicReference<Object> cap = new AtomicReference<>();

        final Async<Integer> a1 = Promises.async();
        final Async<Integer> a2 = Promises.async();
        final Async<Integer> a3 = Promises.async();
        final Async<Integer> a4 = Promises.async();

        final List<Promise<Integer>> listp = Arrays.asList(a1.promise(),
                a2.promise(), a3.promise(), a4.promise());

        Promises.join(listp).on(Throwable.class, cap::set);

        final Exception x2 = new RuntimeException();
        final Exception x3 = new RuntimeException();

        assertNull(cap.get());

        a1.succeed(1);
        assertNull(cap.get());

        a3.fail(x3);
        assertEquals(cap.get(), x3);

        a4.succeed(4);
        assertEquals(cap.get(), x3);

        a2.fail(x2);
        assertEquals(cap.get(), x3);

    }

    @Test
    public void testJoinAsyncFulfilled() {

        final AtomicReference<Object> cap = new AtomicReference<>();

        final Async<Integer> aone = Promises.async();
        final Async<Integer> atwo = Promises.async();

        final List<Promise<Integer>> listp = Arrays.asList(aone.promise(),
                atwo.promise());

        Promises.join(listp).forEach(cap::set);

        assertNull(cap.get());
        atwo.succeed(100);
        assertNull(cap.get());
        aone.succeed(12);

        assertEquals(Arrays.asList(12, 100), cap.get());

    }

    @Test
    public void testJoinDegenerateBroken() {

        final AtomicReference<Object> cap = new AtomicReference<>();

        final Exception x = new RuntimeException();
        final List<Promise<Integer>> listp = Arrays.asList(
                Promises.fulfilled(12), Promises.broken(x));

        Promises.join(listp).on(Throwable.class, cap::set);

        assertEquals(x, cap.get());

    }

    @Test
    public void testJoinDegenerateFulfilled() {

        final AtomicReference<Object> cap = new AtomicReference<>();

        final List<Promise<Integer>> listp = Arrays.asList(
                Promises.fulfilled(12), Promises.fulfilled(100));

        Promises.join(listp).forEach(cap::set);

        assertEquals(Arrays.asList(12, 100), cap.get());

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
    public void testLiftPedOnAsyncBroken() {

        final Function<Promise<Integer>, Promise<String>> lifted = Promises
                .liftP(i -> Promises.fulfilled(String.valueOf(i * 2)));

        final Async<Integer> a = Promises.async();

        final Exception x = new RuntimeException();
        final AtomicReference<Object> actual = new AtomicReference<>();
        lifted.apply(a.promise()).on(Throwable.class, actual::set);

        assertNull(actual.get());

        a.fail(x);

        assertEquals(x, actual.get());

    }

    @Test
    public void testLiftPedOnAsyncFulfilled() {

        final Function<Promise<Integer>, Promise<String>> lifted = Promises
                .liftP(i -> Promises.fulfilled(String.valueOf(i * 2)));

        final Async<Integer> a = Promises.async();

        final AtomicReference<String> actual = new AtomicReference<>();
        lifted.apply(a.promise()).forEach(actual::set);

        assertNull(actual.get());

        a.succeed(12);

        assertEquals("24", actual.get());

    }

    @Test
    public void testLiftPedOnDegenerateBroken() {

        final Function<Promise<Integer>, Promise<String>> lifted = Promises
                .liftP(i -> Promises.fulfilled(String.valueOf(i * 2)));

        final Exception x = new RuntimeException();
        final AtomicReference<Object> actual = new AtomicReference<>();
        lifted.apply(Promises.broken(x)).on(Throwable.class, actual::set);

        assertEquals(x, actual.get());

    }

    @Test
    public void testLiftPedOnDegenerateFulfilled() {

        final Function<Promise<Integer>, Promise<String>> lifted = Promises
                .liftP(i -> Promises.fulfilled(String.valueOf(i * 2)));

        final AtomicReference<String> actual = new AtomicReference<>();
        lifted.apply(Promises.fulfilled(12)).forEach(actual::set);

        assertEquals("24", actual.get());

    }

    @Test(expected = IllegalStateException.class)
    public void testWatchBrokenIllegal() {

        final Async<Object> a = Promises.async();
        a.fail(new RuntimeException());
        a.watch(Promises.fulfilled(999).toBlocking());

    }

    @Test(expected = IllegalStateException.class)
    public void testWatchCancelledIllegal() {

        final Async<Object> a = Promises.async();
        a.cancel();
        a.watch(Promises.fulfilled(999).toBlocking());

    }

    @Test(expected = IllegalStateException.class)
    public void testWatchFulfilledIllegal() {

        final Async<Object> a = Promises.async();
        a.succeed(21);
        a.watch(Promises.fulfilled(999).toBlocking());

    }

}
