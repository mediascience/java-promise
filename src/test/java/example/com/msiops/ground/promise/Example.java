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
package example.com.msiops.ground.promise;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import com.msiops.footing.functional.SupplierT;
import com.msiops.ground.either.Either;
import com.msiops.ground.promise.Async;
import com.msiops.ground.promise.Promise;
import com.msiops.ground.promise.Promises;

public enum Example implements Runnable {

    ADAPT_BLOCKING {
        @Override
        public void run() {

            // @formatter:off

final ExecutorService exec = Executors.newCachedThreadPool();
try {
// BEGIN FOR DOCUMENTATION
final CountDownLatch done = new CountDownLatch(1);
final AtomicInteger cap = new AtomicInteger();

// to blocking
final Async<Integer> src = Promises.async();
final Future<Integer> fv = src.promise().toBlocking();

// from blocking
final Async<Integer> dest = Promises.async();
dest.promise().forEach(v -> {
    cap.set(v);
    done.countDown();
});
final Runnable task = dest.watch(fv);
exec.execute(task); // or just task.run() to block this thread

src.succeed(75);
done.await();
assert cap.get() == 75;
// END FOR DOCUMENTATION
} catch (final InterruptedException e) {
    Thread.currentThread().interrupt();
    throw new RuntimeException("interrupted");
} finally {
    exec.shutdown();
}

            // @formatter:on

        }
    },

    /**
     * Show how to create a promise that is already complete.
     */
    CREATE_DEGENERATE {
        @Override
        public void run() {
            // @formatter:off

final AtomicInteger vcap = new AtomicInteger();
final Promise<Integer> fulfilled = Promises.fulfilled(75);
fulfilled.forEach(vcap::set);
assert vcap.get() == 75;

final AtomicReference<Exception> ecap = new AtomicReference<>();
final Exception x = new RuntimeException();
final Promise<Integer> broken = Promises.broken(x);
broken.on(RuntimeException.class, ecap::set);
assert ecap.get() == x;

final AtomicInteger vcap2 = new AtomicInteger();
final Promise<Integer> fulfilled2 = Promises.of(Either.left(75));
fulfilled2.forEach(vcap2::set);
assert vcap2.get() == 75;

final AtomicReference<Exception> ecap2 = new AtomicReference<>();
final Promise<Integer> broken2 = Promises.of(Either.right(x));
broken2.on(RuntimeException.class, ecap2::set);
assert ecap2.get() == x;

        // @formatter:on
        }
    },

    /**
     * Show how to create an incomplete promise, then complete it.
     */
    CREATE_INCOMPLETE {
        @Override
        public void run() {

            // @formatter:off

final AtomicInteger vcap = new AtomicInteger();
final Async<Integer> af = Promises.async();
final Promise<Integer> toFulfill = af.promise();
toFulfill.forEach(vcap::set);
assert vcap.get() == 0;
af.succeed(75);
assert vcap.get() == 75;

final AtomicReference<Exception> ecap = new AtomicReference<>();
final Async<Integer> ab = Promises.async();
final Promise<Integer> toBreak = ab.promise();
toBreak.on(RuntimeException.class, ecap::set);
assert ecap.get() == null;
final Exception x = new RuntimeException();
ab.fail(x);
assert ecap.get() == x;

final AtomicInteger vcap2 = new AtomicInteger();
final Async<Integer> af2 = Promises.async();
final Promise<Integer> toFulfill2 = af2.promise();
toFulfill2.forEach(vcap2::set);
assert vcap2.get() == 0;
af2.complete(Either.left(75));
assert vcap2.get() == 75;

final AtomicReference<Exception> ecap2 = new AtomicReference<>();
final Async<Integer> ab2 = Promises.async();
final Promise<Integer> toBreak2 = ab2.promise();
toBreak2.on(RuntimeException.class, ecap2::set);
assert ecap2.get() == null;
ab2.complete(Either.right(x));
assert ecap2.get() == x;

            // @formatter:on

        }
    },

    /**
     * Show how to perform work only after after a promise is complete.
     */
    DEFER {
        @Override
        public void run() {

            // @formatter:off

final Async<Object> toFulfill = Promises.async();
final Async<Object> toBreak = Promises.async();

final SupplierT<Promise<String>> finalizer = () -> Promises
        .fulfilled("Finally!");

final AtomicReference<String> cap1 = new AtomicReference<String>();
toFulfill.promise().defer(finalizer).forEach(cap1::set);
assert cap1.get() == null;
toFulfill.succeed(109);
assert cap1.get().equals("Finally!");

final AtomicReference<String> cap2 = new AtomicReference<String>();
toBreak.promise().defer(finalizer).forEach(cap2::set);
assert cap2.get() == null;
toBreak.fail(new Exception()); // prints Finally!
assert cap2.get().equals("Finally!");

            // @formatter:on

        }
    },

    FILTER {
        @Override
        public void run() {

            // @formatter:off

final AtomicReference<Object> cap1 = new AtomicReference<>();
final AtomicReference<Object> cap2 = new AtomicReference<>();

final Async<Integer> async = Promises.async();
async.promise().when(i -> i == 75).map(String::valueOf)
        .forEach(cap1::set);
async.promise().when(i -> i == 100).map(String::valueOf)
        .forEach(cap2::set);

async.succeed(100);

assert cap1.get() == null;
assert cap2.get().equals("100");

            // @formatter:on

        }
    },

    LIFT {
        @Override
        public void run() {

            // @formatter:off

final Function<Promise<Integer>, Promise<Integer>> lifted = Promises
        .lift(i -> 2 * i);

final AtomicInteger cap1 = new AtomicInteger();
final AtomicInteger cap2 = new AtomicInteger();
final AtomicReference<Object> cap3 = new AtomicReference<>();

final Promise<Integer> p1 = Promises.fulfilled(75);
lifted.apply(p1).forEach(cap1::set);
assert cap1.get() == 150;

final Async<Integer> a2 = Promises.async();
lifted.apply(a2.promise()).forEach(cap2::set);
assert cap2.get() == 0;
a2.succeed(75);
assert cap2.get() == 150;

final Async<Integer> a3 = Promises.async();
lifted.apply(a3.promise()).on(Throwable.class, cap3::set);
assert cap3.get() == null;
final Exception x = new RuntimeException();
a3.fail(x);
assert cap3.get() == x;

            // @formatter:on

        }
    },

    /**
     * Show how to transform value.
     */
    MAP {

        @Override
        public void run() {
            // @formatter:off

final AtomicInteger vcap = new AtomicInteger();
Promises.fulfilled(75).map(i -> i * 2).forEach(vcap::set);
assert vcap.get() == 150;

final AtomicReference<Object> ecap = new AtomicReference<>();
final Exception x = new RuntimeException();
Promises.<Integer>broken(x)
    .map(i -> i * 2) // lambda expr not invoked
    .on(Throwable.class, ecap::set);
assert ecap.get() == x;

            // @formatter:on

        };
    },

    MAP_ERROR {
        @Override
        public void run() {

            // @formatter:off

final AtomicReference<Throwable> ecap = new AtomicReference<>();
final AtomicInteger vcap = new AtomicInteger();

final Exception origX = new Exception("orig x");
Promises.broken(origX)
        .mapError(Exception.class, x -> new RuntimeException(x))
        .on(Exception.class, ecap::set);

assert ecap.get() instanceof RuntimeException;
assert ecap.get().getCause() == origX;

Promises.fulfilled(100)
        .mapError(Exception.class, x -> new RuntimeException(x))
        .forEach(vcap::set);
assert vcap.get() == 100;

            // @formatter::on
        }
    },

    /**
     * Show how to recover from error.
     */
    RECOVER {
        @Override
        public void run() {

            // @formatter:off

// recover from broken promise
final AtomicReference<Object> ecap = new AtomicReference<>();
final AtomicReference<Object> rcap = new AtomicReference<>();

final Exception x = new RuntimeException();
final Promise<?> p = Promises.broken(x);
p.on(Throwable.class, ecap::set);
p.recover(Exception.class, err -> Promises.fulfilled("Recovered!"))
    .forEach(rcap::set);

assert ecap.get() == x;
assert rcap.get().equals("Recovered!");

// recovery is not performed when promise is fulfilled
final AtomicReference<Object> vcap = new AtomicReference<>();
final AtomicReference<Object> scap = new AtomicReference<>();

final Promise<String> q = Promises.fulfilled("Recovery Not Needed");
q.forEach(vcap::set);
q.recover(Exception.class, err -> Promises.fulfilled("Recovered!"))
    .forEach(scap::set);

assert vcap.get().equals("Recovery Not Needed");
assert scap.get().equals("Recovery Not Needed");

        // @formatter:on
        }
    },

    /**
     * Show how to bind to downstream promise function.
     */
    THEN {

        @Override
        public void run() {

            // @formatter:off

final AtomicReference<Object> vcap = new AtomicReference<>();
final Async<Object> inner = Promises.async();
Promises.fulfilled(75).then(i -> inner.promise())
        .forEach(vcap::set);
assert vcap.get() == null;
inner.succeed("Hello");
assert vcap.get().equals("Hello");


final AtomicReference<Object> ecap = new AtomicReference<>();
final Exception x = new RuntimeException();
Promises.<Integer>broken(x)
    .then(i -> Promises.fulfilled("Hello")) // lambda expr not invoked
    .on(RuntimeException.class, ecap::set);
assert ecap.get() == x;

            // @formatter:on

        };
    },

    /**
     * Show how to retry on failure.
     */
    THEN_RETRY {
        @Override
        public void run() {

            // @formatter:off

final AtomicBoolean condition = new AtomicBoolean();
final AtomicReference<Async<Boolean>> pendingRetry = new AtomicReference<Async<Boolean>>();
final AtomicReference<Object> vcap = new AtomicReference<>();

Promises.fulfilled(75)
    .then(i -> {
        return condition.get() ? Promises.fulfilled("Done!")
                : Promises.broken(new RuntimeException());
    }, (x, u) -> {
        pendingRetry.set(Promises.async());
        return pendingRetry.get().promise();
    }).forEach(vcap::set);

pendingRetry.get().succeed(true); // try (and fail) again
pendingRetry.get().succeed(true); // false would mean stop retrying
condition.set(true); // next time it will work
assert vcap.get() == null;
pendingRetry.get().succeed(true);
assert vcap.get().equals("Done!");

            // @formatter:on

        }
    }

    ;

}
