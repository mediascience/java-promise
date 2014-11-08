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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;

import com.msiops.ground.promise.Promise;
import com.msiops.ground.promise.Promises;

public class DegenerateToBlockingTest {

    private Promise<Integer> pfulfilled, pbroken;

    private Integer value;

    private Exception x;

    @Before
    public void setup() {

        this.x = new Exception();

        this.value = 12;
        this.pfulfilled = Promises.fulfilled(this.value);
        this.pbroken = Promises.broken(this.x);

    }

    @Test
    public void testBroken() throws InterruptedException, ExecutionException {

        final Future<Integer> fv = this.pbroken.toBlocking();

        try {
            fv.get();
            fail("should throw");
        } catch (final ExecutionException xx) {
            assertEquals(this.x, xx.getCause());
        }

    }

    @Test
    public void testFulfilled() throws InterruptedException, ExecutionException {

        final Future<Integer> fv = this.pfulfilled.toBlocking();

        assertEquals(this.value, fv.get());

    }

}
