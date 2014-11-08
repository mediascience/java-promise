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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

final class FutureAdapter<T> implements Future<T>, Link<T> {

    private final CountDownLatch ready = new CountDownLatch(1);

    private T value = null;

    private ExecutionException x = null;

    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {

        this.ready.await();
        if (this.x != null) {
            throw this.x;
        } else {
            return this.value;
        }
    }

    @Override
    public T get(final long timeout, final TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {

        if (!this.ready.await(timeout, unit)) {
            throw new TimeoutException();
        }

        if (this.x != null) {
            throw this.x;
        } else {
            return this.value;
        }
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {

        return this.ready.getCount() == 0;
    }

    @Override
    public void next(final T pvalue, final Throwable px) {

        if (px != null) {
            this.x = new ExecutionException(px);
        } else {
            this.value = pvalue;
        }

        this.ready.countDown();

    }

}
