name := "funsets"

//scalaVersion := "2.12.7"
scalaVersion := "3.7.3"
scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
 // "-optimise",
 // "-Yinline-warnings"
)

fork := true

javaOptions += "-Xmx2G"

parallelExecution in Test := false
