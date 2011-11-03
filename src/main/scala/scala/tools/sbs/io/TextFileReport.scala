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
package io

import java.text.SimpleDateFormat
import java.util.Date

import scala.tools.nsc.io.File
import scala.tools.sbs.util.Constant.ENDL
import scala.tools.sbs.util.Constant
import scala.tools.sbs.util.FileUtil

/** An implement of {@link Report}, reporting of a simple text file.
 */
class TextFileReport(config: Config) extends Report {

  val reportFile: File = FileUtil.createFile(config.benchmarkDirectory.path: String, "Report") match {
    case Some(file) => file
    case None       => null
  }

  def write(s: String) = FileUtil.write(reportFile.path, s)

  def break() = write("------------------------------------------------------------" + ENDL)

  def ok() = write("Result:---------------------------------------------[  OK  ]")

  def failed() = {
    write("Result:---------------------------------------------[FAILED]")
    write("  Due to:")
  }

  def apply(pack: ResultPack) {
    if (reportFile == null) {
      UI.error("Cannot create report file")
      return
    }

    val date = new SimpleDateFormat("MM/dd/yyyy 'at' HH:mm:ss").format(new Date)

    write("Test date:           " + date)
    write("Benchmark directory: " + config.benchmarkDirectory.path)
    write("Total benchmarks:    " + pack.total)
    write("OK:                  " + pack.ok)
    write("Failed:              " + pack.failed)

    break

    pack foreach (mode => {
      write(mode toReport)
      write("")
      break
      mode foreach (r => {
        write("Benchmark: " + r.benchmarkName)
        emit(r)
        break
      })
    })
  }

  def emit(result: BenchmarkResult): Unit = {
    result match {
      case _: BenchmarkSuccess => ok
      case _                   => failed
    }
    result.toReport foreach (line => write(Constant.INDENT + line))
  }

}
