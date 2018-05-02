lazy val root = project
  .in(file("."))
  .settings(scalaSettings)
  .settings(publishSettings)
  .settings(dependencySettings)
  .enablePlugins(GhpagesPlugin, TutPlugin)

lazy val scalaSettings = Seq(
  scalaVersion := "2.12.6",
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-feature",
    "-language:existentials",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-language:postfixOps",
    "-unchecked",
    "-Xfuture",
    "-Yno-adapted-args",
    "-Ypartial-unification",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard"
  )
)

lazy val publishSettings = Seq(
  ghpagesNoJekyll := true,
  makeSite := (makeSite dependsOn tut).value,
  siteSourceDirectory := tutTargetDirectory.value,
  ghpagesPushSite := (ghpagesPushSite dependsOn makeSite).value,
  git.remoteRepo := "git@github.com:vlovgr/actors-cats-effect-fs2.git"
)

lazy val dependencySettings = Seq(
  libraryDependencies ++= Seq(
    "co.fs2" %% "fs2-core" % "0.10.4"
  )
)
