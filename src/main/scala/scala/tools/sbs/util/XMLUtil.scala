/*
 * XMLUtil
 * 
 * Version
 * 
 * Created on September 25th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package util

import scala.tools.nsc.io.Path.string2path
import scala.tools.nsc.io.Directory
import scala.tools.nsc.io.Path
import scala.tools.sbs.io.LogLevel
import scala.xml.Elem

object XMLUtil {

  def XMLToConfig(xml: Elem): Config = xml match {
    case <Config>
           <directory>{ directory }</directory>
           <modes>{ modesXML@_* }</modes>
           <scalahome>{ scalahome }</scalahome>
           <javahome>{ javahome }</javahome>
           <showLog>{ showLog }</showLog>
           <logLevel>{ logLevelStr }</logLevel>
         </Config> => {
      val modes = for (modeXML <- modesXML) yield if (modeXML.text equals BenchmarkMode.MEMORY.toString) {
        BenchmarkMode.MEMORY
      } else {
        BenchmarkMode.STEADY
      }
      val logLevel = logLevelStr match {
        case _ if (logLevelStr equals LogLevel.ALL.toString) => LogLevel.ALL
        case _ if (logLevelStr equals LogLevel.VERBOSE.toString) => LogLevel.VERBOSE
        case _ if (logLevelStr equals LogLevel.DEBUG.toString) => LogLevel.DEBUG
        case _ => LogLevel.INFO
      }
      new Config(
        Directory(directory.text),
        modes.toList,
        Directory(scalahome.text),
        Directory(javahome.text),
        showLog.text.toBoolean,
        logLevel)
    }
    case _ => null
  }

  def XMLToBenchmark(xml: Elem, config: Config): Benchmark = xml match {
    case <Benchmark>
           <src>{ src }</src>
           <arguments>{ args@_* }</arguments>
           <classpath>{ classpath@_* }</classpath>
           <runs>{ runs }</runs>
           <multiplier>{ multiplier }</multiplier>
           <sampleNumber>{ sampleNumber }</sampleNumber>
           <shouldCompile>{ shouldCompile }</shouldCompile>
         </Benchmark> => {
      val arguments = for (arg <- args) yield arg.text
      val classpathURLs = for (cp <- classpath) yield Path(cp.text).toURL
      BenchmarkFactory(
        Path(src.text),
        arguments.toList,
        classpathURLs.toList,
        runs.text.toInt,
        multiplier.text.toInt,
        sampleNumber.text.toInt,
        shouldCompile.text.toBoolean,
        config)
    }
    case _ => null
  }

}
