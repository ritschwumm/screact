name			:= "screact"

organization	:= "de.djini"

version			:= "0.63.0"

scalaVersion	:= "2.11.4"

scalacOptions	++= Seq(
	"-deprecation",
	"-unchecked",
	"-language:implicitConversions",
	// "-language:existentials",
	// "-language:higherKinds",
	// "-language:reflectiveCalls",
	// "-language:dynamics",
	"-language:postfixOps",
	// "-language:experimental.macros"
	"-feature"
)

conflictManager	:= ConflictManager.strict

libraryDependencies	++= Seq(
	"de.djini"	%% "scutil-core"	% "0.57.0"	% "compile",
	"de.djini"	%%	"scutil-swing"	% "0.57.0"	% "compile"
)
