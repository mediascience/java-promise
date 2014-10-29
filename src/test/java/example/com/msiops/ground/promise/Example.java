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
package example.com.msiops.ground.promise;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import com.msiops.ground.promise.Async;
import com.msiops.ground.promise.Promise;

public enum Example implements Runnable {

    /**
     * Show how to create a promise that is already complete.
     */
    CREATE_DEGENERATE {
        @Override
        public void run() {
            // @formatter:off

final AtomicInteger vcap = new AtomicInteger();
final Promise<Integer> fulfilled = Promise.of(75);
fulfilled.forEach(vcap::set);
assert vcap.get() == 75;

final AtomicReference<Exception> ecap = new AtomicReference<>();
final Exception x = new RuntimeException();
final Promise<Integer> broken = Promise.broken(x);
broken.on(RuntimeException.class, ecap::set);
assert ecap.get() == x;


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
final Async<Integer> af = new Async<>();
final Promise<Integer> toFulfill = af.promise();
toFulfill.forEach(vcap::set);
assert vcap.get() == 0;
af.succeed(75);
assert vcap.get() == 75;

final AtomicReference<Exception> ecap = new AtomicReference<>();
final Async<Integer> ab = new Async<>();
final Promise<Integer> toBreak = ab.promise();
toBreak.on(RuntimeException.class, ecap::set);
assert ecap.get() == null;
final Exception x = new RuntimeException();
ab.fail(x);
assert ecap.get() == x;

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

final Async<Object> toFulfill = new Async<>();
final Async<Object> toBreak = new Async<>();

final Supplier<Promise<String>> finalizer = () -> Promise
        .of("Finally!");

final AtomicReference<String> cap1 = new AtomicReference<String>();
/*
 * this is correct but will not compile in eclipse. Use it for
 * documentation
 */
//toFulfill.promise().defer(finalizer).forEach(cap1::set);
/*
 * this is verbose but compiles in eclipse. Do not use for documentation.
 */
toFulfill.promise().defer(() -> finalizer.get())
        .forEach(cap1::set);
assert cap1.get() == null;
toFulfill.succeed(109);
assert cap1.get().equals("Finally!");

final AtomicReference<String> cap2 = new AtomicReference<String>();
/*
 * this is correct but will not compile in eclipse. Use it for
 * documentation
 */
//toBreak.promise().defer(finalizer).forEach(cap2::set);
/*
 * this is verbose but compiles in eclipse. Do not use for documentation.
 */
toBreak.promise().defer(() -> finalizer.get())
        .forEach(cap2::set);
assert cap2.get() == null;
toBreak.fail(new Exception()); // prints Finally!
assert cap2.get().equals("Finally!");

            // @formatter:on

        }
    },

    /**
     * Show how to bind to downstream promise function.
     */
    FLATMAP {

        @Override
        public void run() {

            // @formatter:off

final AtomicReference<Object> vcap = new AtomicReference<>();
final Async<Object> inner = new Async<>();
Promise.of(75).flatMap(i -> inner.promise())
        .forEach(vcap::set);
assert vcap.get() == null;
inner.succeed("Hello");
assert vcap.get().equals("Hello");


final AtomicReference<Object> ecap = new AtomicReference<>();
final Exception x = new RuntimeException();
Promise.<Integer>broken(x)
    .flatMap(i -> Promise.of("Hello")) // lambda expr not invoked
    .on(RuntimeException.class, ecap::set);
assert ecap.get() == x;

            // @formatter:on

        };
    },

    /**
     * Show how to transform value.
     */
    MAP {

        @Override
        public void run() {
            // @formatter:off

final AtomicInteger vcap = new AtomicInteger();
Promise.of(75).map(i -> i * 2).forEach(vcap::set);
assert vcap.get() == 150;

final AtomicReference<Object> ecap = new AtomicReference<>();
final Exception x = new RuntimeException();
Promise.<Integer>broken(x)
    .map(i -> i * 2) // lambda expr not invoked
    .on(Throwable.class, ecap::set);
assert ecap.get() == x;

            // @formatter:on

        };
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
final Promise<?> p = Promise.broken(x);
p.on(Throwable.class, ecap::set);
p.recover(Exception.class, err -> Promise.of("Recovered!"))
    .forEach(rcap::set);

assert ecap.get() == x;
assert rcap.get().equals(Optional.of("Recovered!"));

// recover when promise is fulfilled
final AtomicInteger vcap = new AtomicInteger();
final AtomicReference<Object> scap = new AtomicReference<Object>();

final Promise<Integer> q = Promise.of(75);
q.forEach(vcap::set);
q.recover(Exception.class, err -> Promise.of("Recovered!"))
    .forEach(scap::set);

assert vcap.get() == 75;
assert scap.get().equals(Optional.empty());

        // @formatter:on
        }
    },

    /**
     * Show how to retry on failure.
     */
    RETRY {
        @Override
        public void run() {

            // @formatter:off

final AtomicBoolean condition = new AtomicBoolean();
final AtomicReference<Async<Boolean>> pendingRetry = new AtomicReference<Async<Boolean>>();
final AtomicReference<Object> vcap = new AtomicReference<>();

Promise.of(75)
    .then(i -> {
        return condition.get() ? Promise.of("Done!")
                : Promise.broken(new RuntimeException());
    }, (x, u) -> {
        pendingRetry.set(new Async<>());
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
