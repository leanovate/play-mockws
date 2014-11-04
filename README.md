play-mockws
===========

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
```scala
libraryDependencies += "de.leanovate.play-mockws" %% "play-mockws" % "0.12" % "test"
```
The last version can be found on [Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22de.leanovate.play-mockws%22)

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
The partial function binds 2 Strings, an HTTP method and the URL, to a Play [Action](https://www.playframework.com/documentation/2.3.x/ScalaActions).

For clarity this partial function is aliased as [MockWS.Routes](src/main/scala/mockws/MockWS.scala)

When calling MockWS.url(), if the HTTP method and the URL are found, the defined play action is evaluated.

##### Controlling the routes

If you want to control more the routes, for example to know whether a route was called or how many times, use the [Route](src/main/scala/mockws/Route.scala) class for this.

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

MockWS is actually only compatible with Play 2.3.x., with Scala 2.10 or 2.11.

## Releasee Notes

* 0.12: remove dependency to specs2 - retain only mockito
* 0.11: add [Route](src/main/scala/mockws/Route.scala)

## Developer info

Travis: [![Build Status](https://travis-ci.org/leanovate/play-mockws.png?branch=master)](https://travis-ci.org/leanovate/play-mockws)

