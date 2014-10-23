name			:= "screact"

organization	:= "de.djini"

version			:= "0.59.0"

scalaVersion	:= "2.11.3"

libraryDependencies	++= Seq(
	"de.djini"	%% "scutil-core"	% "0.53.0"	% "compile",
	"de.djini"	%%	"scutil-swing"	% "0.53.0"	% "compile"
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
