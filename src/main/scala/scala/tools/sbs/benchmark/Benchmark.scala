/*
 * Benchmark
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

import scala.tools.nsc.io.Path.string2path
import scala.tools.nsc.io.File
import scala.tools.nsc.io.Path

trait Benchmark {

  lazy val name: String = src.stripExtension

  /** Path to the benchmark source file / directory.
   */
  def src: Path
  
  /** Arguments of the benchmark.
   */
  def arguments: List[String] = Nil
  
  /** Classpath of the benchmark.
   */
  def classpathURLs: List[URL] = Nil

  /** Logging file for each benchmark's running.
   */
  def logFile =
    if (src.isFile) {
      File(src.path stripSuffix src.extension) addExtension "log"
    } else {
      File(src.path) addExtension "log"
    } createFile ()

  /** Sets the running context and load benchmark classes.
   */
  def init()

  /** Runs the benchmark object and throws Exceptions (if any).
   */
  def run()

  /** Resets the context.
   */
  def reset()

  /** Creates the process command for start up benchmarking.
   */
  def initCommand(): Boolean

  /** Runs the benchmark process.
   */
  def runCommand()

}
