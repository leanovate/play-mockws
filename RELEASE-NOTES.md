# Release notes

## 2.6.3-SNAPSHOT

- Fix withHttpHeaders behavior for FakeWSRequestHolder [33](https://github.com/leanovate/play-mockws/pull/33)
  contribution from [@a-shkarupin](https://github.com/a-shkarupin)
  
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

- add [Route](src/main/scala/mockws/Route.scala)
