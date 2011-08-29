package ndp.scala.benchmarksuite.utility

class Log {

  def apply(message: String) {
    info(message)
  }

  def info(message: String) {
    println(message)
  }

  def debug(message: String) {
    println(message)
  }

  def error(message: String) {
    println(message)
  }

  def verbose(message: String) {
    println(message)
  }

  def yell(message: String) {
    println(message)
  }

}