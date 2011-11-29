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

  /** Invokes a new JVM which uses a typical {@link scala.tools.sbs.Runner}
   *  to run a typical {@link scala.tools.sbs.benchmark.Benchmark}.
   *
   *  @param command	OS command to invoke the wanted jvm under the form of a `Seq[String]`.
   *  @param stdout 	function converts a `String` which is a line from the jvm's standard output to `R`.
   *  @param stderr 	function converts a `String` which is a line from the jvm's standard error to `E`.
   *  @param timeout	maximum time for the jvm to run.
   *
   *  @return
   *  <ul>
   *  <li>A `ArrayBuffer[R]` array of values each had been created from one line of the jvm's standard output.
   *  <li>A `ArrayBuffer[E]` array of values each had been created from one line of the jvm's standard error.
   *  </ul>
   */
  def invoke[R, E](command: Seq[String],
                   stdout: String => R,
                   stderr: String => E,
                   timeout: Int): (ArrayBuffer[R], ArrayBuffer[E])

  /** OS command to invoke an new JVM which has `harness` as the main scala class
   *  and `benchmark` as an argument.
   */
  def command(harness: ObjectHarness, benchmark: Benchmark, classpathURLs: List[URL]): Seq[String]

  /** OS command to invoke an new JVM which has `benchmark` as the main scala class.
   */
  def command(benchmark: Benchmark, classpathURLs: List[URL]): Seq[String]

  /** OS command argument to run with java.
   *  Ex: `Seq("-cp", ".", "scala.tools.nsc.MainGenericRunner", "-version")`.
   */
  def asJavaArgument(benchmark: Benchmark, classpathURLs: List[URL]): Seq[String]

  def asJavaArgument(harness: ObjectHarness, benchmark: Benchmark, classpathURLs: List[URL]): Seq[String]

}

/** Factory object of {@link JVMInvoker}.
 */
object JVMInvokerFactory {

  def apply(log: Log, config: Config): JVMInvoker = new ScalaInvoker(log, config)

}
