import scala.sys.process.Process
import sbt._

object ProcessIO extends ProcessIO

trait ProcessIO {
  val isWindows = sys.props("os.name").toLowerCase().contains("win")
  val cmdPrefix = if (isWindows) "cmd /c " else ""

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
