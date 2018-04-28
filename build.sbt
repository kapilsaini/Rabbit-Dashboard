name := "Rabbit-Dashboard"

organization := "com.autonomous"

version := "1.0"

scalaVersion := "2.11.7"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-optimize", "-Xlint")
classpathTypes += "maven-plugin"

libraryDependencies ++= Seq(
 "com.typesafe.akka" %% "akka-actor" % "2.4.6",
  "com.typesafe.akka" %% "akka-http-core" % "2.4.11",
  "com.typesafe.akka" %% "akka-stream" % "2.4.6",
  "com.typesafe.akka" %% "akka-slf4j" % "2.4.6",
  "ch.qos.logback"    %  "logback-classic" % "1.1.3",
  "org.scala-lang.modules"  %% "scala-swing" % "2.0.0"
 )

resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  "ImageJ Releases" at "http://maven.imagej.net/content/repositories/releases/",
  "Local Maven Repository" at "file:///" + Path.userHome.absolutePath + "/.m2/repository"
)
