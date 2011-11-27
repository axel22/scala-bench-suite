/*
 * ASMInstrumenter
 * 
 * Version 
 * 
 * Created on November 6th, 2011
 * 
 * Created by PDB
 */

package scala.tools.sbs
package instrumenting

import scala.tools.sbs.io.Log

class ASMInstrumenter(val config: Config, val log: Log) extends Instrumenter {
  protected def instrument(benchmark: InstrumentingBenchmark): InstrumentingResult = {
    val instrumentResult = new InstrumentResult
    InstrumentingSuccess(benchmark, instrumentResult)
  }
}