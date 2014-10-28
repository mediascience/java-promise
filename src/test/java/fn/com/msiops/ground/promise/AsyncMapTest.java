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

public class AsyncMapTest {

    private Async<Integer> a;

    private Consumer<Object> c;

    private Function<Integer, Object> f;

    private Promise<Object> m;

    private Object rvalue;

    private Integer value;

    private Exception x;

    @Before
    public void setup() {

        @SuppressWarnings("unchecked")
        final Function<Integer, Object> tf = mock(Function.class);

        @SuppressWarnings("unchecked")
        final Consumer<Object> tc = mock(Consumer.class);

        this.value = 12;

        this.rvalue = "Hello";
        when(tf.apply(this.value)).thenReturn(this.rvalue);

        this.x = new Exception();

        this.a = new Async<>();
        this.m = this.a.promise().map(tf);

        this.f = tf;
        this.c = tc;

    }

    @Test(expected = NullPointerException.class)
    public void testFulfilledNullTransformedIllegal() {

        this.a.succeed(this.value);
        this.a.promise().map(null);
    }

    @Test(expected = NullPointerException.class)
    public void testIncompleteNullTransformedIllegal() {

        this.m.map(null);
    }

    @Test
    public void testMapBroken() {

        this.m.on(Throwable.class, this.c);

        verify(this.c, never()).accept(any());

        this.a.fail(this.x);

        verify(this.f, never()).apply(any());
        verify(this.c).accept(this.x);

    }

    @Test
    public void testMapFulfilled() {

        this.m.forEach(this.c);

        verify(this.f, never()).apply(any());
        verify(this.c, never()).accept(any());

        this.a.succeed(this.value);

        verify(this.c).accept(this.rvalue);

    }

    @Test
    public void testTransformedOnlyOnce() {

        this.m.forEach(this.c);
        this.m.forEach(this.c);

        verify(this.f, never()).apply(any());
        verify(this.c, never()).accept(any());

        this.a.succeed(this.value);

        verify(this.f, times(1)).apply(any());
        verify(this.c, times(2)).accept(this.rvalue);

    }

}