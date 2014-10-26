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

    public static <R> Promise<R> of(final R v) {

        final Promise<R> rval = new Promise<>();
        rval.succeed(v);
        return rval;

    }

    private T value;

    Promise() {

    }

    /**
     * <p>
     * Emit the value of this promise. If the promise is fulfilled when this is
     * invoked, the consumer is invoked immediately with the value. If this
     * promise is broken now or in the future, the consumer is ignored. If this
     * promise is incomplete, the consumer will be invoked if the promise
     * becomes fulfilled later.
     * </p>
     *
     * <p>
     * Exceptions and errors are thrown to the caller if this promise is already
     * fulfilled. Otherwise, they are thrown to the fulfilling caller. (TODO is
     * this the best way?)
     * </p>
     *
     *
     * @param h
     *            value handler. Must not be null.
     *
     * @thows NullPointerException if the handler is null.
     */
    public void forEach(final Consumer<? super T> h) {

        h.accept(this.value);

    }

    void succeed(final T v) {
        this.value = v;
    }

}
