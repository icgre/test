package mesosphere.marathon.state

import mesosphere.UnitTest
import mesosphere.marathon.state.InstanceHandling.KillSelection.{ OldestFirst, YoungestFirst }

class InstanceHandlingTest extends UnitTest {

  "InstanceHandling.KillSelection" should {

    "parse all value 'YoungestFirst'" in {
      InstanceHandling.KillSelection.withName("YoungestFirst") should be(YoungestFirst)
    }

    "parse all value 'OldestFirst'" in {
      InstanceHandling.KillSelection.withName("OldestFirst") should be(OldestFirst)
    }
  }

  it should {
    "throw an exception for an invalid value" in {
      the[NoSuchElementException] thrownBy {
        InstanceHandling.KillSelection.withName("youngestFirst")
      } should have message ("There is no KillSelection with name 'youngestFirst'")
    }
  }

  "InstanceHandling.YoungestFirst" should {
    "select the younger timestamp" in {
      YoungestFirst(Timestamp.zero, Timestamp(1)) should be(false)
      YoungestFirst(Timestamp(1), Timestamp.zero) should be(true)
    }
  }

  "InstanceHandling.OldestFirst" should {
    "select the older timestamp" in {
      OldestFirst(Timestamp.zero, Timestamp(1)) should be(true)
      OldestFirst(Timestamp(1), Timestamp.zero) should be(false)
    }
  }
}
