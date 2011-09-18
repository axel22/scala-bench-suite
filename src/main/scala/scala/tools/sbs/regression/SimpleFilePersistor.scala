/*
 * SimpleFilePersistor
 * 
 * Version
 * 
 * Created on September 18th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package regression

import scala.tools.nsc.io.Directory

trait SimpleFilePersistor {

  def location(): Directory

}