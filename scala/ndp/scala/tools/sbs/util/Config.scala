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

import java.io.{ File => JFile }

import scala.collection.mutable.ArrayBuffer
import scala.tools.nsc.io.Directory
import scala.tools.nsc.io.File

case class Config(CLASSNAME: String,
                  SRCPATH: File,
                  BENCHMARK_DIR: Directory,
                  BENCHMARK_BUILD: Directory,
                  BENCHMARK_TYPE: BenchmarkType.Value,
                  RUNS: Int,
                  MULTIPLIER: Int,
                  SCALA_HOME: Directory,
                  JAVA_HOME: Directory,
                  CLASSPATH: String,
                  FILE_SEPARATOR: String,
                  PERSISTOR_LOC: Directory,
                  COMPILE: Boolean,
                  LOG_FILE: File,
                  LOG_LEVEL: LogLevel.Value,
                  SHOW_LOG: Boolean) {

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
      args(Constant.INDEX_FILE_SEPARATOR),
      new Directory(new JFile(args(Constant.INDEX_PERSISTOR_LOC))),
      args(Constant.INDEX_COMPILE).toBoolean,
      new File(new JFile(args(Constant.INDEX_LOG_FILE))),
      if (args(Constant.INDEX_LOG_LEVEL) equals "debug") {
        LogLevel.DEBUG
      } else if (args(6) equals "verbose") {
        LogLevel.VERBOSE
      } else {
        LogLevel.INFO
      },
      args(Constant.INDEX_SHOW_LOG).toBoolean
    )
  }

  lazy val JAVACMD = JAVA_HOME + FILE_SEPARATOR + "bin" + FILE_SEPARATOR + "java"
  lazy val JAVAPROP = "-Dscala.home=" + SCALA_HOME
  lazy val SCALA_LIB: String = {
    var lib: List[String] = List()
    val libpath = (SCALA_HOME / "lib").createDirectory()
    for (file <- libpath.files) {
      lib ::= file.path
    }
    lib mkString (System getProperty "path.separator")
  }

  def toArgument(): String = {
    var arr = new ArrayBuffer[String](Constant.MARX_ARGUMENT)

    arr(Constant.INDEX_CLASSNAME) = CLASSNAME
    arr(Constant.INDEX_SRCPATH) = SRCPATH.path
    arr(Constant.INDEX_BENCHMARK_DIR) = BENCHMARK_DIR.path
    arr(Constant.INDEX_BENCHMARK_BUILD) = BENCHMARK_BUILD.path
    arr(Constant.INDEX_BENCHMARK_TYPE) = BENCHMARK_TYPE.toString
    arr(Constant.INDEX_RUNS) = RUNS.toString
    arr(Constant.INDEX_MULTIPLIER) = MULTIPLIER.toString
    arr(Constant.INDEX_SCALA_HOME) = SCALA_HOME.path
    arr(Constant.INDEX_JAVA_HOME) = JAVA_HOME.path
    arr(Constant.INDEX_CLASSPATH) = CLASSPATH
    arr(Constant.INDEX_FILE_SEPARATOR) = FILE_SEPARATOR
    arr(Constant.INDEX_PERSISTOR_LOC) = PERSISTOR_LOC.path
    arr(Constant.INDEX_COMPILE) = COMPILE.toString
    arr(Constant.INDEX_LOG_FILE) = LOG_FILE.path
    arr(Constant.INDEX_LOG_LEVEL) = LOG_LEVEL.toString
    arr(Constant.INDEX_SHOW_LOG) = SHOW_LOG.toString

    arr mkString " "
  }
  override def toString(): String = {
    "Config:" +
      "\n        Source:          " + SRCPATH.path +
      "\n        Classname:       " + CLASSNAME +
      "\n        BenchmarkDir:    " + BENCHMARK_DIR.path +
      "\n        Scala home:      " + SCALA_HOME.path +
      "\n        Java home:       " + JAVA_HOME.path +
      "\n        FileSeparator:   " + FILE_SEPARATOR +
      "\n        Runs:            " + RUNS +
      "\n        Multiplier:      " + MULTIPLIER +
      "\n        Previous result: " + PERSISTOR_LOC.path +
      "\n        BenchmarkType:   " + BENCHMARK_TYPE +
      "\n        Compile:         " + COMPILE +
      "\n        Log level:       " + LOG_LEVEL +
      "\n        Show log:        " + SHOW_LOG +
      "\n        Java:            " + JAVACMD +
      "\n        Java properties: " + JAVAPROP +
      "\n        Scala library:   " + SCALA_LIB +
      "\n"
  }

}

object BenchmarkType extends Enumeration {
  type BenchmarType = Value
  val STARTUP, STEADY, MEMORY = Value
}
