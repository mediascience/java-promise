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

/**
 * Link in a promise chain.
 *
 * @param <T>
 */
interface Link<T> {

    /**
     * <p>
     * Pass result downstream. This method must be invoked no more than once
     * although the implementation is not required detect multiple invocations.
     * </p>
     *
     * <p>
     * This method is not allowed to throw anything. Any error generated during
     * continuation should be sent downstream. Behavior when this method throws
     * is undefined.
     * </p>
     *
     * @param value
     *            value to pass if successful. may be null.
     *
     * @param x
     *            error. If non-null, the value argument is ignored and the
     *            failure is propagated to the rest of the chain. If null, then
     *            the result is considered successful and the value argument is
     *            passed on, even if it is null.
     *
     */
    void next(T value, Throwable x);

}
