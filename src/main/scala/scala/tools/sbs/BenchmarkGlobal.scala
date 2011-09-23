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

import scala.tools.nsc.Global
import scala.tools.nsc.Settings
import scala.tools.nsc.io.File
import scala.tools.sbs.util.Config
import scala.tools.sbs.util.Log
import scala.tools.sbs.benchmark.Benchmark

class BenchmarkGlobal(log: Log, config: Config) extends BenchmarkCompiler {

  /** Uses strange named compiler Global to compile.
   */
  def compile(benchmark: Benchmark): Boolean = {
    log.verbose("[Compile] " + benchmark.name)

    def isScala(file: File) = file.hasExtension("scala")
    def colon = System getProperty "path.separator"

    val srcFiles: List[File] =
      if (benchmark.src.isFile) List(benchmark.src.toFile)
      else benchmark.src.toDirectory.deepFiles.filter(isScala(_)).foldLeft(List[File]())((fs, f) => f :: fs)
    val bin = config.benchmarkDirectory / "bin" createDirectory ()
    val settings = new Settings(log.error)
    val (ok, errArgs) = settings.processArguments(
      List("-classpath", benchmark.classpathURLs map (_.toString) mkString colon),
      false)
    settings.outdir.value = bin.path

    if (ok) {
      val compiler = new Global(settings)
      (new compiler.Run) compile (srcFiles map (_.path))
    } else {
      errArgs map (err => log.error(err))
    }
    ok
  }

}
