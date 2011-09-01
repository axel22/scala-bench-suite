package ndp.scala.benchmarksuite
package utility

class Log {

  def apply(message: String) {
    info(message)
  }

  def info(message: String) {
    println("[Info]     " + message)
  }

  def debug(message: String) {
    println("[Debug]    " + message)
  }

  def error(message: String) {
    println("[Error]    " + message)
  }

  def verbose(message: String) {
    println("[Verbose]  " + message)
  }

  def yell(message: String) {
    println("[Console]  " + message)
  }

}