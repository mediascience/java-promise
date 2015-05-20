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

public class RequireTest {

    private Async<Object> asyncRequired;

    private Async<Object> asyncSource;

    private Promise<Object> source, required, produced;

    private Throwable sourceError, requiredError;

    private Object value;

    @Before
    public void setup() {

        this.value = Long.valueOf(190339022L);
        this.sourceError = new IllegalArgumentException("ill eagle");
        this.requiredError = new IllegalStateException("state of denial");

    }

    @Test
    public void testAsyncFailWithAsyncFail() {

        givenSourceAsync();

        givenRequiredAsync();

        whenSynchronized();

        whenSourceFails();

        whenRequiredFails();

        itFailsWithSourceError();

    }

    @Test
    public void testAsyncFailWithAsyncSucceed() {

        givenSourceAsync();

        givenRequiredAsync();

        whenSynchronized();

        whenSourceFails();

        whenRequiredSucceeds();

        itFailsWithSourceError();

    }

    @Test
    public void testAsyncFailWithSyncFail() {

        givenSourceAsync();

        givenRequiredFails();

        whenSynchronized();

        whenSourceFails();

        itFailsWithSourceError();

    }

    @Test
    public void testAsyncFailWithSyncSucceed() {

        givenSourceAsync();

        givenRequiredSucceeds();

        whenSynchronized();

        whenSourceFails();

        itFailsWithSourceError();

    }

    @Test
    public void testAsyncSucceedWithAsyncFail() {

        givenSourceAsync();

        givenRequiredAsync();

        whenSynchronized();

        whenSourceSucceeds();

        whenRequiredFails();

        itFailsWithRequiredError();

    }

    @Test
    public void testAsyncSucceedWithAsyncSucceed() {

        givenSourceAsync();

        givenRequiredAsync();

        whenSynchronized();

        whenSourceSucceeds();

        whenRequiredSucceeds();

        itComputesValue();

    }

    @Test
    public void testAsyncSucceedWithSyncFail() {

        givenSourceAsync();

        givenRequiredFails();

        whenSynchronized();

        whenSourceSucceeds();

        itFailsWithRequiredError();

    }

    @Test
    public void testAsyncSucceedWithSyncSucceed() {

        givenSourceAsync();

        givenRequiredSucceeds();

        whenSynchronized();

        whenSourceSucceeds();

        itComputesValue();

    }

    @Test
    public void testSyncFailWithAsyncFail() {

        givenSourceFails();

        givenRequiredAsync();

        whenSynchronized();

        whenRequiredFails();

        itFailsWithSourceError();

    }

    @Test
    public void testSyncFailWithAsyncSucceed() {

        givenSourceFails();

        givenRequiredAsync();

        whenSynchronized();

        whenRequiredSucceeds();

        itFailsWithSourceError();

    }

    @Test
    public void testSyncFailWithSyncFail() {

        givenSourceFails();

        givenRequiredFails();

        whenSynchronized();

        itFailsWithSourceError();

    }

    @Test
    public void testSyncFailWithSyncSucceed() {

        givenSourceFails();

        givenRequiredSucceeds();

        whenSynchronized();

        itFailsWithSourceError();

    }

    @Test
    public void testSyncSucceedWithAsyncFail() {

        givenSourceSucceeds();

        givenRequiredAsync();

        whenSynchronized();

        whenRequiredFails();

        itFailsWithRequiredError();

    }

    @Test
    public void testSyncSucceedWithAsyncSucceed() {

        givenSourceSucceeds();

        givenRequiredAsync();

        whenSynchronized();

        whenRequiredSucceeds();

        itComputesValue();

    }

    @Test
    public void testSyncSucceedWithSyncFail() {

        givenSourceSucceeds();

        givenRequiredFails();

        whenSynchronized();

        itFailsWithRequiredError();

    }

    @Test
    public void testSyncSucceedWithSyncSucceed() {

        givenSourceSucceeds();

        givenRequiredSucceeds();

        whenSynchronized();

        itComputesValue();

    }

    private void givenRequiredAsync() {
        this.asyncRequired = Promises.async();
        this.required = this.asyncRequired.promise();
    }

    private void givenRequiredFails() {
        this.required = Promises.broken(this.requiredError);
    }

    private void givenRequiredSucceeds() {
        this.required = Promises.fulfilled("REQUIRED");
    }

    private void givenSourceAsync() {
        this.asyncSource = Promises.async();
        this.source = this.asyncSource.promise();
    }

    private void givenSourceFails() {
        this.source = Promises.broken(this.sourceError);
    }

    private void givenSourceSucceeds() {
        this.source = Promises.fulfilled(this.value);
    }

    private void itComputesValue() {

        try {
            assertEquals(this.value, this.produced.toBlocking().get());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("interrupted");
        } catch (final ExecutionException e) {
            fail("computation diverges '" + e.getMessage() + "'");
        }

    }

    private void itFailsWithRequiredError() {

        try {
            this.produced.toBlocking().get();
            fail("should throw");
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("interrupted");
        } catch (final ExecutionException e) {
            assertEquals(this.requiredError, e.getCause());
        }

    }

    private void itFailsWithSourceError() {

        try {
            this.produced.toBlocking().get();
            fail("should throw");
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("interrupted");
        } catch (final ExecutionException e) {
            assertEquals(this.sourceError, e.getCause());
        }

    }

    private void whenRequiredFails() {
        this.asyncRequired.fail(this.requiredError);
    }

    private void whenRequiredSucceeds() {
        this.asyncRequired.succeed("NO RELATION");
    }

    private void whenSourceFails() {
        this.asyncSource.fail(this.sourceError);
    }

    private void whenSourceSucceeds() {
        this.asyncSource.succeed(this.value);
    }

    private void whenSynchronized() {

        this.produced = this.source.require(this.required);

    }

}
