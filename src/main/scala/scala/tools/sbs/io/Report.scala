/* 
 * Version
 * 
 * Created September 5th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package io

trait Report {

  def apply(pack: ResultPack)

}

object ReportFactory {

  def apply(config: Config): Report = {
    new TextFileReport(config)
  }

}
