package app

import app.actors._
import app.syntax._
import cats.Functor
import cats.effect.{Clock, Concurrent}
import cats.syntax.applicative._
import cats.syntax.flatMap._
import cats.syntax.functor._

object auth {
  def requestNewAuthToken[F[_]](
    implicit F: Functor[F],
    clock: Clock[F]
  ): F[AuthToken] =
    clock.now.map(now => AuthToken(now.plusSeconds(3600), "token"))

  def requestActiveAuthToken[F[_]](
    implicit F: Concurrent[F],
    clock: Clock[F]
  ): F[F[AuthToken]] =
    actor[F, Option[AuthToken], AuthToken](
      initialState = None,
      receive = ref =>
        for {
          existingAuthToken <- ref.get
          now <- clock.now
          authToken <- existingAuthToken
            .filter(_.isActive(now))
            .map(_.pure[F])
            .getOrElse {
              for {
                newAuthToken <- requestNewAuthToken
                _ <- ref.set(Some(newAuthToken))
              } yield newAuthToken
            }
        } yield authToken
    )
}
