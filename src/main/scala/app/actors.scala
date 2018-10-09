package app

import app.syntax._
import cats.effect.Concurrent
import cats.effect.concurrent.{Deferred, Ref}
import cats.effect.syntax.concurrent._
import cats.syntax.flatMap._
import cats.syntax.functor._
import fs2.concurrent.Queue

object actors {
  def actor[F[_], S, O](
    initialState: S,
    receive: Ref[F, S] => F[O]
  )(implicit F: Concurrent[F]): F[F[O]] =
    for {
      ref <- Ref.of[F, S](initialState)
      queue <- Queue.unbounded[F, Deferred[F, O]]
      fiber <- (for {
        deferred <- queue.dequeue1
        output <- receive(ref)
        _ <- deferred.complete(output)
      } yield ()).foreverM.void.start
      ask = for {
        deferred <- Deferred[F, O]
        _ <- queue.offer1(deferred)
        output <- (fiber.join race deferred.get)
          .collect { case Right(o) => o }
      } yield output
    } yield ask

  def actorWithInput[F[_], S, I, O](
    initialState: S,
    receive: (I, Ref[F, S]) => F[O]
  )(implicit F: Concurrent[F]): F[I => F[O]] =
    for {
      ref <- Ref.of[F, S](initialState)
      queue <- Queue.unbounded[F, (I, Deferred[F, O])]
      fiber <- (for {
        inputAndDeferred <- queue.dequeue1
        (input, deferred) = inputAndDeferred
        output <- receive(input, ref)
        _ <- deferred.complete(output)
      } yield ()).foreverM.void.start
      ask = (input: I) =>
        for {
          deferred <- Deferred[F, O]
          _ <- queue.offer1((input, deferred))
          output <- (fiber.join race deferred.get)
            .collect { case Right(o) => o }
        } yield output
    } yield ask
}
