
package scala.tools.sbs

import scala.tools.sbs.benchmark.Benchmark
import scala.tools.sbs.pinpoint.CodeInstrumentor.InstrumentingExpression
import scala.tools.sbs.pinpoint.CodeInstrumentor.MethodCallExpression
import scala.tools.sbs.util.Constant

class BenchmarkException(message: String) extends Exception(message)

case class NotSupportedBenchmarkMode(mode: BenchmarkMode)
  extends BenchmarkException("Mode " + mode.description + " is currently not supported")

case class NoPreviousException(benchmark: Benchmark, mode: BenchmarkMode, result: RunSuccess)
  extends BenchmarkException("No previous run result to detect regression")

case class BenchmarkProcessException(benchmark: Benchmark, mode: BenchmarkMode, exitValue: Int)
  extends BenchmarkException("Error in benchmark process exit value: " + exitValue)

case class WrongRunnerException(runner: Runner, mode: BenchmarkMode)
  extends BenchmarkException("Wrong type of runner: " + runner.getClass.getName + " in mode: " + mode.description)

case class MalformedXMLException(runner: Runner, mode: BenchmarkMode, xml: scala.xml.Elem)
  extends BenchmarkException("Malformed xml: " + xml.toString + " from " + runner.getClass.getName + mode.description)

case class AlgorithmFlowException(runner: Class[_]) extends BenchmarkException("Unpredicted flow in: " + runner.getName)

case class MismatchBenchmarkImplementationException(benchmark: Benchmark, runner: Runner) extends BenchmarkException(
  "An implementation of " + benchmark.getClass.getName +
    " is provided to a runner of type " + runner.getClass.getName)