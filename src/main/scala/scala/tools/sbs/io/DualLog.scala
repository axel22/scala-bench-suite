/*
 * DualLog
 * 
 * Version
 * 
 * Created on November 27th, 2011
 * 
 * Created by ND P
 */
package scala.tools.sbs
package io

/** Logging out to text file and console together.
 */
case class DualLog(textLog: TextFileLog, config: Config) extends Log {

  def apply(message: String): Unit = {
    textLog(message)
    UI(message)
  }

}
