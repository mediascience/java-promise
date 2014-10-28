Promises for Java
=========================

[![Build Status](https://travis-ci.org/mediascience/java-promise.svg)](https://travis-ci.org/mediascience/java-promise)

Create chains of futures as an alternative to nested
callbacks.

## Usage

### Include Dependencies
```xml
<dependency>
    <groupId>com.msiops.ground</groupId>
    <artifactId>ground-promise</artifactId>
    <version>${v.promise}</version>
</dependency>
```
See [Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%20%22com.msiops.ground%22%20a%3A%22ground-promise%22) for latest version.

### Create Complete
```java
final Promise<Integer> fulfilled = Promise.of(75);
fulfilled.forEach(System.out::println); // prints 75

final Promise<Integer> broken = Promise.broken(new RuntimeException());
broken.on(Throwable.class, Throwable::printStackTrace); // prints the stack trace
```

### Create Deferred
```java
final Async<Integer> af = new Async<>();
final Promise<Integer> toFulfill = af.promise();
toFulfill.forEach(System.out::println);  // does nothing
af.succeed(75); // prints 75

final Async<Integer> ab = new Async<>();
final Promise<Integer> toBreak = ab.promise();
toBreak.on(Throwable.class, Throwable::printStackTrace); // does nothing
ab.fail(new RuntimeException()); // prints the stack tract
```

### Map
```java
Promise.of(75).map(i -> i * 2).forEach(System.out::println); // prints 150

Promise.<Integer> broken(new RuntimeException()).map(i -> i * 2)
        .on(Throwable.class, Throwable::printStackTrace); // prints stack trace
```

### Flat Map
```java
final Async<Object> inner = new Async<>();

Promise.of(75).map(i -> inner.promise())
        .forEach(System.out::println); // does nothing
inner.succeed("Hello"); // prints Hello

final Promise<?> p = Promise.<Integer> broken(
        new RuntimeException()).flatMap(i -> Promise.of("Hello"));
p.forEach(System.out::println); // does nothing
p.on(Throwable.class, Throwable::printStackTrace); // prints stack trace
```

## Versioning

Releases in the 0.x series are the Wild West. Anything can change between
releases--package names, method signatures, behavior, whatever. But if you
like it as it is right now, all the tests pass so just use it at its current
version and have fun.

The next version series will be 1.x. Every release in that series will be
backward compatible with every lower-numbered release in the same series
except possibly in the case of 1) a bug fix or 2) a correction to an
underspecification.

An incompatible change to the interface, behavior, license, or anything else
after the 1.x series is published will result in a new series, such as
2.x.

## Acknowledgements

This work is inspired by the excellent Q library for Javascript.

The Runaways provided the soundtrack during initial development.

Thanks to Media Science International for being awesome.

## License

Licensed to Media Science International (MSI) under one or more
contributor license agreements. See the NOTICE file distributed with this
work for additional information regarding copyright ownership. MSI
licenses this file to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance with the
License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
License for the specific language governing permissions and limitations
under the License.


