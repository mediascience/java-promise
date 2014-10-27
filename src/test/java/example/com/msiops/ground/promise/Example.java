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
