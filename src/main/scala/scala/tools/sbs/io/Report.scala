/* 
 * Version
 * 
 * Created September 5th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package io

/** Trait for reporting a sbs' running.
 */
trait Report {

  /** Takes a list of benchmarking result
   *  in form of a {@link ResultPack} to produces report.
   */
  def apply(pack: ResultPack)

}

/** Factory object for {@link Report}.
 */
object ReportFactory {

  def apply(config: Config): Report = {
    new TextFileReport(config)
  }

}
