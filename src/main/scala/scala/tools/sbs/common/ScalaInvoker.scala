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
import scala.tools.sbs.Config
import scala.tools.sbs.Runner

import org.apache.commons.math.MathException

/** An implement of {@link JVMInvoker}.
 */
class ScalaInvoker(log: Log, config: Config) extends JVMInvoker {

  private val commandInit = Seq[String](
    config.javacmd,
    "-cp",
    config.scalaLib,
    config.javaProp,
    "scala.tools.nsc.MainGenericRunner",
    "-classpath")

  def invoke(runner: Runner, benchmark: Benchmark): (String, ArrayBuffer[String]) = {
    var result = ""
    var error = ArrayBuffer[String]()
    val processBuilder = Process(command(runner, benchmark))
    val processIO = new ProcessIO(
      _ => (),
      stdout => Source.fromInputStream(stdout).getLines.foreach(line =>
        if (line startsWith "<") result += line else UI(line)),
      stderr => Source.fromInputStream(stderr).getLines.foreach(error += _))

    val process = processBuilder.run(processIO)
    val success = process.exitValue
    (result, error)
  }

  def command(runner: Runner, benchmark: Benchmark) = commandInit ++
    Seq(
      runner.getClass.getProtectionDomain.getCodeSource.getLocation.getPath + COLON +
        config.bin.path + COLON +
        config.scalaLib + COLON +
        classOf[org.apache.commons.math.MathException].getProtectionDomain.getCodeSource.getLocation.getPath,
      runner.getClass.getName replace ("$", ""),
      benchmark.toXML.toString) ++
      config.args

  def command(benchmark: Benchmark) = commandInit ++
    Seq[String](
      config.bin.path + COLON +
        config.scalaLib + COLON +
        classOf[org.apache.commons.math.MathException].getProtectionDomain.getCodeSource.getLocation.getPath,
      benchmark.name) ++
      config.args

}
