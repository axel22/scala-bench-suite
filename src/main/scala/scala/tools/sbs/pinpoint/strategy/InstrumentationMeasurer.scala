/*
 * InstrumentationMeasurer
 * 
 * Version
 * 
 * Created on November 6th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package pinpoint
package strategy

import scala.tools.nsc.io.Directory
import scala.tools.nsc.io.Path
import scala.tools.sbs.common.Backuper
import scala.tools.sbs.io.Log
import scala.tools.sbs.performance.MeasurementResult
import scala.tools.sbs.pinpoint.PinpointBenchmark
import scala.tools.sbs.util.FileUtil

import instrumentation.CodeInstrumentor.InstrumentingMethod
import instrumentation.CodeInstrumentor

abstract class InstrumentationMeasurer(config: Config,
                                       log: Log,
                                       benchmark: PinpointBenchmark,
                                       instrumentedOut: Directory,
                                       backup: Directory) {

  protected def instrumentAndMeasure(declaringClass: String,
                                     instrumentingMethod: String,
                                     instrument: (InstrumentingMethod, CodeInstrumentor) => Unit,
                                     classpathURLs: List[java.net.URL]): MeasurementResult = {
    val instrumentor = CodeInstrumentor(config, log, benchmark.pinpointExclude)
    val (clazz, method) = instrumentor.getClassAndMethod(
      declaringClass,
      instrumentingMethod,
      classpathURLs)
    if (method == null) {
      throw new PinpointingMethodNotFoundException(benchmark)
    }
    instrument(method, instrumentor)
    instrumentor.writeFile(clazz, instrumentedOut)
    val classFile = Path(clazz.getURL.getPath)
    val backuper = Backuper(
      log,
      List(classFile.toFile),
      (clazz.getName split "/.")./:(classFile)((path, _) => path.parent).toDirectory,
      backup)
    backuper.backup
    val result = PinpointMeasurerFactory(config, log).measure(benchmark, instrumentedOut.toURL :: classpathURLs)
    backuper.restore
    FileUtil clean instrumentedOut
    result
  }

}
