/*
 * TextFileReport
 * 
 * Version
 * 
 * Created on September 18th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package util

import java.text.SimpleDateFormat
import java.util.Date
import scala.tools.nsc.io.File
import scala.tools.sbs.benchmark.BenchmarkMode.BenchmarkMode
import scala.tools.sbs.benchmark.Benchmark
import scala.tools.sbs.measurement.MeasurementSuccess
import scala.tools.sbs.measurement.UnwarmableFailure
import scala.tools.sbs.regression.ANOVAFailure
import scala.tools.sbs.regression.BenchmarkResult
import scala.tools.sbs.regression.BenchmarkSuccess
import scala.tools.sbs.regression.ConfidenceIntervalFailure
import scala.tools.sbs.regression.ExceptionFailure
import scala.tools.sbs.regression.ImmeasurableFailure
import scala.tools.sbs.regression.NoPreviousFailure
import scala.tools.sbs.regression.Persistor
import scala.tools.sbs.regression.FilePersistor

class TextFileReport(
  log: Log, config: Config, benchmark: Benchmark, persistor: Persistor, mode: BenchmarkMode) extends Report {

  val reportFile: File = FileUtil.createFile(config.benchmarkDirectory.path: String, "Report") match {
    case Some(file) => file
    case None => null
  }

  def apply(result: BenchmarkResult) {

    if (reportFile == null) {
      UI.error("Cannot create report file")
      return
    }

    def write(s: String) = FileUtil.write(reportFile.path, s)

    val date = new SimpleDateFormat("MM/dd/yyyy 'at' HH:mm:ss").format(new Date)
    val endl = System getProperty "line.separator"

    write("Test date:                     " + date)
    write("Benchmark name:                " + benchmark.name)
    write("Benchmark mode:                " + mode.toString)
    write("Benchmark directory:           " + config.benchmarkDirectory.path)
    write("Measured metrics:")
    result.measurementResult match {
      case success: MeasurementSuccess =>
        success.series foreach (l => write("                               " + (l.toString)))
      case _ => result.measurementResult match {
        case _: UnwarmableFailure => write("                               benchmark unwarmable")
        case _ => write("                               benchmark unreliable")
      }
    }
    write(endl + "Persistor:                     " + persistor.getClass.getName)
    persistor match {
      case sfp: FilePersistor => write("--from:                        " + sfp.location.path)
      case _ => () // TODO
    }
    write(endl + "At confidence level:           " + result.confidenceLevel + "%" + endl)
    write("------------------------------------------------------------" + endl)

    result match {
      case _: BenchmarkSuccess => write("Result:---------------------------------------------[  OK  ]")
      case _ => {
        write("Result:---------------------------------------------[FAILED]" + endl)
        write("--Due to:")
      }
    }

    result match {
      case ci: ConfidenceIntervalFailure => {
        write("----New approach sample mean:  " + ci.means.head)
        write("----Persistor sample mean:")
        write("                               " + ci.means.last) 
        write("----Confidence interval:       [" + ci.CI._1.formatted("%.2f") + "; " + ci.CI._2.formatted("%.2f") + "]")
      }
      case anova: ANOVAFailure => {
        write("----New approach sample mean:  " + anova.means.head)
        write("----Persistor sample mean:")
        anova.means.tail foreach (m => println("                               " + m))
        write("----F-test:")
        write("                        SSA:   " + anova.SSA)
        write("                        SSE:   " + anova.SSE)
        write("                     FValue:   " + anova.FValue)
        write("             F distribution:   " + anova.F)
      }
      case noPrevious: NoPreviousFailure => write("----No previous measurement result to detect regression")
      case immeasurable: ImmeasurableFailure => write("----" + immeasurable.measurementFailure.reason)
      case exception: ExceptionFailure => {
        write("----Exception:                 " + exception.exception.toString)
        write(exception.exception.getStackTraceString)
      }
      case _ => ()
    }
  }

}
