/*
 * BenchmarkCompiler
 * 
 * Version
 * 
 * Created on September 23th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs

import scala.tools.sbs.io.Log

trait BenchmarkCompiler {

  def compile(benchmark: Benchmark): Boolean

}

object BenchmarkCompilerFactory {

  def apply(log: Log, config: Config): BenchmarkCompiler = new BenchmarkGlobal(log, config)

}
