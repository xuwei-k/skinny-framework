package controller

import org.scalatest._
import skinny.test.MockApiController
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class SampleTxApiControllerSpec extends AnyFunSpec with Matchers {

  def createMockController = new SampleTxApiController with MockApiController

  describe("SampleTxApiController") {
    it("throws error") {
      val controller = createMockController
      intercept[RuntimeException] {
        controller.index
      }
    }
  }

}
