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

public final class Async<T> {

    private final Promise<T> p = new Promise<>();

    /**
     * Break the managed {@link Promise}.
     *
     * @param x
     *            error. Must not be null.
     *
     * @throws NullPointerException
     *             if the argument is null.
     *
     */
    public void fail(final Throwable x) {

        this.p.fail(x);

    }

    /**
     * Retrieve the {@link Promise} managed by this instance.
     *
     * @return the managed promise.
     */
    public Promise<T> promise() {
        return this.p;
    }

    /**
     * Fulfill the managed {@link Promise}.
     *
     * @param value
     *            fulfillment value.
     */
    public void succeed(final T value) {
        this.p.succeed(value);
    }

}