/*
 * JVMInvoker
 * 
 * Version
 * 
 * Created on September 24th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package measurement

import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import scala.sys.process.Process
import scala.sys.process.ProcessIO
import scala.tools.sbs.io.Log
import scala.tools.sbs.io.UI
import scala.tools.sbs.util.Constant.COLON

import org.apache.commons.math.MathException

/** Trait used to invoke a new separated JVM.
 */
trait JVMInvoker {

  /** Invokes a new JVM which uses a typical {@link Measurer} to run a typical {@link Benchmark}.
   *
   *  @param measurer	{@link Measurer}
   *  @param benchmark	{@link Benchmark}
   *
   *  @return
   *  <ul>
   *  <li>A `String`  contains xml element representing the measurement result.
   *  <li>A `ArrayBuffer` of `String` contains runtime errors if any.
   *  </ul>
   */
  def invoke(measurer: Measurer, benchmark: Benchmark): (String, ArrayBuffer[String])

}

/** Factory object of {@link JVMInvoker}.
 */
object JVMInvokerFactory {

  def apply(log: Log, config: Config): JVMInvoker = new JVMCommandInvoker(log, config)

}

/** An implement of {@link JVMInvoker}.
 */
class JVMCommandInvoker(log: Log, config: Config) extends JVMInvoker {

  def invoke(measurer: Measurer, benchmark: Benchmark): (String, ArrayBuffer[String]) = {
    val command = Seq[String](
      config.javacmd,
      "-cp",
      config.scalaLib,
      config.javaProp,
      "scala.tools.nsc.MainGenericRunner",
      "-classpath",
      measurer.getClass.getProtectionDomain.getCodeSource.getLocation.getPath + COLON +
        config.bin.path + COLON +
        config.scalaLib + COLON +
        classOf[org.apache.commons.math.MathException].getProtectionDomain.getCodeSource.getLocation.getPath,
      measurer.getClass.getName replace ("$", ""),
      benchmark.toXML.toString) ++ config.args

    for (c <- command) {
      log.debug("[Command]  " + c)
    }

    var result = ""
    var error = ArrayBuffer[String]()
    val processBuilder = Process(command)
    val processIO = new ProcessIO(
      _ => (),
      stdout => Source.fromInputStream(stdout).getLines.foreach(line =>
        if (line startsWith "<") result += line else UI(line)),
      stderr => Source.fromInputStream(stderr).getLines.foreach(error += _))

    val process = processBuilder.run(processIO)
    val success = process.exitValue
    (result, error)
  }

}
