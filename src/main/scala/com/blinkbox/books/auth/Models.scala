package com.blinkbox.books.auth

case class User(id: Int, clientId: Option[Int] = None)

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
}