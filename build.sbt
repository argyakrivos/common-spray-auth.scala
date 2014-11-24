lazy val root = (project in file(".")).
  settings(
    name := "common-spray-auth",
    organization := "com.blinkbox.books",
    version := scala.util.Try(scala.io.Source.fromFile("VERSION").mkString.trim).getOrElse("0.0.0"),
    scalaVersion := "2.11.4",
    crossScalaVersions := Seq("2.11.4"),
    scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-encoding", "utf8", "-target:jvm-1.7", "-Xfatal-warnings", "-Xfuture"),
    resolvers += "spray" at "http://repo.spray.io/",
    libraryDependencies ++= {
      val akkaV = "2.3.7"
      val sprayV = "1.3.2"
      val json4sV = "3.2.11"
      Seq(
        "io.spray"            %%  "spray-can"           % sprayV,
        "io.spray"            %%  "spray-routing"       % sprayV,
        "io.spray"            %%  "spray-client"        % sprayV,
        "com.typesafe.akka"   %%  "akka-actor"          % akkaV,
        "com.blinkbox.books"  %%  "common-json"         % "0.2.4",
        "com.blinkbox.books"  %%  "common-scala-test"   % "0.3.0"   % Test,
        "io.spray"            %%  "spray-testkit"       % sprayV    % Test,
        "com.typesafe.akka"   %%  "akka-testkit"        % akkaV     % Test,
        "com.blinkboxbooks.platform.security" % "blinkbox-security-jwt" % "1.0.13"
          exclude("org.slf4j", "com.springsource.slf4j.org.apache.commons.logging")
          exclude("org.slf4j", "com.springsource.slf4j.log4j")
          exclude("org.slf4j", "com.springsource.slf4j.api")
      )
    }
  )
