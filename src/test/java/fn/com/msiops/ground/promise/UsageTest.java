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

import org.junit.Test;

import com.msiops.ground.promise.Async;
import com.msiops.ground.promise.Promises;

public class UsageTest {

    @Test(expected = IllegalStateException.class)
    public void testAsyncBreakBrokenFails() {
        final Async<Object> a = Promises.async();
        a.succeed(new RuntimeException());
        a.succeed(new RuntimeException());
    }

    @Test(expected = IllegalStateException.class)
    public void testAsyncBreakFulfilledFails() {
        final Async<Object> a = Promises.async();
        a.succeed(25);
        a.succeed(new RuntimeException());
    }

    @Test(expected = NullPointerException.class)
    public void testAsyncFailNullIllegal() {

        Promises.async().fail(null);

    }

    @Test(expected = IllegalStateException.class)
    public void testAsyncFulfillBrokenFails() {
        final Async<Object> a = Promises.async();
        a.succeed(new RuntimeException());
        a.succeed(25);
    }

    @Test(expected = IllegalStateException.class)
    public void testAsyncFulfillFulfilledFails() {

        final Async<Object> a = Promises.async();
        a.succeed(15);
        a.succeed(25);

    }

    @Test(expected = NullPointerException.class)
    public void testAsyncSucceedNullIllegal() {

        Promises.async().succeed(null);

    }

    @Test(expected = NullPointerException.class)
    public void testDegenerateBrokenNullInvalid() {

        Promises.broken(null);

    }

    @Test(expected = NullPointerException.class)
    public void testDegenerateFulfilledNullInvalid() {

        Promises.fulfilled(null);

    }

}
