package scala.tools.sbs

import scala.collection.mutable.ArrayBuffer
import scala.tools.nsc.io.Path.string2path
import scala.tools.nsc.io.Directory
import scala.tools.sbs.io.LogFactory
import scala.tools.sbs.io.UI
import scala.tools.sbs.performance.MeasurementSuccess
import scala.tools.sbs.performance.PerformanceBenchmark
import scala.tools.sbs.performance.Series

package object test {

  val testDir = Directory("D:/University/5thYear/Internship/Working/sbs/sbs.test").createDirectory()
  val args = Array(
    "--benchmarkdir",
    "D:/University/5thYear/Internship/Working/sbs/sbs.test",
    "--multiplier",
    "11",
    "--measurement",
    "31",
    "--steady-performance",
    "--show-log",
    "test.Benchmark")
  val testConfig = new Config(args)
  UI.config = testConfig

  val testLog = LogFactory(testConfig)

  object DummyBenchmark extends PerformanceBenchmark {
    override def name = "dummy"
    def src = testDir
    def arguments = List[String]()
    def classpathURLs = testConfig.classpathURLs
    def multiplier = 1
    def measurement = 10
    def sampleNumber = 0
    def shouldCompile = false
    def createLog(mode: BenchmarkMode) = testLog
    def init() = ()
    def run() = ()
    def reset() = ()
    def context = null
    def profileClasses = List("DummyBenchmark")
    def profileExclude = List("")
    def profileMethod = "run"
    def profileField = ""
    def pinpointClass = "DummyBenchmark"
    def pinpointMethod = "run"
    def pinpointExclude = List("")
    def pinpointPrevious = Directory("")
    def toXML: scala.xml.Elem = <null/>
  }

  val about5k5Arr = ArrayBuffer[Long](5527, 5549, 5601, 5566, 5481, 5487, 5547, 5484, 5542, 5485, 5587)
  val about1kArr1 = ArrayBuffer[Long](1054, 1044, 1043, 1045, 1045, 1046, 1066, 1048, 1051, 1050, 1050)
  val about1kArr2 = ArrayBuffer[Long](1050, 1048, 1044, 1045, 1044, 1049, 1053, 1051, 1048, 1052, 1102)
  val about1kArr3 = ArrayBuffer[Long](1059, 1045, 1052, 1043, 1046, 1049, 1066, 1044, 1046, 1047, 1058)

  val about5k5Series = new Series(testConfig, testLog, about5k5Arr, 99)
  val about1kSeries1 = new Series(testConfig, testLog, about1kArr1, 99)
  val about1kSeries2 = new Series(testConfig, testLog, about1kArr2, 99)
  val about1kSeries3 = new Series(testConfig, testLog, about1kArr3, 99)

  val success1k1 = MeasurementSuccess(about1kSeries1)
  val success1k2 = MeasurementSuccess(about1kSeries2)
  val success1k3 = MeasurementSuccess(about1kSeries3)
  val success5k5 = MeasurementSuccess(about5k5Series)

  val testBenchmark = DummyBenchmark

}
