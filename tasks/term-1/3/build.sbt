name := "funsets"

scalaVersion := "2.13.12"

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked"
)

fork := true

javaOptions += "-Xmx2G"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.17" % Test

Test / parallelExecution := false

Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-oDF")
