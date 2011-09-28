/*
 * ArgumentParser
 * 
 * Version 
 * 
 * Created on September 5th, 2011
 *
 * Created by ND P
 */

package scala.tools.sbs

import java.lang.System
import java.net.URL

import scala.io.Source
import scala.tools.nsc.io.Path.string2path
import scala.tools.nsc.io.Path
import scala.tools.sbs.io.Log
import scala.tools.sbs.io.LogFactory
import scala.tools.sbs.io.UI
import scala.tools.sbs.util.Constant.COLON

/** Parser for the suite's arguments.
 */
object ArgumentParser {

  /** Parses the arguments from command line.
   *
   *  @return	The `Config` object conresponding for the parsed values
   */
  def parse(args: Array[String]): (Config, Log, List[Benchmark]) = {
    val config = new Config(args)
    println(config.toString)
    UI.config = config
    val log = LogFactory(config)
    val benchmarks = if (config.parsed.residualArgs.length > 0) {
      config.parsed.residualArgs flatMap (name => getBenchmark(name, config))
    } else {
      (config.benchmarkDirectory.list map (_.name)).toList flatMap (name => getBenchmark(name, config))
    }
    (config, log, benchmarks)
  }

  def getBenchmark(name: String, config: Config): List[Benchmark] = {
    val srcFile = config.benchmarkDirectory / (name + ".scala")
    val srcDir = config.benchmarkDirectory / name
    if (!srcFile.exists && !srcDir.exists) {
      return Nil
    }
    var argFile = ""

    if (srcFile isFile) {
      argFile = (srcFile.path stripSuffix "scala") + "arg"
    } else if (srcDir isDirectory) {
      argFile = srcDir.path + ".arg"
    } else {
      return Nil
    }

    var runs = config.runs
    var multiplier = config.multiplier
    var sample = config.sample
    var shouldCompile = config.shouldCompile
    var classpathURLs = List[URL]()
    var args = List[String]()
    try {
      for (line <- Source.fromFile(argFile).getLines) {
        if (line startsWith "--runs") {
          runs = (line split " ")(1).toInt
        } else if (line startsWith "--multiplier") {
          multiplier = (line split " ")(1).toInt
        } else if (line startsWith "--classpath") {
          val readCP = (line split " ")(1) split COLON map (Path(_).toCanonical.toURL)
          classpathURLs = (readCP.toList ++ config.classpathURLs).distinct
        } else if (line startsWith "--sample") {
          sample = (line split " ")(1).toInt
        } else if (line startsWith "--noncompile") {
          shouldCompile = false
        } else {
          args = List[String]((line split COLON): _*)
        }
      }

    } catch {
      case e => {
        UI.debug("[Read failed] " + argFile + e.toString)
        Nil
      }
    }
    if (srcFile isFile) {
      List(BenchmarkFactory(name, srcFile, args, classpathURLs, runs, multiplier, sample, shouldCompile, config))
    } else {
      List(BenchmarkFactory(name, srcDir, args, classpathURLs, runs, multiplier, sample, shouldCompile, config))
    }
  }

  def exitOnError(message: String) {
    UI.error(message)
    UI.printUsage
    System.exit(1)
  }

}
