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
import scala.tools.sbs.io.LogLevel.LogLevel

import BenchmarkMode.BenchmarkMode

case class Config(benchmarkDirectory: Directory,
                  modes: List[BenchmarkMode],
                  scalahome: Directory,
                  javahome: Directory,
                  showLog: Boolean,
                  logLevel: LogLevel) {

  def bin: Directory

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

  override def toString(): String = {
    val endl = System getProperty "line.separator"
    "Config:" +
      endl + "        BenchmarkDir:    " + benchmarkDirectory.path +
      endl + "        Scala home:      " + scalahome.path +
      endl + "        Java home:       " + javahome.path +
      endl + "        Java:            " + JAVACMD +
      endl + "        Java properties: " + JAVAPROP +
      endl + "        Scala library:   " + SCALALIB
  }

  def toXML =
    <Config>
      <directory>{ benchmarkDirectory.path }</directory>
      <modes>{ for (mode <- modes) yield <mode>{ mode.toString } </mode> }</modes>
      <scalahome>{ scalahome.path }</scalahome>
      <javahome>{ javahome.path }</javahome>
      <showLog>{ showLog }</showLog>
      <logLevel>{ logLevel }</logLevel>
    </Config>

}
