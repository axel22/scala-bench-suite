/*
 * Config
 * 
 * Version
 * 
 * Created September 5th, 2011
 * 
 * Created by ND P
 */

package ndp.scala.tools.sbs
package util

import java.io.{ File => JFile }
import java.io.FileWriter
import java.lang.Thread.sleep
import java.text.SimpleDateFormat
import java.util.Date
import scala.collection.mutable.ArrayBuffer
import scala.tools.nsc.io.File
import scala.tools.nsc.io.Path
import scala.tools.nsc.io.Directory

object FileUtil {

  /**
   * Writes the given message to the given file.
   */
  def write(filename: String, message: String) {
    val writer = new FileWriter(filename, true)
    writer.write(message)
    writer.write(System getProperty "line.separator")
    writer.close()
  }

  /**
   * Tries to create a new file whose name in the format:
   * <path><slash>YYYYMMDD.hhmmss.<last>
   *
   * @param path	The path to the directory containing the file
   * @param last	The true name of the file (excludes the time prefix)
   *
   * @return	`Some` file if success, `None` otherwises
   */
  def createFile(path: String, last: String): Option[File] = {
    var filename: String = null
    val maxTry = 5
    var i = 0

    while (i < maxTry && filename == null) {

      filename = path + (System getProperty "file.separator") +
        new SimpleDateFormat("yyyyMMdd.HHmmss.").format(new Date) + last

      if (!createFile(filename)) {
        filename = null
        i += 1
        sleep(1000)
      }
    }

    if (filename != null) {
      Some(File(filename))
    } else {
      None
    }
  }

  /**
   * Tries to get the file with the given name.
   *
   * @param filename	The file name
   *
   * @return	`true` if success, `false` otherwise
   */
  def createFile(filename: String): Boolean = {
    val file = new JFile(filename)
    if (file.exists()) {
      false
    } else {
      try {
        file.createNewFile()
        true
      } catch {
        case _ => false
      }
    }
  }

  /**
   * Tries to create a file and write some data to it.
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
        } catch {
          case e => {
            if (log != null) {
              log.debug(file.path + (System getProperty "line.separator") + e.toString())
            } else {
              UI(file.path + (System getProperty "line.separator") + e.toString())
            }
            None
          }
        }
      }
      case None => None
    }
  }

  /**
   * Creates directory structure for benchmarking.
   */
  def createBenchmarkDir(root: String, name: String): Either[Directory, String] = {
    val slash = System getProperty "file.separator"
    try {
      mkDir(root + slash + name) match {
        case Left(dir) => {
          mkDir(dir / "bin") match {
            case Right(err) => Right(err)
            case _ => ()
          }
          mkDir(dir / "lib") match {
            case Right(err) => Right(err)
            case _ => ()
          }
          mkDir(dir / "result") match {
            case Right(err) => Right(err)
            case _ => ()
          }
          mkDir(dir / "src") match {
            case Right(err) => Right(err)
            case _ => ()
          }
          Left(dir)
        }
        case e => e
      }
    } catch {
      case _ => Right("Cannot access directory: " + root)
    }
  }

  /**
   * Clean all contents of a directory.
   *
   * @param dir	The desired directory
   */
  def clean(dir: Path): Option[String] = {
    try {
      dir.toDirectory.list foreach (_.deleteRecursively)
      None
    } catch {
      case _ => Some("Cannot clean directory: " + dir.path)
    }
  }

  /**
   * Creates new directory.
   *
   * @param path	The path of the desired directory
   */
  def mkDir(path: Path): Either[Directory, String] = {
    try {
      Left(path.createDirectory())
    } catch {
      case _ => Right("Cannot create directory: " + path.path)
    }
  }

}