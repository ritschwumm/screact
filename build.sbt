Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / versionScheme := Some("early-semver")

name			:= "screact"
organization	:= "de.djini"
version			:= "0.280.0"

scalaVersion	:= "3.8.0"
scalacOptions	++= Seq(
	"-preview",
	"-feature",
	"-deprecation",
	"-unchecked",
	"-source:future",
	"-language:experimental.into",
	"-Wunused:all",
	"-Wsafe-init",
	"-Wshadow:all",
	"-Wtostring-interpolated",
	"-Werror",
	"-Xkind-projector:underscores",
)

libraryDependencies	++= Seq(
	"de.djini"	%% "scutil-jdk"	% "0.267.0"	% "compile",
	"de.djini"	%% "scutil-gui"	% "0.267.0"	% "compile"
)

wartremoverErrors ++= Seq(
	Wart.AsInstanceOf,
	Wart.IsInstanceOf,
	Wart.StringPlusAny,
	//Wart.ToString,
	Wart.EitherProjectionPartial,
	Wart.OptionPartial,
	Wart.TryPartial,
	Wart.Enumeration,
	Wart.FinalCaseClass,
	Wart.Option2Iterable,
	//Wart.Any,
	Wart.AnyVal,
	//Wart.Nothing,
	Wart.ArrayEquals,
	Wart.LeakingSealed,
	Wart.DefaultArguments,
	// TODO should be enabled, but produces unfixable errors in signal/cell/emitter extensions
	//Wart.Overloading,
	//Wart.IterableOps
)
