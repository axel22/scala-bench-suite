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

import java.net.URL

import scala.collection.mutable.ArrayBuffer
import scala.tools.sbs.benchmark.Benchmark
import scala.tools.sbs.io.Log

/** Trait used to invoke a new separated JVM.
 */
trait JVMInvoker {

  /** Invokes a new JVM which uses a typical {@link Runner} to run a typical {@link Benchmark}.
   *
   *  @param command	jvm command line
   *  @param convert	function converst a `String` from sub-process standard output to a return value.
   *  @param timeout	maximum time for the sub-process to run.
   *
   *  @return
   *  <ul>
   *  <li>A `ArrayBuffer[R]` array of return values.
   *  <li>A `ArrayBuffer[String]` contains runtime errors if any.
   *  </ul>
   */
  def invoke[R](command: Seq[String], convert: String => R, timeout: Int): (ArrayBuffer[R], ArrayBuffer[String])

  /** OS command to invoke an new JVM which has `measurer` as the main scala class
   *  and `benchmark` as an argument.
   */
  def command(harness: ObjectHarness, benchmark: Benchmark, classpathURLs: List[URL]): Seq[String]

  /** OS command to invoke an new JVM which has `benchmark` as the main scala class.
   */
  def command(benchmark: Benchmark, classpathURLs: List[URL]): Seq[String]

  /** OS command argument to run with java.
   *  Ex: `Seq("-cp", ".", "scala.tools.nsc.MainGenericRunner", "-vesion")`.
   */
  def asJavaArgument(benchmark: Benchmark, classpathURLs: List[URL]): Seq[String]

  def asJavaArgument(harness: ObjectHarness, benchmark: Benchmark, classpathURLs: List[URL]): Seq[String]

}

/** Factory object of {@link JVMInvoker}.
 */
object JVMInvokerFactory {

  def apply(log: Log, config: Config): JVMInvoker = new ScalaInvoker(log, config)

}
