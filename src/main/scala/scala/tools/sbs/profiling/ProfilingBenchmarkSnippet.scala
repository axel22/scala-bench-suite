/*
 * ProfilingBenchmarkSnippet
 * 
 * Version 
 * 
 * Created on October 31st, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package profiling

import java.lang.reflect.Method
import java.net.URL

import scala.collection.immutable.List
import scala.tools.nsc.io.Path
import scala.tools.sbs.benchmark.SnippetBenchmark

case class ProfilingBenchmarkSnippet(override val name: String,
                                     override val arguments: List[String],
                                     override val classpathURLs: List[URL],
                                     override val src: Path,
                                     override val sampleNumber: Int,
                                     profileClasses: List[String],
                                     profileExclude: List[String],
                                     profileMethod: String,
                                     profileField: String,
                                     protected val method: Method,
                                     override val context: ClassLoader,
                                     protected val config: Config)
  extends SnippetBenchmark(
    name,
    arguments,
    classpathURLs,
    src,
    sampleNumber,
    method,
    context,
    config)
  with ProfilingBenchmark
