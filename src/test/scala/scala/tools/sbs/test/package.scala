package scala.tools.sbs

import scala.collection.mutable.ArrayBuffer
import scala.tools.nsc.io.Path.string2path
import scala.tools.nsc.io.Directory
import scala.tools.sbs.common.Benchmark
import scala.tools.sbs.common.BenchmarkFactory
import scala.tools.sbs.common.BenchmarkMode
import scala.tools.sbs.io.LogFactory
import scala.tools.sbs.measurement.MeasurementSuccess
import scala.tools.sbs.measurement.Series

package object test {

  val testDir = Directory("sbs.test").createDirectory()
  val args = Array(
    "--benchmarkdir",
    "D:/University/5thYear/Internship/Working/benchmark",
    "--runs",
    "11",
    "--multiplier",
    "31",
    "--steady-performance",
    "--show-log",
    "test.Benchmark")
  val testConfig = new Config(args)

  val testLog = LogFactory(testConfig)

  object DummyBenchmark extends Benchmark {
    override def name = "dummy"
    def src = testDir
    def arguments = List[String]()
    def classpathURLs = testConfig.classpathURLs
    def runs = 1
    def multiplier = 10
    def sampleNumber = 0
    def shouldCompile = false
    def createLog(mode: BenchmarkMode) = testLog
    def init() = ()
    def run() = ()
    def reset() = ()
    def initCommand() = true
    def runCommand() = ()
    def toXML: scala.xml.Elem = <null/>
  }

  val about5k5Arr = ArrayBuffer[Long](5527, 5549, 5601, 5566, 5481, 5487, 5547, 5484, 5542, 5485, 5587)
  val about1kArr1 = ArrayBuffer[Long](1054, 1044, 1043, 1045, 1045, 1046, 1066, 1048, 1051, 1050, 1050)
  val about1kArr2 = ArrayBuffer[Long](1050, 1048, 1044, 1045, 1044, 1049, 1053, 1051, 1048, 1052, 1102)
  val about1kArr3 = ArrayBuffer[Long](1059, 1045, 1052, 1043, 1046, 1049, 1066, 1044, 1046, 1047, 1058)

  val about5k5Series = new Series(testLog, about5k5Arr, 99)
  val about1kSeries1 = new Series(testLog, about1kArr1, 99)
  val about1kSeries2 = new Series(testLog, about1kArr2, 99)
  val about1kSeries3 = new Series(testLog, about1kArr3, 99)

  val success1k1 = MeasurementSuccess(about1kSeries1)
  val success1k2 = MeasurementSuccess(about1kSeries2)
  val success1k3 = MeasurementSuccess(about1kSeries3)
  val success5k5 = MeasurementSuccess(about5k5Series)

  val testBenchmark = BenchmarkFactory("test", testDir / "test", List[String](), Nil, 1, 10, 0, true, testConfig)

}
