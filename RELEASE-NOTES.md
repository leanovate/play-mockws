# Release notes

# 3.0.0

**This release brings a new naming and versioning scheme.**
Starting with 3.0.0, artifacts are published for a specific Play version: `play-mockws-3-0`, `play-mockws-2-9` and `play-mockws-2-8` instead of `play-mockws`.

This is the first release to support Play 3.0 and Pekko 1.0. 

# 2.9.0

This release targets Play 2.9 and Akka 2.6. Scala 2.12 and Java 8 are no longer supported.

See [v2.9.0](https://github.com/leanovate/play-mockws/releases/tag/v2.9.0) for a full list of changes.

## 2.8.1

- add support for request filters (#150) @avdv

See [v2.8.1](https://github.com/leanovate/play-mockws/releases/tag/v2.8.1) for a full list of changes.

## 2.8.0

This release targets Play 2.8 and Akka 2.6. Scala 2.11 is no longer supported!

See [v2.8.0](https://github.com/leanovate/play-mockws/releases/tag/v2.8.0) for a full list of changes.

## 2.7.1

- scala 2.13 support [64](https://github.com/leanovate/play-mockws/pull/64) (from [@avdv](https://github.com/avdv))
- update dependencies [54](https://github.com/leanovate/play-mockws/pull/54)
- sbt 1.2.8 [55](https://github.com/leanovate/play-mockws/pull/55)
- sbt-sonatype 2.5 [56](https://github.com/leanovate/play-mockws/pull/56)
- play 2.7.3 [60](https://github.com/leanovate/play-mockws/pull/60)

## 2.7.0 - 2019/02/11

- Compatibility with Play 2.7 [52](https://github.com/leanovate/play-mockws/pull/52)  
  contribution from [@avdv](https://github.com/avdv)

## 2.6.6 - 2018/06/25

- can define behaviour when no route are found [35](https://github.com/leanovate/play-mockws/pull/35)  
  Fix [issue #25](https://github.com/leanovate/play-mockws/issues/25)  
  contribution from [@SahilLone](https://github.com/SahilLone)


## 2.6.5 - 2018/03/09

- Remove colon after 'Basic' in basic auth [37](https://github.com/leanovate/play-mockws/pull/37)  
  contribution from [@mattrussell-sky](https://github.com/mattrussell-sky)

## 2.6.4 - 2018/02/07

- Fix regression in `withBodyAndContentType` where old headers are discarded [36](https://github.com/leanovate/play-mockws/pull/36)  
  contribution from [@avdv](https://github.com/avdv)

## 2.6.3 - 2017/12/22

- Fix withHttpHeaders behavior for FakeWSRequestHolder [33](https://github.com/leanovate/play-mockws/pull/33)
- Fix withQueryStringParameters behavior for FakeWSRequestHolder [34](https://github.com/leanovate/play-mockws/pull/34)  
  contributions from [@a-shkarupin](https://github.com/a-shkarupin)

## 2.6.2 - 2017/09/21

- Do not use a shutdown hook with helpers trait [31](https://github.com/leanovate/play-mockws/pull/31)

## 2.6.1 - 2017/09/21

- Stop using deprecated actions [27](https://github.com/leanovate/play-mockws/issues/27)  
  [contribution](https://github.com/leanovate/play-mockws/pull/28) from [@1gnition](https://github.com/1gnition)
- Add helpers for users to avoid using deprecated `Action` [29](https://github.com/leanovate/play-mockws/issues/29)  
  [contribution](https://github.com/leanovate/play-mockws/pull/30) from [@raphaelbauer](https://github.com/raphaelbauer)

## 2.6.0 - 2017/05/26

- compatibility with play 2.6.0

## 2.6.0-M1 - 2017/03/03

- compatibility with play 2.6.0-M1 [#22](https://github.com/leanovate/play-mockws/pull/22)  
  contribution from [@domdorn](https://github.com/domdorn), with support from [@matsluni](https://github.com/matsluni)

## 2.5.2 - 2019/05/17

- update dependencies: https://github.com/leanovate/play-mockws/compare/v2.5.1...v2.5.2

## 2.5.1 - 2016/11/03

- add support for HTTP Authentication: Basic [#17](https://github.com/leanovate/play-mockws/pull/17)  
  contribution from [@agebhar1](https://github.com/agebhar1)

## 2.5.0 - 2016/03/07

- release compatible with play 2.5.0

## 2.5.0-RC2

- Fix truncated response in FakeWSRequestHolder [#15](https://github.com/leanovate/play-mockws/pull/15)  
  contribution from [@avdv](https://github.com/avdv)

- release compatible with play 2.5.0-RC2

## 2.5-RC1

- release compatible with play 2.5.0-RC1

## 2.4.2/2.3.2

- Replace mockito with a concrete implementation [#11](https://github.com/leanovate/play-mockws/pull/11)  
  contribution from [@htmldoug](https://github.com/htmldoug)

## 2.4.1/2.3.1

- mock sign() method [#7](https://github.com/leanovate/play-mockws/pull/7)  
  contribution from [@bomgar](https://github.com/bomgar)

## 2.4.0

- first version compatible with play 2.4.y

## 2.3.0

- same release as 0.15 - use the same version number as play (2.3.x compatible with play 2.3.y)

## 0.15

- fix https://github.com/leanovate/play-mockws/issues/6

## 0.14

- support for `allHeaders` + `getResponseBodyAsBytes` [#2](https://github.com/leanovate/play-mockws/pull/2)  
  contribution from [@kwark](https://github.com/kwark)

- support for `withMethod` in `execute` and `stream` [#3](https://github.com/leanovate/play-mockws/pull/3)  
  contribution from [@sebdotv](https://github.com/sebdotv)

- support for `PATCH` method [#4](https://github.com/leanovate/play-mockws/pull/4)  
  contribution from [@knshiro](https://github.com/knshiro)

- Allow WS varargs to be passed as immutable Seqs [#5](https://github.com/leanovate/play-mockws/pull/5)  
  fix from [@jdanbrown](https://github.com/jdanbrown)

## 0.13

- handle URL query parameters

## 0.12

- remove dependency to specs2 - retain only mockito

## 0.11

- add [Route](play-mockws/src/main/scala/mockws/Route.scala)
