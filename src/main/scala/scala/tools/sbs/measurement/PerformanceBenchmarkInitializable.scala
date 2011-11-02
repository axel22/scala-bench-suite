/*
 * PerformanceBenchmarkInitializable
 * 
 * Version 
 * 
 * Created on October 31st, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package measurement

import java.net.URL

import scala.tools.nsc.io.Path
import scala.tools.sbs.benchmark.BenchmarkTemplate
import scala.tools.sbs.benchmark.InitializableBenchmark

class PerformanceBenchmarkInitializable(name: String,
                                        classpathURLs: List[URL],
                                        src: Path,
                                        benchmarkObject: PerformanceBenchmarkTemplate,
                                        context: ClassLoader,
                                        config: Config)
  extends InitializableBenchmark(
    name,
    classpathURLs,
    src,
    benchmarkObject,
    context,
    config)
  with PerformanceBenchmark {

  final val runs = 1

  val multiplier = benchmarkObject.multiplier

}
