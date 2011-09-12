/*
 * Report
 * 
 * Version
 * 
 * Created September 5th, 2011
 * 
 * Created by ND P
 */

package ndp.scala.tools.sbs
package util

import java.text.SimpleDateFormat
import java.util.Date

import scala.collection.mutable.ArrayBuffer

class Report {

  def apply(result: Boolean, due: String) {
    val date = new SimpleDateFormat("MM/dd/yyyy 'at' HH:mm:ss").format(new Date)
    if (result) {
      log.info("[Test: " + date + " Main class: " + benchmark.name + "]    ----------------------------------    [  OK  ]")
    } else {
      log.info("[Test: " + date + " Main class: " + benchmark.name + "]    ----------------------------------    [FAILED]")
      log.info("Due to:")
      log.info(due)
    }
  }

}

object Report {

  def endl = System getProperty "line.separator"

  def dueToFTest(means: ArrayBuffer[Double]) = "New approach:    ---------------    " + means.remove(0) + endl +
    "Others:" + endl + means.foldLeft("") { (others, m) => others + "    ---------------    " + m + endl }

  def dueToCITest(left: Double, right: Double) =
    "Confidence Interval: [" + left.formatted("%.2f") + "; " + right.formatted("%.2f") + "] does not contains 0."

  def dueToException(e: Exception) = e.toString() + (System getProperty "line.separator") + e.getStackTraceString

  def dueToReason(s: String) = s

} 