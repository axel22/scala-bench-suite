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
package performance

import java.net.URL

import scala.tools.nsc.io.Path
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

  val multiplier = 1

  val measurement = benchmarkObject.measurement

}
