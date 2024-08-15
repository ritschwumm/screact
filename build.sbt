Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / versionScheme := Some("early-semver")

name			:= "screact"
organization	:= "de.djini"
version			:= "0.263.0"

scalaVersion	:= "3.5.0"
scalacOptions	++= Seq(
	"-feature",
	"-deprecation",
	"-unchecked",
	"-source:future",
	"-Wunused:all",
	"-Xfatal-warnings",
	"-Xkind-projector:underscores",
)

libraryDependencies	++= Seq(
	"de.djini"	%% "scutil-jdk"	% "0.250.0"	% "compile",
	"de.djini"	%% "scutil-gui"	% "0.250.0"	% "compile"
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
	Wart.JavaConversions,
	Wart.Option2Iterable,
	Wart.JavaSerializable,
	//Wart.Any,
	Wart.AnyVal,
	//Wart.Nothing,
	Wart.ArrayEquals,
	//Wart.ImplicitParameter,
	Wart.ExplicitImplicitTypes,
	Wart.LeakingSealed,
	Wart.DefaultArguments,
	// TODO should be enabled, but produces unfixable errors in signal/cell/emitter extensions
	//Wart.Overloading,
	//Wart.PublicInference,
	//Wart.TraversableOps
)
