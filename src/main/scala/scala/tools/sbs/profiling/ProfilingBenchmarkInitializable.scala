/*
 * ProfilingBenchmarkInitializable
 * 
 * Version 
 * 
 * Created on October 31st, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package profiling

import java.net.URL

import scala.collection.immutable.List
import scala.tools.nsc.io.Path
import scala.tools.sbs.benchmark.InitializableBenchmark

class ProfilingBenchmarkInitializable(name: String,
                                      classpathURLs: List[URL],
                                      src: Path,
                                      benchmarkObject: ProfilingBenchmarkTemplate,
                                      context: ClassLoader,
                                      config: Config)
  extends InitializableBenchmark(
    name,
    classpathURLs,
    src,
    benchmarkObject,
    context,
    config)
  with ProfilingBenchmark {

  override val profileClasses = benchmarkObject.profileClasses

  override val profileExclude = benchmarkObject.profileExclude

  override val profileMethod = benchmarkObject.profileMethod

  override val profileField = benchmarkObject.profileField

}
