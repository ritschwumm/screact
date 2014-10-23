name			:= "screact"

organization	:= "de.djini"

version			:= "0.60.0"

scalaVersion	:= "2.11.2"

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
	"de.djini"	%% "scutil-core"	% "0.54.0"	% "compile",
	"de.djini"	%%	"scutil-swing"	% "0.54.0"	% "compile"
)
