/*
 * GCHarness
 * 
 * Version
 * 
 * Created on October 17th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package profiling

import java.lang.management.ManagementFactory

import scala.collection.JavaConverters.asScalaBufferConverter
import scala.tools.sbs.performance.MeasurementHarness
import scala.tools.sbs.performance.MeasurementResult

/** Extracts garbage collection information.
 */
object GCHarness extends MeasurementHarness[ProfilingBenchmark] {

  override val mode = Profiling

  protected val upperBound = manifest[ProfilingBenchmark]

  def measure(benchmark: ProfilingBenchmark): MeasurementResult = {
    val gcMXBeans = ManagementFactory.getGarbageCollectorMXBeans.asScala.toList
    val memMXBean = ManagementFactory.getMemoryMXBean

    memMXBean.gc

    val gcBefore =
      for (gcMXBean <- gcMXBeans) yield (gcMXBean.getName, gcMXBean.getCollectionCount, gcMXBean.getCollectionTime)
    val memBefore = (memMXBean.getHeapMemoryUsage, memMXBean.getNonHeapMemoryUsage)

    benchmark.init()
    benchmark.run()
    benchmark.reset()

    val gcAfter =
      for (gcMXBean <- gcMXBeans) yield (gcMXBean.getName, gcMXBean.getCollectionCount, gcMXBean.getCollectionTime)
    val memAfter = (memMXBean.getHeapMemoryUsage, memMXBean.getNonHeapMemoryUsage)

    val heap = MemoryUsage(
      memAfter._1.getInit - memBefore._1.getInit,
      memAfter._1.getUsed - memBefore._1.getUsed,
      memAfter._1.getCommitted - memBefore._1.getCommitted,
      memAfter._1.getMax - memBefore._1.getMax)

    val nonHeap = MemoryUsage(
      memAfter._2.getInit - memBefore._2.getInit,
      memAfter._2.getUsed - memBefore._2.getUsed,
      memAfter._2.getCommitted - memBefore._2.getCommitted,
      memAfter._2.getMax - memBefore._2.getMax)

    val ret = MemoryActivity(heap, nonHeap)
    gcAfter.foreach(gc =>
      gcBefore find (_._1 == gc._1) match {
        case Some(bef) => ret.gc(gc._1, gc._2 - bef._2, gc._3 - bef._3)
        case _         => ret.gc(gc._1, gc._2, gc._3)
      })
    ret
  }

}
