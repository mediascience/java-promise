Promises for Java
=========================

[![Build Status](https://travis-ci.org/mediascience/java-promise.svg)](https://travis-ci.org/mediascience/java-promise)

Create chains of futures as an alternative to nested
callbacks.

A promise is a future computation whose (eventual) value
cannot be directly read. A promised value is used exclusively
by continuations. In this implementation, several kinds of
continuation are supported (see Usage below).

A promise can be in one of three states: *incomplete*,
*fulfilled*, or *broken*.  A promise that is fulfilled
or broken is said to be *complete*. Once complete, a
promise's state cannot change.

A promise resulting from binding a continuation is said
to exist *downstream* from the target promise. The
target promise is said to exist *upstream* from the
resulting promise. A continuation that produces a promise
is called a *promise function*. When a promise function is
bound downstream and subsequently invoked, the promise it
produces is hidden to programs but its state is
usually reflected in a promise produced when it was bound.
The particular binding semantics define how the state
is mapped. The hidden promise in this case is said to be
*injected upstream*.


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

### Create Degenerate

A promise can be created already fulfilled or broken.

```java
final AtomicInteger vcap = new AtomicInteger();
final Promise<Integer> fulfilled = Promise.of(75);
fulfilled.forEach(vcap::set);
assert vcap.get() == 75;

final AtomicReference<Exception> ecap = new AtomicReference<>();
final Exception x = new RuntimeException();
final Promise<Integer> broken = Promise.broken(x);
broken.on(RuntimeException.class, ecap::set);
assert ecap.get() == x;
```

### Create Incomplete

The more interesting case is to create a promise that is
not complete. An incomplete promise is a future that
can be chained to any number of continuations.

```java
final AtomicInteger vcap = new AtomicInteger();
final Async<Integer> af = new Async<>();
final Promise<Integer> toFulfill = af.promise();
toFulfill.forEach(vcap::set);
assert vcap.get() == 0;
af.succeed(75);
assert vcap.get() == 75;

final AtomicReference<Exception> ecap = new AtomicReference<>();
final Async<Integer> ab = new Async<>();
final Promise<Integer> toBreak = ab.promise();
toBreak.on(RuntimeException.class, ecap::set);
assert ecap.get() == null;
Exception x = new RuntimeException();
ab.fail(x); 
assert ecap.get() == x;
```

### Map

A map simply transforms the promise's value when it is fulfilled. If
the promise is broken, the transformation is not performed.

```java
final AtomicInteger vcap = new AtomicInteger();
Promise.of(75).map(i -> i * 2).forEach(vcap::set);
assert vcap.get() == 150;

final AtomicReference<Object> ecap = new AtomicReference<>();
final Exception x = new RuntimeException();
Promise.<Integer>broken(x)
    .map(i -> i * 2) // lambda expr not invoked
    .on(Throwable.class, ecap::set);
assert ecap.get() == x;
```

### Flat Map

The ```flatMap(..)``` method binds a promise function downstream. The promise
function is run only if the original promise is fulfilled. The promise function
runs independently and supplies completion to the promise returned by
the ```flatMap(..)``` method.

```java
final AtomicReference<Object> vcap = new AtomicReference<>(); 
final Async<Object> inner = new Async<>();
Promise.of(75).flatMap(i -> inner.promise())
        .forEach(vcap::set);
assert vcap.get() == null;
inner.succeed("Hello");
assert vcap.get().equals("Hello");


final AtomicReference<Object> ecap = new AtomicReference<>();
final Exception x = new RuntimeException();
Promise.<Integer>broken(x)
    .flatMap(i -> Promise.of("Hello")) // lambda expr not invoked
    .on(RuntimeException.class, ecap::set); 
assert ecap.get() == x;
```

### Retry

The ```then(..)``` method binds in the same way as ```flatMap(..)``` but also
accepts a retry policy expressed as a promise function. If the promise
supplied by the work function fails, then the retry function is invoke. When
it is fulfilled with ```true``` the work function is run again and its result
replaces the previous iteration's promise in the provider role. The cycle continues
until a) the work function succeeds, or b) the retry promise breaks or is fulfilled
with ```false```.

```java
final AtomicBoolean condition = new AtomicBoolean();
final AtomicReference<Async<Boolean>> pendingRetry = new AtomicReference<Async<Boolean>>();
final AtomicReference<Object> vcap = new AtomicReference<>();

Promise.of(75)
    .then(i -> {
        return condition.get() ? Promise.of("Done!")
                : Promise.broken(new RuntimeException());
    }, (x, u) -> {
        pendingRetry.set(new Async<>());
        return pendingRetry.get().promise();
    }).forEach(vcap::set);

pendingRetry.get().succeed(true); // try (and fail) again
pendingRetry.get().succeed(true); // false would mean stop retrying
condition.set(true); // next time it will work
assert vcap.get() == null;
pendingRetry.get().succeed(true);
assert vcap.get().equals("Done!");
```


### Defer

```defer(..)``` runs a given promise function only when the
target promise is completed, regardless of whether it is fulfilled
or broken. It is analogous to the ```finally``` handler in a Java
try/catch/finally block.

```java
final Async<Object> toFulfill = new Async<>();
final Async<Object> toBreak = new Async<>();

final Supplier<Promise<String>> finalizer = () -> Promise
        .of("Finally!");

final AtomicReference<String> cap1 = new AtomicReference<String>();
toFulfill.promise().defer(finalizer).forEach(cap1::set);
assert cap1.get() == null;
toFulfill.succeed(109);
assert cap1.get().equals("Finally!");

final AtomicReference<String> cap2 = new AtomicReference<String>();
toBreak.promise().defer(finalizer).forEach(cap2::set);
assert cap2.get() == null;
toBreak.fail(new Exception());
assert cap2.get().equals("Finally!");
```

### Recover

```recover(..)``` runs a given promise function only when the target
promise is broken. It is analogous to the ```catch``` handler in a
Java try/catch/finally block. The downstream is a promise to compute
an ```Optional<T>``` of the upstream promise type T. The downstream
fulfills with an ```Optional.empty()``` if the upstream promise
is itself fulfilled, thus providing a way to signal success down
the recovery chain.

```java
// recover from broken promise
final AtomicReference<Object> ecap = new AtomicReference<>();
final AtomicReference<Object> rcap = new AtomicReference<>();

final Exception x = new RuntimeException();
final Promise<?> p = Promise.broken(x);
p.on(Throwable.class, ecap::set);
p.recover(Exception.class, err -> Promise.of("Recovered!"))
    .forEach(rcap::set);

assert ecap.get() == x;
assert rcap.get().equals(Optional.of("Recovered!"));

// recover when promise is fulfilled
final AtomicInteger vcap = new AtomicInteger();
final AtomicReference<Object> scap = new AtomicReference<Object>();

final Promise<Integer> q = Promise.of(75);
q.forEach(vcap::set);
q.recover(Exception.class, err -> Promise.of("Recovered!"))
    .forEach(scap::set);

assert vcap.get() == 75;
assert scap.get().equals(Optional.empty());
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


