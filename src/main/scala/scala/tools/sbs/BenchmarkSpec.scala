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
import scala.tools.sbs.BenchmarkMode.BenchmarkMode
import scala.tools.sbs.BenchmarkMode.MEMORY
import scala.tools.sbs.BenchmarkMode.PROFILE
import scala.tools.sbs.BenchmarkMode.STARTUP
import scala.tools.sbs.BenchmarkMode.STEADY

trait BenchmarkSpec extends Spec {

  def referenceSpec       = BenchmarkSpec
  def programInfo         = Spec.Info("sbs", "", "scala.tools.sbs.BenchmarkDriver")
  private val modeAcc     = new Spec.Accumulator[String]()
  def modesString = modeAcc.get
  
  private implicit val tokenizeString = FromString.ArgumentsFromString    // String => List[String]

  help("""
    |Usage: sbs [<options>] [<benchmark> <benchmark> ...]
    |  <benchmark>: a path to a benchmark, typically a .scala file or a directory.
    |          Examples: benchmark.scala, ~/files/abcxyz
    |
    |  Benchmark mode:""".stripMargin)

  heading                 ("Benchmark modes")
  protected var _modes: List[BenchmarkMode] = Nil
                          "steady-performance"  / "Benchmarking in steady state" --> (_modes ::= STEADY)
                          "startup-performance" / "Run script files"             --> (_modes ::= STARTUP)
                          "memory-usage"        / "Run shootout tests"           --> (_modes ::= MEMORY)
                          "profiler"            / "Run scalap tests"             --> (_modes ::= PROFILE)

  heading		     	  ("Per-benchmark numbers of running")
  val runs              = "runs"       / "number of benchmark's running each measurement" defaultTo 1
  val multiplier        = "multiplier" / "number of benchmark's measurements"             defaultTo 11
  val sample            = "sample"     / "number of pre-created samples for " +
  		                                                         "detecting regression"   defaultTo 0

  heading               ("Specifying paths and additional flags, ~ means sbs root:")
  protected val benchmarkDirPath  = "benchmarkdir"   / "path from ~ to benchmark directory"   defaultTo "."
  protected val binDirPath        = "bindir"         / "path from ~ to test build"            defaultTo (null: String)
  protected val historyPath       = "history"        / "path to previous measurement results" defaultTo benchmarkDirPath
  protected val classpath         = "classpath"      / "classpath for benchmarks running"     defaultTo ""
  protected val scalaLibPath      = "scala-library"  / "path to scala-library.jar"            defaultTo (null: String)
  protected val scalaCompilerPath = "scala-compiler" / "path to scala-compiler.jar"           defaultTo (null: String)
  val javaOpts          = "javaopts"       / "flags to java on all runs"             defaultToEnv "JAVA_OPTS"
  val scalacOpts        = "scalacopts"     / "flags to scalac on all tests"          defaultToEnv "SCALAC_OPTS"
  protected val javaPath          = "java-home"      / "path to java"         defaultTo (System getProperty "java.home")

  heading                 ("Options influencing output:")
  val shouldCompile     = !("noncompile" / "should re-compile the snippets" --?)
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
