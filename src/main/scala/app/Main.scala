package app

import app.auth.requestActiveAuthToken
import cats.effect.{ExitCode, IO, IOApp}

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    for {
      requestActiveAuthToken <- requestActiveAuthToken[IO]
      activeAuthToken <- requestActiveAuthToken
      _ <- IO(println(activeAuthToken))
    } yield ExitCode.Success
  }
}
