
name := "pgPatch"

scalaVersion := "2.11.6"

version := "1.0"

libraryDependencies ++= Seq(
	"com.github.mauricio" %% "postgresql-async" % "0.2.15",
	"joda-time" % "joda-time" % "2.3",
	"org.scalatest" % "scalatest_2.11" % "2.2.1" % "test"
)

