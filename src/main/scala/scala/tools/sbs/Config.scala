/*
 * Config
 * 
 * Version
 * 
 * Created September 5th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs

import java.lang.System

import scala.tools.nsc.io.Path.string2path
import scala.tools.nsc.io.Directory
import BenchmarkMode.BenchmarkMode
import scala.tools.sbs.util.Constant

case class Config(benchmarkDirectory: Directory,
                  runs: Int,
                  multiplier: Int,
                  sampleNumber: Int,
                  scalahome: Directory,
                  javahome: Directory,
                  compile: Boolean) {

  def bin: Directory
  def modes: List[BenchmarkMode]

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

  def toArgument(): Array[String] = {
    var arr = new Array[String](Constant.MAX_ARGUMENT_CONFIG)

    //    arr(Constant.INDEX_CLASSNAME) = classname
    //    arr(Constant.INDEX_BENCHMARK_ARG) = benchmarkArguments mkString " "
    //    arr(Constant.INDEX_SRCPATH) = srcpath.path
    arr(Constant.INDEX_BENCHMARK_DIR) = benchmarkDirectory.path
    //    arr(Constant.INDEX_BENCHMARK_BUILD) = benchmarkBuild.path
    arr(Constant.INDEX_RUNS) = runs.toString
    arr(Constant.INDEX_MULTIPLIER) = multiplier.toString
    arr(Constant.INDEX_SCALA_HOME) = scalahome.path
    arr(Constant.INDEX_JAVA_HOME) = javahome.path
    arr(Constant.INDEX_SAMPLE_NUMBER) = sampleNumber.toString
    arr(Constant.INDEX_COMPILE) = compile.toString

    arr
  }

  override def toString(): String = {
    val endl = System getProperty "line.separator"
    "Config:" +
      endl + "        BenchmarkDir:    " + benchmarkDirectory.path +
      endl + "        Scala home:      " + scalahome.path +
      endl + "        Java home:       " + javahome.path +
      endl + "        Runs:            " + runs +
      endl + "        Multiplier:      " + multiplier +
      endl + "        Sample number:   " + sampleNumber +
      endl + "        Compile:         " + compile +
      endl + "        Java:            " + JAVACMD +
      endl + "        Java properties: " + JAVAPROP +
      endl + "        Scala library:   " + SCALALIB
  }

}
