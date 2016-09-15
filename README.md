Table of Contents
=================

* [Introduction](#play-mockws)
* [Simple example](#example)
* [Adding play-mockws to your project](#adding-play-mockws-to-your-project)
* [Usage](#usage)
* [Compatibility matrix](#compatibility-matrix)
* [Release notes](#release-notes)

play-mockws
===========

[![Build Status](https://travis-ci.org/leanovate/play-mockws.svg?branch=master)](https://travis-ci.org/leanovate/play-mockws)
[![codecov.io](http://codecov.io/github/leanovate/play-mockws/coverage.svg?branch=master)](http://codecov.io/github/leanovate/play-mockws?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.leanovate.play-mockws/play-mockws_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.leanovate.play-mockws/play-mockws_2.11)

Play MockWS is a mock WS client for Play Framework.

If:
- you write an application in Scala with the [Play Framework](https://playframework.com/)
- the application makes HTTP calls to external web services with the [WS client](https://www.playframework.com/documentation/2.3.x/ScalaWS)
- you want to test your implementation

then you can use `play-mockws` to simulate HTTP requests to external web services in your tests.

## Example

```scala
// simulation of a GET request to http://dns/url
val ws = MockWS {
  case (GET, "http://dns/url") => Action { Ok("http response") }
}

await(ws.url("http://dns/url").get()).body == "http response"
```

## Adding play-mockws to your project

Add MockWS as test dependency in the `build.sbt`:

* for Play 2.5.x:
```scala
libraryDependencies += "de.leanovate.play-mockws" %% "play-mockws" % "2.5.0" % "test"
```
* for Play 2.4.x:
```scala
libraryDependencies += "de.leanovate.play-mockws" %% "play-mockws" % "2.4.2" % "test"
```
* for Play 2.3.x:
```scala
libraryDependencies += "de.leanovate.play-mockws" %% "play-mockws" % "2.3.2" % "test"
```


And use it:
```scala
import mockws.MockWS

val ws = MockWS { ... }
```

## Usage

##### General usage

A `MockWS` instance can be directly constructed with a partial function like this:
```scala
val ws = MockWS {
  case (GET, "/") => Action { Ok("homepage") }
  case (POST, "/users") => Action { request => Created((request.body.asJson.get \ "id").as[String]) }
  case (GET, "/users/24") => Action { NotFound("") }
}
```
The partial function binds 2 Strings, an HTTP method and the URL, to a Play [Action](https://www.playframework.com/documentation/latest/ScalaActions).

For clarity, this partial function is aliased as [MockWS.Routes](src/main/scala/mockws/MockWS.scala)

When calling MockWS.url(), if the HTTP method and the URL are found, the defined play action is evaluated.

##### Controlling the routes

If you want more control on the routes, for example to know whether a route was called or how many times, use the [Route](src/main/scala/mockws/Route.scala) class for this.

Routes can be defined together with the standard function `orElse`.

```scala
val route1 = Route {
  case (GET, "/route1") => Action { Ok("") }
}
val route2 = Route {
  case (GET, "/route2") => Action { Ok("") }
}

val ws = MockWS(route1 orElse route2)

await(ws.url("/route1").get())

route1.called == true
route2.called == false

route1.timeCalled == 1
route2.timeCalled == 0
```

An example how to structure an implementation to test it with MockWS can be found [here](src/test/scala/mockws/Example.scala).

Other examples can be found in the [tests](src/test/scala/mockws/).

## Compatibility Matrix

- MockWS 2.5.x is actually only compatible with Play 2.5.y., with Scala 2.11.
- MockWS 2.4.x is actually only compatible with Play 2.4.y., with Scala 2.10 or 2.11.
- MockWS 2.3.x is actually only compatible with Play 2.3.y., with Scala 2.10 or 2.11.

## Release Notes

* 2.5.0 (2016-03-07): release compatible with play 2.5.0
* 2.5.0-RC2:
  - contribution from [@avdv](https://github.com/avdv): Fix truncated response in FakeWSRequestHolder [#15](https://github.com/leanovate/play-mockws/pull/15)
  - release compatible with play 2.5.0-RC2
* 2.5-RC1: release compatible with play 2.5.0-RC1
* 2.4.2/2.3.2: major contribution from [@htmldoug](https://github.com/htmldoug): Replace mockito with a concrete implementation [#11](https://github.com/leanovate/play-mockws/pull/11)
* 2.4.1/2.3.1: contribution from [@bomgar](https://github.com/bomgar): mock sign() method [#7](https://github.com/leanovate/play-mockws/pull/7)
* 2.4.0: first version compatible with play 2.4.y
* 2.3.0: same release as 0.15 - use the same version number as play (2.3.x compatible with play 2.3.y)
* 0.15: fix https://github.com/leanovate/play-mockws/issues/6
* 0.14:<br>
  - contribution from [@kwark](https://github.com/kwark): support for `allHeaders` + `getResponseBodyAsBytes` [#2](https://github.com/leanovate/play-mockws/pull/2)
  - contribution from [@sebdotv](https://github.com/sebdotv): support for `withMethod` in `execute` and `stream` [#3](https://github.com/leanovate/play-mockws/pull/3)
  - contribution from [@knshiro](https://github.com/knshiro): support for `PATCH` method [#4](https://github.com/leanovate/play-mockws/pull/4)
  - fix from [@jdanbrown](https://github.com/jdanbrown): Allow WS varargs to be passed as immutable Seqs [#5](https://github.com/leanovate/play-mockws/pull/5)
* 0.13: handle URL query parameters
* 0.12: remove dependency to specs2 - retain only mockito
* 0.11: add [Route](src/main/scala/mockws/Route.scala)
