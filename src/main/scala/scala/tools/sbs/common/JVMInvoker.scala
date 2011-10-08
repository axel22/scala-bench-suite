/*
 * JVMInvoker
 * 
 * Version
 * 
 * Created on September 24th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package common

import scala.collection.mutable.ArrayBuffer
import scala.tools.sbs.io.Log
import scala.tools.sbs.measurement.Measurer
import scala.tools.sbs.benchmark.Benchmark

/** Trait used to invoke a new separated JVM.
 */
trait JVMInvoker {

  /** Invokes a new JVM which uses a typical {@link Runner} to run a typical {@link Benchmark}.
   *
   *  @param measurer	{@link Runner}
   *  @param benchmark	{@link Benchmark}
   *
   *  @return
   *  <ul>
   *  <li>A `String`  contains xml element representing the measurement result.
   *  <li>A `ArrayBuffer[String]` contains runtime errors if any.
   *  </ul>
   */
  def invoke(command: Seq[String]): (String, ArrayBuffer[String])

  /** OS command to invoke an new JVM which has `measurer` as the main scala class
   *  and `benchmark` as an argument.
   */
  def command(runner: Runner, benchmark: Benchmark): Seq[String]

  /** OS command to invoke an new JVM which has `benchmark` as the main scala class.
   */
  def command(benchmark: Benchmark): Seq[String]

  /** OS command argument to run with java.
   *  Ex: `Seq("-cp", ".", "scala.tools.nsc.MainGenericRunner", "-vesion")`.
   */
  def asJavaArgument(benchmark: Benchmark): Seq[String]

  def asJavaArgument(runner: Runner, benchmark: Benchmark): Seq[String]

}

/** Factory object of {@link JVMInvoker}.
 */
object JVMInvokerFactory {

  def apply(log: Log, config: Config): JVMInvoker = new ScalaInvoker(log, config)

}
