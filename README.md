Table of Contents
=================

* [Introduction](#play-mockws)
* [Release notes](#release-notes)
* [Simple example](#example)
* [Adding play-mockws to your project](#adding-play-mockws-to-your-project)
* [Usage](#usage)
* [Compatibility matrix](#compatibility-matrix)

## play-mockws

[![Build Status](https://travis-ci.org/leanovate/play-mockws.svg?branch=master)](https://travis-ci.org/leanovate/play-mockws)
[![Codacy Badge](https://api.codacy.com/project/badge/Coverage/a7f45a8cbd2a4085ac03ff8c163e3394)](https://www.codacy.com/app/yann-simon-fr/play-mockws?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=leanovate/play-mockws&amp;utm_campaign=Badge_Coverage)
[![Coverage Status](https://coveralls.io/repos/github/leanovate/play-mockws/badge.svg)](https://coveralls.io/github/leanovate/play-mockws)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.leanovate.play-mockws/play-mockws_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.leanovate.play-mockws/play-mockws_2.11)

Play MockWS is a mock WS client for Play Framework.

If:
- you write an application in Scala with the [Play Framework](https://playframework.com/)
- the application makes HTTP calls to external web services with the [WS client](https://www.playframework.com/documentation/latest/ScalaWS)
- you want to test your implementation

then you can use `play-mockws` to simulate HTTP requests to external web services in your tests.

## Release Notes

see [RELEASE-NOTES.md](RELEASE-NOTES.md)

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

* for Play 2.6.x:
```scala
libraryDependencies += "de.leanovate.play-mockws" %% "play-mockws" % "2.6.0" % Test
```
* for Play 2.5.x:
```scala
libraryDependencies += "de.leanovate.play-mockws" %% "play-mockws" % "2.5.1" % Test
```
* for Play 2.4.x:
```scala
libraryDependencies += "de.leanovate.play-mockws" %% "play-mockws" % "2.4.2" % Test
```
* for Play 2.3.x:
```scala
libraryDependencies += "de.leanovate.play-mockws" %% "play-mockws" % "2.3.2" % Test
```


And use it:
```scala
import mockws.MockWS

val ws = MockWS { ... }
```

## Usage

##### General usage

From the 2.6 version, it is recommended that your tests either extend trait MockWSHelpers or import MockWSHelpers. MockWSHelpers
provides an implicit Materializer you need when working with Play's Actions.

```scala
class MySpec extends FreeSpec with Matchers with MockWSHelpers {
  ...
}
```

or
```scala
import mockws.MockWSHelpers._
```

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

- MockWS 2.6.x is actually only compatible with Play 2.6.y., with Scala 2.12 or 2.11.
- MockWS 2.5.x is actually only compatible with Play 2.5.y., with Scala 2.11.
- MockWS 2.4.x is actually only compatible with Play 2.4.y., with Scala 2.10 or 2.11.
- MockWS 2.3.x is actually only compatible with Play 2.3.y., with Scala 2.10 or 2.11.
