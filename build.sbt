val scala3Version = "3.3.0"

ThisBuild / tlBaseVersion := "1.0"

ThisBuild / organization     := "com.github.tarao"
ThisBuild / organizationName := "my project authors"
ThisBuild / startYear        := Some(2023)
ThisBuild / licenses         := Seq(License.MIT)
ThisBuild / developers := List(
  tlGitHubDev("windymelt", "windymelt"),
)

ThisBuild / scalaVersion       := scala3Version
ThisBuild / crossScalaVersions := Seq(scala3Version)

lazy val compileSettings = Def.settings(
  // 警告をエラーにする
  // tlFatalWarnings := true,

  // デフォルトで設定されるがうまくいかないものを外す
  scalacOptions --= Seq(
    // Scala 3.0.1以降だとうまく動かない
    // https://github.com/lampepfl/dotty/issues/14952
    "-Ykind-projector:underscores",
  ),
  Test / scalacOptions --= Seq(
    // テストだとちょっと厳しすぎる
    "-Wunused:locals",
  ),
  Compile / console / scalacOptions --= Seq(
    // コンソールで import した瞬間はまだ使ってないから当然許したい
    "-Wunused:imports",
  ),
)

lazy val root = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .withoutSuffixFor(JVMPlatform)
  .in(file("."))
  .settings(compileSettings)
  .settings(
    name := "prompt-exercise-scala",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

    libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test
  )

ThisBuild / githubWorkflowJavaVersions := Seq(
  JavaSpec.temurin("8"),
  JavaSpec.temurin("11"),
  JavaSpec.temurin("17"),
)

ThisBuild / githubWorkflowTargetBranches := Seq("main")
