package scala.tools.sbs

import scala.tools.nsc.io.Directory
import scala.tools.nsc.io.File
import scala.tools.sbs.benchmark.BenchmarkFactory
import scala.tools.sbs.util.Config
import scala.tools.sbs.util.LogFactory
import scala.tools.sbs.util.LogLevel.LogLevel
import scala.tools.sbs.util.LogLevel
import scala.tools.sbs.benchmark.BenchmarkMode
import scala.tools.sbs.benchmark.BenchmarkKind
import scala.collection.mutable.ArrayBuffer
import scala.tools.sbs.util.LogLevel

package object test {

  val about5k5Series = ArrayBuffer[Long](5527, 5549, 5601, 5566, 5481, 5487, 5547, 5484, 5542, 5485, 5587)
  val about1kSeries1 = ArrayBuffer[Long](1054, 1044, 1043, 1045, 1045, 1046, 1066, 1048, 1051, 1050, 1050)
  val about1kSeries2 = ArrayBuffer[Long](1050, 1048, 1044, 1045, 1044, 1049, 1053, 1051, 1048, 1052, 1102)
  val about1kSeries3 = ArrayBuffer[Long](1059, 1045, 1052, 1043, 1046, 1049, 1066, 1044, 1046, 1047, 1058)

  val testBenchmarkName = "test.Benchmark"
  val testDir = Directory("sbs.test") createDirectory ()
  val testBenchmarkDir = (testDir / "benchmark") createDirectory ()
  val testBinDir = (testBenchmarkDir / "bin").createDirectory()
  val testScalahome = Directory("D:/University/5thYear/Internship/Working/scala-2.9.1.final")
  val testJavahome = Directory(System getProperty "java.home")
  val testLog = new LogFactory(testBenchmarkName, LogLevel.ALL, true) create testBenchmarkDir
  val testConfig = new Config(testBenchmarkDir, 1, 11, 0, testScalahome, testJavahome, false)
  val testBenchmark = new BenchmarkFactory(
    testLog, testConfig, testBenchmarkName, List(), List(testBinDir.toURL), List(BenchmarkMode.STEADY)).
    create(BenchmarkKind.SNIPPET, testBenchmarkDir)

}
