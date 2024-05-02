Play MockWS Standalone
=================
![CI Workflow](https://github.com/hiveteq/play-mockws-standalone/actions/workflows/ci.yml/badge.svg)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.hiveteq.play/play-mockws-standalone_3/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.hiveteq.play/play-mockws-standalone_3/)

* [Goal](#goal)
* [Simple example](#example)
* [Adding play-mockws to your project](#adding-play-mockws-to-your-project)
  * [Compatibility matrix](#compatibility-matrix)
  * [play-mockws](#play-mockws)
* [Usage](#usage)
  * [General usage](#general-usage) 
  * [Controlling the routes](#controlling-the-routes) 
* [Release notes](#release-notes)

## Goal

Play MockWS Standalone is a mock WS Standalone client for Play Framework.
This is a fork of a [regular Mock WS library](https://github.com/leanovate/play-mockws) that is not designed to work with Standalone WS Client.

If:

- you write an application in Scala with the [Play Framework](https://playframework.com/)
- the application makes HTTP calls to external web services with
  the [WS client](https://www.playframework.com/documentation/latest/ScalaWS)
- you want to test your implementation

then you can use Play MockWS Standalone to simulate HTTP requests to external web services in your tests.

## Example

```scala
// simulation of a GET request to http://dns/url
val ws = MockWS {
  case (GET, "http://dns/url") => Action {
    Ok("http response")
  }
}

await(ws.url("http://dns/url").get()).body == "http response"
```

## Adding play-mockws-standalone to your project

Add MockWS Standalone as test dependency in the `build.sbt`.

### Compatibility Matrix

At the moment only Play 3.x is supported

| play-mock-ws version | Play versions | Scala versions |
|----------------------|---------------|----------------|
| 3.0.x                | 3.0           | 2.13, 3.3      |

### play-mockws

```scala
libraryDependencies += "io.github.hiveteq.play-mockws-standalone" %% "play-mockws-standalone" % "3.0.4" % Test
```

## Usage

### General usage

It is recommended that your tests either extend trait MockWSHelpers or import MockWSHelpers.
MockWSHelpers
provides an implicit Materializer you need when working with Play's Actions.

```scala
class MySpec extends FreeSpec with Matchers with MockWSHelpers with BeforeAndAfterAll {
...

  override def afterAll(): Unit = {
    shutdownHelpers()
  }
}
```

or

```scala
import mockws.MockWSHelpers._
```

A `MockWS` instance can be directly constructed with a partial function like this:

```scala
val ws = MockWS {
  case (GET, "/") => Action {
    Ok("homepage")
  }
  case (POST, "/users") => Action { request => Created((request.body.asJson.get \ "id").as[String]) }
  case (GET, "/users/24") => Action {
    NotFound("")
  }
}
```

The partial function binds 2 Strings, an HTTP method and the URL, to a
Play [Action](https://www.playframework.com/documentation/latest/ScalaActions).

For clarity, this partial function is aliased as [MockWS.Routes](src/main/scala/mockws/MockWS.scala)

When calling MockWS.url(), if the HTTP method and the URL are found, the defined play action is evaluated.

### Controlling the routes

If you want more control on the routes, for example to know whether a route was called or how many times, use
the [Route](src/main/scala/mockws/Route.scala) class for this.

Routes can be defined together with the standard function `orElse`.

```scala
val route1 = Route {
  case (GET, "/route1") => Action {
    Ok("")
  }
}
val route2 = Route {
  case (GET, "/route2") => Action {
    Ok("")
  }
}

val ws = MockWS(route1 orElse route2)

await(ws.url("/route1").get())

route1.called == true
route2.called == false

route1.timeCalled == 1
route2.timeCalled == 0
```

An example how to structure an implementation to test it with MockWS can be
found [here](src/test/scala/mockws/Example.scala).

Other examples can be found in the [tests](src/test/scala/mockws/).

## Release notes

See [RELEASE-NOTES.md](RELEASE-NOTES.md) or [GitHub releases](https://github.com/hiveteq/play-mockws-standalone/releases).
