/*
 * UI
 * 
 * Version
 * 
 * Created on September 5st, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package util

/**
 * User command line interface.
 */
object UI extends Log {

  def logShow = false
  
  def logLevel = LogLevel.ALL
  
  def apply(message: String) = Console println message
  
  def print(message: String) = apply(message)
  
  def printUsage() {
    this print "Usage: sbs <options> <benchmark class> <benchmark arguments>"
    this print "	Options: [--multiplier <multiplier>] [--noncompile] [--classdir <classdir>] [--help]"
    this print "	The benchmark runs <runs> times, forcing a garbage collection between runs."
    this print "	The optional -multiplier causes the benchmark to be repeated <multiplier> times, each time for <runs> executions."
    this print "	The optional -noncompile causes the benchmark not to be recompiled."
    this print "	The optional -classdir causes the generated class files to be placed at <classdir>"
    this print "	The optional -help prints this usage."
  }

}
