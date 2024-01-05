package io.github.windymelt.prompt

object Process {
  def system(cmd: String*): Unit = {
    val proc = java.lang.ProcessBuilder(cmd*).start()
    proc.waitFor()
    ()
  }
}
