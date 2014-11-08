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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.msiops.ground.promise.Async;
import com.msiops.ground.promise.Promise;
import com.msiops.ground.promise.Promises;

public class AsyncFromBlockingTest {

    private Async<Integer> async;

    private CountDownLatch done;

    private ExecutorService exec;

    private AtomicReference<Object> result;

    private Integer value;

    private Exception x;

    @Before
    public void setup() {

        this.x = new Exception();

        this.value = 12;

        this.async = Promises.async();

        this.exec = Executors.newCachedThreadPool();
        this.done = new CountDownLatch(1);

        this.result = new AtomicReference<>();

    }

    @After
    public void teardown() {

        this.exec.shutdown();

    }

    @Test
    public void testAlreadyBroken() throws InterruptedException {

        this.async.promise().on(Throwable.class, this::setResult);

        final Promise<Integer> p = Promises.broken(this.x);

        final Runnable task = this.async.when(p.toBlocking());

        this.exec.execute(task);

        this.done.await();

        assertEquals(this.x, this.result.get());

    }

    @Test
    public void testAlreadyFulfilled() throws InterruptedException {

        this.async.promise().forEach(this::setResult);

        final Promise<Integer> p = Promises.fulfilled(this.value);

        final Runnable task = this.async.when(p.toBlocking());

        this.exec.execute(task);

        this.done.await();

        assertEquals(this.value, this.result.get());

    }

    @Test
    public void testWaitBroken() throws InterruptedException {

        this.async.promise().on(Throwable.class, this::setResult);

        final Async<Integer> src = Promises.async();
        final Runnable task = this.async.when(src.promise().toBlocking());

        this.exec.execute(task);

        /*
         * give the task a chance to become blocked
         */
        Thread.sleep(75);

        src.fail(this.x);

        this.done.await();

        assertEquals(this.x, this.result.get());
    }

    @Test
    public void testWaitFulfilled() throws InterruptedException {

        this.async.promise().forEach(this::setResult);

        final Async<Integer> src = Promises.async();
        final Runnable task = this.async.when(src.promise().toBlocking());

        this.exec.execute(task);

        /*
         * give the task a chance to become blocked
         */
        Thread.sleep(75);

        src.succeed(this.value);

        this.done.await();

        assertEquals(this.value, this.result.get());
    }

    private void setResult(final Object o) {
        this.result.set(o);
        this.done.countDown();
    }

}
