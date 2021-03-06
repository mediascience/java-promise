/**
 * Licensed under the Apache License, Version 2.0 (the "License") under
 * one or more contributor license agreements. See the NOTICE file
 * distributed with this work for information regarding copyright
 * ownership. You may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fn.com.msiops.ground.promise;

import static org.junit.Assert.*;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.msiops.ground.promise.Async;
import com.msiops.ground.promise.Promise;
import com.msiops.ground.promise.Promises;

public class AsyncToBlockingTest {

    private Async<Integer> async;

    private ExecutorService exec;

    private Promise<Integer> p;

    private AtomicReference<Object> result;

    private Integer value;

    private Exception x;

    @Before
    public void setup() {

        this.x = new Exception();

        this.value = 12;

        this.async = Promises.async();
        this.p = this.async.promise();

        this.exec = Executors.newCachedThreadPool();

        this.result = new AtomicReference<>();

    }

    @After
    public void teardown() {

        this.exec.shutdown();

    }

    @Test
    public void testArbitraryBrokenIsDone() {

        final Future<Object> fv = Promises.broken(new RuntimeException())
                .toBlocking();

        assertTrue(fv.isDone());
    }

    @Test
    public void testArbitraryBrokenNotCanceled() {

        final Future<Object> fv = Promises.broken(new RuntimeException())
                .toBlocking();

        assertFalse(fv.isCancelled());

    }

    @Test
    public void testBrokenWaitForever() throws InterruptedException,
            ExecutionException {

        final CountDownLatch done = new CountDownLatch(1);

        this.exec.execute(new Runnable() {
            @Override
            public void run() {

                try {
                    AsyncToBlockingTest.this.p.toBlocking().get();
                } catch (final Throwable t) {
                    AsyncToBlockingTest.this.result.set(t);
                } finally {
                    done.countDown();
                }

            }
        });

        this.async.fail(this.x);

        done.await();

        assertEquals(this.x,
                ((ExecutionException) this.result.get()).getCause());
    }

    @Test
    public void testBrokenWaitImpatiently() throws InterruptedException,
            ExecutionException {

        final CountDownLatch done = new CountDownLatch(1);

        this.exec.execute(new Runnable() {
            @Override
            public void run() {

                try {
                    AsyncToBlockingTest.this.p.toBlocking().get(10,
                            TimeUnit.SECONDS);
                } catch (final Throwable t) {
                    AsyncToBlockingTest.this.result.set(t);
                } finally {
                    done.countDown();
                }

            }
        });

        this.async.fail(this.x);

        done.await();

        assertEquals(this.x,
                ((ExecutionException) this.result.get()).getCause());
    }

    @Test
    public void testCanceledIsDone() {

        final Future<Object> fv = Promises.canceled().toBlocking();

        assertTrue(fv.isDone());
    }

    @Test
    public void testCancellation() throws InterruptedException,
            ExecutionException {

        final Future<Object> fv = Promises.canceled().toBlocking();

        assertTrue(fv.isCancelled());

        try {
            fv.get();
            fail("should throw");
        } catch (final CancellationException cx) {
            // OK
        }

    }

    @Test
    public void testFulfilledWaitForever() throws InterruptedException,
            ExecutionException {

        final CountDownLatch done = new CountDownLatch(1);

        this.exec.execute(new Runnable() {
            @Override
            public void run() {

                try {
                    AsyncToBlockingTest.this.result
                            .set(AsyncToBlockingTest.this.p.toBlocking().get());
                } catch (final Throwable t) {
                    // skip setting
                } finally {
                    done.countDown();
                }

            }
        });

        this.async.succeed(this.value);

        done.await();

        assertEquals(this.value, this.result.get());

    }

    @Test
    public void testFulfilledWaitImpatiently() throws InterruptedException,
            ExecutionException {

        final CountDownLatch done = new CountDownLatch(1);

        this.exec.execute(new Runnable() {
            @Override
            public void run() {

                try {
                    AsyncToBlockingTest.this.result
                            .set(AsyncToBlockingTest.this.p.toBlocking().get(
                                    10, TimeUnit.SECONDS));
                } catch (final Throwable t) {
                    // skip setting
                } finally {
                    done.countDown();
                }

            }
        });

        this.async.succeed(this.value);

        done.await();

        assertEquals(this.value, this.result.get());

    }

    @Test
    public void testIncompleteIsNotDone() {

        final Future<Object> fv = Promises.async().promise().toBlocking();

        assertFalse(fv.isDone());
    }

    @Test
    public void testIncompleteNotCancelled() {

        final Future<Object> fv = Promises.async().promise().toBlocking();

        assertFalse(fv.isCancelled());
    }

    @Test
    public void testNotFulfilledIsDone() {

        final Future<Object> fv = Promises.<Object> fulfilled(12).toBlocking();

        assertTrue(fv.isDone());

    }

    @Test
    public void testNotFulfilledNotCanceled() {

        final Future<Object> fv = Promises.<Object> fulfilled(12).toBlocking();

        assertFalse(fv.isCancelled());

    }

    @Test
    public void testWaitTimeout() throws InterruptedException,
            ExecutionException {

        final CountDownLatch done = new CountDownLatch(1);

        this.exec.execute(new Runnable() {
            @Override
            public void run() {

                try {
                    AsyncToBlockingTest.this.p.toBlocking().get(10,
                            TimeUnit.MILLISECONDS);
                } catch (final Throwable t) {
                    AsyncToBlockingTest.this.result.set(t);
                } finally {
                    done.countDown();
                }

            }
        });

        done.await();

        assertTrue(this.result.get() instanceof TimeoutException);
    }
}
