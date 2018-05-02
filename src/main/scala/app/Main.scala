package app

import cats.effect.IO

object Main extends SafeApp {
  override def run(args: List[String]): IO[Unit] = {
    val auth = new AuthAlg[IO]() {}

    for {
      requestActiveAuthToken <- auth.requestActiveAuthToken
      activeAuthToken <- requestActiveAuthToken
      _ <- IO(println(activeAuthToken))
    } yield ()
  }
}
