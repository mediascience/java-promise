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
import java.util.function.Predicate;

import org.junit.Before;
import org.junit.Test;

import com.msiops.ground.promise.Async;
import com.msiops.ground.promise.Promise;
import com.msiops.ground.promise.Promises;

public class AsyncWhenTest {

    private Async<Integer> a;

    private Consumer<Object> cval, cx;

    private Predicate<Integer> ptrue, pfalse;

    private Integer value;

    private Exception x;

    @Before
    public void setup() {

        @SuppressWarnings("unchecked")
        final Predicate<Integer> tptrue = mock(Predicate.class);

        @SuppressWarnings("unchecked")
        final Predicate<Integer> tpfalse = mock(Predicate.class);

        @SuppressWarnings("unchecked")
        final Consumer<Object> tcval = mock(Consumer.class);

        @SuppressWarnings("unchecked")
        final Consumer<Object> tcx = mock(Consumer.class);

        this.a = Promises.async();

        this.value = 12;

        this.x = new Exception();

        when(tptrue.test(any())).thenReturn(true);
        when(tpfalse.test(any())).thenReturn(false);

        this.ptrue = tptrue;
        this.pfalse = tpfalse;

        this.cval = tcval;
        this.cx = tcx;

    }

    @Test(expected = NullPointerException.class)
    public void testFulfilledNullPredicateIllegal() {

        this.a.succeed(this.value);
        this.a.promise().when(null);

    }

    @Test(expected = NullPointerException.class)
    public void testIncompleteNullPredicateIllegal() {

        this.a.promise().when(null);

    }

    @Test
    public void testMultipleChoice() {

        this.a.promise().when(this.ptrue).forEach(this.cval::accept);
        this.a.promise().when(this.ptrue).forEach(this.cval::accept);
        this.a.promise().when(this.pfalse).forEach(this.cval::accept);
        this.a.promise().when(this.pfalse).forEach(this.cval::accept);

        this.a.succeed(this.value);

        verify(this.cval, times(2)).accept(this.value);

    }

    @Test
    public void testTestedOnlyOnce() {

        final Promise<Integer> p = this.a.promise().when(this.ptrue);

        p.forEach(this.cval);
        p.forEach(this.cval);

        this.a.succeed(this.value);

        verify(this.ptrue, times(1)).test(this.value);
        verify(this.cval, times(2)).accept(this.value);

    }

    @Test
    public void testThrownExceptionSentDownstream() {

        final RuntimeException otherx = new RuntimeException();

        this.a.promise().when(v -> {
            throw otherx;
        }).on(Throwable.class, this.cx);

        verify(this.cx, never()).accept(any());

        this.a.succeed(this.value);

        verify(this.cx).accept(otherx);

    }

    @Test
    public void testWhenBlockFulfilled() {

        final Promise<Integer> p = this.a.promise().when(this.pfalse);
        p.forEach(this.cval::accept);
        p.on(Throwable.class, this.cx::accept);

        this.a.succeed(this.value);

        verify(this.cval, never()).accept(any());
        verify(this.cx, never()).accept(any());

    }

    @Test
    public void testWhenBroken() {

        final Promise<Integer> p = this.a.promise().when(this.ptrue);
        p.forEach(this.cval::accept);
        p.on(Throwable.class, this.cx::accept);

        this.a.fail(this.x);

        verify(this.ptrue, never()).test(any());
        verify(this.cval, never()).accept(any());
        verify(this.cx).accept(this.x);

    }

    @Test
    public void testWhenPassFulfilled() {

        final Promise<Integer> p = this.a.promise().when(this.ptrue);
        p.forEach(this.cval::accept);
        p.on(Throwable.class, this.cx::accept);

        verify(this.cval, never()).accept(any());

        this.a.succeed(this.value);

        verify(this.cval).accept(this.value);
        verify(this.cx, never()).accept(any());

    }

}
