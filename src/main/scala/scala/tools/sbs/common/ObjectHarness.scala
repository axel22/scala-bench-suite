/*
 * ObjectHarness
 * 
 * Version
 * 
 * Created on October 26th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package common

/** Harness to run benchmark in a separated java process.
 *  It should be an `object` extends this trait and implements
 *  the `main` function as the entry point for new process.
 */
trait ObjectHarness {

  def main(args: Array[String]): Unit

  /** Reports the measurement result to the main process.
   */
  def reportResult(result: RunResult) = println(scala.xml.Utility trim result.toXML)

}
