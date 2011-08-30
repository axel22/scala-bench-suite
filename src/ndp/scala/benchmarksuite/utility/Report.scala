package ndp.scala.benchmarksuite.utility
import java.text.SimpleDateFormat
import java.util.Date
import scala.collection.mutable.ArrayBuffer

class Report {
  
  def apply(log: Log, config: Config, result: Boolean, means: ArrayBuffer[Double]) {
    val date = new SimpleDateFormat("MM/dd/yyyy 'at' HH:mm:ss").format(new Date)
    if (result) {
      log("[Test: " + date + "\tMain class: " + config.CLASSNAME + "]\t----------------------------------\t[  OK  ]")
    }
    else {
      log("[Test: " + date + "\tMain class: " + config.CLASSNAME + "]\t----------------------------------\t[FAILED]")
      log("Due to:")
      log("New approach:\t--------------------------------\t" + (means remove 0))
      log("Others:")
      for ( d <- means) {
        log("\t\t\t\t\t\t\t" + d)
      }
    }
  }

}