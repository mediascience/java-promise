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
import com.msiops.ground.promise.Promise;

public class AsyncEmitXTest {

    private Async<Integer> a;

    private ConsumerX<Object, Throwable> c;

    private Promise<Integer> p;

    private Integer value;

    private Exception x;

    @Before
    public void setup() {

        @SuppressWarnings("unchecked")
        final ConsumerX<Object, Throwable> tc = mock(ConsumerX.class);

        this.x = new Exception();

        this.value = 12;
        this.a = new Async<>();
        this.p = this.a.promise();

        this.c = tc;

    }

    @Test
    public void testBrokenHandleError() throws Throwable {

        this.p.onX(Throwable.class, this.c);

        verify(this.c, never()).accept(any());

        this.a.fail(this.x);

        verify(this.c).accept(this.x);

    }

    @Test
    public void testBrokenHandleErrorMultiple() throws Throwable {

        this.p.onX(Throwable.class, this.c);
        this.p.onX(Throwable.class, this.c);

        verify(this.c, never()).accept(any());

        this.a.fail(this.x);

        verify(this.c, times(2)).accept(this.x);

    }

    @Test
    public void testBrokenHandleSelectedError() throws Throwable {

        this.p.onX(Exception.class, this.c);

        this.a.fail(this.x);

        verify(this.c).accept(this.x);

    }

    @Test
    public void testBrokenNotHandleNotSelectedError() throws Throwable {

        this.p.onX(RuntimeException.class, this.c);

        this.a.fail(this.x);

        verify(this.c, never()).accept(any());

    }

    @Test(expected = NullPointerException.class)
    public void testBrokenNullErrorHandlerIllegal() {

        this.a.fail(this.x);
        this.p.onX(Throwable.class, null);

    }

    @Test(expected = NullPointerException.class)
    public void testBrokenNullSelectorIllegal() {

        this.a.fail(this.x);
        this.p.onX(null, this.c);

    }

    @Test
    public void testForEachFnErrorIgnored() {

        this.p.forEachX(v -> {
            throw new RuntimeException();
        });

        this.a.succeed(this.value);

    }

    @Test
    public void testFulfilledHandleMultiple() throws Throwable {

        this.p.forEachX(this.c);
        this.p.forEachX(this.c);

        verify(this.c, never()).accept(any());

        this.a.succeed(this.value);

        verify(this.c, times(2)).accept(this.value);

    }

    @Test
    public void testFulfilledHandleValue() throws Throwable {

        this.p.forEachX(this.c);

        verify(this.c, never()).accept(any());

        this.a.succeed(this.value);

        verify(this.c).accept(this.value);

    }

    @Test
    public void testFulfilledNotHandledError() throws Throwable {

        this.p.onX(Throwable.class, this.c);

        verify(this.c, never()).accept(any());

    }

    @Test(expected = NullPointerException.class)
    public void testFulfilledNullValueHandlerIllegal() {

        this.a.succeed(this.value);
        this.p.forEachX(null);

    }

    @Test(expected = NullPointerException.class)
    public void testIncompleteNullConsumerIllegal() {

        this.p.forEachX(null);

    }

    @Test(expected = NullPointerException.class)
    public void testIncompleteNullHandlerIllegal() {

        this.p.onX(Throwable.class, null);

    }

    @Test(expected = NullPointerException.class)
    public void testIncompleteNullSelectorIllegal() {

        this.p.onX(null, this.c);

    }

    @Test
    public void testOnFnErrorIgnored() {

        this.p.onX(Throwable.class, err -> {
            throw new RuntimeException();
        });

        this.a.fail(this.x);

    }

}
