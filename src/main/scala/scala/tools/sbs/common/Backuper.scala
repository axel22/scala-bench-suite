/*
 * Backuper
 * 
 * Version
 * 
 * Created on November 4th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package common

import scala.collection.mutable.HashMap
import scala.tools.nsc.io.Path.string2path
import scala.tools.nsc.io.Directory
import scala.tools.nsc.io.File
import scala.tools.sbs.io.Log
import scala.tools.sbs.util.FileUtil

/** Backups instrumented .class files
 */
case class Backuper(log: Log, files: List[File], location: Directory, storage: Directory) {

  private var backuped = false

  private val backupFiles = HashMap[File, File]()

  private val placeOf = HashMap[File, Directory]()

  def backup = {
    log.debug("Backup")
    backuped = files forall (file => {
      val relative = location relativize file
      var current = storage
      while (File(current / relative) exists) current = current / "tmp" createDirectory ()
      val backupFile = (current / relative).toFile
      backupFiles.put(file, backupFile)
      placeOf.put(backupFile, current)
      log.debug("Move " + file)
      log.debug("From " + location)
      log.debug("To   " + current)
      val ok = FileUtil.move(file, location, current)
      if (!ok) throw new BackupFailureException(file, location)
      else true
    })
    backuped
  }

  def restore = {
    log.debug("Restore")
    if (backuped) {
      files forall (file => {
        log.debug("Move " + backupFiles(file))
        log.debug("From " + placeOf(backupFiles(file)))
        log.debug("To   " + location)
        val ok = FileUtil.move(backupFiles(file), placeOf(backupFiles(file)), location)
        if (!ok) throw new BackupFailureException(file, location)
        else true
      })
    }
    else {
      throw new java.io.IOException("restore called while not backuped")
    }
  }

}
