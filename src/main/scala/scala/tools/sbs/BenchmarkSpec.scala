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

import scala.tools.cmd.FromString
import scala.tools.cmd.Interpolation
import scala.tools.cmd.Meta
import scala.tools.cmd.Property
import scala.tools.cmd.PropertyMapper
import scala.tools.cmd.Spec
import scala.tools.sbs.util.Constant

/** sbs' command line arguments and flags go here.
 */
trait BenchmarkSpec extends Spec with Meta.StdOpts with Interpolation {

  def referenceSpec = BenchmarkSpec
  def programInfo = Spec.Info("sbs", "", "scala.tools.sbs.BenchmarkDriver")

  private implicit val tokenizeString = FromString.ArgumentsFromString // String => List[String]

  help("""
    |Usage: sbs [<options>] [<benchmark> <benchmark> ...]
    |  <benchmark>: a path to a benchmark, typically a .scala file or a directory.
    |          Examples: benchmark.scala, ~/files/abcxyz
    |  All the per-benchmark <options> will be overriden by corresponding ones in
    |   - .arg file with the same name with the snippet benchmark
    |   - values overriden from templates with the initializable benchmark
    |  Benchmark mode:""".stripMargin)

  protected var _modes: List[BenchmarkMode] = Nil
                              "steady-performance"  / "Benchmarking in steady state"     --> (_modes ::= SteadyState)
                              "startup-performance" / "Benchmarking in startup state"    --> (_modes ::= StartUpState)
                              "memory-usage"        / "Measuring memory usage"           --> (_modes ::= MemoryUsage)
                              "profile"             / "Profiling"                        --> (_modes ::= Profiling)
                              "pinpoint"            / "Pinpointing regression detection" --> (_modes ::= Pinpointing)
                              "all"                 / "run all benchmarking modes"       -->
                              (_modes = List(SteadyState, StartUpState,MemoryUsage,Profiling,Pinpointing,Instrumenting))

  heading                   ("Statistics metrics:")
  val leastConfidenceLevel = "least-confidence-level" / "smallest acceptable confidence level" defaultTo 90
  val _precisionThreshold  = "precision-threshold"    / "%" defaultTo 2
  val reMeasurement        = "re-measurement"         / "maximum # for re-measurements" defaultTo 1
  val warmRepeat           = "warm-repeat"            / "maximum # of repetition to waming up" defaultTo 5

  heading            ("Per-benchmark numbers of performance:")
  val multiplier    = "multiplier" / "# of times to run per measurement" defaultTo 1
  val measurement   = "measurement" / "# of measurements" defaultTo 11
  val sample        = "sample" / "# of pre-created samples" defaultTo 0
  val shouldCompile = !("noncompile" / "not re-compile the benchmarks if set" --?)

  heading                        ("Per-benchmark names for profiling:")
  protected val _profileClasses = "profile-classes" / "classes to be profiled" defaultTo ""
                                  ""                / ("  split by " + Constant.COLON)
  protected val _profileExclude = "profile-exclude" / "classes to be ignored" defaultTo ""
                                  ""                / ("  split by " + Constant.COLON)

  val profileMethod = "profile-method" / "the method to be profiled" defaultTo ""
  val profileField  = "profile-field"  / "the field to be profiled" defaultTo ""
  val shouldGC      = "profile-gc"     / "whether to profile gc's running" --?
  val shouldBoxing  = "profile-boxing" / "whether to profile number of boxing - unboxing" --?
  val shouldStep    = "profile-step"   / "whether to profile number of steps performed" --?

  heading             ("Per-benchmark names for pinpointing regression detection:")
  val pinpointClass  = "pinpoint-class"  / "the insterested class" defaultTo ""
  val pinpointMethod = "pinpoint-method" / "the insterested method" defaultTo ""

  val pinpointBottleneckDectect = "pinpoint-bottleneck" / "whether to detect the bottleneck" --?

  protected val _pinpointPrevious = "pinpoint-previous" / "location of the previous build" defaultTo ""
                                    ""                  / "  should not be included in classpath"
  protected val _pinpointExclude  = "pinpoint-exclude"  / "classes to be ignored" defaultTo ""
                                    ""                  / ("  split by " + Constant.COLON)

  heading                        ("Per-benchmark names for instrumenting:")
  protected val _instrumentMethods = "instrument-methods" / "methods to be instrumented" defaultTo ""
                                     ""                   / ("  split by " + Constant.COLON)

  heading                          ("Specifying paths and additional values, ~ means sbs root:")
  protected val benchmarkDirPath  = "benchmarkdir"   / "path from ~ to benchmark directory" defaultTo "."
  protected val binDirPath        = "bindir"         / "path from ~ to benchmark build" defaultTo ""
  protected val historyPath       = "history"        / "path to measurement result history" defaultTo benchmarkDirPath
  protected val classpath         = "classpath"      / "classpath for benchmarks running" defaultTo ""
  protected val scalaLibPath      = "scala-library"  / "path to scala-library.jar" defaultTo ""
  protected val scalaCompilerPath = "scala-compiler" / "path to scala-compiler.jar" defaultTo ""
  val javaOpts                    = "javaopts"       / "flags to java on all runs" defaultToEnv "JAVA_OPTS"
  val scalacOpts                  = "scalacopts"     / "flags to scalac on all tests" defaultToEnv "SCALAC_OPTS"
  protected val javaPath          = "java-home"      / "path to java" defaultTo (System getProperty "java.home")

  heading        ("Options influencing output:")
  val isShowLog = "show-log" / "show log" --?
  val isVerbose = "verbose"  / "be more verbose" --?
  val isDebug   = "debug"    / "debugging output" --?
  val isQuiet   = "quiet"    / "no command line output" --?

  heading("Other options:")
  val isCleanup    = "cleanup"     / "delete all stale files and dirs before run" --?
  val isNoCleanLog = "noclean-log" / "do not delete any logfiles" --?
  val isHelp       = "help"        / "print usage message" --?

}

object BenchmarkSpec extends BenchmarkSpec with Property {
  lazy val propMapper = new PropertyMapper(BenchmarkSpec) {
    override def isPassThrough(key: String) = key == "sbs.options"
  }

  type ThisCommandLine = BenchmarkCommandLine
  class BenchmarkCommandLine(args: List[String]) extends SpecCommandLine(args) {
    def propertyArgs = BenchmarkSpec.propertyArgs
  }

  override def creator(args: List[String]): ThisCommandLine = new BenchmarkCommandLine(args)
}
