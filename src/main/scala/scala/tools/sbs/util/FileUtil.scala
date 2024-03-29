/*
 * Config
 * 
 * Version
 * 
 * Created September 5th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package util

import java.io.{File => JFile}
import java.io.FileWriter
import java.lang.Thread.sleep
import java.lang.System
import java.text.SimpleDateFormat
import java.util.Date

import scala.collection.mutable.ArrayBuffer
import scala.tools.nsc.io.Path.string2path
import scala.tools.nsc.io.Directory
import scala.tools.nsc.io.File
import scala.tools.nsc.io.Path
import scala.tools.sbs.io.UI
import scala.tools.sbs.util.Constant.SLASH

object FileUtil {

  /** Writes the given message to the given file.
   */
  def write(filename: String, message: String) {
    val writer = new FileWriter(filename, true)
    writer.write(message)
    writer.write(System getProperty "line.separator")
    writer.close()
  }

  /** Tries to create a new file whose name in the format:
   *  <path><slash>YYYYMMDD.hhmmss.<last>
   *
   *  @param path	The path to the directory containing the file
   *  @param last	The true name of the file (excludes the time prefix)
   *
   *  @return	`Some` file if success, `None` otherwises
   */
  def createFile(path: String, last: String): Option[File] = {
    var filename: String = null
    val maxTry = 5
    var i = 0

    while (i < maxTry && filename == null) {

      filename = path + SLASH + new SimpleDateFormat("yyyyMMdd.HHmmss.").format(new Date) + last

      if (!createFile(filename)) {
        filename = null
        i += 1
        sleep(1000)
      }
    }

    if (filename != null) Some(File(filename)) else None
  }

  /** Tries to get the file with the given name.
   *
   *  @param filename	The file name
   *
   *  @return	`true` if success, `false` otherwise
   */
  def createFile(filename: String): Boolean = {
    val file = new JFile(filename)
    if (file.exists) {
      false
    }
    else try {
      file.createNewFile()
      true
    }
    catch { case _ => false }
  }

  /** Tries to create a file and write some data to it.
   */
  def createAndStore(path: String, last: String, whatToWrite: ArrayBuffer[String]): Option[File] = {
    val maybeAFile = FileUtil.createFile(path, last)
    maybeAFile match {
      case Some(file) => {
        try {
          for (line <- whatToWrite) {
            write(file.path, line)
          }
          Some(file)
        }
        catch {
          case e => {
            UI(file.path + (System getProperty "line.separator") + e.toString())
            None
          }
        }
      }
      case None => None
    }
  }

  /** Clean all contents of a directory.
   *
   *  @param dir	The desired directory
   */
  def clean(dir: Path) =
    try dir.toDirectory.list foreach (_.deleteRecursively)
    catch { case _ => UI.error("Cannot clean directory: " + dir.path) }

  /** Clean all .log files in the given directory
   */
  def cleanLog(dir: Path) =
    try dir.toDirectory.deepFiles filter (_.hasExtension("log")) foreach (_.delete)
    catch { case _ => UI.error("Cannot clean directory: " + dir.path) }

  /** Creates new directory.
   *
   *  @param path	The path of the desired directory
   */
  def mkDir(path: Path): Either[Directory, String] =
    try Left(path.createDirectory())
    catch { case _ => Right("Cannot create directory: " + path.path) }

  /** Moves a `File` from a `Directory` to another one with their
   *  realative paths
   */
  def move(file: File, from: Directory, to: Directory): Boolean =
    try {
      val relative = from relativize file
      relative.segments.init./:(to)((parent, child) => parent / child createDirectory ())
      if (file copyTo (to / relative)) file delete
      else false
    }
    catch { case _ => false }

}
