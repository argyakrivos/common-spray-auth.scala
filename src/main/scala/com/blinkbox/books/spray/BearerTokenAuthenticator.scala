package com.blinkbox.books.spray

import com.blinkbox.books.auth.Elevation._
import com.blinkbox.books.auth.{TokenDeserializer, ElevationChecker, User}
import com.blinkbox.security.jwt.TokenException
import spray.http.HttpHeaders.{Authorization, `WWW-Authenticate`}
import spray.http._
import spray.routing.AuthenticationFailedRejection.{CredentialsMissing, CredentialsRejected}
import spray.routing.authentication._
import spray.routing.{AuthenticationFailedRejection, Rejection, RequestContext}
import spray.util._

import scala.concurrent.{ExecutionContext, Future}

class InvalidTokenStatusException(message: String = "The provided token has a non-valid status") extends Exception(message)

trait ElevatedContextAuthenticator[T] extends ContextAuthenticator[T] {
  def withElevation(e: Elevation): ElevatedContextAuthenticator[T]
}

class BearerTokenAuthenticator(deserializer: TokenDeserializer, elevationChecker: ElevationChecker, val elevation: Elevation = Critical)(implicit val executionContext: ExecutionContext)
  extends ElevatedContextAuthenticator[User] {
  import com.blinkbox.books.spray.BearerTokenAuthenticator._

  def withElevation(e: Elevation) = new BearerTokenAuthenticator(deserializer, elevationChecker, e)

  override def apply(ctx: RequestContext): Future[Either[Rejection, User]] =
    ctx.request.headers.findByType[`Authorization`] match {
      case Some(Authorization(OAuth2BearerToken(token))) => authenticate(token)
      case _ => Future.successful(Left(AuthenticationFailedRejection(CredentialsMissing, credentialsMissingHeaders)))
    }

  private def authenticate(token: String): Future[Either[Rejection, User]] =
    deserializer(token) flatMap { user =>
      if (elevation == Unelevated) Future.successful(Right(user))
      else elevationChecker(user) map (_ >= elevation) map {
        case true => Right(user)
        case false => Left(AuthenticationFailedRejection(CredentialsRejected, unverifiedIdentityHeaders))
      }
    } recover {
      case _: TokenException | _: InvalidTokenStatusException => Left(AuthenticationFailedRejection(CredentialsRejected, credentialsInvalidHeaders))
    }
}

object BearerTokenAuthenticator {
  val credentialsMissingHeaders = `WWW-Authenticate`(BearerHttpChallenge.credentialsMissing) :: Nil
  val credentialsInvalidHeaders = `WWW-Authenticate`(BearerHttpChallenge.credentialsInvalid) :: Nil
  val unverifiedIdentityHeaders = `WWW-Authenticate`(BearerHttpChallenge.unverifiedIdentity) :: Nil
}

class BearerHttpChallenge(params: Map[String, String] = Map.empty) extends HttpChallenge(scheme = "Bearer", realm = "", params) {
  override def render[R <: Rendering](r: R): r.type = {
    r ~~ scheme
    if (params.nonEmpty) params.foreach { case (k, v) => r ~~ " " ~~ k ~~ '=' ~~#! v }
    r
  }
}

object BearerHttpChallenge {
  val credentialsMissing = new BearerHttpChallenge
  val credentialsInvalid = new BearerHttpChallenge(params = Map("error" -> "invalid_token", "error_description" -> "The access token is invalid"))
  val unverifiedIdentity = new BearerHttpChallenge(params = Map("error" -> "invalid_token", "error_reason" -> "unverified_identity", "error_description" -> "You need to re-verify your identity"))
}
