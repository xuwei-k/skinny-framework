package controller

import org.scalatest._
import skinny.micro.Format
import skinny.test.MockApiController
import unit.DBSettings
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class AngularProgrammersControllerSpec extends AnyFunSpec with Matchers with DBSettings {

  def createMockController = new AngularXHRProgrammersController with MockApiController

  describe("AngularXHRProgrammersController") {

    it("shows Angular familiar JSON response") {
      val controller = createMockController
      val json       = controller.showResources()(Format.JSON)
      controller.status should equal(200)
      json.toString should startWith(")]}',\n[")
    }

    it("accepts JSON request body") {
      val controller = createMockController
      controller.prepareJSONBodyRequest(
        """{"name": "foo", "favoriteNumber": 123, "plainTextPassword": "12345abcde"}"""
      )
      controller.createResource()
      controller.status should equal(201)
    }

    it("rejects invalid JSON request body") {
      val controller = createMockController
      controller.prepareJSONBodyRequest("""{"name": "foo", favoriteNumber": 123, "plainTextPassword": "12345abcde"}""")
      controller.createResource()
      controller.status should equal(400)
    }
  }

}
