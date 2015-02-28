/**
 * Licensed under the Apache License, Version 2.0 (the "License") under
 * one or more contributor license agreements. See the NOTICE file
 * distributed with this work for information regarding copyright
 * ownership. You may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import com.msiops.ground.promise.Promises;

public class AsyncMapErrorTest {

    private Async<Integer> a;

    private Consumer<Object> c;

    private Function<Exception, Throwable> f;

    private Promise<Integer> m;

    private AssertionError mappedX;

    private Integer value;

    private Exception x;

    @Before
    public void setup() throws Throwable {

        @SuppressWarnings("unchecked")
        final Function<Exception, Throwable> tf = mock(Function.class);

        @SuppressWarnings("unchecked")
        final Consumer<Object> tc = mock(Consumer.class);

        this.value = 12;

        this.x = new Exception();
        this.mappedX = new AssertionError("replacement");
        when(tf.apply(this.x)).thenReturn(this.mappedX);

        this.f = tf;
        this.c = tc;

        this.a = Promises.async();
        this.m = this.a.promise().mapError(Exception.class, this.f);
    }

    @Test(expected = NullPointerException.class)
    public void testBrokenNullTransformIllegal() {

        this.a.fail(this.x);
        this.a.promise().mapError(Exception.class, null);
    }

    @Test(expected = NullPointerException.class)
    public void testIncompleteNullTransformedIllegal() {

        this.a.promise().mapError(Exception.class, null);
    }

    @Test
    public void testMapBroken() throws Throwable {

        this.m.on(Throwable.class, this.c);

        verify(this.c, never()).accept(any());

        this.a.fail(this.x);

        verify(this.c).accept(this.mappedX);

    }

    @Test
    public void testMapFulfilled() throws Throwable {

        this.m.forEach(this.c);

        verify(this.c, never()).accept(any());

        this.a.succeed(this.value);

        verify(this.f, never()).apply(any());
        verify(this.c).accept(this.value);

    }

    @Test
    public void testThrownExceptionSentDownstream() throws Throwable {

        this.a.promise().mapError(Exception.class, x -> {
            throw this.mappedX;
        }).on(Throwable.class, this.c);

        verify(this.c, never()).accept(any());

        this.a.fail(this.x);

        verify(this.c).accept(this.mappedX);

    }

    @Test
    public void testTransformedOnlyOnce() throws Throwable {

        this.m.on(Throwable.class, this.c);
        this.m.on(Throwable.class, this.c);

        verify(this.f, never()).apply(any());
        verify(this.c, never()).accept(any());

        this.a.fail(this.x);

        verify(this.f, times(1)).apply(any());
        verify(this.c, times(2)).accept(this.mappedX);

    }

}
