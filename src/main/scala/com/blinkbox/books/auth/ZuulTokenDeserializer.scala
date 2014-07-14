package com.blinkbox.books.auth

import com.blinkbox.security.jwt.{InvalidTokenException, TokenDecoder}
import scala.concurrent.{ExecutionContext, Future}

class ZuulTokenDeserializer(decoder: TokenDecoder)(implicit val executionContext: ExecutionContext) extends TokenDeserializer {
  override def apply(token: String): Future[User] = Future(decoder.decode(token)) flatMap {
    case claims: java.util.Map[_, _] => Future(User(claims.asInstanceOf[java.util.Map[String, AnyRef]]))
    case _ => Future.failed(new InvalidTokenException("The token does not contain valid claims"))
  }
}


