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

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;

import com.msiops.footing.functional.FunT1;
import com.msiops.ground.either.Either;

public interface Promises {

    public static <T> Async<T> async() {
        return new Async<T>();
    }

    /**
     * Create a broken promise. A broken promise is in its final state.
     *
     * @param <R>
     *            value type.
     *
     * @param x
     *            error, must not be null.
     *
     * @return created promise
     *
     * @throws NullPointerException
     *             if error is null.
     *
     */
    public static <R> Promise<R> broken(final Throwable x) {

        final Promise<R> rval = new Promise<>();
        rval.fail(x);
        return rval;

    }

    /**
     * Create a fulfilled promise. A fulfilled promise is in its final state.
     *
     * @param <R>
     *            value type.
     *
     * @param v
     *            fulfillment value. Must not be null.
     *
     * @return created promise.
     *
     * @throws NullPointerException
     *             if argument is null.
     *
     */
    public static <R> Promise<R> fulfilled(final R v) {

        final Promise<R> rval = new Promise<>();
        rval.succeed(v);
        return rval;

    }

    public static <T> Promise<List<T>> join(final List<Promise<T>> unjoined) {

        final AtomicInteger remaining = new AtomicInteger(unjoined.size());

        final Promise<List<T>> downstream = new Promise<>();

        @SuppressWarnings("unchecked")
        final T[] result = (T[]) new Object[unjoined.size()];

        IntStream.range(0, unjoined.size()).forEach(i -> {
            /*
             * fetch upstream one time in case list is not random access.
             */
            final Promise<T> upstream = unjoined.get(i);
            upstream.forEach(v -> {
                result[i] = v;
                if (remaining.decrementAndGet() == 0) {
                    downstream.succeed(Arrays.asList(result));
                }
            });
            /*
             * the first promise to break breaks the downstream with the same
             * error. note that this does not decrement the remaining counter,
             * preventing the above forEach from fulfilling the downstream
             * promise.
             */
            upstream.on(Throwable.class, downstream::fail);
        });

        return downstream;

    }

    /**
     * Convert a value function into a function that performs the computation
     * inside {@link Promise promises}.
     *
     * @param <T>
     *            value function argument type.
     *
     * @param <R>
     *            value function return type.
     *
     * @param f
     *            function to lift.
     *
     * @return lifted function
     */
    public static <T, R> Function<Promise<T>, Promise<R>> lift(
            final Function<T, R> f) {

        return pv -> pv.map(f);

    }

    /**
     * Convert a promise function into a function that performs the computation
     * inside {@link Promise promises}.
     *
     * @param <T>
     *            value function argument type.
     *
     * @param <R>
     *            value function return promise type.
     *
     *
     * @param f
     *            function to lift.
     *
     * @return lifted function
     */
    public static <T, R> Function<Promise<T>, Promise<R>> liftP(
            final FunT1<T, Promise<R>> f) {

        return pv -> pv.then(f);

    }

    public static <R> Promise<R> of(
            final Either<? extends R, ? extends Throwable> e) {

        final Promise<R> rval = new Promise<>();
        rval.complete(e);
        return rval;

    }

    public static <R> Promise<R> waitFor(final Promise<R> p,
            final Promise<?>... others) {

        final AtomicInteger remaining = new AtomicInteger(1 + others.length);
        final AtomicReference<Either<R, Throwable>> result = new AtomicReference<>();

        final Async<R> control = Promises.async();
        final Consumer<Object> dec = v -> {
            final int rm = remaining.decrementAndGet();
            if (rm == 0) {
                control.complete(result.get());
            }
        };

        p.emit(r -> {
            r.forEach(v -> result.set(Either.left(v)));
            r.swap().forEach(x -> result.set(Either.right(x)));
            dec.accept(r);
        });

        for (final Promise<?> o : others) {
            o.emit(dec::accept);
        }

        return control.promise();

    }
}
