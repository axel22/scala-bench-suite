package scala.tools.sbs.common

import java.lang.Thread.sleep
import java.net.URL

trait BenchmarkTemplate {

  val runs = 1

  val multiplier = 2

  val sampleNumber = 0

  def init = sleep(100)

  def run = sleep(100)

  def reset = ()

}
