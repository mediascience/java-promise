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

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;

import com.msiops.ground.promise.Async;
import com.msiops.ground.promise.Promise;

public class AsyncRecoverTest {

    private Consumer<Object> c;

    private Async<Object> inner;
    private Async<Integer> outer;

    private Promise<Object> r;

    private Function<Exception, Promise<Object>> rf;

    private Object rvalue;

    private Integer value;

    private Exception x, ix;

    @Before
    public void setup() {

        @SuppressWarnings("unchecked")
        final Function<Exception, Promise<Object>> trf = mock(Function.class);

        @SuppressWarnings("unchecked")
        final Consumer<Object> tc = mock(Consumer.class);

        this.outer = new Async<>();
        this.inner = new Async<>();

        this.value = 12;

        this.x = new Exception();

        when(trf.apply(this.x)).thenReturn(this.inner.promise());

        /*
         * hack around eclipse bug. Javac doesn't require the lambda expression.
         */
        this.r = this.outer.promise().recover(Exception.class,
                err -> trf.apply(err));

        this.rvalue = "Hello";

        this.ix = new RuntimeException();

        this.rf = trf;
        this.c = tc;

    }

    @Test
    public void testBoundOnlyOnce() {

        this.r.forEach(this.c);
        this.r.forEach(this.c);

        verify(this.rf, never()).apply(any());

        this.outer.fail(this.x);

        verify(this.c, never()).accept(any());

        this.inner.succeed(this.rvalue);

        verify(this.rf, times(1)).apply(any());
        verify(this.c, times(2)).accept(this.rvalue);

    }

    @Test
    public void testBrokenBroken() {

        this.r.on(Throwable.class, this.c);

        verify(this.rf, never()).apply(any());

        this.outer.fail(this.x);

        verify(this.rf).apply(this.x);
        verify(this.c, never()).accept(any());

        this.inner.fail(this.ix);

        verify(this.c).accept(this.ix);

    }

    @Test
    public void testBrokenFulfilled() {

        this.r.forEach(this.c);

        verify(this.rf, never()).apply(any());

        this.outer.fail(this.x);

        verify(this.rf).apply(this.x);
        verify(this.c, never()).accept(any());

        this.inner.succeed(this.rvalue);

        verify(this.c).accept(this.rvalue);

    }

    @Test(expected = NullPointerException.class)
    public void testBrokenNullSelectorIllegal() {

        this.outer.fail(this.x);
        /*
         * hack around eclipse bug. Javac doesn't require the lambda expression.
         */
        this.outer.promise().recover((Class<Exception>) null,
                err -> this.rf.apply(err));

    }

    @Test(expected = NullPointerException.class)
    public void testBrokenNullTransformedIllegal() {

        this.outer.fail(this.x);
        this.outer.promise().recover(Throwable.class, null);

    }

    /*
     * This is the case for cancellation. Without cancel, there is no way to
     * observe upstream success from the recovery chain.
     */
    @Test
    public void testFulfilled() {

        this.outer.succeed(this.value);

        verify(this.rf, never()).apply(any());
        verify(this.c, never()).accept(any());

    }

    @Test(expected = NullPointerException.class)
    public void testIncompleteNullSelectorIllegal() {

        /*
         * hack around eclipse bug. Javac doesn't require the lambda expression.
         */
        this.outer.promise().recover((Class<Exception>) null,
                err -> this.rf.apply(err));

    }

    @Test(expected = NullPointerException.class)
    public void testIncompleteNullTransformedIllegal() {

        this.outer.promise().recover(Throwable.class, null);

    }

}