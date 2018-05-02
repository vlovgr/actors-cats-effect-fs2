package app

import app.actors._
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
    actor[F, Option[AuthToken], AuthToken](
      initialState = None,
      receive = ref =>
        for {
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
        } yield authToken
    )
}
