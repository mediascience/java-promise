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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.Test;

import com.msiops.ground.promise.Async;
import com.msiops.ground.promise.Promise;
import com.msiops.ground.promise.Promises;

public class ConcurrencyTest {

    @Test
    public void testManyDefers() {

        final int breadsz = 50;

        final ExecutorService exec = Executors.newCachedThreadPool();
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch end = new CountDownLatch(breadsz * 4 + 1);

        final String expected = "HI";
        final Async<Integer> a = Promises.async();
        final Promise<Integer> p = a.promise();

        final AtomicInteger emitted = new AtomicInteger();

        final Supplier<Promise<String>> src = new Supplier<Promise<String>>() {
            @Override
            public Promise<String> get() {
                final Async<String> inner = Promises.async();
                exec.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(100);
                            inner.succeed(expected);
                        } catch (final InterruptedException e) {
                            Thread.currentThread().interrupt();
                        } finally {
                            end.countDown();
                        }
                    }
                });
                return inner.promise();
            }
        };

        for (int i = 0; i < breadsz; i = i + 1) {
            exec.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        start.await();
                        /*
                         * should be able to use src directly but eclipse won't
                         * let me. so doing this hacky workaround. Note that if
                         * use mf as the argument directly, javac has no trouble
                         * with it
                         */
                        p.defer(() -> src.get()).forEach(o -> {
                            if (expected.equals(o)) {
                                emitted.incrementAndGet();
                            }
                        });
                    } catch (final InterruptedException e) {
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
                    a.succeed(12);
                } catch (final InterruptedException e) {
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
                        /*
                         * should be able to use src directly but eclipse won't
                         * let me. so doing this hacky workaround. Note that if
                         * use mf as the argument directly, javac has no trouble
                         * with it
                         */
                        p.defer(() -> src.get()).forEach(o -> {
                            if (expected.equals(o)) {
                                emitted.incrementAndGet();
                            }
                        });
                    } catch (final InterruptedException e) {
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
        } catch (final InterruptedException e) {
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
        final Async<Object> a = Promises.async();
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
                    } catch (final InterruptedException e) {
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
                } catch (final InterruptedException e) {
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
                    } catch (final InterruptedException e) {
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
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("did not finish");
        } finally {
            exec.shutdownNow();
        }

    }

    @Test
    public void testManyFlatMaps() {

        final int breadsz = 50;

        final ExecutorService exec = Executors.newCachedThreadPool();
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch end = new CountDownLatch(breadsz * 4 + 1);

        final String expected = "HI";
        final Async<Integer> a = Promises.async();
        final Promise<Integer> p = a.promise();

        final AtomicInteger emitted = new AtomicInteger();

        final Function<Object, Promise<String>> mf = new Function<Object, Promise<String>>() {
            @Override
            public Promise<String> apply(final Object t) {
                final Async<String> inner = Promises.async();
                exec.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(100);
                            inner.succeed(expected);
                        } catch (final InterruptedException e) {
                            Thread.currentThread().interrupt();
                        } finally {
                            end.countDown();
                        }
                    }
                });
                return inner.promise();
            }
        };

        for (int i = 0; i < breadsz; i = i + 1) {
            exec.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        start.await();
                        /*
                         * should be able to use mf directly but eclipse won't
                         * let me. so doing this hacky workaround. Note that if
                         * use mf as the argument directly, javac has no trouble
                         * with it
                         */
                        p.then(hack -> mf.apply(hack)).forEach(o -> {
                            if (expected.equals(o)) {
                                emitted.incrementAndGet();
                            }
                        });
                    } catch (final InterruptedException e) {
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
                    a.succeed(12);
                } catch (final InterruptedException e) {
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
                        /*
                         * should be able to use mf directly but eclipse won't
                         * let me. so doing this hacky workaround. Note that if
                         * use mf as the argument directly, javac has no trouble
                         * with it
                         */
                        p.then(hack -> mf.apply(hack)).forEach(o -> {
                            if (expected.equals(o)) {
                                emitted.incrementAndGet();
                            }
                        });
                    } catch (final InterruptedException e) {
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
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("did not finish");
        } finally {
            exec.shutdownNow();
        }

    }

    @Test
    public void testManyRecoveries() {

        final int breadsz = 50;

        final ExecutorService exec = Executors.newCachedThreadPool();
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch end = new CountDownLatch(breadsz * 4 + 1);

        final Integer rval = 10100;
        final Integer expected = rval;
        final Async<Integer> a = Promises.async();
        final Promise<Integer> p = a.promise();

        final AtomicInteger emitted = new AtomicInteger();

        final Function<Throwable, Promise<Integer>> h = new Function<Throwable, Promise<Integer>>() {
            @Override
            public Promise<Integer> apply(final Throwable x) {
                final Async<Integer> inner = Promises.async();
                exec.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(100);
                            inner.succeed(rval);
                        } catch (final InterruptedException e) {
                            Thread.currentThread().interrupt();
                        } finally {
                            end.countDown();
                        }
                    }
                });
                return inner.promise();
            }
        };

        for (int i = 0; i < breadsz; i = i + 1) {
            exec.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        start.await();
                        /*
                         * should be able to use h directly but eclipse won't
                         * let me. so doing this hacky workaround. Note that if
                         * use mf as the argument directly, javac has no trouble
                         * with it
                         */
                        p.recover(Throwable.class, x -> h.apply(x)).forEach(
                                o -> {
                                    if (expected.equals(o)) {
                                        emitted.incrementAndGet();
                                    }
                                });
                    } catch (final InterruptedException e) {
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
                    a.fail(new Exception());
                } catch (final InterruptedException e) {
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
                        /*
                         * should be able to use h directly but eclipse won't
                         * let me. so doing this hacky workaround. Note that if
                         * use mf as the argument directly, javac has no trouble
                         * with it
                         */
                        p.recover(Throwable.class, x -> h.apply(x)).forEach(
                                o -> {
                                    if (expected.equals(o)) {
                                        emitted.incrementAndGet();
                                    }
                                });
                    } catch (final InterruptedException e) {
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
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("did not finish");
        } finally {
            exec.shutdownNow();
        }

    }

    @Test
    public void testManyValueEmits() {

        final int breadsz = 50;

        final ExecutorService exec = Executors.newCachedThreadPool();
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch end = new CountDownLatch(breadsz * 2 + 1);

        final Object expected = new Object();
        final Async<Object> a = Promises.async();
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
                    } catch (final InterruptedException e) {
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
                } catch (final InterruptedException e) {
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
                    } catch (final InterruptedException e) {
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
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("did not finish");
        } finally {
            exec.shutdownNow();
        }

    }

    @Test
    public void testManyValueMaps() {

        final int breadsz = 50;

        final ExecutorService exec = Executors.newCachedThreadPool();
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch end = new CountDownLatch(breadsz * 2 + 1);

        final Object expected = 24;
        final Async<Integer> a = Promises.async();
        final Promise<Integer> p = a.promise();

        final AtomicInteger emitted = new AtomicInteger();

        for (int i = 0; i < breadsz; i = i + 1) {
            exec.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        start.await();
                        p.map(n -> 2 * n).forEach(o -> {
                            if (expected.equals(o)) {
                                emitted.incrementAndGet();
                            }
                        });
                    } catch (final InterruptedException e) {
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
                    a.succeed(12);
                } catch (final InterruptedException e) {
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
                        p.map(n -> 2 * n).forEach(o -> {
                            if (expected.equals(o)) {
                                emitted.incrementAndGet();
                            }
                        });
                    } catch (final InterruptedException e) {
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
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("did not finish");
        } finally {
            exec.shutdownNow();
        }

    }

}
