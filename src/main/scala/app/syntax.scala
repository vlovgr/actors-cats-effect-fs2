package app

import java.time.Instant

import cats.effect.{Concurrent, Fiber, Timer}
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.{FlatMap, Functor, MonadError}

import scala.concurrent.duration.MILLISECONDS

object syntax {
  final case class NotCollectedException[A](a: A)
      extends RuntimeException(s"value not collected: $a")

  implicit final class ConcurrentSyntax[F[_], A](val fa: F[A])(implicit F: Concurrent[F]) {
    def start: F[Fiber[F, A]] =
      F.start(fa)

    def race[B](fb: F[B]): F[Either[A, B]] =
      F.race(fa, fb)
  }

  implicit final class FlatMapSyntax[F[_]: FlatMap, A](val fa: F[A]) {
    def forever: F[A] = fa.flatMap(_ => forever)
  }

  implicit final class MonadErrorThrowableSyntax[F[_], A](val fa: F[A])(
    implicit F: MonadError[F, Throwable]
  ) {
    def collect[B](pf: PartialFunction[A, B]): F[B] = {
      fa.flatMap {
        pf.andThen(F.pure)
          .applyOrElse(_, (a: A) => {
            F.raiseError[B](NotCollectedException(a))
          })
      }
    }
  }

  implicit final class TimerSyntax[F[_]: Functor](timer: Timer[F]) {
    def now: F[Instant] =
      timer
        .clockRealTime(MILLISECONDS)
        .map(Instant.ofEpochMilli)
  }
}
