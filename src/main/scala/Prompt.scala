package io.github.windymelt.prompt

import scala.util.matching.Regex
import scala.io.StdIn
import scala.util.boundary
import boundary.break
import scala.io.AnsiColor
import Process.system

object Prompt {
  // Validator for input string.
  // e.g. regex validation, conditional validation, parse sth.
  type Validator[A] = String => Either[String, A]

  enum AcceptedPatternLiteral:
    case String, Boolean
  type AcceptedPattern = Seq[String] | Regex | AcceptedPatternLiteral

  case class DetailedPromptItem[A](
      description: String,
      pattern: AcceptedPattern,
      validator: Validator[A] = Right(_)
  )
  type PromptItem[A] = String | DetailedPromptItem[A]

  def _parse[A](pi: PromptItem[A]): Either[String, A] = pi match {
    case q: String =>
      _promptString[A](
        DetailedPromptItem[A](
          q,
          AcceptedPatternLiteral.String,
          Right(_).asInstanceOf[Either[String, A]]
        )
      )
    case dp @ DetailedPromptItem[A](
          description,
          pattern,
          validator
        ) => // TODO: treat pattern
      pattern match {
        case AcceptedPatternLiteral.String => _promptString(dp)
        case AcceptedPatternLiteral.Boolean => _promptBoolean(dp).asInstanceOf[Either[String, A]] // dirty fix
        case xs: Seq[String]               => _promptList(dp)
        case _                             => ??? // WIP
      }
  }

  def _promptBoolean(dp: DetailedPromptItem[?]): Either[String, Boolean] = {
    val result = boundary:
      system("sh", "-c", "stty -icanon min 1 < /dev/tty")
      system("sh", "-c", "stty -echo < /dev/tty")
      system("sh", "-c", "tput civis")

      // move cursor to head
      print("\r")
      print("\u001b[s") // save cursor pos
      def loop: Nothing = {
        print("\u001b[u") // restore cursor pos
        print(s"${AnsiColor.CYAN}? ${AnsiColor.RESET} ${dp.description} (y/n) ")
        val key = Console.in.read()
        key match {
          case 121 /* y */ => println("y"); break(true)
          case 110 /* n */ => println("n"); break(false)
          case n => loop
        }
      }
      loop

    system("sh", "-c", "stty icanon < /dev/tty")
    system("sh", "-c", "stty echo < /dev/tty")
    system("sh", "-c", "tput cnorm")

    Right(result)
  }

  def _promptString[A](dp: DetailedPromptItem[A]): Either[String, A] = for {
    raw <- Right(
      StdIn
        .readLine(s"${AnsiColor.CYAN}? ${AnsiColor.RESET} ${dp.description} > ")
        .stripLineEnd
    )
    validated <- dp.validator(raw)
  } yield validated

  def _promptList[A](dp: DetailedPromptItem[A]): Either[String, A] = {
    import sys.process._

    def printRow[A](sth: A, isCurrentRow: Boolean, isFinalRow: Boolean): Unit =
      (isCurrentRow, isFinalRow) match {
        case (false, false) => println(s" $sth")
        case (false, true)  => print(s" $sth")
        case (true, false) =>
          println(
            s"${AnsiColor.GREEN}${AnsiColor.UNDERLINED}*${sth}${AnsiColor.RESET}"
          )
        case (true, true) =>
          print(
            s"${AnsiColor.GREEN}${AnsiColor.UNDERLINED}*${sth}${AnsiColor.RESET}"
          )
      }

    val result = boundary:
      system("sh", "-c", "stty -icanon min 1 < /dev/tty")
      system("sh", "-c", "stty -echo < /dev/tty")
      system("sh", "-c", "tput civis")

      var selected = 0
      val rows = dp.pattern.asInstanceOf[Seq[A]]
      // make space
      for (_ <- 0 until rows.size) {
        println()
      }
      // move cursor to head
      print("\r")
      // move cursor upward [row] times
      for (_ <- 0 until rows.size) {
        print("\u001b[1A")
      } // octal literal is not supported; 033
      print("\u001b[s") // save cursor pos
      def loop: Nothing = {
        print("\u001b[u") // restore cursor pos
        println(s"${AnsiColor.CYAN}? ${AnsiColor.RESET} ${dp.description}")
        // render items (default row is emphasized)
        for (r <- rows.zipWithIndex) {
          printRow(r._1, r._2 == selected, r._2 == rows.size - 1)
        }
        // accept keys to move rows
        val key = Console.in.read()
        key match {
          case 10 /* enter */ => break(rows(selected))
          case 27 /* escape seq */ =>
            val (a, b) = (Console.in.read(), Console.in.read())
            (a, b) match {
              case (91, 65) => // up
                selected = (selected - 1 + rows.size) % rows.size
                loop
              case (91, 66) => // down
                selected = (selected + 1 + rows.size) % rows.size
                loop
            }
          case _ => loop
        }
      }
      loop

    system("sh", "-c", "stty icanon < /dev/tty")
    system("sh", "-c", "stty echo < /dev/tty")
    system("sh", "-c", "tput cnorm")

    Right(result)
  }

}
