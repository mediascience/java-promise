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

import com.msiops.ground.promise.Promise;

public class DegenerateEmitTest {

    private Consumer<Object> c;

    private Promise<Integer> pfulfilled, pbroken;

    private Integer value;

    private Exception x;

    @Before
    public void setup() {

        @SuppressWarnings("unchecked")
        final Consumer<Object> tc = mock(Consumer.class);

        this.x = new Exception();

        this.value = 12;
        this.pfulfilled = Promise.of(this.value);
        this.pbroken = Promise.broken(this.x);

        this.c = tc;

    }

    @Test
    public void testBrokenHandleError() {

        this.pbroken.on(Throwable.class, this.c);

        verify(this.c).accept(this.x);

    }

    @Test
    public void testBrokenHandleErrorMultiple() {

        this.pbroken.on(Throwable.class, this.c);
        this.pbroken.on(Throwable.class, this.c);

        verify(this.c, times(2)).accept(this.x);

    }

    @Test
    public void testBrokenHandleSelectedError() {

        this.pbroken.on(Exception.class, this.c);

        verify(this.c).accept(this.x);

    }

    @Test
    public void testBrokenNotHandleNotSelectedError() {

        this.pbroken.on(RuntimeException.class, this.c);

        verify(this.c, never()).accept(any());

    }

    @Test(expected = NullPointerException.class)
    public void testBrokenNullHandlerIllegal() {

        this.pbroken.on(Throwable.class, null);

    }

    @Test(expected = NullPointerException.class)
    public void testBrokenNullSelectorIllegal() {

        this.pbroken.on(null, this.c);

    }

    @Test
    public void testFulfilledEmitValue() {

        this.pfulfilled.forEach(this.c);

        verify(this.c).accept(this.value);

    }

    @Test
    public void testFulfilledEmitValueMultiple() {

        this.pfulfilled.forEach(this.c);
        this.pfulfilled.forEach(this.c);

        verify(this.c, times(2)).accept(this.value);

    }

    @Test
    public void testFulfilledNotHandled() {

        this.pfulfilled.on(Throwable.class, this.c);

        verify(this.c, never()).accept(any());

    }

    @Test(expected = NullPointerException.class)
    public void testFulfilledNullConsumerIllegal() {

        this.pfulfilled.forEach(null);

    }

    @Test
    public void testFulfilledNullEmit() {

        Promise.of(null).forEach(this.c);

        verify(this.c).accept(null);

    }

}
