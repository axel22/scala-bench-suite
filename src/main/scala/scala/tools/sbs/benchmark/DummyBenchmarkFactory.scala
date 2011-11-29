
package scala.tools.sbs
package benchmark

import java.lang.reflect.Method
import scala.tools.sbs.io.Log
import scala.tools.sbs.common.Reflector

class DummyBenchmarkFactory(val log: Log, val config: Config)
  extends Configured
  with BenchmarkFactory {

  /** Creates a `Benchmark` from the given arguments.
   */
  def createFrom(info: BenchmarkInfo): Benchmark = load(
    info,
    (method: Method, context: ClassLoader) => new SnippetBenchmark(
      info.name,
      info.arguments,
      info.classpathURLs,
      info.src,
      info.sampleNumber,
      info.timeout,
      method,
      context,
      config) {},
    (context: ClassLoader) => new InitializableBenchmark(
      info.name,
      info.classpathURLs,
      info.src,
      Reflector(config, log).getObject[BenchmarkTemplate](
        info.name, config.classpathURLs ++ info.classpathURLs),
      context,
      config) {})

}
