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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class Promise<T> {

    /**
     * Create a broken promise. The returned promise is complete.
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
     * Create a fulfilled promise. The returned promise is complete.
     *
     * @param <R>
     *            value type.
     *
     * @param v
     *            fulfillment value. May be null.
     *
     * @return created promise.
     *
     */
    public static <R> Promise<R> of(final R v) {

        final Promise<R> rval = new Promise<>();
        rval.succeed(v);
        return rval;

    }

    private boolean completed = false;

    private Throwable error = null;

    private final List<Link<T>> pending = new ArrayList<Link<T>>();

    private T value = null;

    Promise() {

    }

    /**
     * Perform on completion. Produce a new promise tied to any completion
     * outcome, fulfill or break, of this promise.
     *
     * @param <R>
     *            returned promise's value type.
     *
     * @param src
     *            a promise supplier. The supplier is invoked only when this
     *            promise becomes complete. The supplied promise is inserted
     *            upstream to the returned promise.
     *
     * @return promise to compute a new value when this promise complete.
     */
    public <R> Promise<R> defer(final Supplier<Promise<? extends R>> src) {

        Objects.requireNonNull(src);

        final Promise<R> rval = new Promise<>();

        final Link<T> link = new Link<T>() {
            @Override
            public void next(final T value, final Throwable x) throws Throwable {

                final Promise<? extends R> upstream = src.get();
                /*
                 * don't care about success or failure
                 */
                upstream.forEach(rval::succeed);
                upstream.on(Throwable.class, rval::fail);

            }
        };

        dispatch(link);

        return rval;
    }

    /**
     * Transform the value. Produces a new promise that will be fulfilled
     * independently if this promise is fulfilled. If this promise is fulfilled,
     * a new promise is produced and placed upstream of the returned promise. If
     * this promise is broken, the returned promise is broken immediately and
     * the mapping function is not invoked.
     *
     * @param <R>
     *            produced promise's value type.
     *
     * @param mf
     *            mapping function. Must not be null although the implementation
     *            is not required to check for a null value if it can determine
     *            it will not be invoked.
     *
     * @return new promise of the transformed value.
     *
     */
    public <R> Promise<R> flatMap(
            final Function<? super T, Promise<? extends R>> mf) {

        Objects.requireNonNull(mf);

        final Promise<R> rval = new Promise<>();

        final Link<T> link = new Link<T>() {
            @Override
            public void next(final T value, final Throwable x) throws Throwable {

                if (x == null) {
                    final Promise<? extends R> upstream = mf.apply(value);
                    upstream.forEach(rval::succeed);
                    upstream.on(Throwable.class, rval::fail);
                } else {
                    rval.fail(x);
                }
            }
        };

        dispatch(link);

        return rval;

    }

    /**
     * <p>
     * Emit the value of this promise. If the promise is fulfilled when this is
     * invoked, the handler is invoked immediately with the value. If this
     * promise is broken now or in the future, the handler is ignored. If this
     * promise is incomplete, the handler will be invoked if the promise becomes
     * fulfilled later.
     * </p>
     *
     * <p>
     * {@link Throwable Throwables} thrown by the handler are thrown to the
     * caller if this promise is already fulfilled. Otherwise, they are thrown
     * to the fulfilling caller. (TODO is this the best way?)
     * </p>
     *
     *
     * @param h
     *            value handler. Must not be null although the implementation is
     *            permitted to accept a null value if it can determine it will
     *            not be invoked.
     *
     * @throws NullPointerException
     *             if the handler is null and the promise is not broken.
     */
    public void forEach(final Consumer<? super T> h) {

        Objects.requireNonNull(h);

        final Link<T> link = new Link<T>() {
            @Override
            public void next(final T value, final Throwable x) throws Throwable {
                if (x == null) {
                    /*
                     * not an error so invoke the handler.
                     */
                    h.accept(value);
                }
                /*
                 * else nothing to do
                 */

            }
        };

        dispatch(link);

    }

    /**
     * Transform the value. Produces a new promise that will be fulfilled or
     * broken as the original.
     *
     * @param <R>
     *            the resulting promise's value type.
     *
     * @param f
     *            mapping function. This is invoked only if the original promise
     *            is fulfilled. Must not be null although the implementation is
     *            not required to check for a null value if it can determine it
     *            will not be invoked.
     *
     * @return promise of transformed value.
     */
    public <R> Promise<R> map(final Function<? super T, ? extends R> f) {

        Objects.requireNonNull(f);

        final Promise<R> rval = new Promise<>();

        final Link<T> link = new Link<T>() {
            @Override
            public void next(final T value, final Throwable x) throws Throwable {
                if (x == null) {
                    rval.succeed(f.apply(value));
                } else {
                    rval.fail(x);
                }
            }
        };

        dispatch(link);

        return rval;

    }

    /**
     * <p>
     * Emit the error of this promise. If the promise is broken when this is
     * invoked, the handler is invoked immediately with the error. If this is
     * promise is fulfilled now or in the future, the handler is ignored. If
     * this promise is incomplete, the handler will be invoked if the promise is
     * broken later.
     * </p>
     *
     * <p>
     * {@link Throwable Throwables} thrown by the handler are thrown to the
     * caller if this promise is already broken. Otherwise, they are thrown to
     * the breaking caller. (TODO is this the best way?)
     * </p>
     *
     * @param <X>
     *            type of throwable to handle.
     *
     * @param sel
     *            exception selector. The consumer will be called only if the
     *            error is an instance of this type.
     *
     * @param h
     *            value handler. Must not be null although the implementation is
     *            permitted to accept a null value if it can determine it will
     *            not be invoked.
     *
     * @throws NullPointerException
     *             if the handler or selector is null and the promise is not
     *             fulfilled.
     */
    public <X extends Throwable> void on(final Class<X> sel,
            final Consumer<? super X> h) {

        Objects.requireNonNull(sel);
        Objects.requireNonNull(h);

        final Link<T> link = new Link<T>() {
            @Override
            public void next(final T value, final Throwable x) throws Throwable {

                if (sel.isInstance(x)) {
                    h.accept(sel.cast(x));
                }

            }
        };

        dispatch(link);

    }

    /**
     * Recover from failure. Produces a promise tied to this promise's failure.
     *
     * @param <R>
     *            returned promise's value type.
     *
     * @param <X>
     *            selector token's type.
     *
     * @param sel
     *            selector type token. The handler will be invoked only if the
     *            error is compatible with this type.
     *
     * @param h
     *            error handler.
     *
     * @return new promise to recover from failure.
     */
    public <R, X extends Throwable> Promise<R> recover(final Class<X> sel,
            final Function<? super X, Promise<? extends R>> h) {

        Objects.requireNonNull(sel);
        Objects.requireNonNull(h);

        final Promise<R> rval = new Promise<>();

        final Link<T> link = new Link<T>() {
            @Override
            public void next(final T value, final Throwable x) throws Throwable {

                if (sel.isInstance(x)) {
                    /*
                     * only respond to selected failure
                     */
                    final Promise<? extends R> upstream = h.apply(sel.cast(x));
                    upstream.forEach(rval::succeed);
                    upstream.on(Throwable.class, rval::fail);
                }

            }
        };

        dispatch(link);

        return rval;
    }

    /**
     * <p>
     * Transform the value, potentially retrying on failure. Produces a new
     * promise that will be fulfilled independently if this promise is
     * fulfilled. If this promise is fulfilled, a promise function is invoked
     * and its result placed upstream of the returned promise. If this promise
     * is broken, a retry function is consulted to determine if the original
     * should be tried again.
     * </p>
     *
     * <p>
     * The retry function is a promise function so that a delay can be
     * introduced. The function is passed both the breaking error and the
     * current iteration starting with 1 the first time the promise is broken.
     * The function returns a promise to produce a True or False. If the promise
     * is fulfilled with True, the original promise function will be invoked
     * again. If the promise is fulfilled with False, the returned promise will
     * be broken with the latest error. If the retry promise breaks, the
     * returned promise will fail with the retry promise's error.
     * </p>
     *
     * <p>
     * If retry is not required, use {@link #flatMap(Function)} instead.
     * </p>
     *
     * @param <R>
     *            produced promise's value type.
     *
     * @param mf
     *            mapping function. Must not be null although the implementation
     *            is not required to check for a null value if it can determine
     *            it will not be invoked.
     *
     * @param retry
     *            retry function. Must not be null although the implementation
     *            is not required to check for a null value if it can determine
     *            it will not be invoked.
     *
     * @return new promise of the transformed value.
     *
     */
    public <R, X extends Throwable> Promise<R> then(
            final Function<? super T, Promise<? extends R>> mf,
            final BiFunction<? super X, Integer, Promise<Boolean>> retry) {

        final Promise<R> rval = new Promise<>();

        final Link<T> link = new Link<T>() {
            @Override
            public void next(final T value, final Throwable x) throws Throwable {

                if (x == null) {

                    final Promise<? extends R> upstream = mf.apply(value);
                    upstream.forEach(rval::succeed);

                }

            }
        };

        dispatch(link);

        return rval;
    }

    void fail(final Throwable x) {

        complete(null, Objects.requireNonNull(x));

    }

    void succeed(final T v) {

        complete(v, null);

    }

    private void complete(final T v, final Throwable x) {

        final List<Link<T>> links;
        synchronized (this.pending) {
            if (this.completed) {
                throw new AssertionError(
                        "completion invoked on completed promise");
            }
            this.completed = true;
            this.value = v;
            this.error = x;
            links = new ArrayList<>(this.pending);
            this.pending.clear();
        }

        links.forEach(l -> {
            try {
                l.next(this.value, this.error);
            } catch (final Throwable e) {
                // do nothing yet
                // TODO figure out just what this means
            }
        });
    }

    private void dispatch(final Link<T> link) {

        final boolean immediate;
        synchronized (this.pending) {
            immediate = this.completed;
            if (!immediate) {
                this.pending.add(link);
            }
        }

        if (immediate) {
            try {
                link.next(this.value, this.error);
            } catch (Error | RuntimeException x) {
                throw x;
            } catch (final Throwable x) {
                // do nothing yet
                // TODO figure out just what this means
            }
        }
    }

}
