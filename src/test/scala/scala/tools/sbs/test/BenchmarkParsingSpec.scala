package scala.tools.sbs
package test

import scala.tools.nsc.io.Path.string2path
import scala.tools.nsc.io.Directory
import scala.tools.sbs.common.Profiling
import scala.tools.sbs.common.SteadyState

import org.scalatest.Spec

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

      val conf = new Config(args)

      expect(true)(conf.isHelp)
      expect(Directory("D:/University/5thYear/Internship/Working/benchmark").toCanonical.path)(conf.benchmarkDirectory.path)
      expect(Directory("D:/University/5thYear/Internship/Working/benchmark/bin").toCanonical.path)(conf.bin.path)
      expect(11)(conf.runs)
      expect(31)(conf.multiplier)
      expect(true)(conf.shouldCompile)
      expect(List(Profiling(), SteadyState()))(conf.modes)
    }

  }

}
