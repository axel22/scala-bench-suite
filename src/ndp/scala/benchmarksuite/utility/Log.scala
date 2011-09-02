package ndp.scala.benchmarksuite
package utility

class Log(config: Config) {

  def this() {
    this(new Config(
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      0,
      0,
      null,
      null,
      true,
      LogLevel.VERBOSE,
      true
    ))
  }

  def apply(message: String) {
    Console println message
  }

  def info(message: String) {
    this("[Info]     " + message)
    if (config.SHOW_LOG) {
      UI("[Info]     " + message)
    }
  }

  def debug(message: String) {
    this("[Debug]    " + message)
    if (config.SHOW_LOG) {
      UI("[Debug]    " + message)
    }
  }

  def error(message: String) {
    this("[Error]    " + message)
    if (config.SHOW_LOG) {
      UI("[Error]    " + message)
    }
  }

  def verbose(message: String) {
    this("[Verbose]  " + message)
    if (config.SHOW_LOG) {
      UI("[Verbose]  " + message)
    }
  }

}

object LogLevel extends Enumeration {
  type LogLevel = Value
  val INFO, DEBUG, VERBOSE = Value
}
