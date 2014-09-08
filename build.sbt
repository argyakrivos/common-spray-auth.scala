name := "common-spray-auth"

organization := "com.blinkbox.books"

version := scala.util.Try(scala.io.Source.fromFile("VERSION").mkString.trim).getOrElse("0.0.0")

crossScalaVersions := Seq("2.10.4", "2.11.2")

scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-encoding", "utf8", "-target:jvm-1.7")

resolvers += "spray" at "http://repo.spray.io/"

libraryDependencies ++= {
  val akkaV = "2.3.6"
  val sprayV = "1.3.1"
  val json4sV = "3.2.10"
  Seq(
    "io.spray"            %%  "spray-can"           % sprayV,
    "io.spray"            %%  "spray-routing"       % sprayV,
    "io.spray"            %%  "spray-client"        % sprayV,
    "org.json4s"          %%  "json4s-jackson"      % json4sV,
    "org.json4s"          %%  "json4s-ext"          % json4sV,
    "com.typesafe.akka"   %%  "akka-actor"          % akkaV,
    "com.blinkbox.books"  %%  "common-scala-test"   % "0.3.0"   % "test",
    "io.spray"            %%  "spray-testkit"       % sprayV  % "test",
    "com.typesafe.akka"   %%  "akka-testkit"        % akkaV   % "test",
    "com.blinkboxbooks.platform.security" % "blinkbox-security-jwt" % "1.0.13"
      exclude("org.slf4j", "com.springsource.slf4j.org.apache.commons.logging")
      exclude("org.slf4j", "com.springsource.slf4j.log4j")
      exclude("org.slf4j", "com.springsource.slf4j.api")
  )
}
