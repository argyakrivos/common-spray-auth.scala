package com.blinkbox.books.auth

import com.blinkbox.security.jwt.InvalidClaimException
import scala.collection.convert.WrapAsScala._

object Elevation extends Enumeration {
  type Elevation = Value
  val Unelevated = Value("NONE")
  val Elevated = Value("ELEVATED")
  val Critical = Value("CRITICAL")
}

object UserRole extends Enumeration {
  type UserRole = Value
  val Employee = Value("emp")
  val ITOperations = Value("ops")
  val CustomerServicesManager = Value("csm")
  val CustomerServicesRep = Value("csr")
  val ContentManager = Value("ctm")
  val Merchandising = Value("mer")
  val Marketing = Value("mkt")
  val NotUnderstood = Value("???")
}

import UserRole._

/**
 * An authenticated user.
 *
 * @param id The user identifier.
 * @param clientId The client identifier, if the user is on an authenticated client.
 * @param claims Additional claims that have been asserted about the user.
 */
case class User(id: Int, clientId: Option[Int] = None, claims: Map[String, AnyRef] = Map.empty) {

  /**
   * The roles that the user is in.
   */
  lazy val roles: Set[UserRole] = claims.get("bb/rol") map {
    // irritatingly the java structures List/Array aren't Iterable, so we need multiple cases :-/
    case rs: java.util.List[_] => rs.map(parseRole).toSet
    case rs: Array[_] => rs.map(parseRole).toSet
    case rs: Iterable[_] => rs.map(parseRole).toSet
    case _ => throw new InvalidClaimException("The roles claim is invalid")
  } getOrElse Set()

  /**
   * Checks whether the user is in a specified role.
   *
   * @param role The role to check.
   * @return Whether the user is in the role.
   */
  def isInRole(role: UserRole): Boolean = roles.contains(role)

  // we don't fail if we don't understand the role because the roles may be updated to include
  // other ones without this code being updated everywhere it is deployed, so we'll just map it
  // to a marker value indicating that there were roles that weren't understood.
  private def parseRole(s: Any): UserRole = UserRole.values.find(_.toString == s.toString).getOrElse(NotUnderstood)
}

object User {
  private val UserUrn = """urn:blinkbox:zuul:user:([0-9]+)""".r
  private val ClientUrn = """urn:blinkbox:zuul:client:([0-9]+)""".r
  private val SubjectClaim = "sub"
  private val ClientIdClaim = "bb/cid"

  def apply(claims: java.util.Map[String, AnyRef]): User = User(mapAsScalaMap(claims).toMap)

  def apply(claims: Map[String, AnyRef]): User = {
    val id = claims.get(SubjectClaim) match {
      case Some(UserUrn(n)) => parseIdentifier(n)
      case _ => throw new InvalidClaimException("The user id claim is missing or invalid.")
    }
    val clientId = claims.get(ClientIdClaim) map {
      case ClientUrn(n) => parseIdentifier(n)
      case _ => throw new InvalidClaimException("The client id claim is invalid.")
    }
    User(id, clientId, claims - SubjectClaim - ClientIdClaim)
  }

  private def parseIdentifier(id: String): Int = try {
    id.toInt
  } catch {
    case e: NumberFormatException => throw new InvalidClaimException("The identifier is outside the expected range.")
  }
}