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

import java.util.function.Supplier;

import com.msiops.ground.promise.Async;
import com.msiops.ground.promise.Promise;

public enum Example implements Runnable {

    CONSTRUCT_COMPLETE {
        @Override
        public void run() {
            // @formatter:off


final Promise<Integer> fulfilled = Promise.of(75);
fulfilled.forEach(System.out::println); // prints 75

final Promise<Integer> broken = Promise.broken(new RuntimeException());
broken.on(Throwable.class, Throwable::printStackTrace); // prints the stack trace


        // @formatter:on
        }
    },

    CONSTRUCT_DEFERRED {
        @Override
        public void run() {

            // @formatter:off

final Async<Integer> af = new Async<>();
final Promise<Integer> toFulfill = af.promise();
toFulfill.forEach(System.out::println);  // does nothing
af.succeed(75); // prints 75

final Async<Integer> ab = new Async<>();
final Promise<Integer> toBreak = ab.promise();
toBreak.on(Throwable.class, Throwable::printStackTrace); // does nothing
ab.fail(new RuntimeException()); // prints the stack tract

            // @formatter:on

        }
    },

    DEFER {
        @Override
        public void run() {

            // @formatter:off

final Async<Object> toFulfill = new Async<>();
final Async<Object> toBreak = new Async<>();

final Supplier<Promise<String>> finalizer = () -> Promise
        .of("Finally!");

/*
 * these are correct but eclipse marks them with compile errors. Use
 * them for documentation
 */
// toFulfill.promise().defer(finalizer).forEach(System.out::println);
// toBreak.promise().defer(finalizer).forEach(System.out::println);

/*
 * These are a workaround for eclipse bug. Do not use them in
 * documentation.
 */
toFulfill.promise().defer(() -> finalizer.get())
        .forEach(System.out::println);
toBreak.promise().defer(() -> finalizer.get())
        .forEach(System.out::println);

toFulfill.succeed(109); // prints Finally!
toBreak.fail(new Exception()); // prints Finally!

            // @formatter:on

        }
    },

    RECOVER {
        @Override
        public void run() {

            // @formatter:off
            
Promise.broken(new Exception())
        .recover(Exception.class, x -> Promise.of("Recovered!"))
        .forEach(System.out::println); // prints Recovered!

            // @formatter:on

        }
    },

    FLATMAP {

        @Override
        public void run() {

            // @formatter:off

final Async<Object> inner = new Async<>();

Promise.of(75).map(i -> inner.promise())
        .forEach(System.out::println); // does nothing
inner.succeed("Hello"); // prints Hello

final Promise<?> p = Promise.<Integer> broken(
        new RuntimeException()).flatMap(i -> Promise.of("Hello"));
p.forEach(System.out::println); // does nothing
p.on(Throwable.class, Throwable::printStackTrace); // prints stack trace

            // @formatter:on

        };
    },

    MAP {

        @Override
        public void run() {
            // @formatter:off

Promise.of(75).map(i -> i * 2).forEach(System.out::println); // prints 150

Promise.<Integer> broken(new RuntimeException()).map(i -> i * 2)
        .on(Throwable.class, Throwable::printStackTrace); // prints stack trace

            // @formatter:on

        };
    }

    ;

}
