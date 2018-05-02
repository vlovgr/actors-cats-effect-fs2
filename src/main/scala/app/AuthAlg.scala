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
  final def requestNewAuthToken: F[AuthToken] =
    timer.now.map(now => AuthToken(now.plusSeconds(3600), "token"))

  final def requestActiveAuthToken: F[F[AuthToken]] =
    for {
      ref <- refOf[F, Option[AuthToken]](None)
      queue <- unboundedQueue[F, Promise[F, AuthToken]]
      fiber <- (for {
        promise <- queue.dequeue1
        existingAuthToken <- ref.get
        now <- timer.now
        authToken <- existingAuthToken
          .filter(_.isActive(now))
          .map(_.pure[F])
          .getOrElse {
            for {
              newAuthToken <- requestNewAuthToken
              _ <- ref.setSync(Some(newAuthToken))
            } yield newAuthToken
          }
        _ <- promise.complete(authToken)
      } yield ()).forever.start
      activeAuthToken = for {
        promise <- promise[F, AuthToken]
        _ <- queue.offer1(promise)
        authToken <- (fiber.join race promise.get)
          .collect { case Right(authToken) => authToken }
      } yield authToken
    } yield activeAuthToken
}
