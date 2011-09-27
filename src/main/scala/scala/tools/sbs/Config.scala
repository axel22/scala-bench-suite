/*
 * Config
 * 
 * Version
 * 
 * Created September 5th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs

import java.lang.System

import scala.tools.cmd.Instance
import scala.tools.nsc.io.Path.string2path
import scala.tools.nsc.io.Directory
import scala.tools.nsc.io.File
import scala.tools.nsc.io.Path
import scala.tools.sbs.BenchmarkMode.MEMORY
import scala.tools.sbs.BenchmarkMode.PROFILE
import scala.tools.sbs.BenchmarkMode.STARTUP
import scala.tools.sbs.BenchmarkMode.STEADY
import scala.tools.sbs.util.Constant.COLON
import scala.tools.sbs.util.Constant.SLASH

class Config(args: Array[String])
  extends { val parsed = BenchmarkSpec(args: _*) } with BenchmarkSpec with Instance {

  /** cwd where benchmarking taking place, also the sources directory for all benchmarks.
   */
  val benchmarkDirectory = Directory(benchmarkDirPath).toCanonical.createDirectory()

  /** All benchmark compiles output here, also contains non-compiling-benchmarks.
   */
  val bin: Directory = if (binDirPath == null) {
    (benchmarkDirectory / "bin").toCanonical.createDirectory()
  } else {
    Path(binDirPath).toCanonical.createDirectory()
  }

  /** Contains measurement histories from previous runnings. Layout:
   *  here (history)/
   *                benchmark/
   *                          memory usage history files
   *                          steady state history files
   *                          ...
   */
  val history = Path(historyPath).toCanonical.createDirectory()

  /** Benchmarking modes, includes:
   *  <ul>
   *  <li>Benchmarking in startup state
   *  <li>Benchmarking in steady state
   *  <li>Measuring memory usage
   *  <li>Profiling
   *  </ul>
   */
  val modes = if (_modes.length == 0) List(STEADY, STARTUP, MEMORY, PROFILE) else _modes

  /** Common classpath URLs for every benchmarks
   */
  val classpathURLs = ((bin.path + COLON + classpath) split COLON).toList map (Path(_).toCanonical.toURL)

  val scalaLibraryJar = File(scalaLibPath)

  val scalaCompilerJar = File(scalaCompilerPath)

  val javahome = Directory(javaPath)

  val javacmd = javaPath + SLASH + "bin" + SLASH + "java"

  val javaProp = "-Dscala.home=" + scalaLibraryJar.parent.parent.path

  val scalaLib = scalaLibraryJar.path + COLON + scalaCompilerJar

  override def toString(): String = {
    val endl = System getProperty "line.separator"
    "Config:" +
      endl + "        BenchmarkDir:    " + benchmarkDirectory.path +
      endl + "        Java home:       " + javahome.path +
      endl + "        Java:            " + javacmd +
      endl + "        Java properties: " + javaProp
  }

  def toXML =
    <Config>
      <directory>{ benchmarkDirectory.path }</directory>
      <modes>{ for (mode <- modes) yield <mode>{ mode.toString } </mode> }</modes>
      <javahome>{ javahome.path }</javahome>
      <showLog>{ isShowLog }</showLog>
      <logVerbose>{ isVerbose }</logVerbose>
      <logDebug>{ isDebug }</logDebug>
    </Config>

}
