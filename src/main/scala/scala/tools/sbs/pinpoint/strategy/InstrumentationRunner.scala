/*
 * InstrumentationRunner
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

import scala.tools.nsc.io.Path
import scala.tools.sbs.common.Backuper
import scala.tools.sbs.util.FileUtil

import instrumentation.CodeInstrumentor.InstrumentingMethod
import instrumentation.CodeInstrumentor

trait InstrumentationRunner extends Backupable with Instrumentable {
  self: Configured =>

  def instrumentAndRun[R](benchmark: PinpointBenchmark,
                          declaringClass: String,
                          instrumentingMethod: String,
                          instrument: (InstrumentingMethod, CodeInstrumentor) => Unit,
                          run: => R,
                          classpathURLs: List[java.net.URL]): R = {
    val instrumentor = CodeInstrumentor(config, log, benchmark.pinpointExclude)
    val (clazz, method) = instrumentor.getClassAndMethod(declaringClass, instrumentingMethod, classpathURLs)
    if (method == null) throw new PinpointingMethodNotFoundException(benchmark)
    instrument(method, instrumentor)
    instrumentor.writeFile(clazz, instrumentedOut)
    val classFile = Path(clazz.getURL.getPath)
    val backuper = Backuper(
      log,
      List(classFile.toFile),
      (classFile /: (clazz.getName split "/."))((path, _) => path.parent).toDirectory,
      backupPlace)
    backuper.backup
    val result = run
    backuper.restore
    FileUtil clean instrumentedOut
    result
  }

}
