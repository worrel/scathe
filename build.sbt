val Http4sVersion = "0.18.0-M7"
val Specs2Version = "4.0.2"
val LogbackVersion = "1.2.3"
val CirceVersion = "0.9.0"
val OpRabbitVersion = "2.0.0"
val TSecVersion = "0.0.1-M7"
val MsgPack4sVersion = "0.6.0"
val ScalaLoggingVersion = "3.7.2"
val PostgresDriverVersion = "42.2.1.jre7"
val ScalikeJDBCVersion = "3.2.0"
val CommonsDBCPVersion = "1.4"

val ScalaTestVersion = "3.0.4"

resolvers ++= Seq(Resolver.jcenterRepo,Resolver.bintrayRepo("jmcardon", "tsec"))

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

lazy val root = (project in file("."))
  .settings(
    organization := "com.worrel",
    name := "scathe",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.12.4",
    libraryDependencies ++= Seq(
      "org.http4s"                  %% "http4s-blaze-server"  % Http4sVersion,
      "org.http4s"                  %% "http4s-circe"         % Http4sVersion,
      "org.http4s"                  %% "http4s-dsl"           % Http4sVersion,

      "io.circe"                    %% "circe-generic"        % CirceVersion,
      "io.circe"                    %% "circe-literal"        % CirceVersion,

      "org.velvia"                  %% "msgpack4s"            % MsgPack4sVersion,

      "com.spingo"                  %% "op-rabbit-core"       % OpRabbitVersion,

      "org.scalikejdbc"             %% "scalikejdbc"          % ScalikeJDBCVersion,
      "org.scalikejdbc"             %% "scalikejdbc-config"   % ScalikeJDBCVersion,
      "org.postgresql"              % "postgresql"            % PostgresDriverVersion,
      "commons-dbcp"                % "commons-dbcp"          % CommonsDBCPVersion,

      "io.github.jmcardon"          %% "tsec-http4s"          % TSecVersion,

      "org.scalatest"               %% "scalatest"            % ScalaTestVersion % "test",

      "com.typesafe.scala-logging"  %% "scala-logging"        % ScalaLoggingVersion,
      "ch.qos.logback"              %  "logback-classic"      % LogbackVersion
    )
  )

assemblyJarName in assembly := "scathe-all.jar"