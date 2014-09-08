package com.blinkbox.books.spray

import com.blinkbox.books.spray.AuthDirectives.authToken
import com.blinkbox.books.spray.BearerTokenAuthenticator.{credentialsInvalidHeaders, credentialsMissingHeaders}
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import spray.http.HttpHeaders.Authorization
import spray.http.{GenericHttpCredentials, OAuth2BearerToken}
import spray.routing.AuthenticationFailedRejection
import spray.routing.AuthenticationFailedRejection.{CredentialsMissing, CredentialsRejected}
import spray.routing.Directives.complete
import spray.testkit.ScalatestRouteTest

@RunWith(classOf[JUnitRunner])
class AuthTokenDirectiveTests extends FunSuite with ScalatestRouteTest {

  val route = authToken { x =>
    complete(x)
  }

  test("Extracts token from supported Authorization header") {
    Get("/") ~> Authorization(OAuth2BearerToken("token")) ~> route ~> check {
      assert(responseAs[String] == "token")
    }
  }

  test("Rejects when Authorization header is missing") {
    Get("/") ~> route ~> check {
      assert(rejection == AuthenticationFailedRejection(CredentialsMissing, credentialsMissingHeaders))
    }
  }

  test("Rejects unsupported Authorization header") {
    Get("/") ~> Authorization(GenericHttpCredentials("god", "Argy")) ~> route ~> check {
      assert(rejection == AuthenticationFailedRejection(CredentialsRejected, credentialsInvalidHeaders))
    }
  }
}
