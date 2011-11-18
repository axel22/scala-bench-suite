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
import scala.tools.sbs.benchmark.InfoPack

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
  def parse(args: Array[String]): (Config, Log, InfoPack) = {
    val config = new Config(args)
    UI.config = config
    val log = LogFactory(config)
    val pack = new InfoPack
    config.modes foreach (mode => {
      pack switchMode mode
      val nameList =
        if (config.parsed.residualArgs.length > 0) config.parsed.residualArgs
        else (config.benchmarkDirectory / mode.location).toDirectory.list.toList filterNot (_ hasExtension "arg") map (_.name)
      nameList map (name => getInfo(name, mode, config)) filterNot (_ == null) foreach (pack add _)
    })
    (config, log, pack)
  }

  /** Creates a {@link scala.tools.sbs.benchmarkBenchmarkInfo} object,
   *  which has `name`, along with its specified arguments in the benchmark directory.
   *
   *  @param name	The name of the desired benchmark
   *  @param config
   *
   *  @return	`List(Benchmark)` if there is actually a benchmark with the given name, Nil otherwise
   */
  def getInfo(name: String, mode: BenchmarkMode, config: Config): BenchmarkInfo = {
    val (mainClassName, argFile, maybeSrc) = getSource(name, config.benchmarkDirectory / mode.location) match {
      case Some(source) =>
        if (source isFile) (source.stripExtension, (source.path stripSuffix "scala") + "arg", source)
        else (source.name, source.path + ".arg", source)
      case None => {
        val path = config.benchmarkDirectory / mode.location / name
        if (path.isFile && (path hasExtension "scala")) (name, (path.path stripSuffix "scala") + "arg", null)
        else (name, path.path + ".arg", null)
      }
    }
    var sample = config.sample
    var timeout = config.timeout
    var shouldCompile = config.shouldCompile
    var classpathURLs = List[URL]()
    var args = List[String]()
    var src = maybeSrc

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
        else if (line startsWith "--timeout") {
          timeout = (line split " ")(1).toInt
        }
        else if (line startsWith "--noncompile") {
          shouldCompile = false
        }
        else if (line startsWith "--src") {
          if (src == null) src = (line split " ")(1)
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
    if (src == null) null
    else BenchmarkInfo(
      mainClassName,
      src,
      args,
      classpathURLs,
      sample,
      timeout,
      shouldCompile)
  }

  /** Checks whether a `name` in `path` directory is a benchmark.
   *  If so, returns its source file(s).
   *
   *  @param name	The name to be checked
   *  @param path	The benchmark directory
   *
   *  @return	`Some[Path]` to the source file / directory if `name` is a benchmark `None` otherwise
   */
  def getSource(name: String, path: Path): Option[Path] = {
    val src = path / name
    if (src exists)
      if (src.isFile && src.hasExtension("scala")) Some(src)
      else if ((src isDirectory) &&
        (src.toDirectory.deepFiles exists (p => p.isFile && p.hasExtension("scala")))) Some(src)
      else None
    else None
  }

}
