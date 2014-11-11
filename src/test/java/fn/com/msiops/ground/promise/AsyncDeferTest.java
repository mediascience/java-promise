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

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;

import com.msiops.ground.promise.Async;
import com.msiops.ground.promise.Promise;
import com.msiops.ground.promise.Promises;

public class AsyncDeferTest {

    private Consumer<Object> c;

    private Promise<Object> d;

    private Async<Object> inner;

    private Async<Integer> outer;

    private Object rvalue;

    private Supplier<Promise<Object>> src;

    private Integer value;

    private Exception x, ix;

    @Before
    public void setup() {

        @SuppressWarnings("unchecked")
        final Supplier<Promise<Object>> tsrc = mock(Supplier.class);

        @SuppressWarnings("unchecked")
        final Consumer<Object> tc = mock(Consumer.class);

        this.outer = Promises.async();
        this.inner = Promises.async();

        this.value = 12;

        this.rvalue = "Hello";
        when(tsrc.get()).thenReturn(this.inner.promise());

        this.x = new Exception();
        this.ix = new IllegalArgumentException();

        /*
         * hack around eclipse bug. Javac doesn't require the lambda expression.
         */
        this.d = this.outer.promise().defer(() -> tsrc.get());

        this.src = tsrc;
        this.c = tc;

    }

    @Test
    public void testBrokenDeferBroken() throws Throwable {

        this.d.on(Throwable.class, this.c);

        verify(this.src, never()).get();

        this.outer.fail(this.x);

        verify(this.src).get();

        this.inner.fail(this.ix);

        verify(this.c).accept(this.ix);

    }

    @Test
    public void testBrokenDeferFulfilled() throws Throwable {

        this.d.forEach(this.c);

        verify(this.src, never()).get();

        this.outer.fail(this.x);

        verify(this.src).get();

        this.inner.succeed(this.rvalue);

        verify(this.c).accept(this.rvalue);

    }

    @Test
    public void testBrokenFinalizedOnlyOnce() throws Throwable {

        this.d.forEach(this.c);
        this.d.forEach(this.c);

        this.outer.fail(this.x);
        this.inner.succeed(this.rvalue);

        verify(this.src, times(1)).get();
        verify(this.c, times(2)).accept(this.rvalue);

    }

    @Test(expected = NullPointerException.class)
    public void testBrokenNullSourceIllegal() {

        this.outer.fail(this.x);

        this.outer.promise().defer(null);

    }

    @Test
    public void testContinuationErrorSentDownstream() throws Throwable {

        final Exception x = new Exception();
        this.outer.promise().defer(() -> {
            throw x;
        }).on(Throwable.class, this.c);

        verify(this.c, never()).accept(any());

        this.outer.succeed(this.value);

        verify(this.c).accept(x);

    }

    @Test
    public void testFulfilledDeferBroken() throws Throwable {

        this.d.on(Throwable.class, this.c);

        verify(this.src, never()).get();

        this.outer.succeed(this.value);

        verify(this.src).get();

        this.inner.fail(this.ix);

        verify(this.c).accept(this.ix);

    }

    @Test
    public void testFulfilledDeferFulfilled() throws Throwable {

        this.d.forEach(this.c);

        verify(this.src, never()).get();

        this.outer.succeed(this.value);

        verify(this.src).get();

        this.inner.succeed(this.rvalue);

        verify(this.c).accept(this.rvalue);

    }

    @Test
    public void testFulfilledFinalizedOnlyOnce() throws Throwable {

        this.d.forEach(this.c);
        this.d.forEach(this.c);

        this.outer.succeed(this.value);

        verify(this.src, times(1)).get();

        this.inner.succeed(this.rvalue);

        verify(this.c, times(2)).accept(this.rvalue);

    }

    @Test(expected = NullPointerException.class)
    public void testFulfilledNullSourceIllegal() {

        this.outer.succeed(this.value);

        this.outer.promise().defer(null);

    }

    @Test(expected = NullPointerException.class)
    public void testIncompleteNullSourceIllegal() {

        this.outer.promise().defer(null);

    }

}
