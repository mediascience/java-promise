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

import static org.mockito.Mockito.*;

import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;

import com.msiops.footing.functional.SupplierT;
import com.msiops.ground.promise.Async;
import com.msiops.ground.promise.Promise;
import com.msiops.ground.promise.Promises;

public class DegenerateDeferTest {

    private Consumer<Object> c;

    private Promise<Integer> fulfilled, broken;

    private Async<Object> inner;

    private Object rvalue;

    private SupplierT<Promise<Object>> src;

    private Integer value;

    private Exception x, ix;

    @Before
    public void setup() throws Throwable {

        @SuppressWarnings("unchecked")
        final SupplierT<Promise<Object>> tsrc = mock(SupplierT.class);

        @SuppressWarnings("unchecked")
        final Consumer<Object> tc = mock(Consumer.class);

        this.inner = Promises.async();

        this.value = 12;
        this.fulfilled = Promises.fulfilled(this.value);

        this.rvalue = "Hello";
        when(tsrc.get()).thenReturn(this.inner.promise());

        this.x = new Exception();
        this.broken = Promises.broken(this.x);

        this.ix = new IllegalArgumentException();

        this.src = tsrc;
        this.c = tc;

    }

    @Test
    public void testBrokenDeferBroken() throws Throwable {

        /*
         * hack around eclipse bug. Javac doesn't require the lambda expression.
         */
        this.broken.defer(() -> this.src.get()).on(Throwable.class, this.c);

        this.inner.fail(this.ix);

        verify(this.c).accept(this.ix);

    }

    @Test
    public void testBrokenDeferFulfilled() throws Throwable {

        /*
         * hack around eclipse bug. Javac doesn't require the lambda expression.
         */
        this.broken.defer(() -> this.src.get()).forEach(this.c);

        this.inner.succeed(this.rvalue);

        verify(this.c).accept(this.rvalue);

    }

    @Test
    public void testBrokenFinalizedOnlyOnce() throws Throwable {

        /*
         * hack around eclipse bug. Javac doesn't require the lambda expression.
         */
        final Promise<?> fin = this.broken.defer(() -> this.src.get());

        fin.forEach(this.c);
        fin.forEach(this.c);

        this.inner.succeed(this.rvalue);

        verify(this.src, times(1)).get();
        verify(this.c, times(2)).accept(this.rvalue);

    }

    @Test(expected = NullPointerException.class)
    public void testBrokenNullSourceIllegal() {

        this.fulfilled.defer(null);

    }

    @Test
    public void testContinuationErrorSentDownstream() throws Throwable {

        final Exception x = new Exception();

        this.fulfilled.defer(() -> {
            throw x;
        }).on(Throwable.class, this.c);

        verify(this.c).accept(x);

    }

    @Test
    public void testFulfilledDeferBroken() throws Throwable {

        /*
         * hack around eclipse bug. Javac doesn't require the lambda expression.
         */
        this.fulfilled.defer(() -> this.src.get()).on(Throwable.class, this.c);

        this.inner.fail(this.ix);

        verify(this.c).accept(this.ix);

    }

    @Test
    public void testFulfilledDeferFulfilled() throws Throwable {

        /*
         * hack around eclipse bug. Javac doesn't require the lambda expression.
         */
        this.fulfilled.defer(() -> this.src.get()).forEach(this.c);

        this.inner.succeed(this.rvalue);

        verify(this.c).accept(this.rvalue);

    }

    @Test
    public void testFulfilledFinalizedOnlyOnce() throws Throwable {

        /*
         * hack around eclipse bug. Javac doesn't require the lambda expression.
         */
        final Promise<?> fin = this.fulfilled.defer(() -> this.src.get());

        fin.forEach(this.c);
        fin.forEach(this.c);

        this.inner.succeed(this.rvalue);

        verify(this.src, times(1)).get();
        verify(this.c, times(2)).accept(this.rvalue);

    }

    @Test(expected = NullPointerException.class)
    public void testFulfilledNullSourceIllegal() {

        this.fulfilled.defer(null);

    }

}
