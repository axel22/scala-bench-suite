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

  def apply(log: Log, config: Config, result: Boolean, due: String) {
    val date = new SimpleDateFormat("MM/dd/yyyy 'at' HH:mm:ss").format(new Date)
    if (result) {
      log("[Test: " + date + "\tMain class: " + config.CLASSNAME + "]\t----------------------------------\t[  OK  ]")
    } else {
      log("[Test: " + date + "\tMain class: " + config.CLASSNAME + "]\t----------------------------------\t[FAILED]")
      log("Due to:")
      log(due)
    }
  }

}

object Report {

  def dueToRegression(means: ArrayBuffer[Double]): String = {
    "New approach:\t--------------------------------\t" + (means remove 0) + "\n" +
      "Others:" +
      {
        var others: String = ""
        for (d <- means) {
          others += "\t\t\t\t\t\t\t" + d + "\n"
        }
        others
      }
  }

  def dueToException(e: Exception): String = {
    e.getStackTraceString
  }

}