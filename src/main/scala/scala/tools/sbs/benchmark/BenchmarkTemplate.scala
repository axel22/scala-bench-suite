/*
 * BenchmarkTemplate
 * 
 * Version
 * 
 * Created on October 6th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package benchmark

/** User's benchmarks should extend this `trait` for convenience.
 *  For each measurement, the sbs will call `init()`, `run()` and `reset()`
 *  in order.
 *  The implements of `init` should do all the preparing works before
 *  any hing which will be measured running.
 *  `run`'s performance is what is going to be measured.
 *  `reset` changes everything back to their states before the benchmark run.
 *  For example, following is the source code of a initializable benchmark:
 *  {{{
 *  object OneBenchmark extends scala.tools.sbs.benchmark.BenchmarkTemplate {
 *
 *    private var list = List[Int]()
 *
 *    def init = (1 to 100000) foreach (list ::= _)
 *
 *    def run = list map (i => sort(1 to _))
 *
 *    def sort(r: scala.collection.immutable.Range) = r foreach println
 *
 *    def reset = list = Nil
 *
 *  }
 *
 *  }}}
 */
trait BenchmarkTemplate {

  /** Number of history files to be created for future use.
   *  Benchmarks may need their measurement histories for
   *  regression detection, this value specifies the quantity of
   *  these histories to be generated at the first time a
   *  benchmark runs.
   */
  val sampleNumber = 0

  val timeout = 45000

  def init

  def run // Do something wasting time here

  def reset

}
