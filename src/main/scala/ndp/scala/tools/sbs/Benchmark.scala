package ndp.scala.tools.sbs

import java.io.{File => JFile}

import scala.collection.mutable.ArrayBuffer
import scala.tools.nsc.io.Directory
import scala.tools.nsc.io.File
import scala.tools.nsc.io.Path
import scala.tools.nsc.Global
import scala.tools.nsc.Settings

import ndp.scala.tools.sbs.util.Constant

case class Benchmark(
  name: String,
  arguments: ArrayBuffer[String],
  srcPath: Path,
  buildPath: Path) {

  def this(args: Array[String]) {
    this(
      args(Constant.INDEX_CLASSNAME),
      {
        val arr = new ArrayBuffer[String]
        for (arg <- args(Constant.INDEX_BENCHMARK_ARG) split " ") {
          arr += arg
        }
        arr
      },
      new File(new JFile(args(Constant.INDEX_SRCPATH))),
      new Directory(new JFile(args(Constant.INDEX_BENCHMARK_BUILD)))
    )
  }

  /**
   * Uses strange named compiler Global to compile.
   */
  def compile(): Boolean = {
    log.verbose("[Compile]")

    val settings = new Settings(log.error)
    val (ok, errArgs) = settings.processArguments(
      List(
        "-classpath", config.classpath,
        "-d", buildPath.path,
        srcPath.path),
      true)

    if (ok) {
      val compiler = new Global(settings)
      (new compiler.Run) compile List(srcPath.path)
    } else {
      errArgs map (err => log.error(err))
    }
    ok
  }

}
