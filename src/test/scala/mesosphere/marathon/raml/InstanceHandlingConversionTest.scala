package mesosphere.marathon
package raml

import mesosphere.marathon.state
import mesosphere.UnitTest

import scala.concurrent.duration._

class InstanceHandlingConversionTest extends UnitTest {

  "InstanceHandlingConversion" should {
    "read from RAML" in {
      val raml = InstanceHandling()

      val result: state.InstanceHandling = InstanceHandlingConversion.ramlRead(raml)

      result.unreachableInactiveAfter should be(5.minutes)
      result.unreachableExpungeAfter should be(10.minutes)
    }
  }

  it should {
    "write to RAML" in {
      val strategy = state.InstanceHandling(10.minutes, 20.minutes)

      val raml: InstanceHandling = InstanceHandlingConversion.ramlWrite(strategy)

      raml.unreachableInactiveAfterSeconds should be(600)
      raml.unreachableExpungeAfterSeconds should be(1200)
    }
  }
}
