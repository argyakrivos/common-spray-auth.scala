package com.blinkbox.books.spray

import com.blinkbox.books.spray.AuthDirectives.optionalAuthToken
import com.blinkbox.books.spray.BearerTokenAuthenticator.credentialsMissingHeaders
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import spray.http.HttpHeaders.Authorization
import spray.http.{GenericHttpCredentials, OAuth2BearerToken}
import spray.routing.AuthenticationFailedRejection
import spray.routing.AuthenticationFailedRejection.CredentialsMissing
import spray.routing.Directives.complete
import spray.testkit.ScalatestRouteTest

@RunWith(classOf[JUnitRunner])
class OptionalAuthTokenDirectiveTests extends FunSuite with ScalatestRouteTest {

  val route = optionalAuthToken { x =>
    complete(x)
  }

  test("Extracts token from supported Authorization header") {
    Get("/").withHeaders(Authorization(OAuth2BearerToken("token"))) ~> route ~> check {
      assert(responseAs[String] == "token")
    }
  }

  test("Extracts None when there is no Authorization header") {
    Get("/") ~> route ~> check {
      assert(responseAs[String] == "")
    }
  }

  test("Rejects unsupported Authorization header") {
    Get("/").withHeaders(Authorization(GenericHttpCredentials("god", "Argy"))) ~> route ~> check {
      assert(rejection == AuthenticationFailedRejection(CredentialsMissing, credentialsMissingHeaders))
    }
  }
}
