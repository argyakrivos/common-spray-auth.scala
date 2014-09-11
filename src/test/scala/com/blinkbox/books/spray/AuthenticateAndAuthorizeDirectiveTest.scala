package com.blinkbox.books.spray

import com.blinkbox.books.auth.UserRole.UserRole
import com.blinkbox.books.auth.{RoleConstraint, User, UserRole}
import com.blinkbox.books.spray.AuthDirectives._
import com.typesafe.config.ConfigFactory
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FunSuite, Matchers}
import spray.http.HttpHeaders.Authorization
import spray.http.{BasicHttpCredentials, StatusCodes}
import spray.routing.authentication.{BasicAuth, UserPass}
import spray.routing.{AuthorizationFailedRejection, Directives}
import spray.testkit.ScalatestRouteTest

@RunWith(classOf[JUnitRunner])
class AuthenticateAndAuthorizeDirectiveTest extends FunSuite with Matchers with ScalatestRouteTest with Directives {

  def userWithRoles(roles: UserRole*) = User(1, None, "foo", Map("bb/rol" -> roles))

  def extractUser(roles: UserRole*)(userPass: UserPass): User = userWithRoles(roles: _*)
  val config = ConfigFactory.parseString("John = p4ssw0rd")
  def authenticator(roles: UserRole*) = BasicAuth(realm = "test", config = config, createUser = extractUser(roles: _*) _)

  def createRoute(constraint: RoleConstraint, roles: UserRole*) = authenticateAndAuthorize(authenticator(roles: _*) , constraint) { user =>
    complete("OK")
  }

  import com.blinkbox.books.auth.RoleConstraint._
  import com.blinkbox.books.auth.UserRole._

  test("A user with a single role that is the one required by the route should be allowed") {
    val route = createRoute(hasRole(ContentManager), ContentManager)
    Get("/") ~> addHeader(Authorization(BasicHttpCredentials("John", "p4ssw0rd"))) ~> route ~> check {
      status shouldBe(StatusCodes.OK)
    }
  }

  test("A user with a single role that is not the one required by the route should not be allowed") {
    val route = createRoute(hasRole(ITOperations), ContentManager)
    Get("/") ~> addHeader(Authorization(BasicHttpCredentials("John", "p4ssw0rd"))) ~> route ~> check {
      rejections shouldBe(AuthorizationFailedRejection :: Nil)
    }
  }

  test("A user with a single role that is in the 'hasAny' rule specified in the route should be allowed") {
    val route = createRoute(hasAnyRole(ITOperations, ContentManager), ITOperations)
    Get("/") ~> addHeader(Authorization(BasicHttpCredentials("John", "p4ssw0rd"))) ~> route ~> check {
      status shouldBe(StatusCodes.OK)
    }
  }

  test("A user with a single role that is not in the 'hasAny' rule specified in the route should not be allowed") {
    val route = createRoute(hasAnyRole(ITOperations, ContentManager), Employee)
    Get("/") ~> addHeader(Authorization(BasicHttpCredentials("John", "p4ssw0rd"))) ~> route ~> check {
      rejections shouldBe(AuthorizationFailedRejection :: Nil)
    }
  }

  test("A user with a multiple roles including one that is in the 'hasAny' rule specified in the route should be allowed") {
    val route = createRoute(hasAnyRole(ITOperations, ContentManager), ITOperations, Employee)
    Get("/") ~> addHeader(Authorization(BasicHttpCredentials("John", "p4ssw0rd"))) ~> route ~> check {
      status shouldBe(StatusCodes.OK)
    }
  }

  test("A user with multiple roles but not one that is not in the 'hasAny' rule specified in the route should not be allowed") {
    val route = createRoute(hasAnyRole(ITOperations, ContentManager), Employee, Marketing)
    Get("/") ~> addHeader(Authorization(BasicHttpCredentials("John", "p4ssw0rd"))) ~> route ~> check {
      rejections shouldBe(AuthorizationFailedRejection :: Nil)
    }
  }

  test("A user with a multiple roles that match the ones in the 'hasEvery' rule specified in the route should be allowed") {
    val route = createRoute(hasEveryRole(ITOperations, ContentManager), ITOperations, ContentManager)
    Get("/") ~> addHeader(Authorization(BasicHttpCredentials("John", "p4ssw0rd"))) ~> route ~> check {
      status shouldBe(StatusCodes.OK)
    }
  }

  test("A user with a multiple roles that do not match the ones in the 'hasEvery' rule specified in the route should be allowed") {
    val route = createRoute(hasEveryRole(ITOperations, ContentManager), ITOperations, Employee)
    Get("/") ~> addHeader(Authorization(BasicHttpCredentials("John", "p4ssw0rd"))) ~> route ~> check {
      rejections shouldBe(AuthorizationFailedRejection :: Nil)
    }
  }
}
