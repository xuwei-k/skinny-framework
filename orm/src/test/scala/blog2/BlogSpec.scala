package blog2

import scalikejdbc._
import scalikejdbc.scalatest.AutoRollback

import org.scalatest.{ Tag => _, _ }
import org.scalatest.funspec
import org.scalatest.matchers.should.Matchers

class BlogSpec extends funspec.FixtureAnyFunSpec with Matchers with Connection with CreateTables with AutoRollback {

  override def db(): DB = NamedDB(Symbol("blog2")).toDB()

  override def fixture(implicit session: DBSession): Unit = {
    val postId =
      Post.createWithAttributes(Symbol("title") -> "Hello World!", Symbol("body") -> "This is the first entry...")
    val scalaTagId = Tag.createWithAttributes(Symbol("name") -> "Scala")
    val rubyTagId  = Tag.createWithAttributes(Symbol("name") -> "Ruby")
    val pt         = PostTag.column
    insert.into(PostTag).namedValues(pt.postId -> postId, pt.tagId -> scalaTagId).toSQL.update.apply()
    insert.into(PostTag).namedValues(pt.postId -> postId, pt.tagId -> rubyTagId).toSQL.update.apply()
  }

  describe("hasManyThrough without byDefault") {
    it("should work as expected") { implicit session =>
      val id   = Post.limit(1).apply().head.id
      val post = Post.joins(Post.tagsRef).findById(id)
      post.get.tags.size should equal(2)
    }

    it("should work when joining twice") { implicit session =>
      val id   = Post.limit(1).apply().head.id
      val post = Post.joins(Post.tagsRef, Post.tagsRef).findById(id)
      post.get.tags.size should equal(2)
    }
  }
}
