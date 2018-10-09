package app

import java.time.Instant

import cats.effect.Clock
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.{Functor, MonadError}

import scala.concurrent.duration.MILLISECONDS

object syntax {
  final case class NotCollectedException[A](a: A)
      extends RuntimeException(s"value not collected: $a")

  implicit final class MonadErrorThrowableSyntax[F[_], A](private val fa: F[A]) extends AnyVal {
    def collect[B](pf: PartialFunction[A, B])(implicit F: MonadError[F, Throwable]): F[B] =
      fa.flatMap {
        pf.andThen(F.pure)
          .applyOrElse(_, (a: A) => {
            F.raiseError[B](NotCollectedException(a))
          })
      }
  }

  implicit final class ClockSyntax[F[_]](private val clock: Clock[F]) extends AnyVal {
    def now(implicit F: Functor[F]): F[Instant] =
      clock.realTime(MILLISECONDS).map(Instant.ofEpochMilli)
  }
}
