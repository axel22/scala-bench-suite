/*
 * BenhcmarkSpec
 * 
 * Version
 * 
 * Created on September 23rd, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs

import java.lang.System
import scala.tools.cmd.Spec
import scala.tools.cmd.FromString
import scala.tools.cmd.Property
import scala.tools.cmd.PropertyMapper
import scala.tools.cmd.Interpolation
import scala.tools.cmd.Meta

/** sbs' command line arguments and flags go here.
 */
trait BenchmarkSpec extends Spec with Meta.StdOpts with Interpolation {

  def referenceSpec       = BenchmarkSpec
  def programInfo         = Spec.Info("sbs", "", "scala.tools.sbs.BenchmarkDriver")
  
  private implicit val tokenizeString = FromString.ArgumentsFromString    // String => List[String]

  help("""
    |Usage: sbs [<options>] [<benchmark> <benchmark> ...]
    |  <benchmark>: a path to a benchmark, typically a .scala file or a directory.
    |          Examples: benchmark.scala, ~/files/abcxyz
    |
    |  Benchmark mode:""".stripMargin)

  protected var _modes: List[BenchmarkMode] = Nil
                          "steady-performance"  / "Benchmarking in steady state"  --> (_modes ::= SteadyState)
                          "startup-performance" / "Benchmarking in startup state" --> (_modes ::= StartUpState)
                          "memory-usage"        / "Measuring memory usage"        --> (_modes ::= MemoryUsage)
                          "profiler"            / "Profiling"                     --> (_modes ::= Profiling)
                          "pinpoint"            / "Pinpointing regression detection" --> (_modes ::= Pinpointing)

  heading		     	("Per-benchmark numbers of performance measuring " +
  		                "(will be overriden by corresponding one in .arg file):")
  val runs              = "runs"       / "number running per measurement"        defaultTo 1
  val multiplier        = "multiplier" / "number of  measurements"               defaultTo 11
  val sample            = "sample"     / "number of pre-created samples"         defaultTo 0
  val shouldCompile     = !("noncompile" / "should re-compile the snippets" --?)

  heading                         ("Per-benchmark names for profiling " +
  		                            "(will be overriden by corresponding one in .arg file):")
  protected val _profiledClasses = "profile-class"  / "classes to be profiled, split by ;" defaultTo ""
  protected val _excludeClasses  = "excludes"       / "classes to be ignored, split by ; 'none' for profile anything" defaultTo ""

  val profiledMethod    = "profile-method" / "name of the methoed to be profiled" defaultTo ""
  val profiledField     = "profile-field"  / "name of the field to be profiled"   defaultTo ""
  val shouldGC          = "profile-gc"     / "should profile gc's running" --?
  val shouldBoxing      = "profile-boxing" / "profile number of boxing - unboxing" --?
  val shouldStep        = "profile-step"   / "profile number of steps performed" --?

  heading                         ("Per-benchmark names for pinpointing regression detection " +
  		                            "(will be overriden by corresponding one in .arg file):")
  val pinpointClass      = "pinpoint-class"  / "name of the methoed to be pinpointing detected" defaultTo ""
  val pinpointMethod     = "pinpoint-method" / "name of the field to be pinpointing detected"   defaultTo ""
  
  heading               ("Specifying paths and additional values, ~ means sbs root:")
  protected val benchmarkDirPath  = "benchmarkdir"   / "path from ~ to benchmark directory"   defaultTo "."
  protected val binDirPath        = "bindir"         / "path from ~ to benchmark build"       defaultTo ("": String)
  protected val historyPath       = "history"        / "path to measurement result history"   defaultTo benchmarkDirPath
  protected val classpath         = "classpath"      / "classpath for benchmarks running"     defaultTo ""
  protected val scalaLibPath      = "scala-library"  / "path to scala-library.jar"            defaultTo ("": String)
  protected val scalaCompilerPath = "scala-compiler" / "path to scala-compiler.jar"           defaultTo ("": String)
  val javaOpts                    = "javaopts"       / "flags to java on all runs"            defaultToEnv "JAVA_OPTS"
  val scalacOpts                  = "scalacopts"     / "flags to scalac on all tests"         defaultToEnv "SCALAC_OPTS"
  protected val javaPath          = "java-home"      / "path to java"         defaultTo (System getProperty "java.home")

  heading                 ("Options influencing output:")
  val isShowLog         = "show-log"     / "show log" --?
  val isVerbose         = "verbose"      / "be more verbose" --?
  val isDebug           = "debug"        / "debugging output" --?

  heading                 ("Other options:")
  val isCleanup         = "cleanup"   / "delete all stale files and dirs before run" --?
  val isNoCleanup       = "nocleanup" / "do not delete any logfiles or object dirs" --?
  val isHelp            = "help"      / "print usage message" --?

}

object BenchmarkSpec extends BenchmarkSpec with Property  {
  lazy val propMapper = new PropertyMapper(BenchmarkSpec) {
    override def isPassThrough(key: String) = key == "sbs.options"
  }

  type ThisCommandLine = BenchmarkCommandLine
  class BenchmarkCommandLine(args: List[String]) extends SpecCommandLine(args) {
    def propertyArgs = BenchmarkSpec.propertyArgs
  }
  
  override def creator(args: List[String]): ThisCommandLine = new BenchmarkCommandLine(args)
}
