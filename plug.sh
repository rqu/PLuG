#!/bin/sh

if [ "$# $2 $4" != "5 --in --out" ]
then
	echo "Usage: $0 <INSTR> --in <INPUT> --out <OUTPUT>" >&2
	echo >&2
	echo "    INSTR:  Colon (':') separated jarfiles with" >&2
	echo "            DiSL instrumentations" >&2
	echo "    INPUT:  Jarfile containing program which" >&2
	echo "            will be instrumented" >&2
	echo "    OUTPUT: Where the instrumented program" >&2
	echo "            will be created as jarfile" >&2
	echo >&2
	echo "Example: $0 lib/instr.jar --in build/app.jar --out dist/app_instrumented.jar" >&2
	exit 2
fi

instr="$1"
input="$3"
output="$5"

plug_home="`dirname $0`"
plug_jar="$plug_home/dist/PLuG.jar"

exec java -cp "$instr:$plug_jar" ch.usi.dag.disl.plug.PLuG "$input" > "$output"

# exec should not return

echo "Could not launch Java: exit status $?" >&2
exit 1
