package scala.tools.sbs
package test

import org.scalatest.Spec
import scala.tools.cmd.Instance

class BenchmarkParsingSpec extends Spec {

  describe("Argument specification of sbs") {

    it("recieves and parses arguments") {
      val args = Array(
        "--help",
        "--benchmarkdir",
        "D:/University/5thYear/Internship/Working/benchmark",
        "--runs",
        "11",
        "--profiler",
        "--multiplier",
        "31",
        "--scala-library",
        "D:\\University\\5thYear\\Internship\\Working\\scala-2.9.1.final\\bin\\scala-library.jar",
        "--steady-performance",
        "--show-log",
        "ndp.scala.tools.sbs.benchmark.WhileIterator")

      val SomeSpec = new Config(args)

      expect(true)(SomeSpec.isHelp)
      expect("D:/University/5thYear/Internship/Working/benchmark")(SomeSpec.benchmarkDirPath)
      expect("D:/University/5thYear/Internship/Working/benchmark/bin")(SomeSpec.binDirPath)
      expect("D:\\University\\5thYear\\Internship\\Working\\scala-2.9.1.final")(SomeSpec.javaProp)
      expect(11)(SomeSpec.runs)
      expect(31)(SomeSpec.multiplier)
      expect(true)(SomeSpec.shouldCompile)
      expect(List(BenchmarkMode.PROFILE, BenchmarkMode.STEADY))(SomeSpec.modes)
    }

  }

}
