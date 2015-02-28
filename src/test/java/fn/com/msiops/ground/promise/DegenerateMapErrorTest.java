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

import com.msiops.ground.promise.Promise;
import com.msiops.ground.promise.Promises;

public class DegenerateMapErrorTest {

    private Consumer<Object> c;

    private Function<Exception, Throwable> f;

    private Promise<Integer> fulfilled, broken;

    private Throwable mappedX;

    private Integer value;

    private Exception x;

    @Before
    public void setup() {

        @SuppressWarnings("unchecked")
        final Function<Exception, Throwable> tf = mock(Function.class);

        @SuppressWarnings("unchecked")
        final Consumer<Object> tc = mock(Consumer.class);

        this.value = 12;
        this.fulfilled = Promises.fulfilled(this.value);

        this.x = new Exception();
        this.broken = Promises.broken(this.x);

        this.mappedX = new AssertionError("replacement");
        when(tf.apply(this.x)).thenReturn(this.mappedX);

        this.f = tf;
        this.c = tc;

    }

    @Test(expected = NullPointerException.class)
    public void testFulfilledNullTransformedIllegal() {

        this.fulfilled.map(null);

    }

    @Test
    public void testMapBroken() throws Throwable {

        this.broken.mapError(Exception.class, this.f).on(Throwable.class,
                this.c);

        verify(this.c).accept(this.mappedX);

    }

    @Test
    public void testMapFulfilled() throws Throwable {

        this.fulfilled.mapError(Throwable.class,
                x -> new AssertionError("ignored")).forEach(this.c);

        verify(this.c).accept(this.value);
        verify(this.f, never()).apply(any());

    }

    @Test
    public void testThrownExceptionSentDownstream() throws Throwable {

        this.broken.mapError(Throwable.class, x -> {
            throw (AssertionError) this.mappedX;
        }).on(Throwable.class, this.c);

        verify(this.c).accept(this.mappedX);

    }

    @Test
    public void testTransformedOnlyOnce() throws Throwable {

        final Promise<?> mapped = this.broken.mapError(Exception.class, this.f);

        mapped.on(Throwable.class, this.c);
        mapped.on(Throwable.class, this.c);

        verify(this.f, times(1)).apply(any());
        verify(this.c, times(2)).accept(this.mappedX);

    }

}
