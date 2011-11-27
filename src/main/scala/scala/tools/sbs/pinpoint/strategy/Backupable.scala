package scala.tools.sbs
package pinpoint
package strategy

import scala.tools.nsc.io.Directory

trait Backupable {

  def backupPlace: Directory

}
