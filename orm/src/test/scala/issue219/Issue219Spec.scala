package issue219

import org.scalatest._
import scalikejdbc._
import scalikejdbc.scalatest.AutoRollback
import skinny.dbmigration.DBSeeds
import skinny.orm._
import org.scalatest.funspec
import org.scalatest.matchers.should.Matchers

trait Connection {
  Class.forName("org.h2.Driver")
  ConnectionPool.add(Symbol("issue219"), "jdbc:h2:mem:issue219;MODE=PostgreSQL", "sa", "sa")
}

trait CreateTables extends DBSeeds { self: Connection =>

  override val dbSeedsAutoSession = NamedAutoSession(Symbol("issue219"))

  addSeedSQL(
    sql"""
create table user (
  id bigserial not null,
  name varchar(100) not null)
"""
  )
  addSeedSQL(
    sql"""
create table article (
  id bigserial not null,
  title varchar(100) not null,
  user_id bigint references user(id))
"""
  )
  addSeedSQL(
    sql"""
create table tag (
  id bigserial not null,
  name varchar(100) not null,
  article_id bigint references article(id))
"""
  )
  runIfFailed(sql"select count(1) from article")
}

class Issue219Spec extends funspec.FixtureAnyFunSpec with Matchers with Connection with CreateTables with AutoRollback {

  case class User(id: Long, name: String)
  case class Article(id: Long, title: String, userId: Option[Long], user: Option[User] = None, tags: Seq[Tag] = Nil)
  case class Tag(id: Long, name: String, articleId: Option[Long], article: Option[Article] = None)

  object User extends SkinnyCRUDMapper[User] {
    override val connectionPoolName = Symbol("issue219")
    override def defaultAlias       = createAlias("u")

    override def extract(rs: WrappedResultSet, rn: ResultName[User]) = autoConstruct(rs, rn)
  }
  object Article extends SkinnyCRUDMapper[Article] {
    override val connectionPoolName                                     = Symbol("issue219")
    override def defaultAlias                                           = createAlias("a")
    override def extract(rs: WrappedResultSet, rn: ResultName[Article]) = autoConstruct(rs, rn, "user", "tags")

    lazy val userRef = {
      belongsTo[User](
        right = User,
        merge = (a, u) => a.copy(user = u)
      ).includes[User](
        (as, us) =>
          as.map { a =>
            us.find(u => a.user.exists(_.id == u.id))
              .map(u => a.copy(user = Some(u)))
              .getOrElse(a)
        }
      )
    }

    lazy val tagsRef = hasMany[Tag](
      many = Tag -> Tag.defaultAlias,
      on = (a, t) => sqls.eq(a.id, t.articleId),
      merge = (a, ts) => a.copy(tags = ts)
    ).includes[Tag](
      (as, tags) =>
        as.map { a =>
          a.copy(tags = tags.filter(_.articleId.exists(_ == a.id)))
      }
    )
  }
  object Tag extends SkinnyCRUDMapper[Tag] {
    override val connectionPoolName                                 = Symbol("issue219")
    override def defaultAlias                                       = createAlias("t")
    override def extract(rs: WrappedResultSet, rn: ResultName[Tag]) = autoConstruct(rs, rn, "article")

    lazy val articleRef = {
      belongsTo[Article](
        right = Article,
        merge = (t, a) => t.copy(article = a)
      ).includes[Article](
        (ts, as) =>
          ts.map { t =>
            as.find(a => t.article.exists(_.id == a.id))
              .map(a => t.copy(article = Some(a)))
              .getOrElse(t)
        }
      )
    }
  }

  import Article._

  override def db(): DB = NamedDB(Symbol("issue219")).toDB()

  override def fixture(implicit session: DBSession): Unit = {
    val aliceId = User.createWithAttributes(Symbol("name") -> "Alice")
    val bobId   = User.createWithAttributes(Symbol("name") -> "Bob")
    Seq(
      ("Hello World", Some(aliceId)),
      ("Getting Started with Scala", Some(bobId)),
      ("Functional Programming", None),
      ("How to user sbt", Some(aliceId))
    ).foreach {
      case (title, userId) =>
        Article.createWithAttributes(Symbol("title") -> title, Symbol("userId") -> userId)
    }
    val articles = Article.limit(2).apply()
    Tag.createWithAttributes(Symbol("name") -> "Technical", Symbol("articleId")   -> articles(0).id)
    Tag.createWithAttributes(Symbol("name") -> "Programming", Symbol("articleId") -> articles(0).id)
    Tag.createWithAttributes(Symbol("name") -> "Scala", Symbol("articleId")       -> articles(1).id)
  }

  def id(implicit session: DBSession): Long = {
    Article.where(Symbol("title") -> "Functional Programming").apply().head.id
  }

  describe("find without associations") {
    it("should return results as expected") { implicit session =>
      Article.findAll().size should equal(4)
      Article.findById(id).isDefined should equal(true)
    }
  }

  describe("find with #joins associations") {
    it("should return results as expected") { implicit session =>
      Article.joins(userRef).findAll().size should equal(4)
      Article.joins(userRef, tagsRef).findAll().size should equal(4)

      Article.joins(userRef).findById(id).isDefined should equal(true)
      Article.joins(userRef, tagsRef).findById(id).isDefined should equal(true)
    }
  }

  describe("find with #includes associations") {
    it("should return results as expected") { implicit session =>
      Article.includes(userRef).findAll().size should equal(4)
      Article.joins(tagsRef).includes(userRef).findAll().size should equal(4)

      // issue #219 findById with #includes doesn't return a result as expected when associations are absent
      Article.includes(userRef).findById(id).isDefined should equal(true)
      Article.includes(userRef, tagsRef).findById(id).isDefined should equal(true)
    }
  }

  describe("find with #includes and #joins associations") {
    it("should return results as expected") { implicit session =>
      Article.joins(userRef).includes(tagsRef).findById(id).isDefined should equal(true)
      Article.joins(tagsRef).includes(userRef).findById(id).isDefined should equal(true)
    }
  }

}
