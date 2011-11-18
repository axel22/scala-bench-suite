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

import scala.collection.mutable.HashMap
import scala.io.Source
import scala.tools.nsc.io.Path
import scala.tools.sbs.common.BenchmarkCompiler
import scala.tools.sbs.io.UI

/** Holds the information of benchmarks before compiling.
 */
case class BenchmarkInfo(name: String,
                         src: Path,
                         arguments: List[String],
                         classpathURLs: List[URL],
                         sampleNumber: Int,
                         timeout: Int,
                         shouldCompile: Boolean) {

  def isCompiledOK(compiler: BenchmarkCompiler, config: Config): Boolean =
    if (shouldCompile && !(compiler compile this)) {
      UI.error("Compile failed: " + this.name + " src: " + src.path)
      false
    }
    else {
      true
    }

  def expand(benchmarkFactory: BenchmarkFactory, config: Config): Benchmark = benchmarkFactory createFrom this

}

object BenchmarkInfo {

  def readInfo(src: Path, options: List[String]): HashMap[String, String] = {
    val argFile =
      if (src isFile) (src.path stripSuffix "scala") + "arg"
      else src.path + ".arg"
    val map = HashMap[String, String]()
    try {
      val argBuffer = Source.fromFile(argFile)
      for (line <- argBuffer.getLines; option <- options)
        if (line startsWith option) map put (option, (line split " ")(1))
      argBuffer close
    }
    catch { case e => UI.debug("[Read failed] " + argFile + "\n" + e.toString) }
    map
  }

}
