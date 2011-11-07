/*
 * PerformanceException
 * 
 * Version
 * 
 * Created on November 6th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package performance

class PerformanceException(message: String) extends BenchmarkException(message)

case class NoPreviousException(benchmark: PerformanceBenchmark, mode: BenchmarkMode, result: RunSuccess)
  extends PerformanceException("No previous run result to detect regression")

case class BenchmarkProcessException(benchmark: PerformanceBenchmark, mode: BenchmarkMode, exitValue: Int)
  extends PerformanceException("Error in benchmark process exit value: " + exitValue)

case class MalformedXMLException(runner: Runner, mode: BenchmarkMode, xml: scala.xml.Elem)
  extends BenchmarkException("Malformed xml: " + xml.toString + " from " + runner.getClass.getName + mode.description)
