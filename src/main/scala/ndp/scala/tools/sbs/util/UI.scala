/*
 * UI
 * 
 * Version
 * 
 * Created on September 5st, 2011
 * 
 * Created by ND P
 */

package ndp.scala.tools.sbs
package util

/**
 * User command line interface.
 */
object UI {

  def apply(x: Any) = Console println x
  
  def print(x: Any) = Console println x

  def error(x: Any) = Console println "[Error] " + x

  def printUsage() {
    this print "Usage: BenchmarkSuite --srcpath <scala source file> --runs <runs> [Options] <MainClassName>"
    this print "	Options: [--multiplier <multiplier>] [--noncompile] [--classdir <classdir>] [--help]"
    this print "	The benchmark runs <runs> times, forcing a garbage collection between runs."
    this print "	The optional -multiplier causes the benchmark to be repeated <multiplier> times, each time for <runs> executions."
    this print "	The optional -noncompile causes the benchmark not to be recompiled."
    this print "	The optional -classdir causes the generated class files to be placed at <classdir>"
    this print "	The optional -help prints this usage."
  }

}