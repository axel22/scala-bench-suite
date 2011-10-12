/*
 * ScalaInvoker
 * 
 * Version
 * 
 * Created October 2nd, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package common

import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import scala.sys.process.Process
import scala.sys.process.ProcessIO
import scala.tools.sbs.benchmark.Benchmark
import scala.tools.sbs.io.Log
import scala.tools.sbs.io.UI
import scala.tools.sbs.util.Constant.COLON
import org.apache.commons.math.MathException
import scala.tools.nsc.util.ClassPath

/** An implement of {@link JVMInvoker}.
 */
class ScalaInvoker(log: Log, config: Config) extends JVMInvoker {

  /** `java` or `./jre/bin/java`, etc...
   */
  private val java = Seq(config.javacmd)

  /** `-cp <scala-library.jar, scala-compiler.jar> -Dscala=<scala-home> scala.tools.nsc.MainGenericRunner`
   */
  private val asScala = Seq("-cp", config.scalaLib, config.javaProp, "scala.tools.nsc.MainGenericRunner")

  /** `-cp <classpath from config; classpath from benchmark>`
   */
  private def asScalaClasspath(benchmark: Benchmark) =
    Seq("-cp", ClassPath.fromURLs(config.classpathURLs ++ benchmark.classpathURLs: _*))

  /** `-cp <classpath from config; classpath from benchmark> Runner`
   */
  private def asRunner(runner: Runner, benchmark: Benchmark) =
    asScalaClasspath(benchmark) ++ Seq(runner.getClass.getName replace ("$", ""))

  /** `-cp <classpath from config; classpath from benchmark> Benchmark`
   */
  private def asBenchmark(benchmark: Benchmark) = asScalaClasspath(benchmark) ++ Seq(benchmark.name)

  /** `-cp <scala-library.jar, scala-compiler.jar> -Dscala.home=<scala-home> scala.tools.nsc.MainGenericRunner
   *  -cp <classpath from config; classpath from benchmark> Benchmark benchmark.arguments`
   */
  def asJavaArgument(benchmark: Benchmark) = asScala ++ asBenchmark(benchmark) ++ benchmark.arguments

  /** `-cp <scala-library.jar, scala-compiler.jar> -Dscala.home=<scala-home> scala.tools.nsc.MainGenericRunner
   *  -cp <classpath from config; classpath from benchmark> Runner benchmark.toXML config.args`
   */
  def asJavaArgument(runner: Runner, benchmark: Benchmark) =
    asScala ++ asRunner(runner, benchmark) ++ Seq(benchmark.toXML.toString) ++ config.args

  def command(runner: Runner, benchmark: Benchmark) = java ++ asJavaArgument(runner, benchmark)

  def command(benchmark: Benchmark) = java ++ asScala ++ asJavaArgument(benchmark)

  def invoke(command: Seq[String]): (String, ArrayBuffer[String]) = {
    var result = ""
    var error = ArrayBuffer[String]()
    val processBuilder = Process(command)

    log.debug(command mkString " ")

    val processIO = new ProcessIO(
      _ => (),
      stdout => Source.fromInputStream(stdout).getLines.foreach(line =>
        if (line startsWith "<") result += line else UI(line)),
      stderr => Source.fromInputStream(stderr).getLines.foreach(error += _))

    val process = processBuilder.run(processIO)
    val success = process.exitValue

    log.debug("Sub-process exit value: " + success)

    (result, error)
  }

}
