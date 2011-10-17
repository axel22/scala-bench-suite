/*
 * BenchmarkInfo
 * 
 * Version
 * 
 * Created on Octber 7th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package benchmark

import java.net.URL

import scala.tools.nsc.io.Path
import scala.tools.sbs.common.BenchmarkCompiler
import scala.tools.sbs.io.UI
import scala.tools.sbs.Config

/** Holds the information of benchmarks before compiling.
 */
case class BenchmarkInfo(name: String,
                         src: Path,
                         arguments: List[String],
                         classpathURLs: List[URL],
                         runs: Int,
                         multiplier: Int,
                         sampleNumber: Int,
                         shouldCompile: Boolean,
                         profiledClasses: List[String],
                         excludeClasses: List[String],
                         profiledMethod: String,
                         profiledField: String,
                         pinpointClass: String,
                         pinpointMethod: String) {

  def expand(compiler: BenchmarkCompiler, config: Config): Benchmark =
    if (shouldCompile && !(compiler compile this)) {
      UI.error("Compile failed: " + this.name + " src: " + src.path)
      null
    }
    else {
      BenchmarkFactory(this, config)
    }

}
