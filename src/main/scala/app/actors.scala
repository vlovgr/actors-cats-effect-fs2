package app

import app.syntax._
import cats.effect.ConcurrentEffect
import cats.syntax.flatMap._
import cats.syntax.functor._
import fs2.async._

import scala.concurrent.ExecutionContext

object actors {
  def actor[F[_], S, O](
    initialState: S,
    receive: Ref[F, S] => F[O]
  )(implicit F: ConcurrentEffect[F],
    context: ExecutionContext
  ): F[F[O]] = {
    for {
      ref <- refOf[F, S](initialState)
      queue <- unboundedQueue[F, Promise[F, O]]
      fiber <- (for {
        promise <- queue.dequeue1
        output <- receive(ref)
        _ <- promise.complete(output)
      } yield ()).forever.start
      ask = for {
        promise <- promise[F, O]
        _ <- queue.offer1(promise)
        output <- (fiber.join race promise.get)
          .collect { case Right(authToken) => authToken }
      } yield output
    } yield ask
  }

  def actorWithInput[F[_], S, I, O](
    initialState: S,
    receive: (I, Ref[F, S]) => F[O]
  )(implicit F: ConcurrentEffect[F],
    context: ExecutionContext
  ): F[I => F[O]] = {
    for {
      ref <- refOf[F, S](initialState)
      queue <- unboundedQueue[F, (I, Promise[F, O])]
      fiber <- (for {
        inputAndPromise <- queue.dequeue1
        (input, promise) = inputAndPromise
        output <- receive(input, ref)
        _ <- promise.complete(output)
      } yield ()).forever.start
      ask = (input: I) =>
        for {
          promise <- promise[F, O]
          _ <- queue.offer1((input, promise))
          output <- (fiber.join race promise.get)
            .collect { case Right(o) => o }
        } yield output
    } yield ask
  }
}
