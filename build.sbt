scalaVersion := "2.12.1"

name := "highloadcup"
organization := "com.github.nyukhalov"
version := "1.0"

resolvers ++= Seq(
  "Typesafe repository"       at "http://repo.typesafe.com/typesafe/releases/",
  "Sonatype OSS Snapshots"    at "https://oss.sonatype.org/content/repositories/snapshots"
)
resolvers += Resolver.bintrayRepo("hseeberger", "maven")

libraryDependencies += "org.typelevel" %% "cats" % "0.9.0"
libraryDependencies ++= Seq(
  "com.typesafe.akka"           %% "akka-actor"             % "2.5.3",
  "com.typesafe.akka"           %% "akka-stream"            % "2.5.3",
  "com.typesafe.akka"           %% "akka-http"              % "10.0.9",
  "com.typesafe"                %  "config"                 % "1.3.1",
  "ch.qos.logback"              %  "logback-classic"        % "1.2.3",
  "com.typesafe.scala-logging"  %% "scala-logging"          % "3.7.2",
  "com.github.pathikrit" %% "better-files" % "3.0.0",
  "joda-time" % "joda-time" % "2.9.9",
  "de.heikoseeberger" %% "akka-http-circe" % "1.18.0",
  "io.circe"                %% "circe-core"             % "0.8.0",
  "io.circe"                %% "circe-generic"          % "0.8.0",
  "io.circe"                %% "circe-parser"           % "0.8.0",

  "com.typesafe.akka" %% "akka-testkit" % "2.5.3" % Test,
  "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.3" % Test,
  "com.typesafe.akka" %% "akka-http-testkit" % "10.0.9" % Test,
  "org.specs2" %% "specs2-core" % "3.9.1" % "test",
  "org.specs2" %% "specs2-mock" % "3.9.1" % "test"
)


exportJars := true
mainClass in Compile := Option("com.github.nyukhalov.highloadcup.Boot")

enablePlugins(JavaAppPackaging, sbtdocker.DockerPlugin)

dockerfile in docker := {
  val appDir: File = stage.value
  val targetDir = "/app"

  new Dockerfile {
    from("frolvlad/alpine-oraclejdk8")
    copy(appDir, targetDir)

    runRaw(
      """apk update && \
         apk upgrade && \
         apk add bash
      """)

    expose(80)
    entryPointRaw(s"$targetDir/bin/${executableScriptName.value} -J-Xms3584m -J-Xmx3584m -J-server -J-XX:+DisableExplicitGC")

//    env("SPARK_BUILD", s"spark-${sparkVersion}-bin-hadoop2.4")
//    runRaw("""wget http://d3kbcqa49mib13.cloudfront.net/$SPARK_BUILD.tgz && \
//              tar -xvf $SPARK_BUILD.tgz && \
//              mv $SPARK_BUILD /spark && \
//              rm $SPARK_BUILD.tgz
//           """)
//    run("mkdir", "-p", "/database")
  }
}

// can slow down building image
//buildOptions in docker := BuildOptions(
//  cache = false,
//  removeIntermediateContainers = BuildOptions.Remove.Always
//)
