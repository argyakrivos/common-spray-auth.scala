package com.blinkbox.books.spray

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions
import spray.http.HttpHeaders.Authorization
import spray.routing._
import spray.routing.AuthenticationFailedRejection.{CredentialsMissing, CredentialsRejected}
import spray.routing.authentication.{ContextAuthenticator, Authentication}
import spray.routing.directives.BasicDirectives.{extract, provide}
import spray.routing.directives.FutureDirectives.onSuccess
import spray.routing.directives.HeaderDirectives.optionalHeaderValueByType
import spray.routing.directives.RouteDirectives.reject
import spray.http.OAuth2BearerToken

import com.blinkbox.books.spray.ZuulTokenAuthenticator.credentialsInvalidHeaders

object AuthDirectives {

  def optionalAuthenticate[T](magnet: OptionalAuthMagnet[T]): Directive1[Option[T]] = magnet.directive

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

class OptionalAuthMagnet[T](authDirective: Directive1[Authentication[T]])(implicit executor: ExecutionContext) {
  val directive: Directive1[Option[T]] = authDirective.flatMap {
    case Right(user) => provide(Some(user))
    case Left(rejection @ AuthenticationFailedRejection(CredentialsMissing, _)) => provide(None)
    case Left(rejection) => reject(rejection)
  }
}

object OptionalAuthMagnet {
  implicit def fromFutureAuth[T](auth: ⇒ Future[Authentication[T]])(implicit executor: ExecutionContext): OptionalAuthMagnet[T] =
    new OptionalAuthMagnet(onSuccess(auth))

  implicit def fromContextAuthenticator[T](auth: ContextAuthenticator[T])(implicit executor: ExecutionContext): OptionalAuthMagnet[T] =
    new OptionalAuthMagnet(extract(auth).flatMap(onSuccess(_)))
}
