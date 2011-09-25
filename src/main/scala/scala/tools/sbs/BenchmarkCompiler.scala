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

  def compile(args: List[String]): Boolean

  def compile(benchmark: Benchmark): Boolean

}

class BenchmarkCompilerFactory(log: Log, config: Config) {

  def create() = new BenchmarkGlobal(log, config)

}