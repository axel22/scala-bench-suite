/*
 * BenchmarkException
 * 
 * Version
 * 
 * Created on November 6th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs

import scala.tools.sbs.benchmark.Benchmark
import scala.tools.sbs.util.Constant
import scala.tools.nsc.io.Directory
import scala.tools.nsc.io.File

class BenchmarkException(message: String) extends Exception(message)

case class NotSupportedBenchmarkMode(mode: BenchmarkMode)
  extends BenchmarkException("Mode " + mode.description + " is currently not supported")

case class WrongRunnerException(runner: Runner, mode: BenchmarkMode)
  extends BenchmarkException("Wrong type of runner: " + runner.getClass.getName + " in mode: " + mode.description)

case class AlgorithmFlowException(runner: Class[_]) extends BenchmarkException("Unpredicted flow in: " + runner.getName)

case class MismatchBenchmarkImplementationException(benchmark: Benchmark, runner: Runner) extends BenchmarkException(
  "An implementation of " + benchmark.getClass.getName +
    " is provided to a runner of type " + runner.getClass.getName)

case class BackupFailureException(file: File, target: Directory)
  extends Exception("Moving " + file + " to " + target + " failed")
