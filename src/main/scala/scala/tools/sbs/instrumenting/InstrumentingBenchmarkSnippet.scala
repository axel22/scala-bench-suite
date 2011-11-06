/*
 * InstrumentingBenchmarkSnippet
 * 
 * Version 
 * 
 * Created on November 6th, 2011
 * 
 * Created by PDB
 */

package scala.tools.sbs
package instrumenting

import java.lang.reflect.Method
import java.net.URL

import scala.collection.immutable.List
import scala.tools.nsc.io.Path
import scala.tools.sbs.benchmark.SnippetBenchmark

case class InstrumentingBenchmarkSnippet(override val name: String,
                                     override val arguments: List[String],
                                     override val classpathURLs: List[URL],
                                     override val src: Path,
                                     override val sampleNumber: Int,
                                     instrumentMethods: List[String],
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
  with InstrumentingBenchmark
