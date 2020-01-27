package controller
import org.scalatest._
import skinny.test._
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
class FileUploadControllerSpec extends AnyFunSpec with Matchers {
  def createMockController = new FileUploadController with MockServlet
  describe("FileUploadController") {
    it("should work with MockServlet") {
      try {
        val controller = createMockController
        controller.form
        controller.status should equal(200)
      } catch {
        case e: Exception =>
          e.printStackTrace
          throw e
      }
    }
  }
}
