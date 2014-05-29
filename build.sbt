name			:= "screact"

organization	:= "de.djini"

version			:= "0.50.0"

scalaVersion	:= "2.11.0"

libraryDependencies	++= Seq(
	"de.djini"	%% "scutil-core"	% "0.44.0"	% "compile",
	"de.djini"	%%	"scutil-swing"	% "0.44.0"	% "compile"
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
