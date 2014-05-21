package com.blinkbox.books.auth

import com.blinkbox.security.jwt.{InvalidClaimException, InvalidTokenException, TokenDecoder}
import scala.concurrent.{ExecutionContext, Future}

class ZuulTokenDeserializer(decoder: TokenDecoder)(implicit val executionContext: ExecutionContext) extends TokenDeserializer {
  private val UserUrn = """urn:blinkbox:zuul:user:(\d+)""".r
  private val ClientUrn = """urn:blinkbox:zuul:client:(\d+)""".r

  override def apply(token: String): Future[User] = Future(decoder.decode(token)) flatMap {
    case claims: java.util.Map[_, _] => user(claims)
    case _ => Future.failed(new InvalidTokenException("The token does not contain valid claims"))
  }

  private def user(claims: java.util.Map[_, _]): Future[User] = Future { User(userId(claims), clientId(claims)) }

  private def userId(claims: java.util.Map[_, _]): Int = claims.get("sub") match {
    case UserUrn(id) => identifier(id)
    case _ => throw new InvalidClaimException("The token does not contain a valid user id claim.")
  }

  private def clientId(claims: java.util.Map[_, _]): Option[Int] = Option(claims.get("bb/cid")) map {
    case ClientUrn(id) => identifier(id)
    case _ => throw new InvalidClaimException("The token contains an invalid client id claim.")
  }

  private def identifier(id: String) = try {
    id.toInt
  } catch {
    case e: NumberFormatException => throw new InvalidClaimException("The identifier is outside the expected range.")
  }
}


