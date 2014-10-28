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

public class DegenerateFlatMapTest {

    private Consumer<Object> c;

    private Promise<Integer> fulfilled, broken;

    private Async<Object> inner;

    private Function<Integer, Promise<Object>> mf;

    private Object rvalue;

    private Integer value;

    private Exception x;

    @Before
    public void setup() {

        @SuppressWarnings("unchecked")
        final Function<Integer, Promise<Object>> tmf = mock(Function.class);

        @SuppressWarnings("unchecked")
        final Consumer<Object> tc = mock(Consumer.class);

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
    public void testBoundOnlyOnce() {

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
    public void testFlatMapBroken() {

        /*
         * hack around eclipse bug. Javac doesn't require the lambda expression.
         */
        this.broken.flatMap(i -> this.mf.apply(i)).on(Throwable.class, this.c);

        verify(this.mf, never()).apply(any());
        verify(this.c).accept(this.x);

    }

    @Test
    public void testFlatMapFulfilledBroken() {

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
    public void testFlatMapFulfilledFulfilled() {

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
