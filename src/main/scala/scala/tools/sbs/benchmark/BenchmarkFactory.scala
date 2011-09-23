/*
 * BenchmarkFactory
 * 
 * Version
 * 
 * Created on September 17th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package benchmark

import java.net.URL

import scala.tools.nsc.io.Directory
import scala.tools.sbs.benchmark.BenchmarkMode.BenchmarkMode
import scala.tools.sbs.util.Config
import scala.tools.sbs.util.Log

class BenchmarkFactory(log: Log,
                       config: Config,
                       name: String,
                       arguments: List[String],
                       classpath: List[URL],
                       modes: List[BenchmarkMode]) {

  def create(directory: Directory): Benchmark = {
    new SnippetBenchmark(name, arguments, modes, classpath, directory, log, config)
  }

}