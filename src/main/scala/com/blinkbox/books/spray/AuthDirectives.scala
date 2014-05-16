package com.blinkbox.books.spray

import spray.http.HttpHeaders.Authorization
import spray.routing._
import spray.routing.AuthenticationFailedRejection.CredentialsRejected
import spray.routing.directives.BasicDirectives.provide
import spray.routing.directives.HeaderDirectives.optionalHeaderValueByType
import spray.routing.directives.RouteDirectives.reject
import spray.http.OAuth2BearerToken
import scala.Some

import com.blinkbox.books.spray.ZuulTokenAuthenticator.credentialsInvalidHeaders

object AuthDirectives {

  val optionalAuthToken: Directive1[Option[String]] = optionalHeaderValueByType[Authorization]().flatMap {
    case None         => provide(None)
    case Some(header) => extractAuthToken(header) match {
      case Right(token)    => provide(Some(token))
      case Left(rejection) => reject(rejection)
    }
  }

  private def extractAuthToken(header: Authorization): Either[Rejection, String] = header match {
    case Authorization(OAuth2BearerToken(token)) => Right(token)
    case _ => Left(AuthenticationFailedRejection(CredentialsRejected, credentialsInvalidHeaders))
  }
}
