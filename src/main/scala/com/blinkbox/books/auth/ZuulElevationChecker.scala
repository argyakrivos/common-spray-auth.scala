package com.blinkbox.books.auth

import akka.actor.ActorRefFactory
import akka.util.Timeout
import com.blinkbox.books.auth.Elevation._
import org.json4s.DefaultFormats
import org.json4s.ext.EnumNameSerializer
import org.json4s.native.JsonMethods._
import scala.concurrent.Future
import scala.concurrent.duration._
import spray.client.pipelining._
import spray.http._
import spray.http.HttpCharsets._
import spray.http.MediaTypes._
import spray.httpx.Json4sSupport
import spray.httpx.unmarshalling.Unmarshaller

private case class ZuulSession(tokenElevation: Elevation)

class ZuulElevationChecker(sessionUri: String)(implicit val actorRefFactory: ActorRefFactory) extends TokenElevationChecker with Json4sSupport {
  implicit val dispatcher = actorRefFactory.dispatcher
  implicit val timeout = Timeout(2.seconds)
  implicit val json4sFormats = DefaultFormats + new EnumNameSerializer(Elevation)

  implicit def unmarshaller[T: Manifest] = Unmarshaller[T](`application/json`) {
    case entity: HttpEntity.NonEmpty => parse(entity.asString(defaultCharset = `UTF-8`)).camelizeKeys.extract[T]
  }

  private def pipeline(token: String): HttpRequest => Future[ZuulSession] = (
    addHeader("Accept", "application/json")
      ~> addHeader("Authorization", s"Bearer $token")
      ~> sendReceive
      ~> unmarshal[ZuulSession]
    )

  def apply(token: String): Future[Elevation] = pipeline(token)(Get(sessionUri)).map(_.tokenElevation)
}
