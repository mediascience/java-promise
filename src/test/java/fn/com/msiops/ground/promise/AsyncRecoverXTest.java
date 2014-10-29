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

public class AsyncRecoverXTest {

    private Consumer<Object> c;

    private Async<Object> inner;
    private Async<Integer> outer;

    private Promise<Optional<Object>> r;

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

        this.r = this.outer.promise().recoverX(Exception.class,
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
        verify(this.c, times(2)).accept(Optional.of(this.rvalue));

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

        verify(this.c).accept(Optional.of(this.rvalue));

    }

    @Test(expected = NullPointerException.class)
    public void testBrokenNullSelectorIllegal() {

        this.outer.fail(this.x);
        this.outer.promise().recoverX((Class<Exception>) null,
                err -> this.rf.apply(err));

    }

    @Test(expected = NullPointerException.class)
    public void testBrokenNullTransformedIllegal() {

        this.outer.fail(this.x);
        this.outer.promise().recoverX(Throwable.class, null);

    }

    @Test
    public void testContinuationErrorSentDownstream() {

        final RuntimeException x = new RuntimeException();
        this.outer.promise().recoverX(Throwable.class, err -> {
            throw x;
        }).on(Throwable.class, this.c);

        verify(this.c, never()).accept(any());

        this.outer.fail(this.x);

        verify(this.c).accept(x);

    }

    @Test
    public void testFulfilled() {

        this.r.forEach(this.c);

        this.outer.succeed(this.value);

        verify(this.rf, never()).apply(any());

        /*
         * fulfillment of the original is signaled with an empty fulfillment of
         * the recoverX promise.
         */
        verify(this.c).accept(Optional.empty());

    }

    @Test(expected = NullPointerException.class)
    public void testIncompleteNullSelectorIllegal() {

        this.outer.promise().recoverX((Class<Exception>) null,
                err -> this.rf.apply(err));

    }

    @Test(expected = NullPointerException.class)
    public void testIncompleteNullTransformedIllegal() {

        this.outer.promise().recoverX(Throwable.class, null);

    }

}
