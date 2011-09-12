package ndp.scala.tools.sbs
package measurement

import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.net.URL
import scala.tools.nsc.io.Path
import scala.tools.nsc.util.ClassPath
import scala.tools.nsc.util.ScalaClassLoader
import scala.tools.nsc.Global
import scala.tools.nsc.Settings
import scala.tools.nsc.io.Directory
import scala.tools.nsc.io.File

case class Benchmark(name: String,
                     arguments: List[String],
                     classpathURLs: List[URL],
                     src: List[File],
                     bin: Directory) {

  /**
   * Benchmark `main` method.
   */
  private var method: Method = null

  /**
   * Current class loader context.
   */
  private val oldContext = Thread.currentThread.getContextClassLoader

  /**
   * Uses strange named compiler Global to compile.
   */
  def compile(): Boolean = {
    log.verbose("[Compile]")

    val settings = new Settings(log.error)
    val (ok, errArgs) = settings.processArguments(List("-classpath", config.classpath, "-d", bin.path), false)

    log.debug(settings.d.value)
    settings.outdir.value = bin.path

    if (ok) {
      val compiler = new Global(settings)
      (new compiler.Run) compile (src map (_.path))
    } else {
      errArgs map (err => log.error(err))
    }
    ok
  }

  /**
   * Sets the running context and load benchmark classes.
   */
  def init() {
    try {
      val classLoader = (ScalaClassLoader fromURLs classpathURLs)
      val clazz = classLoader.tryToInitializeClass(name) getOrElse (throw new ClassNotFoundException(name))
      method = clazz.getMethod("main", classOf[Array[String]])
      if (!Modifier.isStatic(method.getModifiers)) {
        throw new NoSuchMethodException(name + ".main is not static")
      }
      Thread.currentThread.setContextClassLoader(classLoader)
    } catch {
      case x: ClassNotFoundException => throw new ClassNotFoundException(
        name + " args = " + (arguments mkString ", ") + ", classpath = " + ClassPath.fromURLs(classpathURLs: _*))
    }
  }

  /**
   * Runs the benchmark object and throws Exceptions (if any).
   */
  def run() = method.invoke(null, Array(arguments.toArray: AnyRef): _*)

  /**
   * Resets the context.
   */
  def finallize() = Thread.currentThread.setContextClassLoader(oldContext)

  override def toString = "Benchmark [" + name + "] [" + arguments mkString " "

}
