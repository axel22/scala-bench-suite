package ndp.scala.tools.sbs
package measurement

import ndp.scala.tools.sbs.util.Config
import ndp.scala.tools.sbs.util.Constant
import ndp.scala.tools.sbs.util.Log
import ndp.scala.tools.sbs.util.UI

trait SubProcessHarness {

  /**
   * Entry point for this measuring sub-process 
   * in case a different JVC arguments from sbs' specified.
   */
  def main(args: Array[String]): Unit = {
    try {
      rebuildSettings(args)
      reportResult(run())
      System.exit(0)
    } catch {
      case e =>
        reportResult(Right(e.toString + (System getProperty "line.separator") + e.getStackTraceString))
        System.exit(1)
    }
  }
  
  def run(): Either[BenchmarkResult, String]
  
  /**
   * Reports the measurement result to the main process.
   */
  def reportResult(result: Either[BenchmarkResult, String]) {
    result match {
      case Left(series) => {
        Console println series.toString()
      }
      case Right(s) => {
        Console println s
      }
    }
  }
  
  /**
   *
   */
  def rebuildSettings(args: Array[String]): (Config, Log) = {
    val confArgs = args take Constant.MAX_ARGUMENT_CONFIG
    val logArgs = args slice (Constant.MAX_ARGUMENT_CONFIG, args.length)

    for (c <- confArgs) {
      UI("Config " + c)
    }
    for (l <- logArgs) {
      UI("Log    " + l)
    }

    config = new Config(confArgs)
    log = new Log(logArgs)

    (config, log)
  }
  
}