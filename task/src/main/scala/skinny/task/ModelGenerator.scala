package skinny.task

import java.io.File
import org.apache.commons.io.FileUtils

/**
 * Model generator.
 */
object ModelGenerator extends ModelGenerator

trait ModelGenerator extends CodeGenerator {

  private def showUsage = {
    println("Usage: sbt \"task/run g model member name:String birthday:Option[LocalDate]\"")
  }

  def run(args: List[String]) {
    args.toList match {
      case name :: attributes =>
        val attributePairs: Seq[(String, String)] = attributes.flatMap { attribute =>
          attribute.toString.split(":") match {
            case Array(k, v) => Some(k -> v)
            case _ => None
          }
        }
        generate(name, None, attributePairs)
      case _ => showUsage
    }
  }

  def generate(name: String, tableName: Option[String], attributePairs: Seq[(String, String)]) {
    val modelClassName = toClassName(name)
    val productionFile = new File(s"src/main/scala/model/${modelClassName}.scala")
    val productionCode =
      s"""package model
        |
        |import skinny.orm._, feature._
        |import scalikejdbc._, SQLInterpolation._
        |import org.joda.time._
        |
        |case class ${modelClassName}(
        |  id: Long,
        |${attributePairs.map { case (k, t) => s"  ${k}: ${addDefaultValueIfOption(t)}" }.mkString(",\n")},
        |  createdAt: DateTime,
        |  updatedAt: Option[DateTime] = None
        |)
        |
        |object ${modelClassName} extends SkinnyCRUDMapper[${modelClassName}] with TimestampsFeature[${modelClassName}] {
        |${tableName.map(t => "  override val tableName = \"" + t + "\"").getOrElse("")}
        |  override val defaultAlias = createAlias("${modelClassName.head.toLower}")
        |
        |  override def extract(rs: WrappedResultSet, rn: ResultName[${modelClassName}]): ${modelClassName} = new ${modelClassName}(
        |    id = rs.long(rn.id),
        |${attributePairs.map { case (k, t) => "    " + k + "= rs." + toExtractorMethodName(t) + "(rn." + k + ")" }.mkString(",\n")},
        |    createdAt = rs.dateTime(rn.createdAt),
        |    updatedAt = rs.dateTimeOpt(rn.updatedAt)
        |  )
        |}
        |""".stripMargin
    writeIfAbsent(productionFile, productionCode)
  }

  def generateSpec(name: String, attributePairs: Seq[(String, String)]) {
    val modelClassName = toClassName(name)
    val specFile = new File(s"src/test/scala/model/${modelClassName}Spec.scala")
    FileUtils.forceMkdir(specFile.getParentFile)
    val specCode =
      s"""package model
        |
        |import org.scalatra.test.scalatest._
        |import skinny.test._
        |import scalikejdbc._, SQLInterpolation._, test._
        |import org.joda.time._
        |import model._
        |
        |class ${modelClassName}Spec extends ScalatraFlatSpec with AutoRollback {
        |}
        |""".stripMargin
    writeIfAbsent(specFile, specCode)
  }
}
