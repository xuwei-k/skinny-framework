package skinny.session.jdbc

import skinny.orm._
import scalikejdbc._, SQLInterpolation._
import org.joda.time.DateTime
import java.io._
import grizzled.slf4j.Logging

case class SkinnySession(
    id: Long, createdAt: DateTime, expireAt: DateTime,
    servletSessions: Seq[ServletSession] = Nil,
    attributes: Seq[SkinnySessionAttribute] = Nil) extends Logging {

  import SkinnySession._

  private[this] val results = new scala.collection.concurrent.TrieMap[String, (LastOperation, Any)]

  def getAttribute(name: String): Object = {
    if (results.filter(_._2._1 == Remove).exists(_._1 == name)) {
      null
    } else if (results.filter(_._2._1 == Set).exists(_._1 == name)) {
      results.filter(_._2._1 == Set)
        .find(_._1 == name)
        .get._2._2
        .asInstanceOf[Object]
    } else {
      attributes.find(_.name == name)
        .map { attr => attributeToObject(attr.name, attr.value) }
        .map(_.asInstanceOf[Object]).orNull[Object]
    }
  }

  def setAttribute(name: String, value: Any) = {
    results.update(name, (Set, value))
  }
  def removeAttribute(name: String) = {
    results.update(name, (Remove, None))
  }

  def save(): Unit = {
    results.foreach {
      case (name, (Set, value)) => SkinnySession.setAttributeToDatabase(id, name, value)
      case (name, (Remove, value)) => SkinnySession.removeAttributeFromDatabase(id, name)
    }
  }

  private[this] def attributeToObject(k: String, v: Any): Any = v match {
    case null => null
    case None => null
    case some if some.isInstanceOf[Some[_]] =>
      attributeToObject(k, some.asInstanceOf[Some[_]].orNull[Any](null))
    case bytes: Array[Byte] =>
      try {
        val b = new ByteArrayInputStream(bytes)
        val is = new ObjectInputStream(b)
        attributeToObject(k, is.readObject)
      } catch {
        case e: StreamCorruptedException =>
          logger.info(s"Failed to load attribute for $k because ${e.getMessage}")
          null
      }
    case v => v
  }

  def attributeNames: Seq[String] = attributes.map(_.name) ++ results.filter(_._2._1 == Set).map(_._1)

}

object SkinnySession extends SkinnyCRUDMapper[SkinnySession] with Logging {

  sealed trait LastOperation
  case object Set extends LastOperation
  case object Remove extends LastOperation

  override def tableName = "skinny_sessions"
  override def defaultAlias = createAlias("ss")
  override def extract(rs: WrappedResultSet, n: ResultName[SkinnySession]) = new SkinnySession(
    id = rs.get(n.id),
    createdAt = rs.get(n.createdAt),
    expireAt = rs.get(n.expireAt))

  val servletSessionsAlias = ServletSession.createAlias("svs")
  val servletSessionsRef = {
    hasMany[ServletSession](
      many = ServletSession -> servletSessionsAlias,
      on = (ss, svs) => sqls.eq(ss.id, svs.skinnySessionId),
      merge = (ss, svs) => ss.copy(servletSessions = svs)
    )
  }

  val attributesRef = hasMany[SkinnySessionAttribute](
    many = SkinnySessionAttribute -> SkinnySessionAttribute.createAlias("attrs"),
    on = (s, a) => sqls.eq(s.id, a.skinnySessionId),
    merge = (s, as) => s.copy(attributes = as)
  ).byDefault

  def findOrCreate(jsessionId: String, newJsessionId: Option[String], expireAt: DateTime)(implicit s: DBSession = autoSession): SkinnySession = {
    joins(attributesRef, servletSessionsRef).findBy(sqls.eq(servletSessionsAlias.jsessionId, jsessionId)).map { session =>
      newJsessionId.foreach { newId =>
        joins(servletSessionsRef).findBy(sqls.eq(servletSessionsAlias.jsessionId, newId)).getOrElse {
          insert.into(ServletSession).namedValues(
            ServletSession.column.jsessionId -> newId,
            ServletSession.column.skinnySessionId -> session.id).toSQL.update.apply()
          joins(attributesRef).findById(session.id).get
        }
      }
      // postpone session timeout
      updateById(session.id).withNamedValues(column.expireAt -> expireAt)
      session

    }.getOrElse {
      val skinnySessionId = createWithNamedValues(
        column.createdAt -> DateTime.now,
        column.expireAt -> expireAt
      )
      insert.into(ServletSession).namedValues(
        ServletSession.column.jsessionId -> jsessionId,
        ServletSession.column.skinnySessionId -> skinnySessionId
      ).toSQL.update.apply()
      joins(attributesRef).findById(skinnySessionId).get
    }
  }

  def invalidate(jsessionId: String)(implicit s: DBSession = autoSession): Unit = {
    val sv = servletSessionsAlias
    val idOpt = select(sv.skinnySessionId).from(ServletSession as sv)
      .where.eq(sv.jsessionId, jsessionId).toSQL.map(_.long(1)).single.apply()
    idOpt.foreach { id =>
      delete.from(ServletSession).where.eq(ServletSession.column.skinnySessionId, id).toSQL.update.apply()
      delete.from(SkinnySessionAttribute).where.eq(SkinnySessionAttribute.column.skinnySessionId, id).toSQL.update.apply()
      deleteById(id)
    }
  }

  private[this] def toSerializable(v: Any): Any = v match {
    case null => null
    case None => null
    case Some(v) => toSerializable(v)
    case v => {
      val bytes = new ByteArrayOutputStream
      val out = new ObjectOutputStream(bytes)
      out.writeObject(v)
      bytes.toByteArray
    }
  }

  private def setAttributeToDatabase(id: Long, name: String, value: Any)(implicit s: DBSession = autoSession): Unit = {
    if (name != null) {
      val c = SkinnySessionAttribute.column
      val namedValues = Seq(
        c.skinnySessionId -> id,
        c.name -> name,
        c.value -> toSerializable(value)
      )
      // easy upsert implementation
      try {
        if (update(SkinnySessionAttribute).set(namedValues: _*)
          .where.eq(c.skinnySessionId, id).and.eq(c.name, name)
          .toSQL.update.apply() == 0) {

          insert.into(SkinnySessionAttribute).namedValues(namedValues: _*).toSQL.update.apply()
        }
      } catch {
        case e: Exception =>
          try update(SkinnySessionAttribute).set(namedValues: _*)
            .where.eq(c.skinnySessionId, id).and.eq(c.name, name)
            .toSQL.update.apply()
          catch {
            case e: Exception =>
              logger.info(s"Failed to set attribute ($name -> $value) for id: ${id}")
          }
      }
    }
  }

  private def removeAttributeFromDatabase(id: Long, name: String)(implicit s: DBSession = autoSession): Unit = {
    val c = SkinnySessionAttribute.column
    delete.from(SkinnySessionAttribute).where.eq(c.skinnySessionId, id).and.eq(c.name, name)
      .toSQL.update.apply()
  }

}
