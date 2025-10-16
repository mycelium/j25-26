name := "funsets"
version := "1.0.0"
scalaVersion := "2.12.20"

addCompilerPlugin("org.scalameta" % "semanticdb-scalac" % "4.8.14" cross CrossVersion.full)
scalacOptions ++= Seq(
  "-Yrangepos",
  "-deprecation", 
  "-unchecked",
  "-feature",
  "-Xfatal-warnings"  
)

// Уберите эти опции, если проблема сохранится
// scalacOptions += "-optimise" 
// scalacOptions += "-Yinline-warnings"

fork := true
javaOptions += "-Xmx2G"
parallelExecution in Test := false

mainClass in Compile := Some("funsets.Main")