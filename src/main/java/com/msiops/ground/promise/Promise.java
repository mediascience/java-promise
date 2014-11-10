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
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import com.msiops.footing.functional.FunT1;
import com.msiops.footing.functional.FunT2;
import com.msiops.footing.functional.SupplierT;
import com.msiops.ground.either.Either;

/**
 * <p>
 * A promise is a future computation whose (evantual) value cannot be directly
 * read. A promised value is used exclusively by continuations. In this
 * implementation, several kinds of continuation are supported (see Usage
 * below).
 * </p>
 *
 * <p>
 * A promise can be in one of three states: <em>incomplete</em>,
 * <em>fulfilled</em>, or <em>broken</em>. A promise that is fulfilled or broken
 * is said to be <em>complete</em>. Once complete, a promise's state cannot
 * change.
 * </p>
 *
 * <p>
 * A promise resulting from binding a continuation is said to exist
 * <em>downstream</em> from the target promise. The target promise is said to
 * exist <em>upstream</em> from the resulting promise. A continuation that
 * produces a promise is called a <em>promise function</em>. When a promise
 * function is bound downstream and subsequently invoked, the promise it
 * produces is hidden to programs but its state is usually reflected in a
 * promise produced when it was bound. The particular binding semantics define
 * how the state is mapped. The hidden promise in this case is said to be
 * <em>injected upstream</em>.
 * </p>
 *
 *
 * @param <T>
 *            promised value type.
 */
public final class Promise<T> {

    private boolean completed = false;

    private Throwable error = null;

    private final List<Link<T>> pending = new ArrayList<Link<T>>();

    private T value = null;

    Promise() {

    }

    /**
     * <p>
     * Perform on completion. Produce a new promise tied to any completion
     * outcome, fulfill or break, of this promise.
     * </p>
     *
     * <p>
     * Any {@link Throwable} thrown by the continuation is passed downstream
     * </p>
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
    public <R> Promise<R> defer(final SupplierT<Promise<R>> src) {

        Objects.requireNonNull(src);

        final Promise<R> rval = new Promise<>();

        final Link<T> link = new Link<T>() {
            @Override
            public void next(final T value, final Throwable x) {

                final Promise<? extends R> upstream;
                try {
                    upstream = src.get();
                } catch (final Throwable t) {
                    rval.fail(t);
                    return;
                }
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

    public void emit(final Consumer<? super Either<T, Throwable>> h) {

        Objects.requireNonNull(h);

        final Link<T> link = new Link<T>() {

            @Override
            public void next(final T value, final Throwable x) {

                final Either<T, Throwable> result;
                if (x == null) {
                    result = Either.left(value);
                } else {
                    result = Either.right(x);
                }
                try {
                    h.accept(result);
                } catch (final Throwable e) {
                    /*
                     * silently ignore error in destination.
                     */
                }

            }
        };

        dispatch(link);

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
     * Any {@link Throwable} thrown from the handler is silently ignored.
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
            public void next(final T value, final Throwable x) {
                if (x == null) {
                    /*
                     * not an error so invoke the handler.
                     */
                    try {
                        h.accept(value);
                    } catch (final Throwable err) {
                        /*
                         * silently ignore error in terminal continuation. See
                         * issue #9.
                         */
                    }
                }
                /*
                 * else nothing to do
                 */

            }
        };

        dispatch(link);

    }

    /**
     * <p>
     * Transform the value. Produces a new promise that will be fulfilled or
     * broken as the original.
     * </p>
     *
     * <p>
     * Any {@link Throwable} thrown by the continuation is passed downstream
     * </p>
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
    public <R> Promise<R> map(final Function<? super T, R> f) {

        Objects.requireNonNull(f);

        final Promise<R> rval = new Promise<>();

        final Link<T> link = new Link<T>() {
            @Override
            public void next(final T value, final Throwable x) {
                if (x == null) {
                    final R rv;
                    try {
                        rv = f.apply(value);
                    } catch (final Throwable t) {
                        rval.fail(t);
                        return;
                    }
                    rval.succeed(rv);
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
     * Any {@link Throwable} thrown from the handler is silently ignored.
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
            public void next(final T value, final Throwable x) {

                if (sel.isInstance(x)) {
                    try {
                        h.accept(sel.cast(x));
                    } catch (final Throwable err) {
                        /*
                         * silently ignore error in terminal continuation. See
                         * issue #9.
                         */
                    }
                }
            }
        };

        dispatch(link);

    }

    /**
     * <p>
     * Emit a signal if this promise is canceled. If the promise is canceled
     * when this is invoked, the handler is invoked immediately. If this promise
     * is fulfilled now or in the future, the handler is ignored. If this
     * promise is incomplete, the handler will be invoked if the promise is
     * canceled later.
     * </p>
     *
     * <p>
     * Any {@link Throwable} thrown from the handler is silently ignored.
     * </p>
     *
     *
     * @param h
     *            signal handler. Must not be null although the implementation
     *            is not required to check for a null value if it can determine
     *            it will not be invoked.
     *
     * @throws NullPointerException
     *             if the handler is null and the promise is canceled or
     *             incomplete.
     */
    public void onCanceled(final Runnable h) {

        Objects.requireNonNull(h);

        final Link<T> link = new Link<T>() {
            @Override
            public void next(final T value, final Throwable x) {

                if (x instanceof CancellationException) {
                    try {
                        h.run();
                    } catch (final Throwable err) {
                        /*
                         * silently ignore error in terminal continuation. See
                         * issue #9.
                         */
                    }
                }
            }
        };

        dispatch(link);
    }

    /**
     * <p>
     * Recover from failure. Produces a promise tied to this promise's failure.
     * </p>
     *
     * <p>
     * Any {@link Throwable} thrown by the continuation is passed downstream
     * </p>
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
     *            error handler. This returns a promise
     *
     * @return new promise to recover from failure.
     */
    public <R, X extends Throwable> Promise<R> recover(final Class<X> sel,
            final FunT1<? super X, Promise<R>> h) {

        Objects.requireNonNull(sel);
        Objects.requireNonNull(h);

        final Promise<R> rval = new Promise<>();

        final Link<T> link = new Link<T>() {
            @Override
            public void next(final T value, final Throwable x) {

                if (sel.isInstance(x)) {
                    /*
                     * only respond to selected failure
                     */

                    final Promise<? extends R> upstream;
                    try {
                        upstream = h.apply(sel.cast(x));
                    } catch (final Throwable t) {
                        rval.fail(t);
                        return;
                    }
                    upstream.forEach(v -> rval.succeed(v));
                    upstream.on(Throwable.class, rval::fail);
                } else {
                    rval.cancel();
                }

            }
        };

        dispatch(link);

        return rval;
    }

    /**
     * <p>
     * Transform the value. Produces a new promise that will be fulfilled
     * independently if this promise is fulfilled. If this promise is fulfilled,
     * a new promise is produced and placed upstream of the returned promise. If
     * this promise is broken, the returned promise is broken immediately and
     * the mapping function is not invoked.
     * </p>
     *
     * <p>
     * Any {@link Throwable} thrown by the continuation is passed downstream
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
     * @return new promise of the transformed value.
     *
     */
    public <R> Promise<R> then(final FunT1<? super T, Promise<R>> mf) {

        Objects.requireNonNull(mf);

        final Promise<R> rval = new Promise<>();

        final Link<T> link = new Link<T>() {
            @Override
            public void next(final T value, final Throwable x) {

                if (x == null) {
                    final Promise<? extends R> upstream;
                    try {
                        upstream = mf.apply(value);
                    } catch (final Throwable t) {
                        rval.fail(t);
                        return;
                    }
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
     * Any {@link Throwable} thrown by the continuation causes a retry check.
     * Any {@link Throwable} thrown by the retry function prevents further
     * retries with the retry function's exceptions sent downstream as failure.
     * </p>
     *
     * <p>
     * If retry is not required, use {@link #then(FunT1)} instead.
     * </p>
     *
     * @param <R>
     *            produced promise's value type.
     *
     * @param mf
     *            mapping function. Must not be null although the implementation
     *            is not required to check for a null value if it can determine
     *            it will not be invoked.
     * @param retry
     *            retry function. Must not be null although the implementation
     *            is not required to check for a null value if it can determine
     *            it will not be invoked.
     * @return new promise of the transformed value.
     *
     */
    public <R> Promise<R> then(final FunT1<? super T, Promise<R>> mf,
            final FunT2<Throwable, Integer, Promise<Boolean>> retry) {

        Objects.requireNonNull(mf);
        Objects.requireNonNull(retry);

        final Promise<R> rval = new Promise<>();

        final Link<T> link = new Link<T>() {

            final AtomicInteger rindex = new AtomicInteger();
            final AtomicReference<Promise<? extends R>> upstream = new AtomicReference<>();

            @Override
            public void next(final T value, final Throwable x) {

                if (x == null) {
                    proceed(value);
                } else {
                    rval.fail(x);
                }

            }

            private void maybeRetry(final Throwable x) {
                final Promise<Boolean> pretry;
                try {
                    pretry = retry.apply(x, this.rindex.incrementAndGet());
                } catch (final Throwable t) {
                    rval.fail(t);
                    return;
                }
                pretry.forEach(b -> {
                    if (b) {
                        proceed(Promise.this.value);
                    } else {
                        rval.fail(x);
                    }
                });
                pretry.on(Throwable.class, rval::fail);
            }

            private void proceed(final T value) {
                try {
                    this.upstream.set(mf.apply(value));
                } catch (final Throwable t) {
                    maybeRetry(t);
                    return;
                }
                this.upstream.get().forEach(rval::succeed);
                this.upstream.get().on(Throwable.class, this::maybeRetry);
            }
        };

        dispatch(link);

        return rval;
    }

    /**
     * <p>
     * Convert to a blocking future. The state of the returned value follows the
     * completion status of this instance. If this promise is broken, the
     * returned future's {@link Future#get()} will throw the corresponding
     * exception (encapsulated in {@link ExecutionException}). If this promise
     * is fulfilled, the returned future's {@link Future#get()} will return the
     * value.
     * </p>
     *
     * @return blocking future.
     */
    public Future<T> toBlocking() {

        final FutureAdapter<T> rval = new FutureAdapter<>();
        dispatch(rval);
        return rval;

    }

    /**
     * Filter the value.
     *
     * @param f
     *            pass predicate. Must not be null although the implementation
     *            is not required to check for a null value if it can determine
     *            it will not be invoked.
     *
     * @return promise to fulfill with the value only if it passes the
     *         predicate.
     *
     * @throws NullPointerException
     *             if a null predicate is passed and the promise is fulfilled or
     *             incomplete.
     */
    public Promise<T> when(final Predicate<? super T> f) {

        Objects.requireNonNull(f);

        final Promise<T> rval = new Promise<>();

        final Link<T> link = new Link<T>() {

            @Override
            public void next(final T value, final Throwable x) {

                if (x != null) {
                    rval.fail(x);
                } else {
                    final boolean pass;
                    try {
                        pass = f.test(value);
                    } catch (final Throwable t) {
                        rval.fail(t);
                        return;
                    }
                    if (pass) {
                        rval.succeed(value);
                    } else {
                        rval.cancel();
                    }
                }
            }
        };

        dispatch(link);

        return rval;
    }

    void cancel() {

        complete(null, new CancellationException());

    }

    void complete(final Either<? extends T, ? extends Throwable> e) {

        e.forEach(v -> {
            complete(v, null);
        });

        e.swap().forEach(x -> {
            complete(null, x);
        });

    }

    void fail(final Throwable x) {

        complete(null, Objects.requireNonNull(x));

    }

    void succeed(final T v) {

        complete(Objects.requireNonNull(v), null);

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
            } catch (final Throwable err) {
                throw new AssertionError("unexpected error back-propagation",
                        err);
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
            } catch (final Throwable err) {
                throw new AssertionError("unexpected error back-propagation",
                        err);
            }
        }
    }

}
