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

import org.junit.Before;
import org.junit.Test;

import com.msiops.ground.promise.Async;
import com.msiops.ground.promise.ConsumerX;
import com.msiops.ground.promise.FunctionX;
import com.msiops.ground.promise.Promise;

public class DegenerateFlatMapTest {

    private ConsumerX<Object, Throwable> c;

    private Promise<Integer> fulfilled, broken;

    private Async<Object> inner;

    private FunctionX<Integer, Promise<Object>, Throwable> mf;

    private Object rvalue;

    private Integer value;

    private Exception x;

    @Before
    public void setup() throws Throwable {

        @SuppressWarnings("unchecked")
        final FunctionX<Integer, Promise<Object>, Throwable> tmf = mock(FunctionX.class);

        @SuppressWarnings("unchecked")
        final ConsumerX<Object, Throwable> tc = mock(ConsumerX.class);

        this.inner = new Async<>();

        this.value = 12;
        this.fulfilled = Promise.of(this.value);

        this.rvalue = "Hello";
        when(tmf.apply(this.value)).thenReturn(this.inner.promise());

        this.x = new Exception();
        this.broken = Promise.broken(this.x);

        this.mf = tmf;
        this.c = tc;

    }

    @Test
    public void testBoundOnlyOnce() throws Throwable {

        /*
         * hack around eclipse bug. Javac doesn't require the lambda expression.
         */
        final Promise<?> mapped = this.fulfilled.flatMap(i -> this.mf.apply(i));

        mapped.forEach(this.c);
        mapped.forEach(this.c);

        verify(this.c, never()).accept(any());

        this.inner.succeed(this.rvalue);

        verify(this.mf, times(1)).apply(any());
        verify(this.c, times(2)).accept(this.rvalue);

    }

    @Test
    public void testContinuationErrorSentDownstream() throws Throwable {

        final RuntimeException x = new RuntimeException();

        this.fulfilled.flatMap(v -> {
            throw x;
        }).on(Throwable.class, this.c);

        verify(this.c).accept(x);

    }

    @Test
    public void testFlatMapBroken() throws Throwable {

        /*
         * hack around eclipse bug. Javac doesn't require the lambda expression.
         */
        this.broken.flatMap(i -> this.mf.apply(i)).on(Throwable.class, this.c);

        verify(this.mf, never()).apply(any());
        verify(this.c).accept(this.x);

    }

    @Test
    public void testFlatMapFulfilledBroken() throws Throwable {

        /*
         * hack around eclipse bug. Javac doesn't require the lambda expression.
         */
        this.fulfilled.flatMap(i -> this.mf.apply(i)).on(Throwable.class,
                this.c);

        verify(this.c, never()).accept(any());

        this.inner.fail(this.x);

        verify(this.c).accept(this.x);

    }

    @Test
    public void testFlatMapFulfilledFulfilled() throws Throwable {

        /*
         * hack around eclipse bug. Javac doesn't require the lambda expression.
         */
        this.fulfilled.flatMap(i -> this.mf.apply(i)).forEach(this.c);

        verify(this.c, never()).accept(any());

        this.inner.succeed(this.rvalue);

        verify(this.c).accept(this.rvalue);

    }

    @Test(expected = NullPointerException.class)
    public void testFulfilledNullTransformedIllegal() {

        this.fulfilled.flatMap(null);

    }

}
