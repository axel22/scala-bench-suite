/*
 * ReportFactory
 * 
 * Version
 * 
 * Created on September 18th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package io

import BenchmarkMode.BenchmarkMode
import scala.tools.sbs.regression.Persistor

class ReportFactory(log: Log, config: Config) {

  def create(benchmark: Benchmark, persistor: Persistor, mode: BenchmarkMode): Report = {
    new TextFileReport(log, config, benchmark, persistor, mode)
  }

}
