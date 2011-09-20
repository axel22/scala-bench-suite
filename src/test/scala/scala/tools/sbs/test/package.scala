package scala.tools.sbs

import scala.tools.nsc.io.Directory
import scala.tools.nsc.io.File
import scala.tools.sbs.benchmark.BenchmarkFactory
import scala.tools.sbs.util.Config
import scala.tools.sbs.util.LogFactory
import scala.tools.sbs.util.LogLevel
import scala.tools.sbs.benchmark.BenchmarkMode
import scala.tools.sbs.benchmark.BenchmarkKind

package object test {

  val testBenchmarkName = "test.benchmark"
  val testDir = Directory("sbs.test") createDirectory ()
  val testBenchmarkDir = (testDir / "benchmark") createDirectory ()
  val testBinDir = (testBenchmarkDir / "bin") createDirectory ()
  val testSrcDir = (testBenchmarkDir / "src") createDirectory ()
  val testSrc = testSrcDir.deepFiles.filter(_.hasExtension("scala")).foldLeft(List[File]())((src, f) => f :: src)
  val testScalahome = Directory("D:/University/5thYear/Internship/Working/scala-2.9.1.final")
  val testJavahome = Directory(System getProperty "java.home")
  val testLog = new LogFactory(testBenchmarkName, LogLevel.ALL, true) create testBenchmarkDir
  val testConfig = new Config(testBenchmarkDir, 1, 1, 0, testScalahome, testJavahome, false)
  val testBenchmark = new BenchmarkFactory(
    testLog, testConfig, testBenchmarkName, List(), List(testBinDir.toURL), List(BenchmarkMode.STEADY)).
    create(BenchmarkKind.SNIPPET, testSrc, testBinDir)

}
