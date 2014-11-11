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

import com.msiops.ground.promise.Promise;
import com.msiops.ground.promise.Promises;

public class DegenerateEmitTest {

    private Consumer<Object> c;

    private Promise<Integer> pfulfilled, pbroken, pcanceled;

    private Runnable r;

    private Integer value;

    private Exception x;

    @Before
    public void setup() {

        @SuppressWarnings("unchecked")
        final Consumer<Object> tc = mock(Consumer.class);

        this.x = new Exception();

        this.value = 12;
        this.pfulfilled = Promises.fulfilled(this.value);
        this.pbroken = Promises.broken(this.x);
        this.pcanceled = Promises.canceled();

        this.c = tc;
        this.r = mock(Runnable.class);

    }

    @Test
    public void testBrokenDoesNotEmitValue() throws Throwable {

        this.pbroken.forEach(this.c);

        verify(this.c, never()).accept(any());

    }

    @Test
    public void testBrokenEmit() throws Throwable {

        this.pbroken.emit(e -> {
            e.swap().forEach(v -> {
                try {
                    this.c.accept(v);
                } catch (final Throwable t) {
                    throw new AssertionError("should not happen", t);
                }
            });
        });

        verify(this.c).accept(this.x);

    }

    @Test
    public void testBrokenEmitMultiple() throws Throwable {

        this.pbroken.emit(e -> {
            e.swap().forEach(v -> {
                try {
                    this.c.accept(v);
                } catch (final Throwable t) {
                    throw new AssertionError("should not happen", t);
                }
            });
        });

        this.pbroken.emit(e -> {
            e.swap().forEach(v -> {
                try {
                    this.c.accept(v);
                } catch (final Throwable t) {
                    throw new AssertionError("should not happen", t);
                }
            });
        });

        verify(this.c, times(2)).accept(this.x);

    }

    @Test(expected = NullPointerException.class)
    public void testBrokenEmitNullconsumerIllegal() {

        this.pbroken.emit(null);

    }

    @Test
    public void testBrokenHandleError() throws Throwable {

        this.pbroken.on(Throwable.class, this.c);

        verify(this.c).accept(this.x);

    }

    @Test
    public void testBrokenHandleErrorMultiple() throws Throwable {

        this.pbroken.on(Throwable.class, this.c);
        this.pbroken.on(Throwable.class, this.c);

        verify(this.c, times(2)).accept(this.x);

    }

    @Test
    public void testBrokenHandleSelectedError() throws Throwable {

        this.pbroken.on(Exception.class, this.c);

        verify(this.c).accept(this.x);

    }

    @Test
    public void testBrokenNotHandleNotSelectedError() throws Throwable {

        this.pbroken.on(RuntimeException.class, this.c);

        verify(this.c, never()).accept(any());

    }

    @Test(expected = NullPointerException.class)
    public void testBrokenNullHandlerIllegal() {

        this.pbroken.on(Throwable.class, null);

    }

    @Test(expected = NullPointerException.class)
    public void testBrokenNullSelectorIllegal() {

        this.pbroken.on(null, this.c);

    }

    @Test
    public void testBrokenOnCanceled() {

        this.pbroken.onCanceled(this.r);
        verify(this.r, never()).run();

    }

    @Test
    public void testCanceledDoesNotEmitValue() throws Throwable {

        this.pcanceled.forEach(this.c);

        verify(this.c, never()).accept(any());

    }

    @Test
    public void testCanceledEmit() throws Throwable {

        this.pcanceled.emit(e -> {
            e.swap().forEach(v -> {
                try {
                    this.c.accept(v);
                } catch (final Throwable t) {
                    throw new AssertionError("should not happen", t);
                }
            });
        });

        verify(this.c).accept(any(CancellationException.class));

    }

    @Test
    public void testCanceledEmitMultiple() throws Throwable {

        this.pcanceled.emit(e -> {
            e.swap().forEach(v -> {
                try {
                    this.c.accept(v);
                } catch (final Throwable t) {
                    throw new AssertionError("should not happen", t);
                }
            });
        });

        this.pcanceled.emit(e -> {
            e.swap().forEach(v -> {
                try {
                    this.c.accept(v);
                } catch (final Throwable t) {
                    throw new AssertionError("should not happen", t);
                }
            });
        });

        verify(this.c, times(2)).accept(any(CancellationException.class));

    }

    @Test(expected = NullPointerException.class)
    public void testCanceledEmitNullconsumerIllegal() {

        this.pcanceled.emit(null);

    }

    @Test
    public void testCanceledHandleError() throws Throwable {

        this.pcanceled.on(Throwable.class, this.c);

        verify(this.c).accept(any(CancellationException.class));

    }

    @Test
    public void testCanceledHandleErrorMultiple() throws Throwable {

        this.pcanceled.on(Throwable.class, this.c);
        this.pcanceled.on(Throwable.class, this.c);

        verify(this.c, times(2)).accept(any(CancellationException.class));

    }

    @Test
    public void testCanceledHandleSelectedError() throws Throwable {

        this.pcanceled.on(CancellationException.class, this.c);

        verify(this.c).accept(any(CancellationException.class));

    }

    @Test
    public void testCanceledNotHandleNotSelectedError() throws Throwable {

        this.pcanceled.on(NullPointerException.class, this.c);

        verify(this.c, never()).accept(any());

    }

    @Test(expected = NullPointerException.class)
    public void testCanceledNullHandlerIllegal() {

        this.pcanceled.on(Throwable.class, null);

    }

    @Test(expected = NullPointerException.class)
    public void testCanceledNullSelectorIllegal() {

        this.pcanceled.on(null, this.c);

    }

    @Test
    public void testCanceledOnCanceled() {

        this.pcanceled.onCanceled(this.r);
        verify(this.r).run();

    }

    @Test
    public void testForEachFnErrorIgnored() {

        this.pfulfilled.forEach(v -> {
            throw new RuntimeException();
        });

    }

    @Test
    public void testFulfilledDoesNotEmitError() throws Throwable {

        this.pfulfilled.on(Throwable.class, this.c);

        verify(this.c, never()).accept(any());

    }

    @Test
    public void testFulfilledEmit() throws Throwable {

        this.pfulfilled.emit(e -> {
            e.forEach(v -> {
                try {
                    this.c.accept(v);
                } catch (final Throwable t) {
                    throw new AssertionError("should not happen", t);
                }
            });
        });

        verify(this.c).accept(this.value);

    }

    @Test
    public void testFulfilledEmitMultiple() throws Throwable {

        this.pfulfilled.emit(e -> {
            e.forEach(v -> {
                try {
                    this.c.accept(v);
                } catch (final Throwable t) {
                    throw new AssertionError("should not happen", t);
                }
            });
        });

        this.pfulfilled.emit(e -> {
            e.forEach(v -> {
                try {
                    this.c.accept(v);
                } catch (final Throwable t) {
                    throw new AssertionError("should not happen", t);
                }
            });
        });

        verify(this.c, times(2)).accept(this.value);

    }

    @Test(expected = NullPointerException.class)
    public void testFulfilledEmitNullconsumerIllegal() {

        this.pfulfilled.emit(null);

    }

    @Test
    public void testFulfilledHandleValue() throws Throwable {

        this.pfulfilled.forEach(this.c);

        verify(this.c).accept(this.value);

    }

    @Test
    public void testFulfilledHandleValueMultiple() throws Throwable {

        this.pfulfilled.forEach(this.c);
        this.pfulfilled.forEach(this.c);

        verify(this.c, times(2)).accept(this.value);

    }

    @Test(expected = NullPointerException.class)
    public void testFulfilledNullConsumerIllegal() {

        this.pfulfilled.forEach(null);

    }

    @Test
    public void testFulfilledOnCanceled() {

        this.pfulfilled.onCanceled(this.r);
        verify(this.r, never()).run();

    }

    @Test
    public void testOnFnErrorIgnored() {

        this.pbroken.on(Throwable.class, err -> {
            throw new RuntimeException();
        });

    }

}
