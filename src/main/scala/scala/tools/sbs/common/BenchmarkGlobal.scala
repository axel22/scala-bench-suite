/*
 * BenchmarkGlobal
 * 
 * Version
 * 
 * Created on September 24th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package common

import scala.tools.nsc.io.File
import scala.tools.nsc.Global
import scala.tools.nsc.Settings
import scala.tools.sbs.benchmark.BenchmarkInfo
import scala.tools.sbs.io.Log
import scala.tools.sbs.util.Constant.COLON
import scala.tools.sbs.Config

/** An implement of {@link BenchmarkCompiler}.
 */
class BenchmarkGlobal(log: Log, config: Config) extends BenchmarkCompiler {

  /** Uses strange named compiler `Global` to compile.
   */
  def compile(benchmarkInfo: BenchmarkInfo): Boolean = {
    log.verbose("[Compile] " + benchmarkInfo.name)

    def isScala(file: File) = file.hasExtension("scala")

    val srcFiles: List[File] =
      if (benchmarkInfo.src.isFile) List(benchmarkInfo.src.toFile)
      else benchmarkInfo.src.toDirectory.deepFiles.filter(isScala).foldLeft(List[File]())((fs, f) => f :: fs)

    log.debug(srcFiles.toString)

    val settings = new Settings(log.error)
    val (ok, errArgs) =
      settings.processArguments(
        List(
          "-classpath",
          (config.classpathURLs map (_.getPath.toString) mkString COLON) +
            (benchmarkInfo.classpathURLs map (_.getPath.toString) mkString COLON)),
        false)
    settings.outdir.value = config.bin.path

    log.debug(settings.toString)

    if (ok) {
      val compiler = new Global(settings)
      new compiler.Run compile (srcFiles map (_.path))
      !compiler.reporter.hasErrors
    } else {
      errArgs map (err => log.error(err))
      false
    }
  }

}
