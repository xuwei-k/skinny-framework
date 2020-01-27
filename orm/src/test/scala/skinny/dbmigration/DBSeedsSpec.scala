package skinny.dbmigration

import scalikejdbc.scalatest.AutoRollback
import org.scalatest.fixture
import skinny.orm.{ Connection, CreateTables }
import org.scalatest.funspec
import org.scalatest.matchers.should.Matchers

class DBSeedsSpec
    extends funspec.FixtureAnyFunSpec
    with Matchers
    with Connection
    with CreateTables // just testing lock condition
    with AutoRollback {

  // see SkinnyORMSpec

}
