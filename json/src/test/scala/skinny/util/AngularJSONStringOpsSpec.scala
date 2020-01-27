package skinny.util

import org.scalatest._
import skinny.json.AngularJSONStringOps
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class AngularJSONStringOpsSpec extends AnyFlatSpec with Matchers {

  behavior of "AngularJSONStringOps"

  case class Sample(firstName: String, age: Int)

  it should "be available" in {
    new AngularJSONStringOps {
      useJSONVulnerabilityProtection should equal(true)
      useUnderscoreKeysForJSON should equal(false)
    }
  }

}
