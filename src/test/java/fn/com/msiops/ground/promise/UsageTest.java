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
package fn.com.msiops.ground.promise;

import static org.junit.Assert.*;

import org.junit.Test;

import com.msiops.ground.promise.Async;
import com.msiops.ground.promise.Promise;

public class UsageTest {

    @Test(expected = IllegalStateException.class)
    public void testAsyncBreakBrokenFails() {
        final Async<Object> a = new Async<>();
        a.succeed(new RuntimeException());
        a.succeed(new RuntimeException());
    }

    @Test(expected = IllegalStateException.class)
    public void testAsyncBreakFulfilledFails() {
        final Async<Object> a = new Async<>();
        a.succeed(25);
        a.succeed(new RuntimeException());
    }

    @Test(expected = NullPointerException.class)
    public void testAsyncFailNullIllegal() {

        new Async<Object>().fail(null);

    }

    @Test(expected = IllegalStateException.class)
    public void testAsyncFulfillBrokenFails() {
        final Async<Object> a = new Async<>();
        a.succeed(new RuntimeException());
        a.succeed(25);
    }

    @Test(expected = IllegalStateException.class)
    public void testAsyncFulfillFulfilledFails() {

        final Async<Object> a = new Async<>();
        a.succeed(15);
        a.succeed(25);

    }

    @Test(expected = NullPointerException.class)
    public void testDegenerateBrokenNullInvalid() {

        Promise.broken(null);

    }

    @Test
    public void testDegenerateBrokenThrowsHandlerExceptions() {

        final Promise<Integer> p = Promise.broken(new Exception());

        try {
            p.on(Exception.class, x -> {
                throw new RuntimeException();
            });
            fail("should throw");
        } catch (final RuntimeException x) {
            // OK
        }
    }

    @Test
    public void testDegenerateFulfilledThrowsHandlerExceptions() {

        final Promise<Integer> p = Promise.of(12);

        try {
            p.forEach(i -> {
                throw new RuntimeException();
            });
            fail("should throw");
        } catch (final RuntimeException x) {
            // OK
        }

    }

}
