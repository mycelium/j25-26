name := "recfun"

scalaVersion := "2.13.12"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.17" % Test

Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-oDF")

