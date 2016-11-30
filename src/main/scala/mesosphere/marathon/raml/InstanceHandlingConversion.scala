package mesosphere.marathon
package raml

import mesosphere.marathon.state
import scala.concurrent.duration._

/**
  * Conversion from [[mesosphere.marathon.state.InstanceHandling]] to [[mesosphere.marathon.raml.InstanceHandling]].
  */
trait InstanceHandlingConversion {

  implicit val ramlRead = Reads[InstanceHandling, state.InstanceHandling] { handling =>
    state.InstanceHandling(
      unreachableInactiveAfter = handling.unreachableInactiveAfterSeconds.seconds,
      unreachableExpungeAfter = handling.unreachableExpungeAfterSeconds.seconds)
  }

  implicit val ramlWrite = Writes[state.InstanceHandling, InstanceHandling]{ handling =>
    InstanceHandling(
      unreachableInactiveAfterSeconds = handling.unreachableInactiveAfter.toSeconds,
      unreachableExpungeAfterSeconds = handling.unreachableExpungeAfter.toSeconds)
  }
}

object InstanceHandlingConversion extends InstanceHandlingConversion
