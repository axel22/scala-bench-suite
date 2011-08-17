#!/bin/bash
exec > /dev/null
# Scala Benchmark Suite
# Created on August 16th 2011
# Created by ND P


function runCompile() {
	if [ ! -d $1 ] ; then
		mkdir $1
	fi
	echo "scalac -d $1 $2"
	echo Compiling...
	scalac -d $1 $2 | goto RUN_BENCHMARK
}

function runBenchmark() {
	echo "scala BenchmarkDriver.jar $1 $2 $3 $4 $5"
	echo "Benchmark running..."
	logfile=`date +%Y%m%d.%H%M`.log
	scala BenchmarkDriver.jar $1 $2 $3 $4 $5 > $logfile
}

function printUsage() {
	echo 'Usage: BenchmarkSuite -src <scala source file> -warmup <warm up> -runs <runs> [-multiplier <multiplier>] [-noncompile] [-classdi] <classdir>] [-help]'
	echo '	The benchmark is warmed up <warm up> times, then run <runs> times, forcing a garbage collection between runs.'
	echo '	The optional -multiplier causes the benchmark to be repeated <multiplier> times, each time for <runs> executions.'
	echo '	The optional -noncompile causes the benchmark not to be recompiled.'
	echo '	The optional -d causes the generated class files to be placed at <classdir>.'
	echo '	The optional -help prints this usage.'
}

multiplier=
classdir=
compile=

while [ $# -gt 0 ] ; do
	case $1 in 
		"-help")
			printUsage
			;;
		"-src")
			classname=%~n2
			srcpath=%~p2
			srcdrive=%~d2
			src=$2
			shift 2
			;;
		"-warmup")
			warmup=$2
			shift 2
			;;
		"-runs")
			runs=$2
			shift 2
			;;
		"-multiplier")
			multiplier=$2
			shift 2
			;;
		"-classdir")
			classdir=$2
			shift 2
			;;
		"-noncompile")
			compile=no
			shift
			;;
	esac
done

if [ -z $src ] ; then
	printUsage
	exit 1
elif [ -z $warmup ] ; then
	printUsage
	exit 2
elif [ -z $runs ] ; then
	printUsage
	exit 3
elif [ -z $multiplier ] ; then
	multiplier=1
	if [ -z $classdir ] ; then
		classdir=$srcdrive$srcpath\\build
	fi
	if [ -n $compile ] ; then
		# echo [Arguments] $classname $classdir $warmup $runs $multiplier $compile
		runBenchmark $classname $classdir $warmup $runs $multiplier
	else
		# echo [Arguments] $classname $classdir $warmup $runs $multiplier $compile
		runCompile $classdir $src
		runBenchmark $classname $classdir $warmup $runs $multiplier
	fi
	echo "End of benchmarking."
	exit 0
fi
