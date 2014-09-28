name			:= "screact"

organization	:= "de.djini"

version			:= "0.57.1"

scalaVersion	:= "2.11.2"

libraryDependencies	++= Seq(
	"de.djini"	%% "scutil-core"	% "0.51.1"	% "compile",
	"de.djini"	%%	"scutil-swing"	% "0.51.1"	% "compile"
)

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
