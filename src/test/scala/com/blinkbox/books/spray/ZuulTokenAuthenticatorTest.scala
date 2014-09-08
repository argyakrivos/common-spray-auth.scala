package com.blinkbox.books.spray

import com.blinkbox.books.auth._
import com.blinkbox.books.auth.Elevation._
import com.blinkbox.security.jwt.TokenException
import java.security.InvalidKeyException
import org.junit.runner.RunWith
import org.scalatest.{FunSuite, Matchers}
import org.scalatest.junit.JUnitRunner
import scala.concurrent.{ExecutionContext, Future}
import spray.can.Http.RequestTimeoutException
import spray.http.HttpChallenge
import spray.http.HttpHeaders._
import spray.http.StatusCodes._
import spray.routing.{AuthenticationFailedRejection, Route, HttpService}
import spray.routing.AuthenticationFailedRejection.{CredentialsRejected, CredentialsMissing}
import spray.testkit.ScalatestRouteTest

trait AuthenticatedService extends HttpService {
  implicit val executionContext: ExecutionContext
  def route(authenticator: BearerTokenAuthenticator): Route = get {
    path("") {
      authenticate(authenticator) { user =>
        complete(OK)
      }
    }
  }
}

@RunWith(classOf[JUnitRunner])
class ZuulTokenAuthenticatorTest extends FunSuite with ScalatestRouteTest with Matchers with AuthenticatedService {
  override implicit val actorRefFactory = system
  override implicit val executionContext = actorRefFactory.dispatcher

  val credentialsMissingHeaders = `WWW-Authenticate`(HttpChallenge("Bearer", realm = "")) :: Nil
  val credentialsInvalidHeaders = `WWW-Authenticate`(HttpChallenge("Bearer", realm = "", params = Map("error" -> "invalid_token", "error_description" -> "The access token is invalid"))) :: Nil
  val insufficientElevationHeaders = `WWW-Authenticate`(HttpChallenge("Bearer", realm = "", params = Map("error" -> "invalid_token", "error_reason" -> "unverified_identity", "error_description" -> "You need to re-verify your identity"))) :: Nil

  test("Authentication succeeds with valid bearer token and elevation") {
    val authenticator = new BearerTokenAuthenticator(_ => Future(User(123, None, "mytoken")), _ => Future(Critical))
    Get("/") ~> addHeader("Authorization", "Bearer x") ~> route(authenticator) ~> check {
      status should be(OK)
    }
  }

  test("The token is reported as missing if there is no Authorization header") {
    val authenticator = new BearerTokenAuthenticator(_ => Future(User(123, None, "mytoken")), _ => Future(Critical))
    Get("/") ~> route(authenticator) ~> check {
      rejection should be(AuthenticationFailedRejection(CredentialsMissing, credentialsMissingHeaders))
    }
  }

  test("The token is reported as missing if there is no bearer token") {
    val authenticator = new BearerTokenAuthenticator(_ => Future(User(123, None, "mytoken")), _ => Future(Critical))
    Get("/") ~> addHeader("Authorization", "Bearer") ~> route(authenticator) ~> check {
      rejection should be(AuthenticationFailedRejection(CredentialsMissing, credentialsMissingHeaders))
    }
  }

  test("The token is reported as missing if the wrong scheme is used") {
    val authenticator = new BearerTokenAuthenticator(_ => Future(User(123, None, "mytoken")), _ => Future(Critical))
    Get("/") ~> addHeader("Authorization", "Basic x") ~> route(authenticator) ~> check {
      rejection should be(AuthenticationFailedRejection(CredentialsMissing, credentialsMissingHeaders))
    }
  }

  test("The token is reported as invalid if it is invalid") {
    val authenticator = new BearerTokenAuthenticator(_ => Future.failed(new TokenException), _ => Future(Critical))
    Get("/") ~> addHeader("Authorization", "Bearer x") ~> route(authenticator) ~> check {
      rejection should be(AuthenticationFailedRejection(CredentialsRejected, credentialsInvalidHeaders))
    }
  }

  test("The token's identity is reported as unverified if elevation is too low") {
    val authenticator = new BearerTokenAuthenticator(_ => Future(User(123, None, "mytoken")), _ => Future(Unelevated))
    Get("/") ~> addHeader("Authorization", "Bearer x") ~> route(authenticator) ~> check {
      rejection should be(AuthenticationFailedRejection(CredentialsRejected, insufficientElevationHeaders))
    }
  }

  test("An internal server error occurs if the server is missing the crypto keys") {
    val authenticator = new BearerTokenAuthenticator(_ => Future.failed(new InvalidKeyException), _ => Future(Critical))
    Get("/") ~> addHeader("Authorization", "Bearer x") ~> route(authenticator) ~> check {
      status should be(InternalServerError)
    }
  }

  test("An internal server error occurs if checking the elevation times out") {
    val authenticator = new BearerTokenAuthenticator(_ => Future(User(123, None, "mytoken")), _ => Future.failed(new RequestTimeoutException(Get("/"), "")))
    Get("/") ~> addHeader("Authorization", "Bearer x") ~> route(authenticator) ~> check {
      status should be(InternalServerError)
    }
  }

  test("The elevation checker is not called if the desired elevation is Unelevated") {
    val authenticator = new BearerTokenAuthenticator(_ => Future(User(123, None, "mytoken")), _ => Future.failed(new Exception("Should not be called"))).withElevation(Unelevated)
    Get("/") ~> addHeader("Authorization", "Bearer x") ~> route(authenticator) ~> check {
      status should be(OK)
    }
  }
}
