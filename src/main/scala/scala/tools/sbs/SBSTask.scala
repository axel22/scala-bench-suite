/*
 * SBSTask
 * 
 * Version
 * 
 * Created on November 16th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package ant

import org.apache.tools.ant.Task

class SBSTask extends Task {

  private var args: Array[String] = _
  def setArgs(input: String) {
    args = input split "  "
  }

  override def execute() = BenchmarkDriver main args

}
