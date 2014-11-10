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
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.IntStream;

import com.msiops.footing.functional.FunT1;
import com.msiops.footing.tuple.Pair;
import com.msiops.footing.tuple.Triplet;
import com.msiops.footing.tuple.Tuple;
import com.msiops.footing.tuple.Tuple4;
import com.msiops.footing.tuple.Tuple5;
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

    public static <R> Promise<R> canceled() {

        final Promise<R> rval = new Promise<>();
        rval.fail(new CancellationException());
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

    public static <T, U> Promise<Pair<T, U>> waitFor(final Promise<T> t,
            final Promise<U> u) {

        /*
         * The internal inconsistency warnings below are displayed in Eclipse
         * only. Found Eclipse bug 432110 which may explain them. These warnings
         * are not emitted by javac. I cannot find any annotation to disable
         * this warning. --gjw
         */
        return t.then(tv -> {
            return u.then(uv -> {
                return Promises.fulfilled(Tuple.of(tv, uv));
            });
        });
    }

    public static <T, U, V> Promise<Triplet<T, U, V>> waitFor(
            final Promise<T> t, final Promise<U> u, final Promise<V> v) {

        /*
         * The internal inconsistency warnings below are displayed in Eclipse
         * only. Found Eclipse bug 432110 which may explain them. These warnings
         * are not emitted by javac. I cannot find any annotation to disable
         * this warning. --gjw
         */
        return t.then(tv -> {
            return u.then(uv -> {
                return v.then(vv -> {
                    return Promises.fulfilled(Tuple.of(tv, uv, vv));
                });
            });
        });
    }

    public static <T, U, V, W> Promise<Tuple4<T, U, V, W>> waitFor(
            final Promise<T> t, final Promise<U> u, final Promise<V> v,
            final Promise<W> w) {

        /*
         * The internal inconsistency warnings below are displayed in Eclipse
         * only. Found Eclipse bug 432110 which may explain them. These warnings
         * are not emitted by javac. I cannot find any annotation to disable
         * this warning. --gjw
         */
        return t.then(tv -> {
            return u.then(uv -> {
                return v.then(vv -> {
                    return w.then(wv -> {
                        return Promises.fulfilled(Tuple.of(tv, uv, vv, wv));
                    });
                });
            });
        });
    }

    public static <T, U, V, W, X> Promise<Tuple5<T, U, V, W, X>> waitFor(
            final Promise<T> t, final Promise<U> u, final Promise<V> v,
            final Promise<W> w, final Promise<X> x) {

        /*
         * The internal inconsistency warnings below are displayed in Eclipse
         * only. Found Eclipse bug 432110 which may explain them. These warnings
         * are not emitted by javac. I cannot find any annotation to disable
         * this warning. --gjw
         */
        return t.then(tv -> {
            return u.then(uv -> {
                return v.then(vv -> {
                    return w.then(wv -> {
                        return x.then(xv -> {
                            return Promises.fulfilled(Tuple.of(tv, uv, vv, wv,
                                    xv));
                        });
                    });
                });
            });
        });
    }

}
