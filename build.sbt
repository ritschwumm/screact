name			:= "screact"
organization	:= "de.djini"
version			:= "0.172.0"

scalaVersion	:= "2.12.10"
scalacOptions	++= Seq(
	"-deprecation",
	"-unchecked",
	"-language:implicitConversions",
	// "-language:existentials",
	// "-language:higherKinds",
	// "-language:reflectiveCalls",
	// "-language:dynamics",
	// "-language:experimental.macros"
	"-feature",
	"-Ywarn-unused-import",
	"-Xfatal-warnings",
	"-Xlint"
)

conflictManager		:= ConflictManager.strict withOrganization "^(?!(org\\.scala-lang|org\\.scala-js)(\\..*)?)$"
libraryDependencies	++= Seq(
	"de.djini"	%% "scutil-core"	% "0.160.0"	% "compile",
	"de.djini"	%% "scutil-swing"	% "0.160.0"	% "compile"
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
	Wart.Overloading,
	//Wart.PublicInference,
	Wart.TraversableOps
)
