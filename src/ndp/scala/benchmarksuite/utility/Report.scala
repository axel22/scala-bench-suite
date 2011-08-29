package ndp.scala.benchmarksuite.utility
import java.text.SimpleDateFormat
import java.util.Date

class Report {
  
  def apply(log: Log, config: Config, result: Boolean) {
    val date = new SimpleDateFormat("MM/dd/yyyy 'at' HH:mm:ss").format(new Date)
    if (result) {
      log("[Test: " + date + "\tMain class: " + config.CLASSNAME + "]\t----------------------------------\t[  OK  ]")
    }
    else {
      log("[Test: " + date + "\tMain class: " + config.CLASSNAME + "]\t----------------------------------\t[FAILED]")
    }
  }

}