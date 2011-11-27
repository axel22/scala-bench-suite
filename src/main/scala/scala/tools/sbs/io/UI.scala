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
package io

/** User command line interface.
 */
object UI extends Log {

  var config: Config = null

  def apply(message: String) = if (config != null && !config.isQuiet) Console println message

  def print(message: String) = this(message)

  override def info(message: String) {
    this("[Info]     " + message)
  }

  override def error(message: String) {
    this("[Error]    " + message)
  }

}
