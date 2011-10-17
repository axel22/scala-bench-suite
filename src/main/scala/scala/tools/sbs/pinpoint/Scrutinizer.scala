/*
 * Scrutinizer
 * 
 *  Version
 *  
 *  Created on October 13th, 2011
 *  
 *  Created by ND P
 */

package scala.tools.sbs
package pinpoint

import scala.tools.sbs.benchmark.Benchmark

trait Scrutinizer extends Runner {

  def run(benchmark: Benchmark): RunResult = scrutinize(benchmark)

  def scrutinize(benchmark: Benchmark): ScrutinyResult

}

object ScrutinizerFactory {

  def apply(config: Config): Scrutinizer = {
    new MethodScrutinizer(config)
  }

}
