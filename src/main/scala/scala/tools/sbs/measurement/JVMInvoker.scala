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

import java.lang.System

import scala.collection.mutable.ArrayBuffer
import scala.sys.process.Process
import scala.sys.process.ProcessIO
import scala.tools.sbs.benchmark.Benchmark
import scala.tools.sbs.io.Log
import scala.tools.sbs.measurement.Measurer

import org.apache.commons.math.MathException

trait JVMInvoker {

  def invoke(measurer: Measurer, benchmark: Benchmark): ArrayBuffer[String]

}

object JVMInvokerFactory {
  
  def apply(log: Log, config: Config) = new JVMCommandInvoker(log, config)
  
}

class JVMCommandInvoker(log: Log, config: Config) extends JVMInvoker {

  def invoke(measurer: Measurer, benchmark: Benchmark): ArrayBuffer[String] = {
    val command = Seq(
      config.JAVACMD,
      "-cp",
      config.SCALALIB,
      config.JAVAPROP,
      "scala.tools.nsc.MainGenericRunner",
      "-classpath",
      measurer.getClass.getProtectionDomain.getCodeSource.getLocation.getPath +
        (System.getProperty("path.separator")) +
        config.bin.path +
        (System.getProperty("path.separator")) +
        classOf[org.apache.commons.math.MathException].getProtectionDomain.getCodeSource.getLocation.getPath,
      measurer.getClass.getName replace ("$", ""))

    for (c <- command) {
      log.verbose("[Command]  " + c)
    }

    var output = ArrayBuffer[String]()
    val processBuilder = Process(command)
    val processIO = new ProcessIO(
      _ => (),
      stdout => scala.io.Source.fromInputStream(stdout).getLines.foreach(output += _),
      _ => ())

    val process = processBuilder.run(processIO)
    val success = process.exitValue
    output
  }

}