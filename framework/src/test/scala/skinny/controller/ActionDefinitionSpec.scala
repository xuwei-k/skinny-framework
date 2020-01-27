package skinny.controller

import org.scalatest._
import skinny.micro.constant.HttpMethod
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ActionDefinitionSpec extends AnyFlatSpec with Matchers {

  behavior of "ActionDefinition"

  it should "be available" in {
    val method     = HttpMethod.apply("GET")
    val definition = ActionDefinition(Symbol("index"), method, (m: HttpMethod, path: String) => true)

    definition.name should equal(Symbol("index"))
    definition.method should equal(method)
    definition.matcher.apply(null, null) should equal(true)
  }

}
