package fn.com.msiops.ground.promise;

import static org.junit.Assert.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.msiops.ground.promise.Async;
import com.msiops.ground.promise.Promise;

public class ConcurrencyTest {

    @Test
    public void testManyValueEmits() {

        final int breadsz = 50;

        final ExecutorService exec = Executors.newCachedThreadPool();
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch end = new CountDownLatch(breadsz * 2 + 1);

        final Object expected = new Object();
        final Async<Object> a = new Async<>();
        final Promise<Object> p = a.promise();

        final AtomicInteger emitted = new AtomicInteger();

        for (int i = 0; i < breadsz; i = i + 1) {
            exec.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        start.await();
                        p.forEach(o -> {
                            if (expected.equals(o)) {
                                emitted.incrementAndGet();
                            }
                        });
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        end.countDown();
                    }
                }
            });
        }
        exec.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    start.await();
                    a.succeed(expected);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    end.countDown();
                }
            }
        });
        for (int i = 0; i < breadsz; i = i + 1) {
            exec.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        start.await();
                        p.forEach(o -> {
                            if (expected.equals(o)) {
                                emitted.incrementAndGet();
                            }
                        });
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        end.countDown();
                    }
                }
            });
        }

        start.countDown();
        try {
            end.await();
            assertEquals(breadsz * 2, emitted.get());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("did not finish");
        } finally {
            exec.shutdownNow();
        }

    }

    @Test
    public void testManyErrorEmits() {

        final int breadsz = 50;

        final ExecutorService exec = Executors.newCachedThreadPool();
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch end = new CountDownLatch(breadsz * 2 + 1);

        final Exception expected = new Exception();
        final Async<Object> a = new Async<>();
        final Promise<Object> p = a.promise();

        final AtomicInteger emitted = new AtomicInteger();

        for (int i = 0; i < breadsz; i = i + 1) {
            exec.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        start.await();
                        p.on(Throwable.class, x -> {
                            if (expected.equals(x)) {
                                emitted.incrementAndGet();
                            }
                        });
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        end.countDown();
                    }
                }
            });
        }
        exec.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    start.await();
                    a.fail(expected);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    end.countDown();
                }
            }
        });
        for (int i = 0; i < breadsz; i = i + 1) {
            exec.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        start.await();
                        p.on(Throwable.class, x -> {
                            if (expected.equals(x)) {
                                emitted.incrementAndGet();
                            }
                        });
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        end.countDown();
                    }
                }
            });
        }

        start.countDown();
        try {
            end.await();
            assertEquals(breadsz * 2, emitted.get());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("did not finish");
        } finally {
            exec.shutdownNow();
        }

    }

}
