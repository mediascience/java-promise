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
import java.util.function.Consumer;

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

    private boolean complete = false;

    private Throwable error = null;

    private final List<Link<T>> pending = new ArrayList<Link<T>>();

    private T value = null;

    Promise() {

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
     *            value handler. Must not be null.
     *
     * @throws NullPointerException
     *             if the handler is null.
     */
    public void forEach(final Consumer<? super T> h) {

        if (!this.complete) {
            final Link<T> link = new Link<T>() {
                @Override
                public void next(final T value, final Throwable x)
                        throws Throwable {
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
            this.pending.add(link);

        } else if (this.error == null) {
            h.accept(this.value);
        }

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
     *            value handler. Must not be null.
     *
     * @throws NullPointerException
     *             if the handler is null.
     */
    public <X extends Throwable> void on(final Class<X> sel,
            final Consumer<? super X> h) {

        if (!this.complete) {
            final Link<T> link = new Link<T>() {
                @Override
                public void next(final T value, final Throwable x)
                        throws Throwable {

                    if (sel.isInstance(x)) {
                        h.accept(sel.cast(x));
                    }

                }
            };
            this.pending.add(link);
        } else if (sel.isInstance(this.error)) {
            h.accept(sel.cast(this.error));
        }

    }

    void fail(final Throwable t) {

        this.complete = true;
        this.error = Objects.requireNonNull(t);

        this.pending.forEach(l -> {
            try {
                l.next(this.value, this.error);
            } catch (final Throwable e) {
                // do nothing yet
                // TODO figure out just what this means
            }
        });

    }

    void succeed(final T v) {

        this.complete = true;
        this.value = v;

        this.pending.forEach(l -> {
            try {
                l.next(this.value, this.error);
            } catch (final Throwable e) {
                // do nothing yet
                // TODO figure out just what this means
            }
        });
    }

}