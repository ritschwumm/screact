name			:= "screact"

organization	:= "de.djini"

version			:= "0.7.0"

scalaVersion	:= "2.9.2"

libraryDependencies	++= Seq(
	"de.djini"	%% "scutil"	% "0.8.0"	% "compile"
)

scalacOptions	++= Seq("-deprecation", "-unchecked")
