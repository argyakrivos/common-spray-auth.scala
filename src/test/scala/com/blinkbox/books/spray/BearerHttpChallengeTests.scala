package com.blinkbox.books.spray

import org.scalatest.{FunSuite, Matchers}
import spray.http.StringRendering

class BearerHttpChallengeTests extends FunSuite with Matchers {

  test("A challenge with no params renders correctly") {
    val challenge = new BearerHttpChallenge()
    val rendering = new StringRendering()
    challenge.render(rendering)
    rendering.get should be("""Bearer""")
  }

  test("A challenge with error and error_description params renders correctly") {
    val challenge = new BearerHttpChallenge(Map("error" -> "invalid_token", "error_description" -> "The access token expired"))
    val rendering = new StringRendering()
    challenge.render(rendering)
    rendering.get should be("""Bearer error="invalid_token", error_description="The access token expired"""")
  }

  test("A challenge with error, error_reason and error_description params renders correctly") {
    val challenge = new BearerHttpChallenge(Map("error" -> "invalid_token", "error_reason" -> "unverified_identity", "error_description" -> "User identity must be reverified"))
    val rendering = new StringRendering()
    challenge.render(rendering)
    rendering.get should be("""Bearer error="invalid_token", error_reason="unverified_identity", error_description="User identity must be reverified"""")
  }

}
