showCurrentGitBranch

git.useGitDescribe := true

lazy val commonSettings = Seq(
  organization := "org.hathitrust.htrc",
  organizationName := "HathiTrust Research Center",
  organizationHomepage := Some(url("https://www.hathitrust.org/htrc")),
  scalaVersion := "2.13.8",
  scalacOptions ++= Seq(
    "-feature",
    "-deprecation",
    "-language:postfixOps",
    "-language:implicitConversions"
  ),
  resolvers ++= Seq(
    Resolver.mavenLocal,
    "HTRC Nexus Repository" at "https://nexus.htrc.illinois.edu/repository/maven-public"
  ),
  externalResolvers := Resolver.combineDefaultResolvers(resolvers.value.toVector, mavenCentral = false),
  Compile / packageBin / packageOptions += Package.ManifestAttributes(
    ("Git-Sha", git.gitHeadCommit.value.getOrElse("N/A")),
    ("Git-Branch", git.gitCurrentBranch.value),
    ("Git-Version", git.gitDescribedVersion.value.getOrElse("N/A")),
    ("Git-Dirty", git.gitUncommittedChanges.value.toString),
    ("Build-Date", new java.util.Date().toString)
  )
)

lazy val wartRemoverSettings = Seq(
  Compile / compile / wartremoverErrors ++= Warts.unsafe.diff(Seq(
    Wart.DefaultArguments,
    Wart.NonUnitStatements,
    Wart.StringPlusAny
  ))
)

lazy val publishSettings = Seq(
  publishTo := {
    val nexus = "https://nexus.htrc.illinois.edu/"
    if (isSnapshot.value)
      Some("HTRC Snapshots Repository" at nexus + "repository/snapshots")
    else
      Some("HTRC Releases Repository"  at nexus + "repository/releases")
  },
  // force to run 'test' before 'package' and 'publish' tasks
  publish := (publish dependsOn Test / test).value,
  Keys.`package` := (Compile / Keys.`package` dependsOn Test / test).value
)

lazy val ammoniteSettings = Seq(
  libraryDependencies +=
    {
      val version = scalaBinaryVersion.value match {
        case "2.10" => "1.0.3"
        case _ ⇒  "2.5.3"
      }
      "com.lihaoyi" % "ammonite" % version % Test cross CrossVersion.full
    },
  Test / sourceGenerators += Def.task {
    val file = (Test / sourceManaged).value / "amm.scala"
    IO.write(file, """object amm extends App { ammonite.Main.main(args) }""")
    Seq(file)
  }.taskValue,
  connectInput := true,
  outputStrategy := Some(StdoutOutput)
)

lazy val `running-headers` = (project in file("."))
  .enablePlugins(GitVersioning, GitBranchPrompt)
  .settings(commonSettings)
  .settings(wartRemoverSettings)
  .settings(publishSettings)
  .settings(ammoniteSettings)
  .settings(
    name := "running-headers",
    description := "Library that performs header/body/footer identification " +
      "over a set of pages in a volume",
    licenses += "Apache2" -> url("http://www.apache.org/licenses/LICENSE-2.0"),
    libraryDependencies ++= Seq(
      "org.hathitrust.htrc"           %% "scala-utils"              % "2.13",
      "org.scala-lang.modules"        %% "scala-collection-compat"  % "2.7.0",
      "org.scalacheck"                %% "scalacheck"               % "1.16.0"    % Test,
      "org.scalatest"                 %% "scalatest"                % "3.2.12"    % Test,
      "org.scalatestplus"             %% "scalacheck-1-15"          % "3.2.11.0"  % Test
    ),
    ThisBuild / versionScheme := Some("semver-spec"),
    crossScalaVersions := Seq("2.13.8", "2.12.15")
  )
