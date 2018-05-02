package app

import cats.effect.{IO, Timer}

import scala.concurrent.ExecutionContext

trait SafeApp {
  def run(args: List[String]): IO[Unit]

  protected implicit lazy val executionContext: ExecutionContext =
    scala.concurrent.ExecutionContext.global

  protected implicit lazy val timer: Timer[IO] =
    IO.timer(executionContext)

  final def main(args: Array[String]): Unit =
    run(args.toList).unsafeRunSync()
}
