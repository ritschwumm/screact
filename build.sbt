name			:= "screact"

organization	:= "de.djini"

version			:= "0.12.0"

scalaVersion	:= "2.9.2"

libraryDependencies	++= Seq(
	"de.djini"	%% "scutil"	% "0.11.0"	% "compile"
)

scalacOptions	++= Seq("-deprecation", "-unchecked")
