name := "scala benchmarking suite"
 
version := "0.1"

scalaVersion := "2.9.1"

libraryDependencies <+= scalaVersion( "org.scala-lang" % "scala-compiler" % _ )

libraryDependencies += "org.apache.commons" % "commons-math" % "2.2"

libraryDependencies += "org.scalatest" % "scalatest_2.9.1" % "1.6.1"
