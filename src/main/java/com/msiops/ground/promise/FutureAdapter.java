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

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

final class FutureAdapter<T> implements Future<T>, Link<T> {

    private boolean canceled = false;

    private final Object monitor = new Object();

    private T value = null;

    private ExecutionException x = null;

    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {

        /*
         * cancellation only succeeds if it has succeeded already.
         */
        return isCancelled();
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {

        synchronized (this.monitor) {
            while (!complete()) {
                this.monitor.wait();
            }
            if (this.canceled) {
                throw new CancellationException();
            } else if (this.x != null) {
                throw this.x;
            } else {
                return this.value;
            }
        }
    }

    @Override
    public T get(final long timeout, final TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {

        final long deadline = System.currentTimeMillis()
                + TimeUnit.MILLISECONDS.convert(timeout, unit);

        synchronized (this.monitor) {
            while (!complete()) {
                final long now = System.currentTimeMillis();
                if (now >= deadline) {
                    throw new TimeoutException();
                }
                this.monitor.wait(deadline - now);
            }
            if (this.canceled) {
                throw new CancellationException();
            } else if (this.x != null) {
                throw this.x;
            } else {
                return this.value;
            }
        }
    }

    @Override
    public boolean isCancelled() {

        synchronized (this.monitor) {
            return this.canceled;
        }
    }

    @Override
    public boolean isDone() {

        return complete();
    }

    @Override
    public void next(final T pvalue, final Throwable px) {

        synchronized (this.monitor) {
            if (px instanceof CancellationException) {
                this.canceled = true;
            } else if (px != null) {
                this.x = new ExecutionException(px);
            } else {
                this.value = pvalue;
            }
            this.monitor.notifyAll();
        }

    }

    private boolean complete() {
        synchronized (this.monitor) {
            /*
             * note that a null fulfilled value is illegal so at least one of
             * these must be true on completion.
             */
            return this.canceled || this.value != null || this.x != null;
        }

    }

}
