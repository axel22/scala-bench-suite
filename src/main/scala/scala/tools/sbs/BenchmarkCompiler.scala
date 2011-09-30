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

/** Represents a compiler used to compile benchmarks from scala sources.
 */
trait BenchmarkCompiler {

  /** Compiles the given `Benchmark`.
   *
   *  @return	`true` if successfully, `false` otherwise
   */
  def compile(benchmark: Benchmark): Boolean

}

/** Factory object used to create a compiler to compile a snippet.
 */
object BenchmarkCompilerFactory {

  def apply(log: Log, config: Config): BenchmarkCompiler = new BenchmarkGlobal(log, config)

}
