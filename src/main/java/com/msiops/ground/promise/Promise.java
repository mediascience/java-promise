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

import java.util.function.Consumer;

public final class Promise<T> {

    public static <R> Promise<R> broken(final Throwable t) {

        final Promise<R> rval = new Promise<>();
        rval.fail(t);
        return rval;

    }

    public static <R> Promise<R> of(final R v) {

        final Promise<R> rval = new Promise<>();
        rval.succeed(v);
        return rval;

    }

    private Throwable error;

    private T value;

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

        if (this.error == null) {
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

        if (sel.isInstance(this.error)) {
            h.accept(sel.cast(this.error));
        }

    }

    private void fail(final Throwable t) {

        this.error = t;

    }

    private void succeed(final T v) {
        this.value = v;
    }

}
