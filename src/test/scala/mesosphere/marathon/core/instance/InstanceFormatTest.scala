package mesosphere.marathon
package core.instance

import mesosphere.UnitTest
import mesosphere.marathon.state.InstanceHandling
import play.api.libs.json._

import scala.concurrent.duration._

class InstanceFormatTest extends UnitTest {

  import Instance._

  "Instance.KillSelectionFormat" should {
    "create a proper JSON object from YoungestFirst" in {
      val json = Json.toJson(InstanceHandling.KillSelection.YoungestFirst)
      json.as[String] should be("YoungestFirst")
    }

    "create a proper JSON object from OldestFirst" in {
      val json = Json.toJson(InstanceHandling.KillSelection.OldestFirst)
      json.as[String] should be("OldestFirst")
    }
  }

  "Instance.instanceHandlingFormat" should {
    "parse a proper JSON" in {
      val json = Json.parse("""{ "unreachableInactiveAfter": 1, "unreachableExpungeAfter": 2, "killSelection": "YoungestFirst" }""")
      json.as[InstanceHandling].killSelection should be(InstanceHandling.KillSelection.YoungestFirst)
      json.as[InstanceHandling].unreachableInactiveAfter should be(1.second)
      json.as[InstanceHandling].unreachableExpungeAfter should be(2.seconds)
    }

    "not parse a JSON with empty fields" in {
      val json = Json.parse("""{ "unreachableExpungeAfter": 2 }""")
      a[JsResultException] should be thrownBy { json.as[InstanceHandling] }
    }

    "fail on an invalid kill selection" in {
      val json = Json.parse("""{ "unreachableInactiveAfter": 1, "unreachableExpungeAfter": 2, "killSelection": "youngestFirst" }""")
      the[JsResultException] thrownBy {
        json.as[InstanceHandling]
      } should have message ("JsResultException(errors:List((/killSelection,List(ValidationError(List(There is no KillSelection with name 'youngestFirst'),WrappedArray())))))")
    }
  }

  "Instance.instanceFormat" should {
    "fill InstanceHandling with defaults if empty" in {
      val json = Json.parse(
        """{ "instanceId": { "idString": "app.instance-1337" },
          |  "tasksMap": {},
          |  "runSpecVersion": "2015-01-01",
          |  "agentInfo": { "host": "localhost", "attributes": [] },
          |  "state": { "since": "2015-01-01", "condition": { "str": "Running" } }
          |}""".stripMargin)
      val instance = json.as[Instance]

      instance.instanceHandling.killSelection should be(InstanceHandling.DefaultKillSelection)
      instance.instanceHandling.unreachableInactiveAfter should be(InstanceHandling.DefaultTimeUntilInactive)
      instance.instanceHandling.unreachableExpungeAfter should be(InstanceHandling.DefaultTimeUntilExpunge)
    }
  }
}
