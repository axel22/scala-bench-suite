/*
 * SubJVMMeasurer
 * 
 * Version
 * 
 * Created September 25th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package measurement

import java.net.URL

import scala.collection.mutable.ArrayBuffer
import scala.tools.sbs.benchmark.Benchmark
import scala.tools.sbs.common.JVMInvokerFactory
import scala.tools.sbs.io.Log
import scala.tools.sbs.io.UI

/** Measures benchmark metric by invoking a new clean JVM.
 */
class SubJVMMeasurer(protected val log: Log,
                     protected val config: Config,
                     val mode: BenchmarkMode,
                     measurementHarness: MeasurementHarness[_])
  extends Measurer {

  /** Measures with default classpath as `config.classpathURLs ++ benchmark.classpathURLs`.
   */
  def measure(benchmark: PerformanceBenchmark): MeasurementResult =
    measure(benchmark, config.classpathURLs ++ benchmark.classpathURLs)

  /** Lauches a new process with a {@link MeasurementHarness} runs a
   *  {@link scala.tools.sbs.Benchmark}. User classes will be loaded from
   *  the given `classpathURLs`.
   */
  def measure(benchmark: PerformanceBenchmark, classpathURLs: List[URL]): MeasurementResult = {
    val invoker = JVMInvokerFactory(log, config)
    val (result, error) = invoker invoke (invoker.command(measurementHarness, benchmark, classpathURLs))
    if (error.length > 0) {
      error foreach log.error
      ExceptionMeasurementFailure(new Exception(error mkString "\n"))
    }
    else {
      dispose(result, benchmark, mode)
    }
  }

  /** Disposes a xml string to get the {@link MeasurementResult} it represents.
   *
   *  @param result	A `String` contains and xml element.
   *
   *  @return	The corresponding `MeasurementResult`
   */
  protected def dispose(result: String, benchmark: Benchmark, mode: BenchmarkMode): MeasurementResult = try {
    val xml = scala.xml.Utility trim (scala.xml.XML loadString result)
    xml match {
      case <MeasurementSuccess>{ _ }</MeasurementSuccess> =>
        MeasurementSuccess(new Series(
          config,
          log,
          ArrayBuffer((xml \\ "value") map (_.text.toLong): _*),
          (xml \\ "confidenceLevel").text.toInt))
      case <UnwarmableMeasurementFailure/> =>
        new UnwarmableMeasurementFailure
      case <UnreliableMeasurementFailure/> =>
        new UnreliableMeasurementFailure
      case <ProcessMeasurementFailure>{ exitValue }</ProcessMeasurementFailure> =>
        new ProcessMeasurementFailure(exitValue.text.toInt)
      case <ExceptionMeasurementFailure>{ ect }</ExceptionMeasurementFailure> =>
        ExceptionMeasurementFailure(new Exception(ect.text))
      case <UnsupportedBenchmarkMeasurementFailure/> =>
        UnsupportedBenchmarkMeasurementFailure(benchmark, mode)
      case _ =>
        new ProcessMeasurementFailure(0)
    }
  }
  catch {
    case e: org.xml.sax.SAXParseException => {
      UI.error("Malformed XML: " + result)
      log.error("Malformed XML: " + result)
      throw e
    }
    case e: Exception => {
      UI.error("Malformed XML: " + result)
      log.error("Malformed XML: " + result)
      throw new MalformedXMLException(this, mode, scala.xml.XML loadString result)
    }
  }

}
