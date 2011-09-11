/*
 * BenchmarkDriver
 * 
 * Version 
 * 
 * Created on September 5th, 2011
 *
 * Created by ND P
 */

package ndp.scala.tools.sbs

import ndp.scala.tools.sbs.measurement.MemoryHarness
import ndp.scala.tools.sbs.measurement.SteadyHarness
import ndp.scala.tools.sbs.regression.Persistor
import ndp.scala.tools.sbs.util.Constant

/**
 * Object controls the runtime of benchmark classes to do measurements.
 *
 * @author ND P
 */
object BenchmarkDriver {

  /**
   * Start point of the benchmark driver
   * Does the following:
   * <ul>
   * <li>Parse input parameters
   * <li>Compile the sources of the benchmarks if necessary
   * <li>Run all the benchmarks with the specified parameters
   * <li>Load the previous results
   * <li>Run comparisons to previous results
   * </ul>
   */
  def main(args: Array[String]): Unit = {

    val (c, l, b) = ArgumentParser.parse(args)
    config = c
    log = l
    benchmark = b

    log.debug(config.toString())

    try {
      if (config.compile) {
        if (!benchmark.compile()) {
          System.exit(1)
        }
      }
      if (config.sampleNumber > 0) {
        Persistor.generate(config.sampleNumber)
      }

      log.verbose("[Measure]")

      val confArg = config.toArgument()
      val logArg = log.toArgument()

      val command = Seq(
        config.JAVACMD,
        "-cp",
        config.SCALALIB,
        config.JAVAPROP,
        "scala.tools.nsc.MainGenericRunner",
        "-classpath",
        MemoryHarness.getClass.getProtectionDomain.getCodeSource.getLocation.getPath +
          (System.getProperty("path.separator")) +
          benchmark.buildPath.path +
          (System.getProperty("path.separator")) +
          classOf[org.apache.commons.math.MathException].getProtectionDomain.getCodeSource.getLocation.getPath,
        MemoryHarness.getClass.getName replace ("$", ""),
        confArg(Constant.INDEX_CLASSNAME),
        confArg(Constant.INDEX_SRCPATH),
        confArg(Constant.INDEX_BENCHMARK_DIR),
        confArg(Constant.INDEX_BENCHMARK_BUILD),
        confArg(Constant.INDEX_BENCHMARK_TYPE),
        confArg(Constant.INDEX_RUNS),
        confArg(Constant.INDEX_MULTIPLIER),
        confArg(Constant.INDEX_SCALA_HOME),
        confArg(Constant.INDEX_JAVA_HOME),
        confArg(Constant.INDEX_CLASSPATH),
        confArg(Constant.INDEX_PERSISTOR_LOC),
        confArg(Constant.INDEX_COMPILE),
        logArg(Constant.INDEX_LOG_FILE),
        logArg(Constant.INDEX_LOG_LEVEL),
        logArg(Constant.INDEX_SHOW_LOG)
      )
      
      for (c <- command) {
        log.verbose("--Command--  " + c)
      }
      
      /*var arr = ArrayBuffer[String]()
      val processBuilder = Process(command)
      val processIO = new ProcessIO(
          _ => (),
          stdout => scala.io.Source.fromInputStream(stdout).getLines.foreach(arr.+=),
          stderr => scala.io.Source.fromInputStream(stderr).getLines.foreach(println))
      
      val process = processBuilder.run(processIO)
      val success = process.exitValue
      
      for (ret <- arr) {
        log.verbose("[Result]  " + ret)
      }*/
      MemoryHarness.run() match {
        case Left(ret) => println(ret.toString())
        case Right(s) => println(s)
      }
    } catch {
      /*case e: java.lang.reflect.InvocationTargetException => {
        e.getCause match {
        case n: java.lang.ClassNotFoundException => {
          log error "Class " + n.getMessage() + " not found."
        }
        case n: java.lang.NoClassDefFoundError => log error "Class " + n.getMessage() + " not found."
        case n
         => log error n.toString
        }
      }
      case e: java.lang.ClassNotFoundException => {
        log debug "Class " + e.getMessage + " not found."
      }
      case f: Exception => {
        val report = new Report
        report(log, config, Constant.FAILED, Report dueToException f)
      }*/
      case e: Exception => {
        throw e
        //        val report = new Report
        //        report(log, config, Constant.FAILED, Report dueToException e)
      }
    }
  }

}
