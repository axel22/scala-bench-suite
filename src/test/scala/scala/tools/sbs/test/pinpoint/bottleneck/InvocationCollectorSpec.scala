package scala.tools.sbs.test
package pinpoint
package bottleneck

import scala.tools.nsc.io.Path.string2path
import scala.tools.sbs.benchmark.BenchmarkInfo
import scala.tools.sbs.common.BenchmarkCompilerFactory
import scala.tools.sbs.pinpoint.bottleneck.InvocationCollector
import scala.tools.sbs.pinpoint.PinpointBenchmark
import scala.tools.sbs.pinpoint.PinpointBenchmarkFactory
import scala.tools.sbs.util.FileUtil

import org.scalatest.Spec

class InvocationCollectorSpec extends Spec {

  val benchmarkContent = """
	class A extends scala.tools.sbs.pinpoint.PinpointBenchmarkTemplate {
	
	  def init() = ()
	
	  def run() = rec(true)
    
      def rec(dis: Boolean): Unit =
        if (dis) rec(false)
        else {
          foo
          bar
          baz
        }
    
      def foo = ()
    
      def bar = ()
    
      def baz = ()
	
	  def reset() = ()
    
	}"""

  val className = "A"

  val methodName = "run"

  val invoCollectorDir = testDir / "InvocationCollectorSpec" createDirectory ()

  val invoCollectorFile = invoCollectorDir / "A.scala" toFile

  val instrumentedOut = invoCollectorDir / "instrumented" createDirectory ()

  val backupPlace = invoCollectorDir / "backup" createDirectory ()

  def define(content: String) = {
    invoCollectorFile.deleteIfExists
    FileUtil createFile invoCollectorFile.path
    FileUtil.write(invoCollectorFile.path, content)
  }

  val benchmarkInfo = new BenchmarkInfo("A", invoCollectorFile, Nil, Nil, 0, -1, true)

  val benchmark = ({
    define(benchmarkContent)
    val compiler = BenchmarkCompilerFactory(testLog, testConfig)
    benchmarkInfo.isCompiledOK(compiler, testConfig)
    benchmarkInfo.expand(new PinpointBenchmarkFactory(testLog, testConfig), testConfig)
  }).asInstanceOf[PinpointBenchmark]

  describe("InvocationCollector") {

    it("abc xyz") {
      val collector = new InvocationCollector(
        testConfig,
        testLog,
        benchmark,
        className,
        methodName,
        instrumentedOut,
        backupPlace)
      collector.graph traverse (m => println(m prototype))
    }

  }

}
