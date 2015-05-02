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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.msiops.footing.functional.FunT1;
import com.msiops.ground.promise.Async;
import com.msiops.ground.promise.Promise;
import com.msiops.ground.promise.Promises;

public class AsyncRecoverTest {

    @Mock
    private Consumer<Object> c;

    private Async<Integer> inner;

    private Async<Integer> outer;

    private Promise<Integer> r;

    @Mock
    private FunT1<Exception, Promise<Integer>> rf;

    private Integer rvalue;

    private Integer value;

    private Exception x, ix;

    @Before
    public void setup() throws Throwable {

        MockitoAnnotations.initMocks(this);

        this.outer = Promises.async();

        this.inner = Promises.async();

        this.value = 12;

        this.rvalue = 12390;

        this.x = new Exception();

        when(this.rf.apply(this.x)).thenReturn(this.inner.promise());

        this.r = this.outer.promise().recover(Exception.class,
                err -> this.rf.apply(err));

        this.ix = new RuntimeException();

    }

    @Test
    public void testBoundOnlyOnce() throws Throwable {

        this.r.forEach(this.c);
        this.r.forEach(this.c);

        verify(this.rf, never()).apply(any());

        this.outer.fail(this.x);

        verify(this.c, never()).accept(any());

        this.inner.succeed(this.rvalue);

        verify(this.rf, times(1)).apply(any());
        verify(this.c, times(2)).accept(this.rvalue);

    }

    @Test
    public void testBrokenBroken() throws Throwable {

        this.r.on(Throwable.class, this.c);

        verify(this.rf, never()).apply(any());

        this.outer.fail(this.x);

        verify(this.rf).apply(this.x);
        verify(this.c, never()).accept(any());

        this.inner.fail(this.ix);

        verify(this.c).accept(this.ix);

    }

    @Test
    public void testBrokenFulfilled() throws Throwable {

        this.r.forEach(this.c);

        verify(this.rf, never()).apply(any());

        this.outer.fail(this.x);

        verify(this.rf).apply(this.x);
        verify(this.c, never()).accept(any());

        this.inner.succeed(this.rvalue);

        verify(this.c).accept(this.rvalue);

    }

    @Test(expected = NullPointerException.class)
    public void testBrokenNullSelectorIllegal() {

        this.outer.fail(this.x);
        this.outer.promise().recover((Class<Exception>) null,
                err -> this.rf.apply(err));

    }

    @Test(expected = NullPointerException.class)
    public void testBrokenNullTransformedIllegal() {

        this.outer.fail(this.x);
        this.outer.promise().recover(Throwable.class, null);

    }

    @Test
    public void testContinuationErrorSentDownstream() throws Throwable {

        final RuntimeException x = new RuntimeException();
        this.outer.promise().recover(Throwable.class, err -> {
            throw x;
        }).on(Throwable.class, this.c);

        verify(this.c, never()).accept(any());

        this.outer.fail(this.x);

        verify(this.c).accept(x);

    }

    @Test
    public void testFulfilled() throws Throwable {

        this.r.forEach(this.c);

        this.outer.succeed(this.value);

        /*
         * recovery is not invoked
         */
        verify(this.rf, never()).apply(any());

        /*
         * outer fulfilled value is passed
         */
        verify(this.c).accept(this.value);

    }

    @Test(expected = NullPointerException.class)
    public void testIncompleteNullSelectorIllegal() {

        this.outer.promise().recover((Class<Exception>) null,
                err -> this.rf.apply(err));

    }

    @Test(expected = NullPointerException.class)
    public void testIncompleteNullTransformedIllegal() {

        this.outer.promise().recover(Throwable.class, null);

    }

}
