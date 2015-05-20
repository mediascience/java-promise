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

import static org.junit.Assert.*;

import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Test;

import com.msiops.ground.promise.Async;
import com.msiops.ground.promise.Promise;
import com.msiops.ground.promise.Promises;

public class JoinTest {

    private Promise<Object> inner;

    private Async<Object> innerAsync;

    private Throwable innerError, outerError;

    private Promise<Object> joined;

    private Async<Promise<Object>> outerAsync;

    private Promise<Promise<Object>> unjoined;

    private Object value;

    public void itFailsWithInner() {

        try {
            this.joined.toBlocking().get();
            fail("should throw");
        } catch (final InterruptedException e) {
            fail("interrupted");
        } catch (final ExecutionException e) {
            assertEquals(this.innerError, e.getCause());
        }

    }

    public void itFailsWithOuter() {

        try {
            this.joined.toBlocking().get();
            fail("should throw");
        } catch (final InterruptedException e) {
            fail("interrupted");
        } catch (final ExecutionException e) {
            assertEquals(this.outerError, e.getCause());
        }

    }

    @Before
    public void setup() {

        this.value = "THIS IS A BOFFO VALUE";

        this.innerError = new IllegalArgumentException("INNER");
        this.outerError = new IllegalStateException("OUTER");

    }

    @Test
    public void testJoinAsyncFailAsyncFail() {

        givenInnerAsync();

        givenOuterAsync();

        whenJoined();

        whenOuterFails();

        whenInnerFails();

        itFailsWithOuter();

    }

    @Test
    public void testJoinAsyncFailAsyncSucceed() {

        givenInnerAsync();

        givenOuterAsync();

        whenJoined();

        whenOuterFails();

        whenInnerSucceeds();

        itFailsWithOuter();

    }

    @Test
    public void testJoinAsyncFailSyncFail() {

        givenInnerFails();

        givenOuterAsync();

        whenJoined();

        whenOuterFails();

        itFailsWithOuter();

    }

    @Test
    public void testJoinAsyncFailSyncSucceed() {

        givenInnerSucceeds();

        givenOuterAsync();

        whenJoined();

        whenOuterFails();

        itFailsWithOuter();

    }

    @Test
    public void testJoinAsyncSucceedAsyncFail() {

        givenInnerAsync();

        givenOuterAsync();

        whenJoined();

        whenOuterSucceeds();

        whenInnerFails();

        itFailsWithInner();

    }

    @Test
    public void testJoinAsyncSucceedAsyncSucceed() {

        givenInnerAsync();

        givenOuterAsync();

        whenJoined();

        whenOuterSucceeds();

        whenInnerSucceeds();

        itComputesValue();

    }

    @Test
    public void testJoinAsyncSucceedSyncFail() {

        givenInnerFails();

        givenOuterAsync();

        whenJoined();

        whenOuterSucceeds();

        itFailsWithInner();

    }

    @Test
    public void testJoinAsyncSucceedSyncSucceed() {

        givenInnerSucceeds();

        givenOuterAsync();

        whenJoined();

        whenOuterSucceeds();

        itComputesValue();

    }

    @Test
    public void testJoinSyncFail() {

        givenOuterFail();

        whenJoined();

        itFailsWithOuter();

    }

    @Test
    public void testJoinSyncSucceedAsyncFail() {

        givenInnerAsync();

        givenOuterSucceed();

        whenJoined();

        whenInnerFails();

        itFailsWithInner();

    }

    @Test
    public void testJoinSyncSucceedAsyncSucceed() {

        givenInnerAsync();

        givenOuterSucceed();

        whenJoined();

        whenInnerSucceeds();

        itComputesValue();

    }

    @Test
    public void testJoinSyncSucceedSyncFail() {

        givenInnerFails();

        givenOuterSucceed();

        whenJoined();

        itFailsWithInner();

    }

    @Test
    public void testJoinSyncSucceedSyncSucceed() {

        givenInnerSucceeds();

        givenOuterSucceed();

        whenJoined();

        itComputesValue();

    }

    private void givenInnerAsync() {
        this.innerAsync = Promises.async();
        this.inner = this.innerAsync.promise();
    }

    private void givenInnerFails() {
        this.inner = Promises.broken(this.innerError);
    }

    private void givenInnerSucceeds() {
        this.inner = Promises.fulfilled(this.value);
    }

    private void givenOuterAsync() {
        this.outerAsync = Promises.async();
        this.unjoined = this.outerAsync.promise();

    }

    private void givenOuterFail() {
        this.unjoined = Promises.broken(this.outerError);
    }

    private void givenOuterSucceed() {
        this.unjoined = Promises.fulfilled(this.inner);
    }

    private void itComputesValue() {
        try {
            final Object actual = this.joined.toBlocking().get();
            assertEquals(this.value, actual);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("interrupted");
        } catch (final ExecutionException e) {
            fail("diverged '" + e.getCause().getMessage() + "'");
        }
    }

    private void whenInnerFails() {
        this.innerAsync.fail(this.innerError);
    }

    private void whenInnerSucceeds() {
        this.innerAsync.succeed(this.value);
    }

    private void whenJoined() {

        this.joined = Promises.join(this.unjoined);

    }

    private void whenOuterFails() {
        this.outerAsync.fail(this.outerError);
    }

    private void whenOuterSucceeds() {
        this.outerAsync.succeed(this.inner);
    }
}
