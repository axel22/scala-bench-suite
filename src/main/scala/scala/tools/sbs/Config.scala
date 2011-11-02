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
import scala.tools.sbs.io.UI
import scala.tools.sbs.util.Constant.COLON
import scala.tools.sbs.util.Constant.SLASH
import scala.tools.sbs.util.Constant
import scala.tools.sbs.util.FileUtil

import org.apache.commons.math.MathException

/** Configurations for sbs running.
 */
case class Config(args: Array[String])
    extends { val parsed = BenchmarkSpec(args: _*) } with BenchmarkSpec with Instance {

  def helpMsg = BenchmarkSpec.helpMsg

  /** cwd where benchmarking taking place, also the sources directory for all benchmarks.
   */
  val benchmarkDirectory = FileUtil.mkDir(Directory(benchmarkDirPath).toCanonical) match {
    case Left(dir) => dir
    case Right(s) => {
      UI.error(s)
      UI("Benchmark directory changes into cwd")
      Directory(".")
    }
  }

  /** All benchmark compiled output are here, also contains not-compiled-benchmarks.
   */
  val bin = if (binDirPath == "") {
    FileUtil.mkDir((benchmarkDirectory / "bin").toCanonical) match {
      case Left(dir) => dir
      case Right(s) => {
        UI.error(s)
        benchmarkDirectory
      }
    }
  }
  else {
    FileUtil.mkDir(Path(binDirPath).toCanonical) match {
      case Left(dir) => dir
      case Right(s) => {
        UI.error(s)
        benchmarkDirectory
      }
    }
  }

  /** Contains measurement histories from previous runnings. Layout:
   *  here (history)/
   *                steady/
   *                      benchmark/
   *                               steady state history files
   *                               ...
   *                memory/
   *                       ....
   */
  val history = FileUtil.mkDir(Path(historyPath).toCanonical) match {
    case Left(dir) => dir
    case Right(s) => {
      UI.error(s)
      benchmarkDirectory
    }
  }

  val profileExclude =
    if (_profileExclude == "") List("java.*", "javax.*", "sun.*", "com.sun.*", "org.apache.common.math.*")
    else if (_profileExclude equals "none") Nil
    else (_profileExclude split Constant.COLON) toList

  val profileClasses = (_profileClasses split Constant.COLON) toList

  val pinpointExclude =
    if (_pinpointExclude == "") List("java.*", "javax.*", "sun.*", "com.sun.*", "org.apache.common.math.*")
    else if (_pinpointExclude == "none") Nil
    else (_pinpointExclude split Constant.COLON) toList

  val pinpointPrevious = Directory(
    if (_pinpointPrevious == "") ".pinpointprevious"
    else _pinpointPrevious)

  /** `List` of {@link BenchmarkMode}. May include:
   *  <ul>
   *  <li>Benchmarking in startup state
   *  <li>Benchmarking in steady state
   *  <li>Measuring memory usage
   *  <li>Profiling
   *  </ul>
   */
  val modes = if (_modes == Nil) List(SteadyState, StartUpState, MemoryUsage, Profiling) else _modes

  /** `File` path of scala-library.jar.
   */
  val scalaLibraryJar: File = getJar(scalaLibPath, "scala-library.jar")

  /** `File` path of scala-compiler.jar.
   */
  val scalaCompilerJar = getJar(scalaCompilerPath, "scala-compiler.jar")

  private def getJar(path: String, name: String): File = {
    if (path == "") {
      try {
        val clazz =
          if (name contains "library") classOf[scala.ScalaObject]
          else scala.tools.nsc.MainGenericRunner.getClass
        Path(clazz.getProtectionDomain.getCodeSource.getLocation.getPath).toCanonical.toFile
      }
      catch {
        case _ => {
          val classpath = List("java.class.path",
            "java.boot.class.path",
            "sun.boot.class.path").flatMap(s => System.getProperty(s, "") split COLON)
          classpath.find(Path(_).name equals name) match {
            case None      => throw new Error("Cannot find default " + name)
            case Some(str) => Path(str).toCanonical.toFile
          }
        }
      }
    }
    else {
      Path(path).toCanonical.toFile
    }
  }

  /** Common classpath URLs for every benchmarks.
   */
  val classpathURLs =
    List(
      scalaLibraryJar.toURL,
      scalaCompilerJar.toURL,
      this.getClass.getProtectionDomain.getCodeSource.getLocation,
      classOf[org.apache.commons.math.MathException].getProtectionDomain.getCodeSource.getLocation,
      bin.toURL) ++
      ((classpath split COLON).toList map (Path(_).toCanonical.toURL)) distinct

  val javahome = Directory(javaPath)

  val javacmd = javaPath + SLASH + "bin" + SLASH + "java"

  val javaProp = "-Dscala.home=" + scalaLibraryJar.parent.parent.path

  /** Scala-library.jar and scala-compiler.jar in form of classpath.
   */
  val scalaLib = scalaLibraryJar.path + COLON + scalaCompilerJar

  override def toString(): String = {
    val endl = System getProperty "line.separator"
    "Config:" +
      endl + "        benchmarkDir:    " + benchmarkDirectory.path +
      endl + "        bin              " + bin.path +
      endl + "        history          " + history.path +
      endl + "        modes            " + modes.toString +
      endl + "        classpath        " + classpathURLs +
      endl + "        scalaLibraryJar  " + scalaLibraryJar.path +
      endl + "        scalaCompilerJar " + scalaCompilerJar.path +
      endl + "        Java home:       " + javahome.path +
      endl + "        Java:            " + javacmd +
      endl + "        Java properties: " + javaProp
  }

}
