package app

import app.syntax._
import cats.effect._
import cats.implicits._
import fs2.async._

import scala.concurrent.ExecutionContext

abstract class AuthAlg[F[_]](
  implicit F: ConcurrentEffect[F],
  context: ExecutionContext,
  timer: Timer[F]
) {
  final def requestNewAuthToken: F[AuthToken] = ???

  final def requestActiveAuthToken: F[F[AuthToken]] = ???
}
