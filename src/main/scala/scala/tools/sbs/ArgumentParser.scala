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
import scala.tools.nsc.io.Directory
import scala.tools.nsc.io.Path
import scala.tools.sbs.benchmark.BenchmarkInfo
import scala.tools.sbs.io.Log
import scala.tools.sbs.io.LogFactory
import scala.tools.sbs.io.UI
import scala.tools.sbs.util.Constant.COLON

/** Parser for the suite's arguments.
 */
object ArgumentParser {

  /** Parses the arguments from command line.
   *
   *  @return
   *  <ul>
   *  <li>The {@link Config} object conresponding for the parsed values
   *  <li>The {@link Log}
   *  <li>The `List` of benchmarks to be run
   *  </ul>
   */
  def parse(args: Array[String]): (Config, Log, List[BenchmarkInfo]) = {
    val config = new Config(args)
    UI.config = config
    val log = LogFactory(config)
    val nameList =
      if (config.parsed.residualArgs.length > 0) config.parsed.residualArgs
      else if (config.isAll) (config.benchmarkDirectory.list map (_.name)).toList
      else Nil
    val benchmarks = nameList map (name => getBenchmark(name, config)) filterNot (_ == null)
    (config, log, benchmarks)
  }

  /** Gets a `Benchmark` object, which has `name`, along with its specified arguments in the benchmark directory.
   *
   *  @param name	The name of the desired benchmark
   *  @param config
   *
   *  @return	`List(Benchmark)` if there is actually a benchmark with the given name, Nil otherwise
   */
  def getBenchmark(name: String, config: Config): BenchmarkInfo =
    getSource(name, config.benchmarkDirectory) match {
      case Some(src) => {
        val (mainClassName, argFile) =
          if (src isFile) (src.stripExtension, (src.path stripSuffix "scala") + "arg")
          else (src.name, src.path + ".arg")
        var sample = config.sample
        var shouldCompile = config.shouldCompile
        var classpathURLs = List[URL]()
        var args = List[String]()

        try {
          val argBuffer = Source.fromFile(argFile)
          argBuffer.getLines foreach (line =>
            if (line startsWith "--classpath") {
              val readCP = (line split " ")(1) split COLON map (Path(_).toCanonical.toURL)
              classpathURLs = (readCP.toList ++ config.classpathURLs).distinct
            }
            else if (line startsWith "--sample") {
              sample = (line split " ")(1).toInt
            }
            else if (line startsWith "--noncompile") {
              shouldCompile = false
            }
            else if (line startsWith "-") {
              // Does nothing
            }
            else {
              args = List[String]((line split " "): _*)
            })
          argBuffer.close()
        }
        catch { case e => UI.debug("[Read failed] " + argFile + "\n" + e.toString) }
        BenchmarkInfo(
          mainClassName,
          src,
          args,
          classpathURLs,
          sample,
          shouldCompile)
      }
      case _ => null
    }

  /** Checks whether a `name` in `path` directory is a benchmark.
   *  If so, returns its source file(s).
   *
   *  @param name	The name to be checked
   *  @param path	The benchmark directory
   *
   *  @return	`Some[Path]` to the source file / directory if `name` is a benchmark `None` otherwise
   */
  def getSource(name: String, path: Directory): Option[Path] = {
    val src = path / name
    if (src exists)
      if (src.isFile && src.hasExtension("scala")) Some(src)
      else if ((src isDirectory) &&
        (src.toDirectory.deepFiles.filter(p => p.isFile && p.hasExtension("scala"))).length > 0) Some(src)
      else None
    else None
  }

  /** Prints a message and exit program with error code of 1.
   */
  def exitOnError(message: String) {
    UI.error(message)
    UI.printUsage
    System.exit(1)
  }

}
