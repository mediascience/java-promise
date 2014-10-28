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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;

import com.msiops.ground.promise.Async;
import com.msiops.ground.promise.Promise;

public class DegenerateThenTest {

    private Consumer<Object> c;

    @SuppressWarnings("unused")
    private Promise<Integer> fulfilled, broken;

    private List<Async<Boolean>> retries;

    private Object rvalue;

    private Integer value;

    private List<Async<Object>> work;

    private Exception x;

    @Before
    public void setup() {

        this.work = new ArrayList<>();
        this.retries = new ArrayList<>();

        @SuppressWarnings("unchecked")
        final Consumer<Object> tc = mock(Consumer.class);

        this.value = 12;
        this.fulfilled = Promise.of(this.value);

        this.rvalue = "Hello";

        this.x = new Exception();
        this.broken = Promise.broken(this.x);

        this.c = tc;

    }

    @Test
    public void testSucceedsFirstTime() {

        this.fulfilled.then(this::doWork, this::doRetry).forEach(this.c);

        this.work.get(0).succeed(this.rvalue);

        assertEquals(1, this.work.size());
        assertTrue(this.retries.isEmpty());

        verify(this.c).accept(this.rvalue);

    }

    private Promise<Boolean> doRetry(final Throwable t, final Integer i) {

        final Async<Boolean> a = new Async<>();
        this.retries.add(a);
        return a.promise();

    }

    private Promise<Object> doWork(final Integer i) {

        final Async<Object> a = new Async<>();
        this.work.add(a);
        return a.promise();

    }

}
