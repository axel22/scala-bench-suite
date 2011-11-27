/*
 * Configured
 * 
 * Version
 * 
 * Created by ND P
 * 
 * Created on Novemeber 27th, 2011
 */

package scala.tools.sbs

import scala.tools.sbs.io.Log

/** Extended by other classes for convenience referring
 *  `Log` and `Config` in concrete method definitions.
 */
trait Configured {

  def config: Config

  def log: Log

}
