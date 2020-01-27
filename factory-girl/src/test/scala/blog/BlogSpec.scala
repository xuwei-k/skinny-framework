package blog

import scalikejdbc._
import scalikejdbc.scalatest.AutoRollback

import org.scalatest.fixture
import skinny.test.FactoryGirl
import skinny.logging.Logging
import org.scalatest.funspec
import org.scalatest.matchers.should.Matchers

class BlogSpec extends funspec.FixtureAnyFunSpec with Matchers with Connection with CreateTables with AutoRollback with Logging {

  override def db(): DB = NamedDB(Symbol("fg")).toDB()

  describe("variables") {
    it("should be available") { implicit session =>
      val post = FactoryGirl(Post).withVariables(Symbol("name") -> "Kaz").create()
      post.title should equal("I just started this blog")
      post.body should equal("Hello, everyone! My name is Kaz. And bulah bulah...")
    }

    it("should be available with string interpolation") { implicit session =>
      val post = FactoryGirl(Post, Symbol("post2")).withVariables(Symbol("name") -> "Kaz").create()
      post.title should equal("I just started this blog")
      post.body should not equal ("Hello, everyone! My name is Kaz. And bulah bulah... ${System.currentTimeMillis}")
    }

    it("should throw exception when key is absent") { implicit session =>
      intercept[IllegalStateException] {
        try FactoryGirl(Post).create()
        catch {
          case e: Exception =>
            logger.info(s"Exception: ${e.getClass.getCanonicalName}", e)
            throw e
        }
      }
    }
  }

  describe("non-string values") {
    it("should be accepted") { implicit session =>
      intercept[Exception] {
        FactoryGirl(Post).create(Symbol("name") -> None) // should not accepted as 'None'
      }
    }
  }
}
