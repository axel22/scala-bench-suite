/*
 * MemoryProfiler
 * 
 * Version
 * 
 * Created on October 23rd, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package profiling

import scala.tools.sbs.common.JVMInvokerFactory
import scala.tools.sbs.io.Log

class MemoryProfiler(log: Log, config: Config) {

  def profile(benchmark: ProfilingBenchmark, profile: Profile): ProfilingResult = {
    val invoker = JVMInvokerFactory(log, config)
    val (result, error) = invoker.invoke(
      invoker.command(GCHarness, benchmark, config.classpathURLs ++ benchmark.classpathURLs),
      scala.xml.XML.loadString,
      benchmark.timeout)
    if (error.length > 0) {
      error foreach log.error
      ProfilingException(benchmark, new Exception(error mkString "\n"))
    }
    else {
      profile useMemory dispose(result.head)
      ProfilingSuccess(benchmark, profile)
    }
  }

  /** Disposes a xml string to get the {@link scala.tools.sbs.profiling.MemoryActivity} it represents.
   *
   *  @param result	A `String` contains and xml element.
   *
   *  @return	The corresponding `MemoryActivity`
   */
  def dispose(result: scala.xml.Elem): MemoryActivity = try {
    val xml = scala.xml.Utility trim result
    val heapNode = xml \\ "heap"
    val heap = MemoryUsage(
      (heapNode \\ "init").text.toLong,
      (heapNode \\ "used").text.toLong,
      (heapNode \\ "committed").text.toLong,
      (heapNode \\ "max").text.toLong)
    val nonHeapNode = xml \\ "nonHeap"
    val nonHeap = MemoryUsage(
      (nonHeapNode \\ "init").text.toLong,
      (nonHeapNode \\ "used").text.toLong,
      (nonHeapNode \\ "committed").text.toLong,
      (nonHeapNode \\ "max").text.toLong)
    val gcs = List(
      (xml \\ "gc") map (
        gc => GarbageCollection(
          (gc \\ "name").text,
          (gc \\ "cycle").text.toInt,
          (gc \\ "time").text.toLong)): _*)
    new MemoryActivity(heap, nonHeap, gcs)
  }
  catch {
    case e: Exception => {
      log.error("Malformed XML: " + result)
      throw new Exception("Malformed XML: " + result)
    }
  }

}
