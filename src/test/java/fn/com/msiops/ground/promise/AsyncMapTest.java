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

import com.msiops.ground.promise.Async;
import com.msiops.ground.promise.FunctionX;
import com.msiops.ground.promise.Promise;
import com.msiops.ground.promise.Promises;

public class AsyncMapTest {

    private Async<Integer> a;

    private Consumer<Object> c;

    private FunctionX<Integer, Object> f;

    private Promise<Object> m;

    private Object rvalue;

    private Integer value;

    private Exception x;

    @Before
    public void setup() throws Throwable {

        @SuppressWarnings("unchecked")
        final FunctionX<Integer, Object> tf = mock(FunctionX.class);

        @SuppressWarnings("unchecked")
        final Consumer<Object> tc = mock(Consumer.class);

        this.value = 12;

        this.rvalue = "Hello";
        when(tf.apply(this.value)).thenReturn(this.rvalue);

        this.x = new Exception();

        this.a = Promises.async();
        this.m = this.a.promise().map(v -> tf.apply(v));

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
    public void testMapBroken() throws Throwable {

        this.m.on(Throwable.class, this.c);

        verify(this.c, never()).accept(any());

        this.a.fail(this.x);

        verify(this.f, never()).apply(any());
        verify(this.c).accept(this.x);

    }

    @Test
    public void testMapFulfilled() throws Throwable {

        this.m.forEach(this.c);

        verify(this.f, never()).apply(any());
        verify(this.c, never()).accept(any());

        this.a.succeed(this.value);

        verify(this.c).accept(this.rvalue);

    }

    @Test
    public void testThrownExceptionSentDownstream() throws Throwable {

        final RuntimeException x = new RuntimeException();

        this.a.promise().map(v -> {
            throw x;
        }).on(Throwable.class, this.c);

        verify(this.c, never()).accept(any());

        this.a.succeed(12);

        verify(this.c).accept(x);

    }

    @Test
    public void testTransformedOnlyOnce() throws Throwable {

        this.m.forEach(this.c);
        this.m.forEach(this.c);

        verify(this.f, never()).apply(any());
        verify(this.c, never()).accept(any());

        this.a.succeed(this.value);

        verify(this.f, times(1)).apply(any());
        verify(this.c, times(2)).accept(this.rvalue);

    }

}
