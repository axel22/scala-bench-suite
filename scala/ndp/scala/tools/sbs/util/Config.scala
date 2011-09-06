/*
 * Config
 * 
 * Version
 * 
 * Created September 5th, 2011
 * 
 * Created by ND P
 */

package ndp.scala.tools.sbs
package util

import java.io.{File => JFile}

import scala.collection.mutable.ArrayBuffer
import scala.tools.nsc.io.Directory
import scala.tools.nsc.io.File

import BenchmarkType.BenchmarkType

case class Config(classname: String,
                  srcpath: File,
                  benchmarkDirectory: Directory,
                  benchmarkBuild: Directory,
                  benchmarkType: BenchmarkType,
                  runs: Int,
                  multiplier: Int,
                  scalahome: Directory,
                  javahome: Directory,
                  classpath: String,
                  persistorLocation: Directory,
                  compile: Boolean) {

  def this(args: Array[String]) {
    this(
      args(Constant.INDEX_CLASSNAME),
      new File(new JFile(args(Constant.INDEX_SRCPATH))),
      new Directory(new JFile(args(Constant.INDEX_BENCHMARK_DIR))),
      new Directory(new JFile(args(Constant.INDEX_BENCHMARK_BUILD))),
      if (args(Constant.INDEX_BENCHMARK_TYPE) equals "Startup") {
        BenchmarkType.STARTUP
      } else if (args(Constant.INDEX_BENCHMARK_TYPE) equals "Steady") {
        BenchmarkType.STEADY
      } else {
        BenchmarkType.MEMORY
      },
      args(Constant.INDEX_RUNS).toInt,
      args(Constant.INDEX_MULTIPLIER).toInt,
      new Directory(new JFile(args(Constant.INDEX_SCALA_HOME))),
      new Directory(new JFile(args(Constant.INDEX_JAVA_HOME))),
      args(Constant.INDEX_CLASSPATH),
      new Directory(new JFile(args(Constant.INDEX_PERSISTOR_LOC))),
      args(Constant.INDEX_COMPILE).toBoolean
    )
  }

  val JAVACMD = javahome + System.getProperty("file.separator") + "bin" + System.getProperty("file.separator") + "java"
  val JAVAPROP = "-Dscala.home=" + scalahome
  val SCALALIB: String = {
    var lib: List[String] = List()
    val libpath = (scalahome / "lib").createDirectory()
    for (file <- libpath.files) {
      lib ::= file.path
    }
    lib mkString (System getProperty "path.separator")
  }

  def toArgument(): String = {
    var arr = new Array[String](Constant.MAX_ARGUMENT_CONFIG)

    arr(Constant.INDEX_CLASSNAME) = classname
    arr(Constant.INDEX_SRCPATH) = srcpath.path
    arr(Constant.INDEX_BENCHMARK_DIR) = benchmarkDirectory.path
    arr(Constant.INDEX_BENCHMARK_BUILD) = benchmarkBuild.path
    arr(Constant.INDEX_BENCHMARK_TYPE) = benchmarkType.toString
    arr(Constant.INDEX_RUNS) = runs.toString
    arr(Constant.INDEX_MULTIPLIER) = multiplier.toString
    arr(Constant.INDEX_SCALA_HOME) = scalahome.path
    arr(Constant.INDEX_JAVA_HOME) = javahome.path
    arr(Constant.INDEX_CLASSPATH) = classpath
    arr(Constant.INDEX_PERSISTOR_LOC) = persistorLocation.path
    arr(Constant.INDEX_COMPILE) = compile.toString

    arr mkString " "
  }
  override def toString(): String = {
    "Config:" +
      "\n        Source:          " + srcpath.path +
      "\n        Classname:       " + classname +
      "\n        BenchmarkDir:    " + benchmarkDirectory.path +
      "\n        Scala home:      " + scalahome.path +
      "\n        Java home:       " + javahome.path +
      "\n        Runs:            " + runs +
      "\n        Multiplier:      " + multiplier +
      "\n        Previous result: " + persistorLocation.path +
      "\n        BenchmarkType:   " + benchmarkType +
      "\n        Compile:         " + compile +
      "\n        Java:            " + JAVACMD +
      "\n        Java properties: " + JAVAPROP +
      "\n        Scala library:   " + SCALALIB +
      "\n"
  }

}

object BenchmarkType extends Enumeration {
  type BenchmarkType = Value
  val STARTUP, STEADY, MEMORY = Value
}
