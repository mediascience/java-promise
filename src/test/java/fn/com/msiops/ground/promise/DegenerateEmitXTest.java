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

import com.msiops.ground.promise.ConsumerX;
import com.msiops.ground.promise.Promise;

public class DegenerateEmitXTest {

    private ConsumerX<Object, Throwable> c;

    private Promise<Integer> pfulfilled, pbroken;

    private Integer value;

    private Exception x;

    @Before
    public void setup() {

        @SuppressWarnings("unchecked")
        final ConsumerX<Object, Throwable> tc = mock(ConsumerX.class);

        this.x = new Exception();

        this.value = 12;
        this.pfulfilled = Promise.of(this.value);
        this.pbroken = Promise.broken(this.x);

        this.c = tc;

    }

    @Test
    public void testBokenDoesNotEmitValue() throws Throwable {

        this.pbroken.forEachX(this.c);

        verify(this.c, never()).accept(any());

    }

    @Test
    public void testBrokenHandleError() throws Throwable {

        this.pbroken.onX(Throwable.class, this.c);

        verify(this.c).accept(this.x);

    }

    @Test
    public void testBrokenHandleErrorMultiple() throws Throwable {

        this.pbroken.onX(Throwable.class, this.c);
        this.pbroken.onX(Throwable.class, this.c);

        verify(this.c, times(2)).accept(this.x);

    }

    @Test
    public void testBrokenHandleSelectedError() throws Throwable {

        this.pbroken.onX(Exception.class, this.c);

        verify(this.c).accept(this.x);

    }

    @Test
    public void testBrokenNotHandleNotSelectedError() throws Throwable {

        this.pbroken.onX(RuntimeException.class, this.c);

        verify(this.c, never()).accept(any());

    }

    @Test(expected = NullPointerException.class)
    public void testBrokenNullHandlerIllegal() {

        this.pbroken.onX(Throwable.class, null);

    }

    @Test(expected = NullPointerException.class)
    public void testBrokenNullSelectorIllegal() {

        this.pbroken.onX(null, this.c);

    }

    @Test
    public void testForEachFnErrorIgnored() {

        this.pfulfilled.forEachX(v -> {
            throw new RuntimeException();
        });

    }

    @Test
    public void testFulfilledDoesNotEmitError() throws Throwable {

        this.pfulfilled.onX(Throwable.class, this.c);

        verify(this.c, never()).accept(any());

    }

    @Test
    public void testFulfilledHandleValue() throws Throwable {

        this.pfulfilled.forEachX(this.c);

        verify(this.c).accept(this.value);

    }

    @Test
    public void testFulfilledHandleValueMultiple() throws Throwable {

        this.pfulfilled.forEachX(this.c);
        this.pfulfilled.forEachX(this.c);

        verify(this.c, times(2)).accept(this.value);

    }

    @Test(expected = NullPointerException.class)
    public void testFulfilledNullConsumerIllegal() {

        this.pfulfilled.forEachX(null);

    }

    @Test
    public void testOnFnErrorIgnored() {

        this.pbroken.onX(Throwable.class, err -> {
            throw new RuntimeException();
        });

    }

}
