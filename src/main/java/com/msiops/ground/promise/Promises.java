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
package com.msiops.ground.promise;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
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

    /**
     * <p>
     * Join mapped promises. It takes a promise to compute a promise to compute
     * a value and produces a promise to produce a value.
     * </p>
     *
     *
     * @param unjoined
     * @return
     */
    public static <T> Promise<T> join(final Promise<Promise<T>> unjoined) {

        final Async<T> rasync = async();

        /*
         * three cases
         */
        // 1: inner and outer succeed
        unjoined.forEach(pt -> pt.forEach(rasync::succeed));

        // 2: outer succeeds, inner fails
        unjoined.forEach(pt -> pt.on(Throwable.class, rasync::fail));

        // 3: outer fails
        unjoined.on(Throwable.class, rasync::fail);

        return rasync.promise();
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

    /**
     * Unite a list of promises. This takes a list of promises and unites them
     * under a single promise to return a list of values.
     *
     * @param distinct
     *            list of promises to unite. May be empty but must not be null.
     *
     * @return promised list of values.
     *
     * @throws NullPointerException
     *             if argument is null.
     */
    public static <T> Promise<List<T>> unite(final List<Promise<T>> distinct) {

        final AtomicInteger remaining = new AtomicInteger(distinct.size());

        final Promise<List<T>> downstream = new Promise<>();

        @SuppressWarnings("unchecked")
        final T[] result = (T[]) new Object[distinct.size()];

        IntStream.range(0, distinct.size()).forEach(i -> {
            /*
             * fetch upstream one time in case list is not random access.
             */
            final Promise<T> upstream = distinct.get(i);
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
     * Unite promises into a single promise.
     *
     * @param t
     *            first promise. Must not be null.
     *
     * @param u
     *            second promise. Must not be null.
     *
     * @param <T>
     *            first promise value type.
     *
     * @param <U>
     *            second promise value type.
     *
     * @return promise to produce a tuple corresponding to the argument value
     *         types.
     *
     * @throws NullPointerException
     *             if any argument is null.
     *
     */
    public static <T, U> Promise<Pair<T, U>> unite(final Promise<T> t,
            final Promise<U> u) {

        Objects.requireNonNull(u);

        return t.then(tv -> u.map(uv -> Tuple.of(tv, uv)));

    }

    /**
     * Unite promises into a single promise.
     *
     * @param t
     *            first promise. Must not be null.
     *
     * @param u
     *            second promise. Must not be null.
     *
     * @param v
     *            third promise. Must not be null.
     *
     * @param <T>
     *            first promise value type.
     *
     * @param <U>
     *            second promise value type.
     *
     * @param <V>
     *            third promise value type.
     *
     * @return promise to produce a tuple corresponding to the argument value
     *         types.
     *
     * @throws NullPointerException
     *             if any argument is null.
     *
     */
    public static <T, U, V> Promise<Triplet<T, U, V>> unite(final Promise<T> t,
            final Promise<U> u, final Promise<V> v) {

        Objects.requireNonNull(u);
        Objects.requireNonNull(v);

        return t.then(tv -> u.then(uv -> v.map(vv -> Tuple.of(tv, uv, vv))));
    }

    /**
     * Unite promises into a single promise.
     *
     * @param t
     *            first promise. Must not be null.
     *
     * @param u
     *            second promise. Must not be null.
     *
     * @param v
     *            third promise. Must not be null.
     *
     * @param w
     *            fourth promise. Must not be null.
     *
     * @param <T>
     *            first promise value type.
     *
     * @param <U>
     *            second promise value type.
     *
     * @param <V>
     *            third promise value type.
     *
     * @param <W>
     *            fourth promise value type.
     *
     *
     * @return promise to produce a tuple corresponding to the argument value
     *         types.
     *
     * @throws NullPointerException
     *             if any argument is null.
     *
     */
    public static <T, U, V, W> Promise<Tuple4<T, U, V, W>> unite(
            final Promise<T> t, final Promise<U> u, final Promise<V> v,
            final Promise<W> w) {

        Objects.requireNonNull(u);
        Objects.requireNonNull(v);
        Objects.requireNonNull(w);

        return t.then(tv -> u.then(uv -> v.then(vv -> w.map(wv -> Tuple.of(tv,
                uv, vv, wv)))));
    }

    /**
     * Unite promises into a single promise.
     *
     * @param t
     *            first promise. Must not be null.
     *
     * @param u
     *            second promise. Must not be null.
     *
     * @param v
     *            third promise. Must not be null.
     *
     * @param w
     *            fourth promise. Must not be null.
     *
     * @param x
     *            fifth promise. Must not be null.
     *
     * @param <T>
     *            first promise value type.
     *
     * @param <U>
     *            second promise value type.
     *
     * @param <V>
     *            third promise value type.
     *
     * @param <W>
     *            fourth promise value type.
     *
     * @param <X>
     *            fifth promise value type.
     *
     * @return promise to produce a tuple corresponding to the argument value
     *         types.
     *
     * @throws NullPointerException
     *             if any argument is null.
     *
     */
    public static <T, U, V, W, X> Promise<Tuple5<T, U, V, W, X>> unite(
            final Promise<T> t, final Promise<U> u, final Promise<V> v,
            final Promise<W> w, final Promise<X> x) {

        Objects.requireNonNull(u);
        Objects.requireNonNull(v);
        Objects.requireNonNull(w);
        Objects.requireNonNull(x);

        return t.then(tv -> u.then(uv -> v.then(vv -> w.then(wv -> x
                .map(xv -> Tuple.of(tv, uv, vv, wv, xv))))));

    }
}
