package skinny.task

import org.scalatest._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class AssetsPrecompileTaskSpec extends AnyFlatSpec with Matchers {

  it should "be available" in {
    AssetsPrecompileTask.main(Array("tmp"))
  }

}
