package skinny.validator

import org.scalatest._

import org.joda.time._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class futureSpec extends AnyFlatSpec with Matchers {

  behavior of "future"

  it should "be available" in {
    val validate = future
    validate.name should equal("future")

    validate(param("x" -> null)).isSuccess should equal(false)

    validate(param("x" -> DateTime.now.minusDays(1))).isSuccess should equal(false)

    val judPast = DateTime.now.minusDays(1).toDate
    validate(param("x" -> judPast)).isSuccess should equal(false)

    validate(param("x" -> DateTime.now.plusDays(1))).isSuccess should equal(true)

    val judFuture = DateTime.now.plusDays(1).toDate
    validate(param("x" -> judFuture)).isSuccess should equal(true)
  }

}
