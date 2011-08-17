#!/bin/bash
exec > /dev/null
# Scala Benchmark Suite
# Created on August 16th 2011
# Created by ND P


# Creates output class folder if necessary and invokes the default scala compiler to compile benchmark source file.
# @param $1	The directory for bytecode class files.
# @param $2	The benchmark source file name.
function runCompile() {
	[ -d $1 ] || mkdir $1
	echo "scalac -d $1 $2"
	echo 'Compiling...'
	scalac -d $1 $2
}

# Invokes the harnesses .jar file to do benchmarking.
# @param $1	The benchmark class name.
# @param $2	The benchmark class directory.
# @param $3	The iterator of warm up phase.
# @param $4	The running iterator.
# @param $5	The multiplier iterator.
function runBenchmark() {
	echo "scala BenchmarkDriver.jar $1 $2 $3 $4 $5"
	echo "Benchmark running..."
	logfile=`date +%Y%m%d.%H%M`.log
	scala BenchmarkDriver.jar $1 $2 $3 $4 $5 > $logfile
}

# Prints out the suite's usage.
function printUsage() {
	echo 'Usage: BenchmarkSuite -src <scala source file> -warmup <warm up> -runs <runs> [-multiplier <multiplier>] [-noncompile] [-classdi] <classdir>] [-help]'
	echo '	The benchmark is warmed up <warm up> times, then run <runs> times, forcing a garbage collection between runs.'
	echo '	The optional -multiplier causes the benchmark to be repeated <multiplier> times, each time for <runs> executions.'
	echo '	The optional -noncompile causes the benchmark not to be recompiled.'
	echo '	The optional -d causes the generated class files to be placed at <classdir>.'
	echo '	The optional -help prints this usage.'
}

# Initialize variables
multiplier=
classdir=
compile=

# Parse commandline arguments
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
		"-*")
			echo "$0: error - unrecognized option $1" 1>&2
			exit 1
			;;
		"*")
			printUsage
			;;
	esac
done

if [ -z $src ] ; then			# No benchmark source file specified
	printUsage
	exit 1
elif [ -z $warmup ] ; then		# No warm up iterator specified
	printUsage
	exit 2
elif [ -z $runs ] ; then		# No runs iterator specified
	printUsage
	exit 3
elif [ -z $multiplier ] ; then	# No multiplier iterator specified, assign by 1
	
	multiplier=1
	
	if [ -z $classdir ] ; then				# No directory for bytecode class files specified
		classdir=$srcdrive$srcpath\\build	# Set the default folder
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
