name			:= "screact"
organization	:= "de.djini"
version			:= "0.78.0"

scalaVersion	:= "2.11.6"
scalacOptions	++= Seq(
	"-deprecation",
	"-unchecked",
	"-language:implicitConversions",
	// "-language:existentials",
	// "-language:higherKinds",
	// "-language:reflectiveCalls",
	// "-language:dynamics",
	// "-language:postfixOps",
	// "-language:experimental.macros"
	"-feature",
	"-Ywarn-unused-import",
	"-Xfatal-warnings"
)

conflictManager	:= ConflictManager.strict
libraryDependencies	++= Seq(
	"de.djini"	%% "scutil-core"	% "0.70.0"	% "compile",
	"de.djini"	%% "scutil-swing"	% "0.70.0"	% "compile"
)
