package controller

import org.scalatest._
import skinny.test.MockController
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class DashboardControllerSpec extends AnyFunSpec with Matchers {

  def createMockController = new DashboardController with MockController

  describe("DashboardController") {
    it("works with Futures") {
      val controller = createMockController
      controller.index
      controller.status should equal(200)
    }
  }

}
