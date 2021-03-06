val scala211 = "2.11.8"
val scala212 = "2.12.2"
val scalaVersionSelect = scala212
val akkaHttpDep = "com.typesafe.akka" %% "akka-http" % "10.0.9"

val scalatest = Def.setting(
  "org.scalatest" %%% "scalatest" % "3.2.0-SNAP7" % "test")

val react = Def.setting(
  "com.github.japgolly.scalajs-react" %%% "extra" % "1.1.0")

val cats = Def.setting(
  "org.typelevel" %%% "cats" % "0.9.0"
)

val autowireDeps = Def.setting(Seq(   
  "com.lihaoyi" %%% "autowire" % "0.2.6",
  "io.suzaku" %%% "boopickle" % "1.2.6"))

val mhtmlDeps = Def.setting(Seq(
  "in.nvilla" %%% "monadic-html" % "0.3.2",
  "in.nvilla" %%% "monadic-rx-cats" % "0.3.2"
))

val reactDeps = Seq(
  "org.webjars.bower" % "react" % "0.14.5" /
    "react-with-addons.js" minified "react-with-addons.min.js" commonJSName "React",
  "org.webjars.bower" % "react" % "0.14.5" /
    "react-dom.js" minified "react-dom.min.js" dependsOn "react-with-addons.js" commonJSName "ReactDOM")

val commonSettings = Seq(
  version := "1.0-SNAPSHOT",
  scalaVersion := scalaVersionSelect,
  scalacOptions := Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-feature",
    "-unchecked",
    "-Xfatal-warnings",
    //"-Xlint",
    "-Xlint:-unused,_", "-Ywarn-unused:imports",
    "-Yno-adapted-args",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard",
    "-Xfuture"),
  resolvers += Resolver.sonatypeRepo("public"),
  testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oDF"),
  // Shared /config between all projects
  unmanagedClasspath in Compile <+= (baseDirectory) map { bd => Attributed.blank(bd / ".." / "config") },
  unmanagedClasspath in Runtime <++= (unmanagedClasspath in Compile),
  unmanagedClasspath in Test <++= (unmanagedClasspath in Compile)
) ++ warnUnusedImport

val settingsJVM = commonSettings

val settingsJS = Seq(
  emitSourceMaps := true,   
  persistLauncher in Compile := true,   
  persistLauncher in Test := false,   
  skip in packageJSDependencies := false,   
  // Uniformises fastOptJS/fullOptJS output file name   
  artifactPath in (Compile, fastOptJS) :=   
    ((crossTarget in (Compile, fastOptJS)).value / ((moduleName in fastOptJS).value + "-opt.js"))   
) ++ commonSettings ++
  // Specifies where to store the outputs of Scala.js compilation.
  Seq(fullOptJS, fastOptJS, packageJSDependencies, packageScalaJSLauncher, packageMinifiedJSDependencies)   
    .map(task => crossTarget in (Compile, task) := file("web-static/static/target"))

lazy val model = crossProject   
  .crossType(CrossType.Pure)   
  .settings(commonSettings: _*)
  .settings(libraryDependencies += scalatest.value)   
lazy val modelJVM = model.jvm
lazy val modelJS = model.js

lazy val `web-client` = project
  .enablePlugins(ScalaJSPlugin)
  .settings(settingsJS: _*)
  .settings(libraryDependencies ++=
    Seq(react.value, scalatest.value) ++
      autowireDeps.value ++ mhtmlDeps.value
  )
  .settings(jsDependencies ++= reactDeps)
  .dependsOn(modelJS)

lazy val `web-server` = project
  .settings(settingsJVM: _*)
  // Include web-static in ressources for static serving
  .settings(unmanagedResourceDirectories in Compile += baseDirectory.value / ".." / "web-static")
  // Remove scala.js target from file watch to prevent compilation loop
  .settings(watchSources := watchSources.value.filterNot(_.getPath.contains("target")))
  // Add web-client sources to file watch
  .settings(watchSources <++= (watchSources in `web-client`))
  // Make compile depend on Scala.js fast compilation
  .settings(compile <<= (compile in Compile) dependsOn (fastOptJS in Compile in `web-client`))
  // Make re-start depend on Scala.js fast compilation
  .settings(reStart <<= reStart dependsOn (fastOptJS in Compile in `web-client`))
  // Make assembly depend on Scala.js full optimization
  .settings(assembly <<= assembly dependsOn (fullOptJS in Compile in `web-client`))
  .settings(libraryDependencies ++= Seq(akkaHttpDep, scalatest.value) ++ autowireDeps.value)
  .dependsOn(modelJVM)

lazy val warnUnusedImport = Seq(
  scalacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 10)) => Seq()
      case _ => Seq("-Ywarn-unused-import")
    }
  },
  scalacOptions in (Compile, console) ~= {_.filterNot("-Ywarn-unused-import" == _)},
  scalacOptions in (Test, console) <<= (scalacOptions in (Compile, console)))
