play-mockws
===========

MockWS is a mock [WS client for Play Framework](https://www.playframework.com/documentation/2.3.x/ScalaWS) in Scala.

It can simulate HTTP requests to external web services.

## Example

```scala
val ws = MockWS {
  case (GET, "/url") => Action { Ok("http response") }
}

await(ws.url("/url").get()).body == "http response"
```

The simulation of the HTTP response for the WS call is simply implemented with a standard Play [Action](https://www.playframework.com/documentation/2.3.x/ScalaActions).

An example how to structure an implementation to test it with MockWS can be found [here](src/test/scala/mockws/Example.scala).

Other examples can be found in the [tests](src/test/scala/mockws/MockWSTest.scala).


## Usage

Add MockWS as test dependency in the `build.sbt`:
```scala
libraryDependencies += "de.leanovate.play-mockws" %% "play-mockws" % "0.0.9" % "test"
```
The last version can be found on [Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22de.leanovate.play-mockws%22)

And use it:
```scala
import mockws.MockWS

val ws = MockWS { ... }
```

## Compatibility Matrix

MockWS is actually only compatible with Play 2.3.x., with Scala 2.10 or 2.11.


## Developer info

Travis: [![Build Status](https://travis-ci.org/leanovate/play-mockws.png?branch=master)](https://travis-ci.org/leanovate/play-mockws)

