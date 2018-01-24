name := "users"
version := "1.0.0"

scalaVersion := "2.12.4"
scalacOptions ++= Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-Ypartial-unification"
)

resolvers +=
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies ++= Seq(
  "com.softwaremill.quicklens" %% "quicklens"            % "1.4.11",
  "org.typelevel"              %% "cats-core"            % "1.0.1",
  "com.typesafe.akka"          %% "akka-http"            % "10.1.0-RC1",
  "com.typesafe.akka"          %% "akka-stream"          % "2.5.8",
  "io.circe"                   %% "circe-core"           % "0.9.0",
  "io.circe"                   %% "circe-generic"        % "0.9.0",
  "io.circe"                   %% "circe-generic-extras" % "0.9.0",
  "io.circe"                   %% "circe-parser"         % "0.9.0",
  "org.scalatest"              %% "scalatest"            % "3.0.4"      % "test",
  "com.typesafe.akka"          %% "akka-http-testkit"    % "10.1.0-RC1",
  compilerPlugin("org.spire-math" %% "kind-projector" % "0.9.4")
)
