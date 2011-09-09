package ndp.scala.tools.sbs
package regression

import ndp.scala.tools.sbs.measurement.SteadyHarness

object SampleGenerator {
  
  /**
   * Generates sample results.
   */
  def generate() {
    var i = 0
    while (i < config.sampleNumber) {
      SteadyHarness.run() match {
        case Left(ret) => {
          ret.store() match {
            case Some(_) => {
              log.debug("--Stored--")
              i += 1
              log.verbose("--Got " + i + " sample(s)--")
            }
            case _ => {
              log.debug("--Cannot store--")
            }
          }
        }
        case Right(s) => {
          log.debug("--At " + getClass().getName() + ": " + s + "--")
        }
      }
    }
  }

}