import NPMRunHook.runProcessSync
import play.sbt.PlayRunHook
import sbt._

import scala.sys.process.Process

object NPMRunHook {
  val isWindows = sys.props("os.name").toLowerCase().contains("win")
  val cmdPrefix = if (isWindows) "cmd /c " else ""
  val buildCommand = "npm run build"

  def stage(base: File, log: Logger): File = {
    Seq("npm install", "npm run build").foreach { cmd =>
      runProcessSync(cmd, base, log)
    }
    base
  }

  def runProcessSync(command: String, base: File, log: Logger): Unit = {
    val actualCommand = canonical(command)
    log.info(s"Running '$actualCommand'...")
    val rc = Process(actualCommand, base).run(log).exitValue()
    if (rc != 0) {
      throw new Exception(s"$actualCommand failed with $rc")
    }
  }

  def canonical(command: String) = s"$cmdPrefix$command"
}

/** Try https://stackoverflow.com/questions/269494/how-can-i-cause-a-child-process-to-exit-when-the-parent-does
  *
  * @see https://torre.me.uk/2019/03/06/scala-play-rest-and-angular/
  * @see https://gist.github.com/jroper/387b05830044d006eb231abd1bc768e5
  */
class NPMRunHook(base: File, target: File, log: Logger) extends PlayRunHook {
  private var watchProcess: Option[Process] = None

  val installCommand = "npm install"
  val watchCommand = "npm run watch"

  override def beforeStarted(): Unit = {
    val cacheFile = target / "package-json-last-modified"
    val cacheLastModified = if (cacheFile.exists()) {
      try {
        IO.read(cacheFile).trim.toLong
      } catch {
        case _: NumberFormatException => 0L
      }
    }
    val lastModified = (base / "package.json").lastModified()
    // Check if package.json has changed since we last ran this
    if (cacheLastModified != lastModified) {
      runProcessSync(installCommand, base, log)
      IO.write(cacheFile, lastModified.toString)
    }
  }

  override def afterStarted(): Unit = {
    val cmd = NPMRunHook.canonical(watchCommand)
    log.info(s"Watching with '$cmd'...")
    watchProcess = Some(Process(cmd, base).run(log))
  }

  override def afterStopped(): Unit = {
    watchProcess.foreach(_.destroy())
    watchProcess = None
    if (NPMRunHook.isWindows) {
      // Node child processes are not properly killed with `process.destroy()` on Windows. This gets the job done.
      runProcessSync("taskkill /im node.exe /F", base, log)
    }
  }
}
