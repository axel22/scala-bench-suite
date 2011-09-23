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

import scala.tools.cmd.Spec
import scala.tools.cmd.FromString
import scala.tools.cmd.PropertyMapper
import scala.tools.cmd.Property

trait BenchmarkSpec extends Spec {

  def referenceSpec       = BenchmarkSpec
  def programInfo         = Spec.Info("sbs", "", "scala.tools.sbs.BenchmarkDriver")
  private val mode        = new Spec.Accumulator[String]()
  protected def testModes = mode.get

  private implicit val tokenizeString = FromString.ArgumentsFromString    // String => List[String]

  help("""
    |Usage: sbs [<options>] [<benchmark> <benchmark> ...]
    |  <benchmark>: a path to a benchmark, typically a .scala file or a directory.
    |          Examples: benchmark.scala, ~/files/abcxyz
    |
    |  Benchmark mode:""".stripMargin)

  val isAll         = ("all"                       / "run all tests (default, unless no options given)" --?)
                      (mode("steady-performance")  / "Compile files that are expected to build" --?)
                      (mode("startup-performance") / "Run script files" --?)
                      (mode("memory-usage")        / "Run shootout tests" --?)
                      (mode("profiler")            / "Run scalap tests" --?)

  heading             ("Specifying paths and additional flags, ~ means repository root:")

  val benchmarkDir  = "benchmarkdir" / "path from ~ to benchmark directory"      defaultTo "."
  val buildDir      = "builddir"     / "path from ~ to test build"               defaultTo "build"
  val srcDir        = "srcdir"       / "path from --benchmarkdir to sources"     defaultTo "."
  val javaOpts      = "javaopts"     / "flags to java on all runs"            defaultToEnv "JAVA_OPTS"
  val javacOpts     = "javacopts"    / "flags to javac on all runs"           defaultToEnv "JAVAC_OPTS"
  val scalacOpts    = "scalacopts"   / "flags to scalac on all tests"         defaultToEnv "SCALAC_OPTS"

  heading             ("Options influencing output:")
  val isCompile     = !("noncompile"     / "show the individual steps taken by each test" --?)
  val isShowLog     = "show-log"         / "show log on failures" --?
  val isVerbose     = "verbose"   / "be more verbose (additive with --trace)" --?
  val isDebug       = "debug"     / "maximum debugging output" --?

  heading             ("Other options:")
  val isCleanup     = "cleanup"       / "delete all stale files and dirs before run" --?
  val isNoCleanup   = "nocleanup"     / "do not delete any logfiles or object dirs" --?
}

object BenchmarkSpec extends BenchmarkSpec with Property  {
  lazy val propMapper = new PropertyMapper(BenchmarkSpec) {
    override def isPassThrough(key: String) = key == "sbs.options"
  }

  class BenchmarkCommandLine(args: List[String]) extends SpecCommandLine(args) {
    def propertyArgs = BenchmarkSpec.propertyArgs
  }

  override def creator(args: List[String]): BenchmarkCommandLine = new BenchmarkCommandLine(args)
}
