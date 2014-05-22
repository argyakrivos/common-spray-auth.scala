package com.blinkbox.books.spray

import com.typesafe.config.ConfigFactory
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, FunSuite}
import spray.http.{HttpChallenge, BasicHttpCredentials, OAuth2BearerToken}
import spray.http.HttpHeaders.{`WWW-Authenticate`, Authorization}
import spray.http.StatusCodes.OK
import spray.routing.authentication.{BasicAuth, UserPass}
import spray.routing.AuthenticationFailedRejection
import spray.routing.AuthenticationFailedRejection.CredentialsRejected
import spray.routing.Directives.complete
import spray.testkit.ScalatestRouteTest

import com.blinkbox.books.spray.AuthDirectives.optionalAuthenticate

@RunWith(classOf[JUnitRunner])
class OptionalAuthenticateDirectiveTests extends FunSuite with Matchers with ScalatestRouteTest {

  def extractUser(userPass: UserPass): String = userPass.user
  val config = ConfigFactory.parseString("John = p4ssw0rd")
  val authenticator = BasicAuth(realm = "test", config = config, createUser = extractUser _)

  val route = optionalAuthenticate(authenticator) { optionalUser =>
    val response = optionalUser.getOrElse("Nobody")
    complete(response)
  }

  test("Authenticates a user with valid credentials") {
    Get("/") ~> addHeader(Authorization(BasicHttpCredentials("John", "p4ssw0rd"))) ~> route ~> check {
      status should be(OK)
      responseAs[String] should be("John")
    }
  }

  test("Authenticates no user when no credentials are passed") {
    Get("/") ~> route ~> check {
      status should be(OK)
      responseAs[String] should be("Nobody")
    }
  }

  test("Rejects the request when the credentials are invalid") {
    Get("/") ~> addHeader(Authorization(BasicHttpCredentials("John", "wrongpwd"))) ~> route ~> check {
      rejection should be(AuthenticationFailedRejection(CredentialsRejected, List(`WWW-Authenticate`(HttpChallenge("Basic", "test")))))
    }
  }

  test("Rejects the request when the credentials use an unsupported scheme") {
    Get("/") ~> addHeader(Authorization(OAuth2BearerToken("token"))) ~> route ~> check {
      rejection should be(AuthenticationFailedRejection(CredentialsRejected, List(`WWW-Authenticate`(HttpChallenge("Basic", "test")))))
    }
  }
}
