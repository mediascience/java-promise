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

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.CancellationException;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;

import com.msiops.ground.promise.Async;
import com.msiops.ground.promise.Promise;
import com.msiops.ground.promise.Promises;

public class AsyncEmitTest {

    private Async<Integer> a;

    private Consumer<Object> c;

    private Promise<Integer> p;

    private Runnable r;

    private Integer value;

    private Exception x;

    @Before
    public void setup() {

        @SuppressWarnings("unchecked")
        final Consumer<Object> tc = mock(Consumer.class);

        this.x = new Exception();

        this.value = 12;
        this.a = Promises.async();
        this.p = this.a.promise();

        this.c = tc;
        this.r = mock(Runnable.class);

    }

    @Test
    public void testBrokenEmit() throws Throwable {

        this.p.emit(e -> {
            e.swap().forEach(v -> {
                try {
                    this.c.accept(v);
                } catch (final Throwable t) {
                    throw new AssertionError("should not happen", t);
                }
            });
        });

        verify(this.c, never()).accept(any());

        this.a.fail(this.x);

        verify(this.c).accept(this.x);

    }

    @Test
    public void testBrokenEmitMultiple() throws Throwable {

        this.p.emit(e -> {
            e.swap().forEach(v -> {
                try {
                    this.c.accept(v);
                } catch (final Throwable t) {
                    throw new AssertionError("should not happen", t);
                }
            });
        });

        this.p.emit(e -> {
            e.swap().forEach(v -> {
                try {
                    this.c.accept(v);
                } catch (final Throwable t) {
                    throw new AssertionError("should not happen", t);
                }
            });
        });

        verify(this.c, never()).accept(any());

        this.a.fail(this.x);

        verify(this.c, times(2)).accept(this.x);

    }

    @Test
    public void testBrokenHandleError() throws Throwable {

        this.p.on(Throwable.class, this.c);

        verify(this.c, never()).accept(any());

        this.a.fail(this.x);

        verify(this.c).accept(this.x);

    }

    @Test
    public void testBrokenHandleErrorMultiple() throws Throwable {

        this.p.on(Throwable.class, this.c);
        this.p.on(Throwable.class, this.c);

        verify(this.c, never()).accept(any());

        this.a.fail(this.x);

        verify(this.c, times(2)).accept(this.x);

    }

    @Test
    public void testBrokenHandleSelectedError() throws Throwable {

        this.p.on(Exception.class, this.c);

        this.a.fail(this.x);

        verify(this.c).accept(this.x);

    }

    @Test
    public void testBrokenNotHandleNotSelectedError() throws Throwable {

        this.p.on(RuntimeException.class, this.c);

        this.a.fail(this.x);

        verify(this.c, never()).accept(any());

    }

    @Test(expected = NullPointerException.class)
    public void testBrokenNullErrorHandlerIllegal() {

        this.a.fail(this.x);
        this.p.on(Throwable.class, null);

    }

    @Test(expected = NullPointerException.class)
    public void testBrokenNullSelectorIllegal() {

        this.a.fail(this.x);
        this.p.on(null, this.c);

    }

    @Test
    public void testBrokenOnCanceled() {

        this.a.promise().onCanceled(this.r);
        this.a.fail(this.x);
        verify(this.r, never()).run();

    }

    @Test
    public void testCanceledEmit() throws Throwable {

        this.p.emit(e -> {
            e.swap().forEach(v -> {
                try {
                    this.c.accept(v);
                } catch (final Throwable t) {
                    throw new AssertionError("should not happen", t);
                }
            });
        });

        verify(this.c, never()).accept(any());

        this.a.cancel();

        verify(this.c).accept(any(CancellationException.class));

    }

    @Test
    public void testCanceledEmitMultiple() throws Throwable {

        this.p.emit(e -> {
            e.swap().forEach(v -> {
                try {
                    this.c.accept(v);
                } catch (final Throwable t) {
                    throw new AssertionError("should not happen", t);
                }
            });
        });

        this.p.emit(e -> {
            e.swap().forEach(v -> {
                try {
                    this.c.accept(v);
                } catch (final Throwable t) {
                    throw new AssertionError("should not happen", t);
                }
            });
        });

        verify(this.c, never()).accept(any());

        this.a.cancel();

        verify(this.c, times(2)).accept(any(CancellationException.class));

    }

    @Test
    public void testCanceledHandleError() throws Throwable {

        this.p.on(Throwable.class, this.c);

        verify(this.c, never()).accept(any());

        this.a.cancel();

        verify(this.c).accept(any(CancellationException.class));

    }

    @Test
    public void testCanceledHandleErrorMultiple() throws Throwable {

        this.p.on(Throwable.class, this.c);
        this.p.on(Throwable.class, this.c);

        verify(this.c, never()).accept(any());

        this.a.cancel();

        verify(this.c, times(2)).accept(any(CancellationException.class));

    }

    @Test
    public void testCanceledHandleSelectedError() throws Throwable {

        this.p.on(CancellationException.class, this.c);

        this.a.cancel();

        verify(this.c).accept(any(CancellationException.class));

    }

    @Test
    public void testCanceledNotHandleNotSelectedError() throws Throwable {

        this.p.on(NullPointerException.class, this.c);

        this.a.cancel();

        verify(this.c, never()).accept(any());

    }

    @Test(expected = NullPointerException.class)
    public void testCanceledNullErrorHandlerIllegal() {

        this.a.cancel();
        this.p.on(Throwable.class, null);

    }

    @Test(expected = NullPointerException.class)
    public void testCanceledNullHandlerIllegal() {

        this.a.cancel();
        this.a.promise().onCanceled(null);

    }

    @Test(expected = NullPointerException.class)
    public void testCanceledNullSelectorIllegal() {

        this.a.cancel();
        this.p.on(null, this.c);

    }

    @Test
    public void testCanceledOnCanceled() {

        this.a.promise().onCanceled(this.r);
        verify(this.c, never()).accept(any());
        this.a.cancel();
        verify(this.r).run();

    }

    @Test
    public void testForEachFnErrorIgnored() {

        this.p.forEach(v -> {
            throw new RuntimeException();
        });

        this.a.succeed(this.value);

    }

    @Test
    public void testFulfilledEmit() throws Throwable {

        this.p.emit(e -> {
            e.forEach(v -> {
                try {
                    this.c.accept(v);
                } catch (final Throwable t) {
                    throw new AssertionError("should not happen", t);
                }
            });
        });

        verify(this.c, never()).accept(any());

        this.a.succeed(this.value);

        verify(this.c).accept(this.value);

    }

    @Test
    public void testFulfilledEmitMultiple() throws Throwable {

        this.p.emit(e -> {
            e.forEach(v -> {
                try {
                    this.c.accept(v);
                } catch (final Throwable t) {
                    throw new AssertionError("should not happen", t);
                }
            });
        });

        this.p.emit(e -> {
            e.forEach(v -> {
                try {
                    this.c.accept(v);
                } catch (final Throwable t) {
                    throw new AssertionError("should not happen", t);
                }
            });
        });

        verify(this.c, never()).accept(any());

        this.a.succeed(this.value);

        verify(this.c, times(2)).accept(this.value);

    }

    @Test
    public void testFulfilledHandleMultiple() throws Throwable {

        this.p.forEach(this.c);
        this.p.forEach(this.c);

        verify(this.c, never()).accept(any());

        this.a.succeed(this.value);

        verify(this.c, times(2)).accept(this.value);

    }

    @Test
    public void testFulfilledHandleValue() throws Throwable {

        this.p.forEach(this.c);

        verify(this.c, never()).accept(any());

        this.a.succeed(this.value);

        verify(this.c).accept(this.value);

    }

    @Test
    public void testFulfilledNotHandledError() throws Throwable {

        this.p.on(Throwable.class, this.c);

        verify(this.c, never()).accept(any());

    }

    @Test(expected = NullPointerException.class)
    public void testFulfilledNullValueHandlerIllegal() {

        this.a.succeed(this.value);
        this.p.forEach(null);

    }

    @Test
    public void testFulfilledOnCanceled() {

        this.a.promise().onCanceled(this.r);
        this.a.succeed(this.value);
        verify(this.r, never()).run();

    }

    @Test(expected = NullPointerException.class)
    public void testIncompleteEmitNullconsumerIllegal() {

        this.p.emit(null);

    }

    @Test(expected = NullPointerException.class)
    public void testIncompleteNullCancelHandlerIllegal() {

        this.a.promise().onCanceled(null);

    }

    @Test(expected = NullPointerException.class)
    public void testIncompleteNullConsumerIllegal() {

        this.p.forEach(null);

    }

    @Test(expected = NullPointerException.class)
    public void testIncompleteNullHandlerIllegal() {

        this.p.on(Throwable.class, null);

    }

    @Test(expected = NullPointerException.class)
    public void testIncompleteNullSelectorIllegal() {

        this.p.on(null, this.c);

    }

    @Test
    public void testOnFnErrorIgnored() {

        this.p.on(Throwable.class, err -> {
            throw new RuntimeException();
        });

        this.a.fail(this.x);

    }

}
