@echo off
rem Scala Benchmark Suite
rem Copyright 2011 HCMUT - EPFL
rem Created on May 25th 2011
rem By ND P


goto PARSE_ARGUMENTS

:PARSE_ARGUMENTS
set multiplier=
set classdir=
set compile=
:PARSE_ARGUMENTS_LOOP
if (%1) == () goto FINALIZE_ARGUMENTS
if (%1) == (-help) goto PRINT_USAGE
if (%1) == (-src) (
	set classname=%~n2
	set srcpath=%~p2
	set srcdrive=%~d2
	set src=%2
	shift
	shift
	goto PARSE_ARGUMENTS_LOOP
)
if (%1) == (-warmup) (
	set warmup=%2
	shift
	shift
	goto PARSE_ARGUMENTS_LOOP
)
if (%1) == (-runs) (
	set runs=%2
	shift
	shift
	goto PARSE_ARGUMENTS_LOOP
)
if (%1) == (-multiplier) (
	set multiplier=%2
	shift
	shift
	goto PARSE_ARGUMENTS_LOOP
)
if (%1) == (-classdir) (
	set classdir=%2
	shift
	shift
	goto PARSE_ARGUMENTS_LOOP
)
if (%1) == (-noncompile) (
	set compile=no
	shift
	goto PARSE_ARGUMENTS_LOOP
)

goto PRINT_USAGE

:FINALIZE_ARGUMENTS
if (%src%) == () goto PRINT_USAGE
if (%warmup%) == () goto PRINT_USAGE
if (%runs%) == () goto PRINT_USAGE
if (%multiplier%) == () set multiplier=1
if (%classdir%) == () set classdir=%srcdrive%%srcpath%build
if not (%compile%)==() (
	rem echo [Arguments] %classname% %classdir% %warmup% %runs% %multiplier% %compile%
	goto RUN_BENCHMARK
)
rem echo [Arguments] %classname% %classdir% %warmup% %runs% %multiplier% %compile%
goto COMPILE

:COMPILE
if not exist %classdir% (
	mkdir %classdir%
)
echo scalac -d %classdir% %src%
echo Compiling...
scalac -d %classdir% %src% | goto RUN_BENCHMARK

:RUN_BENCHMARK
echo scala BenchmarkDriver.jar %classname% %classdir% %warmup% %runs% %multiplier%
echo Benchmark running...
scala BenchmarkDriver.jar %classname% %classdir% %warmup% %runs% %multiplier% > out.out

:END
echo End of benchmarking.
goto :EOF

:PRINT_USAGE
echo Usage: BenchmarkSuite -src ^<scala source file^> -warmup ^<warm up^> -runs ^<runs^> [-multiplier ^<multiplier^>] [-noncompile] [-classdir ^<classdir^>] [-help]
echo 	The benchmark is warmed up ^<warm up^> times, then run ^<runs^> times, forcing a garbage collection between runs.
echo 	The optional -multiplier causes the benchmark to be repeated ^<multiplier^> times, each time for ^<runs^> executions.
echo 	The optional -noncompile causes the benchmark not to be recompiled.
echo 	The optional -d causes the generated class files to be placed at ^<classdir^>
echo 	The optional -help prints this usage.

