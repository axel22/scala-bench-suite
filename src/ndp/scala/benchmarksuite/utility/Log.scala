package ndp.scala.benchmarksuite.utility

class Log {

  def apply(message: String) {
    info(message)
  }

  def info(message: String) {
    println("[Info]\t\t" + message)
  }

  def debug(message: String) {
    println("[Debug]\t\t" + message)
  }

  def error(message: String) {
    println("[Error]\t\t" + message)
  }

  def verbose(message: String) {
    println("[Verbose]\t" + message)
  }

  def yell(message: String) {
    println("[Console]\t" + message)
  }

}