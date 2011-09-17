/*
 * Measurer
 * 
 * Version
 * 
 * Created on September 17th, 2011
 * 
 * Cretead by ND P
 */

package scala.tools.sbs.measurement

trait Measurer {

  def run(benchmark: Benchmark): MeasurementResult
  
}
