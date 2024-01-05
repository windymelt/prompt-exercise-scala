package io.github.windymelt.prompt

object Main {
  @main def hello: Unit =
    println(Prompt._parse[Boolean](Prompt.DetailedPromptItem("boolean", Prompt.AcceptedPatternLiteral.Boolean, _ => Right(true))))
    println(Prompt._parse[String]("Your name"))
    println(
      Prompt._parse[String](
        Prompt.DetailedPromptItem(
          "number",
          (0 to 10).map(_.toString()).toSeq,
        )
      )
    )
}