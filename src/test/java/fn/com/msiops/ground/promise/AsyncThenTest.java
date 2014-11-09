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

import org.junit.Before;
import org.junit.Test;

import com.msiops.footing.functional.FunT1;
import com.msiops.ground.promise.Async;
import com.msiops.ground.promise.Promise;
import com.msiops.ground.promise.Promises;

public class AsyncThenTest {

    private Consumer<Object> c;

    private Async<Object> inner;

    private Promise<Object> m;

    private FunT1<Integer, Promise<Object>> mf;

    private Async<Integer> outer;

    private Object rvalue;

    private Integer value;

    private Exception x;

    @Before
    public void setup() throws Throwable {

        @SuppressWarnings("unchecked")
        final FunT1<Integer, Promise<Object>> tmf = mock(FunT1.class);

        @SuppressWarnings("unchecked")
        final Consumer<Object> tc = mock(Consumer.class);

        this.inner = Promises.async();
        this.outer = Promises.async();

        this.value = 12;

        this.rvalue = "Hello";
        when(tmf.apply(this.value)).thenReturn(this.inner.promise());

        this.x = new Exception();

        /*
         * hack around eclipse bug. Javac doesn't require the lambda expression.
         */
        this.m = this.outer.promise().then(i -> tmf.apply(i));

        this.mf = tmf;
        this.c = tc;

    }

    @Test
    public void testBoundOnlyOnce() throws Throwable {

        this.m.forEach(this.c);
        this.m.forEach(this.c);

        verify(this.c, never()).accept(any());

        this.outer.succeed(this.value);

        verify(this.c, never()).accept(any());

        this.inner.succeed(this.rvalue);

        verify(this.mf, times(1)).apply(any());
        verify(this.c, times(2)).accept(this.rvalue);

    }

    @Test
    public void testContinuationErrorSentDownstream() throws Throwable {

        final RuntimeException x = new RuntimeException();

        this.outer.promise().then(v -> {
            throw x;
        }).on(Throwable.class, this.c);

        verify(this.c, never()).accept(any());

        this.outer.succeed(12);

        verify(this.c).accept(x);

    }

    @Test
    public void testFlatMapBroken() throws Throwable {

        this.m.on(Throwable.class, this.c);

        verify(this.c, never()).accept(any());

        this.outer.fail(this.x);

        verify(this.mf, never()).apply(any());
        verify(this.c).accept(this.x);

    }

    @Test
    public void testFlatMapFulfilledBroken() throws Throwable {

        this.m.on(Throwable.class, this.c);

        verify(this.c, never()).accept(any());

        this.outer.succeed(this.value);

        verify(this.c, never()).accept(any());

        this.inner.fail(this.x);

        verify(this.c).accept(this.x);

    }

    @Test
    public void testFlatMapFulfilledFulfilled() throws Throwable {

        this.m.forEach(this.c);

        verify(this.c, never()).accept(any());

        this.outer.succeed(this.value);

        verify(this.c, never()).accept(any());

        this.inner.succeed(this.rvalue);

        verify(this.c).accept(this.rvalue);

    }

    @Test(expected = NullPointerException.class)
    public void testFulfilledNullTransformedIllegal() {

        this.outer.succeed(this.value);

        this.m.then(null);

    }

    @Test(expected = NullPointerException.class)
    public void testIncompleteNullTransformedIllegal() {

        this.m.then(null);

    }

}
