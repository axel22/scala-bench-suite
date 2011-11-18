/*
 * PerformanceBenchmarkSnippet
 * 
 * Version 
 * 
 * Created on October 30th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package performance

import java.lang.reflect.Method
import java.net.URL

import scala.tools.nsc.io.Path
import scala.tools.sbs.benchmark.SnippetBenchmark

/** An implement of {@link PerformanceBenchmark} trait.
 *  `method` is the `main(args: Array[String])` method of the benchmark `object`.
 */
case class PerformanceBenchmarkSnippet(override val name: String,
                                       override val arguments: List[String],
                                       override val classpathURLs: List[URL],
                                       override val src: Path,
                                       override val sampleNumber: Int,
                                       override val timeout: Int,
                                       multiplier: Int,
                                       measurement: Int,
                                       protected val method: Method,
                                       override val context: ClassLoader,
                                       protected val config: Config)
  extends SnippetBenchmark(
    name,
    arguments,
    classpathURLs,
    src,
    sampleNumber,
    timeout,
    method,
    context,
    config)
  with PerformanceBenchmark
