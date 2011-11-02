package scala.tools.sbs.test
package common

import scala.tools.sbs.benchmark.Benchmark
import scala.tools.sbs.BenchmarkMode
import org.scalatest.Spec
import scala.tools.sbs.common.JVMInvokerFactory
import scala.tools.sbs.common.ObjectHarness
import scala.tools.nsc.util.ClassPath

class JVMInvokerSpec extends Spec {

  object DummyBenchmark extends Benchmark {
    def name = "Dummy"
    def arguments = List("$1", "$2")
    def classpathURLs = List(testDir.toURL)
    def sampleNumber = 0
    def createLog(mode: BenchmarkMode) = testLog
    def init() = ()
    def run() = ()
    def reset() = ()
    def context = ClassLoader.getSystemClassLoader
    def toXML: scala.xml.Elem = <DummyBenchmark/>
  }

  object DummyHarness extends ObjectHarness {

    def main(args: Array[String]) {}

  }

  val invoker = JVMInvokerFactory(testLog, testConfig)

  describe("A JVMInvoker") {

    it("should create precise OS java arguments which intended to launch a harness") {
      expect(Seq(
        "-cp",
        testConfig.scalaLib,
        testConfig.javaProp,
        "scala.tools.nsc.MainGenericRunner",
        "-cp",
        ClassPath.fromURLs(testConfig.classpathURLs ++ DummyBenchmark.classpathURLs: _*),
        DummyHarness.getClass.getName.replace("$", ""),
        scala.xml.Utility.trim(DummyBenchmark.toXML).toString) ++ testConfig.args)(
        invoker.asJavaArgument(DummyHarness, DummyBenchmark, testConfig.classpathURLs ++ DummyBenchmark.classpathURLs))
    }

    it("should create precise OS java arguments which intended to launch a snippet benchmark") {
      expect(Seq(
        "-cp",
        testConfig.scalaLib,
        testConfig.javaProp,
        "scala.tools.nsc.MainGenericRunner",
        "-cp",
        ClassPath.fromURLs(testConfig.classpathURLs ++ DummyBenchmark.classpathURLs: _*),
        DummyBenchmark.name) ++ DummyBenchmark.arguments)(
        invoker.asJavaArgument(DummyBenchmark, testConfig.classpathURLs ++ DummyBenchmark.classpathURLs))
    }

  }

}
