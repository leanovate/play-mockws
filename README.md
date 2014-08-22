play-mockws
===========

MockWS is a mock [WS client for Play Framework](https://www.playframework.com/documentation/2.3.x/ScalaWS)

It can simulate HTTP requests to external web services.

## Example

```scala
val ws = MockWS {
  case (GET, "/url") => Action { Ok("http response") }
}

await(ws.url("/url").get()).body == "http response"
```

An example how to structure an implementation to test it with MockWS can be found [here](src/test/scala/mockws/Example.scala)

Other examples can be found in the [tests](src/test/scala/mockws/MockWSTest.scala)


## Usage

Add MockWS as test dependency.
```scala
libraryDependencies += "de.leanovate.play-mockws" %% "play-mockws" % "<version>" % "test"
```
The last version can be found on [Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22de.leanovate.play-mockws%22)

And you can use it:
```scala
import mockws.MockWS

val ws = MockWS { ... }
```

## Compatibility Matrix

MockWS is actually only compatible with Play 2.3.x.


## Developer info

Travis: [![Build Status](https://travis-ci.org/leanovate/play-mockws.png?branch=master)](https://travis-ci.org/leanovate/play-mockws)

