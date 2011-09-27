package scala.tools.sbs

import java.lang.System

import scala.collection.mutable.ArrayBuffer
import scala.tools.nsc.io.Path.string2path
import scala.tools.nsc.io.Directory
import scala.tools.sbs.io.LogFactory

package object test {

  val about5k5Series = ArrayBuffer[Long](5527, 5549, 5601, 5566, 5481, 5487, 5547, 5484, 5542, 5485, 5587)
  val about1kSeries1 = ArrayBuffer[Long](1054, 1044, 1043, 1045, 1045, 1046, 1066, 1048, 1051, 1050, 1050)
  val about1kSeries2 = ArrayBuffer[Long](1050, 1048, 1044, 1045, 1044, 1049, 1053, 1051, 1048, 1052, 1102)
  val about1kSeries3 = ArrayBuffer[Long](1059, 1045, 1052, 1043, 1046, 1049, 1066, 1044, 1046, 1047, 1058)

  val testDir = Directory("sbs.test") createDirectory ()
  val testBenchmarkDir = testDir
  val testBinDir = (testBenchmarkDir / "bin").createDirectory()
  val testScalahome = Directory("D:/University/5thYear/Internship/Working/scala-2.9.1.final")
  val testJavahome = Directory(System getProperty "java.home")
  val testConfig = new Config(Array(""))

  val testLog = LogFactory(testConfig)
  val testBenchmark =
    BenchmarkFactory(testDir / "test", List[String](), List(testBinDir.toURL), 1, 10, 0, true, testConfig)

}
