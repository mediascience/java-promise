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

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;

import com.msiops.ground.promise.Async;
import com.msiops.ground.promise.Promise;

public class DegenerateRecoverTest {

    private Consumer<Object> c;

    private Promise<Integer> fulfilled, broken;

    private Async<Object> inner;

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

        this.inner = new Async<>();

        this.value = 12;
        this.fulfilled = Promise.of(this.value);

        this.x = new Exception();
        this.broken = Promise.broken(this.x);

        when(trf.apply(this.x)).thenReturn(this.inner.promise());

        this.rvalue = "Hello";

        this.ix = new RuntimeException();

        this.rf = trf;
        this.c = tc;

    }

    @Test
    public void testBoundOnlyOnce() {

        /*
         * hack around eclipse bug. Javac doesn't require the lambda expression.
         */
        final Promise<?> recovery = this.broken.recover(Exception.class,
                err -> this.rf.apply(err));

        recovery.forEach(this.c);
        recovery.forEach(this.c);

        verify(this.c, never()).accept(any());

        this.inner.succeed(this.rvalue);

        verify(this.rf, times(1)).apply(any());
        verify(this.c, times(2)).accept(Optional.of(this.rvalue));

    }

    @Test
    public void testBrokenBroken() {

        /*
         * hack around eclipse bug. Javac doesn't require the lambda expression.
         */
        this.broken.recover(Exception.class, err -> this.rf.apply(err)).on(
                Throwable.class, this.c);

        verify(this.rf).apply(this.x);
        verify(this.c, never()).accept(any());

        this.inner.fail(this.ix);

        verify(this.c).accept(this.ix);

    }

    @Test
    public void testBrokenFulfilled() {

        /*
         * hack around eclipse bug. Javac doesn't require the lambda expression.
         */
        this.broken.recover(Exception.class, err -> this.rf.apply(err))
                .forEach(this.c);

        verify(this.rf).apply(this.x);
        verify(this.c, never()).accept(any());

        this.inner.succeed(this.rvalue);

        verify(this.c).accept(Optional.of(this.rvalue));

    }

    @Test(expected = NullPointerException.class)
    public void testBrokenNullSelectorIllegal() {

        /*
         * hack around eclipse bug. Javac doesn't require the lambda expression.
         */
        this.fulfilled.recover((Class<Exception>) null,
                err -> this.rf.apply(err));

    }

    @Test(expected = NullPointerException.class)
    public void testBrokenNullTransformedIllegal() {

        this.broken.recover(Throwable.class, null);

    }

    /*
     * This is the case for cancellation. Without cancel, there is no way to
     * observe upstream success from the recovery chain.
     */
    @Test
    public void testFulfilled() {

        /*
         * hack around eclipse bug. Javac doesn't require the lambda expression.
         */
        this.fulfilled.recover(Exception.class, err -> this.rf.apply(err))
                .forEach(this.c);

        verify(this.rf, never()).apply(any());

        /*
         * fulfillment of the original is signaled with an empty fulfillment of
         * the recover promise.
         */
        verify(this.c).accept(Optional.empty());

    }

}
