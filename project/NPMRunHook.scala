import play.sbt.PlayRunHook
import sbt._

import scala.sys.process.Process

/** Try https://stackoverflow.com/questions/269494/how-can-i-cause-a-child-process-to-exit-when-the-parent-does
  *
  * @see https://torre.me.uk/2019/03/06/scala-play-rest-and-angular/
  * @see https://gist.github.com/jroper/387b05830044d006eb231abd1bc768e5
  */
class NPMRunHook(base: File, target: File, log: Logger) extends PlayRunHook {
  private var watchProcess: Option[Process] = None

  val isWindows = sys.props("os.name").toLowerCase().contains("win")
  val cmdPrefix = if (isWindows) "cmd /c " else ""
  val installCommand = s"${cmdPrefix}npm install"
  val watchCommand = s"${cmdPrefix}npm run watch"

  private def runProcessSync(command: String): Unit = {
    log.info(s"Running '$command'...")
    val rc = Process(command, base).run(log).exitValue()
    if (rc != 0) {
      throw new Exception(s"$command failed with $rc")
    }
  }

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
      runProcessSync(installCommand)
      IO.write(cacheFile, lastModified.toString)
    }
  }

  override def afterStarted(): Unit = {
    log.info(s"Watching with '$watchCommand'...")
    watchProcess = Some(Process(watchCommand, base).run(log))
  }

  override def afterStopped(): Unit = {
    watchProcess.foreach(_.destroy())
    watchProcess = None
  }
}
