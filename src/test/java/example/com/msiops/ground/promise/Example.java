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
package example.com.msiops.ground.promise;

import com.msiops.ground.promise.Async;
import com.msiops.ground.promise.Promise;

public enum Example implements Runnable {

    CONSTRUCT_COMPLETE {
        @Override
        public void run() {
            // @formatter:off
            
            
final Promise<Integer> fulfilled = Promise.of(75);
fulfilled.forEach(System.out::println); // prints 75

final Promise<Integer> broken = Promise.broken(new RuntimeException());
broken.on(Throwable.class, Throwable::printStackTrace); // prints the stack trace


        // @formatter:on
        }
    },

    CONSTRUCT_DEFERRED {
        @Override
        public void run() {

            // @formatter:off

final Async<Integer> af = new Async<>();
final Promise<Integer> toFulfill = af.promise();
toFulfill.forEach(System.out::println);  // does nothing
af.succeed(75); // prints 75

final Async<Integer> ab = new Async<>();
final Promise<Integer> toBreak = ab.promise();
toBreak.on(Throwable.class, Throwable::printStackTrace); // does nothing
ab.fail(new RuntimeException()); // prints the stack tract
            
            // @formatter:on
            
        }
    }
    
    
    ;
    
    
}
