package btfubackup.core

import btfubackup.BTFU
import com.mojang.realmsclient.util.Option
import java.nio.file.Path

interface LogWrapper {
//    fun debug(s: String): Unit = {
//        if (BTFU.cfg.debug) warn("debug: $s")
//    }
    fun err(s: String)
    fun warn(s: String)
    fun warn(s: String, e: Throwable)
}

//object BTFU {
//    var cfg: BTFUConfig = _
//    var log: LogWrapper = _
//    var serverLive = false
//}

abstract class BTFUBase(log: LogWrapper) {
//    BTFU.log = log

    abstract val dediShlooper: () -> String?

    /**
     * @param path to check
     * @return Some(errormessage) if there is a problem, or None if the backup path is acceptable
     */
    fun startupPathChecks(path: Path): String? {
        if (path.equals(BTFU.cfg.mcDir))
            return "Backups directory is not set or matches your minecraft directory."

        if (!path.toFile().exists())
            return "Backups directory ${'"'}$path${'"'} does not exist."

        if (FileActions.subdirectoryOf(path, BTFU.cfg.mcDir))
            return """Backups directory ${'"'}$path${'"'} is inside your minecraft directory ${'"'}${BTFU.cfg.mcDir}${'"'}.
This mod backups your entire minecraft directory, so that won't work."""

        if (FileActions.subdirectoryOf(BTFU.cfg.mcDir, path))
            return """Backups directory ${'"'}$path${'"'} encompasses your minecraft server!
are you trying to back up to a directory your minecraft server is in?)"""

        return null
    }

    def handleStartupPathChecks(): Boolean = {
        if (!startupPathChecks()) return false

        // rename backups that conform to notDateFormat
        FileActions.backupFilesFor(BTFU.cfg.notDateFormat).foreach{case (dir, date) =>
            BTFU.cfg.backupDir.resolve(dir).toFile.renameTo(
                BTFU.cfg.backupDir.resolve(BTFU.cfg.dateFormat.format(new Date(date))).toFile
            )
        }

        true
    }

    def startupPathChecks(): Boolean = {
        if (!BTFU.cfg.backupDir.equals(BTFU.cfg.mcDir) && FileActions.subdirectoryOf(BTFU.cfg.mcDir, BTFU.cfg.backupDir)) {
            btfubanner()
            log.err(s"Backups directory ${'"'}${BTFU.cfg.backupDir}${'"'} encompasses your minecraft server!\n" +
            s"(are you trying to run a backup without copying it, or back up to a directory your minecraft server is in?")
            return false
        }

        java.nio.file.Files.getAttribute(BTFU.cfg.mcDir.resolve("server.properties"), "unix:nlink") match {
            case i: Integer => if (i > 1) {
            btfubanner()
            log.err(s"Your minecraft server directory is hardlinked!\n" +
            s"(Are you trying to run a backup that you moved instead of copying?  Doing so would corrupt your backup history!)")
            return false
        }
            case _ =>
        }

        startupPathChecks(BTFU.cfg.backupDir).foreach { error =>
            (if (BTFU.cfg.disablePrompts) None else getDediShlooper) match {
                case Some(shlooper) =>
                Thread.sleep(1500)
                btfubanner()
                var pathCheck: Option[String] = Some(error)
                var enteredPath: Path = null
                do {
                    log.err(pathCheck.get)
                    log.err("Please enter a new path and press enter (or exit out and edit btfu.cfg)")

                    var enteredString = shlooper().getOrElse(return false)
                    if (enteredString.equals("stop")) return false
                    if (enteredString.startsWith("~/")) enteredString = System.getProperty("user.home") + enteredString.substring(1)
                    pathCheck = try {
                        enteredPath = FileActions.canonicalize(new File(enteredString).toPath)
                        startupPathChecks(enteredPath)
                    } catch {
                        case t: InvalidPathException => Some(s"Invalid path: ${t.getMessage}")
                        case t: Throwable => Some(s"${t.getClass.getCanonicalName}: ${t.getMessage}")
                    }
                } while (pathCheck.isDefined)

                log.err(s"Awesome!  Your backups will go in $enteredPath.  I will shut up until something goes wrong!")
                BTFU.cfg.c.setBackupDir(enteredPath)
                return true
                case None =>
                btfubanner()
                log.err(s"/============================================================")
                log.err(s"| $error")
                log.err(s"| Please configure the backup path in btfu.cfg.")
                log.err(s"\\============================================================")
                return false
            }
        }
        true
    }

    def btfubanner(): Unit = {
        Seq(
            ",'\";-------------------;\"`.",
            ";[]; BBB  TTT FFF U  U ;[];",
            ";  ; B  B  T  F   U  U ;  ;",
            ";  ; B  B  T  F   U  U ;  ;",
            ";  ; BBB   T  FFF U  U ;  ;",
            ";  ; B  B  T  F   U  U ;  ;",
            ";  ; B  B  T  F   U  U ;  ;",
            ";  ; BBB   T  F    UU  ;  ;",
            ";  `.                 ,'  ;",
            ";    \"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"    ;",
            ";    ,-------------.---.  ;",
            ";    ;  ;\"\";       ;   ;  ;",
            ";    ;  ;  ;       ;   ;  ;",
            ";    ;  ;  ;       ;   ;  ;",
            ";//||;  ;  ;       ;   ;||;",
            ";\\\\||;  ;__;       ;   ;\\/;",
            " `. _;          _  ;  _;  ;",
            "   \" \"\"\"\"\"\"\"\"\"\"\" \"\"\"\"\" \"\"\""
        ).foreach{s => log.err("               " + s)}
    }
}