/*
 * BenchmarkMode
 * 
 * Version
 * 
 * Created on September 17th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs

object BenchmarkMode extends Enumeration {
  type BenchmarkMode = Value
  val STARTUP, STEADY, MEMORY = Value
}
