package mockws

import java.io.{File, FileWriter}
import java.nio.file.Path

import akka.stream.scaladsl.Source
import akka.util.ByteString
import org.scalatest.prop.PropertyChecks
import org.scalatest.{FunSuite, Matchers}
import play.api.http.HttpEntity
import play.api.libs.ws.WSResponse
import play.api.mvc.MultipartFormData.DataPart
import play.api.mvc.{Action, ResponseHeader, Result}
import play.api.test.Helpers._

import scala.collection.immutable.Seq

/**
  * Tests that [[MockWS]] simulates a WS client, in particular the methods involving authentication
  */
class FileTest extends FunSuite with Matchers with PropertyChecks {

  val sendFileContentBackRaw = Action {
    req =>
      // we're expecting files here, thats why we're doing get on the optional
      val firstFile = req.body.asRaw.get.asFile

      val response = new StringBuffer(200)
      response.append(req.method)
      response.append(":")
      response.append(req.headers.get("content-type").getOrElse("missing")  )
      response.append(": ")

      val source = scala.io.Source.fromFile(firstFile)

      for (line <- source.getLines) {
        response.append(line)
      }
      source.close()

      Result(
        header = ResponseHeader(200),
        body = HttpEntity.Strict(ByteString(response.toString), Some("text/plain"))
      )
  }

  val sendFileContentBackText = Action {
    req =>
      // we're expecting files here, thats why we're doing get on the optional
      val content = req.body.asText.getOrElse("missing")

      val response = new StringBuffer(200)
      response.append(req.method)
      response.append(":")
      response.append(req.headers.get("content-type").getOrElse("missing")  )
      response.append(": ")

      response.append(content)

      Result(
        header = ResponseHeader(200),
        body = HttpEntity.Strict(ByteString(response.toString), Some("text/plain"))
      )
  }

  val sendFileContentBackJson = Action {
    req =>
      // we're expecting JSON here, thats why we're doing get on the optional
      val content = req.body.asJson.get

      val response = new StringBuffer(200)
      response.append(req.method)
      response.append(":")
      response.append(req.headers.get("content-type").getOrElse("missing")  )
      response.append(": ")

      response.append(content.toString())

      Result(
        header = ResponseHeader(200),
        body = HttpEntity.Strict(ByteString(response.toString), Some("application/json"))
      )
  }

  val sendFileContentBackXML = Action {
    req =>
      // we're expecting JSON here, thats why we're doing get on the optional
      val content = req.body.asXml.get

      val response = new StringBuffer(200)
      response.append(req.method)
      response.append(":")
      response.append(req.headers.get("content-type").getOrElse("missing")  )
      response.append(": ")

      response.append(content.toString())

      Result(
        header = ResponseHeader(200),
        body = HttpEntity.Strict(ByteString(response.toString), Some("application/xml"))
      )
  }


  test("RAW: receive the file content back of what we sent as [POST]") {
    val file = File.createTempFile("mockws", ".bin")
    try {

      val fw = new FileWriter(file)
      fw.write("this is a test")
      fw.close()

      val ws = MockWS {
        case (POST, "/post") ⇒
          sendFileContentBackRaw
      }

      val response : WSResponse = await(ws.url("/post").post(file))
      response.body shouldEqual "POST:application/octet-stream: this is a test"
      response.header("content-type") shouldBe Some("text/plain")

      ws.close()
    } finally {
      file.deleteOnExit()
    }
  }

  test("RAW: receive the file content back of what we sent as [PUT]") {
    val file = File.createTempFile("mockws", ".bin")
    try {

      val fw = new FileWriter(file)
      fw.write("this is a test")
      fw.close()

      val ws = MockWS {
        case (PUT, "/put") ⇒
          sendFileContentBackRaw
      }

      val response : WSResponse = await(ws.url("/put").put(file))
      response.body shouldEqual "PUT:application/octet-stream: this is a test"
      response.header("content-type") shouldBe Some("text/plain")

      ws.close()
    } finally {
      file.deleteOnExit()
    }
  }

  test("RAW: receive the file content back of what we sent as [PATCH]") {
    val file = File.createTempFile("mockws", ".bin")
    try {

      val fw = new FileWriter(file)
      fw.write("this is a test")
      fw.close()

      val ws = MockWS {
        case (PATCH, "/patch") ⇒
          sendFileContentBackRaw
      }

      val response : WSResponse = await(ws.url("/patch").patch(file))
      response.body shouldEqual "PATCH:application/octet-stream: this is a test"
      response.header("content-type") shouldBe Some("text/plain")

      ws.close()
    } finally {
      file.deleteOnExit()
    }
  }




  test("Text: receive the file content back of what we sent as [POST]") {
    val file = File.createTempFile("mockws", ".txt")
    try {

      val fw = new FileWriter(file)
      fw.write("this is a test")
      fw.close()

      val ws = MockWS {
        case (POST, "/post") ⇒
          sendFileContentBackText
      }

      val response : WSResponse = await(ws.url("/post").post(file))
      response.body shouldEqual "POST:text/plain: this is a test"
      response.header("content-type") shouldBe Some("text/plain")

      ws.close()
    } finally {
      file.deleteOnExit()
    }
  }

  test("Text: receive the file content back of what we sent as [PUT]") {
    val file = File.createTempFile("mockws", ".txt")
    try {

      val fw = new FileWriter(file)
      fw.write("this is a test")
      fw.close()

      val ws = MockWS {
        case (PUT, "/put") ⇒
          sendFileContentBackText
      }

      val response : WSResponse = await(ws.url("/put").put(file))
      response.body shouldEqual "PUT:text/plain: this is a test"
      response.header("content-type") shouldBe Some("text/plain")

      ws.close()
    } finally {
      file.deleteOnExit()
    }
  }

  test("Text: receive the file content back of what we sent as [PATCH]") {
    val file = File.createTempFile("mockws", ".txt")
    try {

      val fw = new FileWriter(file)
      fw.write("this is a test")
      fw.close()

      val ws = MockWS {
        case (PATCH, "/patch") ⇒
          sendFileContentBackText
      }

      val response : WSResponse = await(ws.url("/patch").patch(file))
      response.body shouldEqual "PATCH:text/plain: this is a test"
      response.header("content-type") shouldBe Some("text/plain")

      ws.close()
    } finally {
      file.deleteOnExit()
    }
  }








  test("JSON: receive the file content back of what we sent as [POST]") {
    val file = File.createTempFile("mockws", ".json")
    try {

      val fw = new FileWriter(file)
      fw.write("{\"key\": \"value\"     }")
      fw.close()

      val ws = MockWS {
        case (POST, "/post") ⇒
          sendFileContentBackJson
      }

      val response : WSResponse = await(ws.url("/post").post(file))
      response.body shouldEqual "POST:application/json: {\"key\":\"value\"}"
      response.header("content-type") shouldBe Some("application/json")

      ws.close()
    } finally {
      file.deleteOnExit()
    }
  }

  test("JSON: receive the file content back of what we sent as [PUT]") {
    val file = File.createTempFile("mockws", ".json")
    try {

      val fw = new FileWriter(file)
      fw.write("{\"key\": \"value\"     }")
      fw.close()

      val ws = MockWS {
        case (PUT, "/put") ⇒
          sendFileContentBackJson
      }

      val response : WSResponse = await(ws.url("/put").put(file))
      response.body shouldEqual "PUT:application/json: {\"key\":\"value\"}"
      response.header("content-type") shouldBe Some("application/json")

      ws.close()
    } finally {
      file.deleteOnExit()
    }
  }

  test("JSON: receive the file content back of what we sent as [PATCH]") {
    val file = File.createTempFile("mockws", ".json")
    try {

      val fw = new FileWriter(file)
      fw.write("{\"key\": \"value\"     }")
      fw.close()

      val ws = MockWS {
        case (PATCH, "/patch") ⇒
          sendFileContentBackJson
      }

      val response : WSResponse = await(ws.url("/patch").patch(file))
      response.body shouldEqual "PATCH:application/json: {\"key\":\"value\"}"
      response.header("content-type") shouldBe Some("application/json")

      ws.close()
    } finally {
      file.deleteOnExit()
    }
  }





  //////////////// XML

  test("XML: receive the file content back of what we sent as [POST]") {
    val file = File.createTempFile("mockws", ".xml")
    try {

      val fw = new FileWriter(file)
      fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?><key>value</key>")
      fw.close()

      val ws = MockWS {
        case (POST, "/post") ⇒
          sendFileContentBackXML
      }

      val response : WSResponse = await(ws.url("/post").post(file))
      response.body shouldEqual "POST:application/xml: <key>value</key>"
      response.header("content-type") shouldBe Some("application/xml")

      ws.close()
    } finally {
      file.deleteOnExit()
    }
  }

  test("XML: receive the file content back of what we sent as [PUT]") {
    val file = File.createTempFile("mockws", ".xml")
    try {

      val fw = new FileWriter(file)
      fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?><key>value</key>")
      fw.close()

      val ws = MockWS {
        case (PUT, "/put") ⇒
          sendFileContentBackXML
      }

      val response : WSResponse = await(ws.url("/put").put(file))
      response.body shouldEqual "PUT:application/xml: <key>value</key>"
      response.header("content-type") shouldBe Some("application/xml")

      ws.close()
    } finally {
      file.deleteOnExit()
    }
  }

  test("XML: receive the file content back of what we sent as [PATCH]") {
    val file = File.createTempFile("mockws", ".xml")
    try {

      val fw = new FileWriter(file)
      fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?><key>value</key>")
      fw.close()

      val ws = MockWS {
        case (PATCH, "/patch") ⇒
          sendFileContentBackXML
      }

      val response : WSResponse = await(ws.url("/patch").patch(file))

      response.body shouldEqual "PATCH:application/xml: <key>value</key>"
      response.header("content-type") shouldBe Some("application/xml")


      ws.close()
    } finally {
      file.deleteOnExit()
    }
  }



  // Upload as MISC (no content-type detected)
  test("Undefined Mime-Type: receive the file content back of what we sent as [PATCH]") {
    val file = File.createTempFile("mockws", ".something")
    try {

      val fw = new FileWriter(file)
      fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?><key>value</key>")
      fw.close()

      val ws = MockWS {
        case (PATCH, "/patch") ⇒
          sendFileContentBackRaw
      }

      val response : WSResponse = await(ws.url("/patch").patch(file))

      response.body shouldEqual "PATCH:missing: <?xml version=\"1.0\" encoding=\"UTF-8\"?><key>value</key>"
      response.header("content-type") shouldBe Some("text/plain")


      ws.close()
    } finally {
      file.deleteOnExit()
    }
  }


  // Upload as MISC (no content-type detected)
  test("override content-type: receive the file content back of what we sent as [PATCH]") {
    val file = File.createTempFile("mockws", ".something")
    try {

      val fw = new FileWriter(file)
      fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?><key>value</key>")
      fw.close()

      val ws = MockWS {
        case (PATCH, "/patch") ⇒
          sendFileContentBackText
      }

      val response : WSResponse = await(ws.url("/patch").withHeaders(("Content-Type", "text/plain")).patch(file))

      response.body shouldEqual "PATCH:text/plain: <?xml version=\"1.0\" encoding=\"UTF-8\"?><key>value</key>"
      response.header("content-type") shouldBe Some("text/plain")


      ws.close()
    } finally {
      file.deleteOnExit()
    }
  }

  // Upload as MISC (no content-type detected)
  test("override content-type of file sent back of what we sent as (content type specified file [PATCH]") {
    val file = File.createTempFile("mockws", ".json")
    try {

      val fw = new FileWriter(file)
      fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?><key>value</key>")
      fw.close()

      val ws = MockWS {
        case (PATCH, "/patch") ⇒
          sendFileContentBackXML
      }

      val response : WSResponse = await(ws.url("/patch").withBody(file).withHeaders(("Content-Type", "application/xml")).withMethod("patch").execute())

      response.body shouldEqual "PATCH:application/xml: <key>value</key>"
      response.header("content-type") shouldBe Some("application/xml")


      ws.close()
    } finally {
      file.deleteOnExit()
    }
  }


}
