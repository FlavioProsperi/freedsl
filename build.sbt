
def modules = Seq(dsl, random, log)

lazy val root =
  Project(id = "all", base = file(".")).settings(settings: _*).
    aggregate(modules.map(_.project): _*).dependsOn(modules.map(p => p: ClasspathDep[ProjectReference]): _*)

def settings = Seq (
  scalaVersion := "2.11.8",
  scalaOrganization := "org.typelevel",
  organization := "fr.iscpif.freedsl",
  crossScalaVersions := Seq("2.11.8"),
  resolvers += Resolver.bintrayRepo("projectseptemberinc", "maven"),
  libraryDependencies += "com.projectseptember" %% "freek" % "0.6.2",
  libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  addCompilerPlugin("com.milessabin" % "si2712fix-plugin" % "1.2.0" cross CrossVersion.full),
  addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
  addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.2"),
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
    else Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  },
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },
  licenses := Seq("Affero GPLv3" -> url("http://www.gnu.org/licenses/")),
  homepage := Some(url("https://github.com/ISCPIF/freedsl")),
  scmInfo := Some(ScmInfo(url("https://github.com/ISCPIF/freedsl.git"), "scm:git:git@github.com:ISCPIF/freedsl.git")),
  pomExtra := (
    <developers>
      <developer>
        <id>romainreuillon</id>
        <name>Romain Reuillon</name>
      </developer>
    </developers>
  )
)

lazy val random = Project(id = "random", base = file("random")).settings(settings: _*) dependsOn(dsl)
lazy val log = Project(id = "log", base = file("log")).settings(settings: _*) dependsOn(dsl)
lazy val dsl = Project(id = "dsl", base = file("dsl")).settings(settings: _*)



