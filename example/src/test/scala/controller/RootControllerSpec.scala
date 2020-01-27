package controller

import org.scalatest._
import skinny.test.MockController
import skinny.DBSettings
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class RootControllerSpec extends AnyFunSpec with Matchers with DBSettings {

  describe("RootController") {

    def createMockController = new RootController with MockController

    describe("skinny session filter") {
      it("works") {
        val controller = createMockController
        controller.index
        controller.status should equal(200)
      }
    }

  }
}
