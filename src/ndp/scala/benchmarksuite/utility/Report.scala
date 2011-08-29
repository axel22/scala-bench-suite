package ndp.scala.benchmarksuite.utility

class Report {
  
  def apply(log: Log, result: Boolean) {
    if (result) {
      log("Pass")
    }
    else {
      log("Failed")
    }
  }

}