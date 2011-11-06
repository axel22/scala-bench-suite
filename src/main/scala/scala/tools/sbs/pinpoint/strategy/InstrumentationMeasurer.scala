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

import scala.tools.nsc.io.Path.string2path
import scala.tools.nsc.io.Directory
import scala.tools.nsc.io.Path
import scala.tools.sbs.common.Backuper
import scala.tools.sbs.io.Log
import scala.tools.sbs.performance.MeasurementResult
import scala.tools.sbs.pinpoint.instrumentation.CodeInstrumentor.InstrumentingMethod
import scala.tools.sbs.pinpoint.instrumentation.CodeInstrumentor
import scala.tools.sbs.util.FileUtil

abstract class InstrumentationMeasurer(config: Config,
                                       log: Log,
                                       benchmark: PinpointBenchmark,
                                       instrumentor: CodeInstrumentor,
                                       instrumented: Directory,
                                       backup: Directory) {

  protected def instrumentAndMeasure(instrument: InstrumentingMethod => Unit,
                                     classpathURLs: List[java.net.URL]): MeasurementResult = {
    val (clazz, method) = instrumentor.getClassAndMethod(
      benchmark.pinpointClass,
      benchmark.pinpointMethod,
      classpathURLs)
    if (method == null) {
      throw new PinpointingMethodNotFoundException(benchmark)
    }
    instrument(method)
    instrumentor.writeFile(clazz, instrumented)
    val classFile = Path(clazz.getURL.getPath)
    val backuper = Backuper(
      log,
      List(classFile.toFile),
      (clazz.getName split "/.")./:(classFile)((path, _) => path.parent).toDirectory,
      backup)
    backuper.backup
    val result = PinpointMeasurerFactory(config, log).measure(benchmark, instrumented.toURL :: classpathURLs)
    backuper.restore
    FileUtil clean instrumented
    result
  }

}
