name			:= "screact"

organization	:= "de.djini"

version			:= "0.38.0"

scalaVersion	:= "2.10.3"

libraryDependencies	++= Seq(
	"de.djini"	%% "scutil"	% "0.32.0"	% "compile"
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
