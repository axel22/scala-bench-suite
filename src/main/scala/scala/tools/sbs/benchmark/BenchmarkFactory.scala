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
import scala.tools.sbs.io.Log
import scala.tools.nsc.io.Path

object BenchmarkFactory {

  def apply(src: Path, arguments: List[String], classpathURLs: List[URL], log: Log, config: Config): Benchmark =
    new SnippetBenchmark(src, arguments, classpathURLs, log, config)

}