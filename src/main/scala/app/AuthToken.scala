package app

import java.time.Instant

final case class AuthToken(
  expiresAt: Instant,
  value: String
) {
  def isActive(now: Instant): Boolean =
    now isBefore expiresAt
}
