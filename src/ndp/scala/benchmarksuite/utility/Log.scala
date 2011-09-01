package ndp.scala.benchmarksuite
package utility

class Log(config: Config) {

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