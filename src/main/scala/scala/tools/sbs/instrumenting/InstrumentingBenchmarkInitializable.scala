/*
 * InstrumentingBenchmarkInitializable
 * 
 * Version 
 * 
 * Created on November 6th, 2011
 * 
 * Created by PDB
 */

package scala.tools.sbs
package instrumenting

import java.net.URL

import scala.collection.immutable.List
import scala.tools.nsc.io.Path
import scala.tools.sbs.benchmark.InitializableBenchmark

class InstrumentingBenchmarkInitializable(name: String,
                                      classpathURLs: List[URL],
                                      src: Path,
                                      benchmarkObject: InstrumentingBenchmarkTemplate,
                                      context: ClassLoader,
                                      config: Config)
  extends InitializableBenchmark(
    name,
    classpathURLs,
    src,
    benchmarkObject,
    context,
    config)
  with InstrumentingBenchmark {

  override val instrumentMethods = benchmarkObject.instrumentMethod

}
