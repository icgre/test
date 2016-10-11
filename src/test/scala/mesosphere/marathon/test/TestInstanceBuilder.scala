package mesosphere.marathon
package test

import mesosphere.marathon.stream._
import mesosphere.marathon.core.condition.Condition
import mesosphere.marathon.core.instance.Instance
import mesosphere.marathon.core.instance.update.InstanceUpdateOperation
import mesosphere.marathon.core.launcher.InstanceOpFactory
import mesosphere.marathon.core.launcher.impl.InstanceOpFactoryImpl
import mesosphere.marathon.core.task.Task
import mesosphere.marathon.state.{AppDefinition, RunSpec}
import mesosphere.marathon.tasks.PortsMatch
import mesosphere.mesos.ResourceMatcher.ResourceMatch
import mesosphere.mesos.TaskBuilder
import mesosphere.test.Clocked
import org.apache.mesos

class TestInstanceBuilder() extends Clocked {
  private[this] val config = MarathonTestHelper.defaultConfig()
  private[this] val opFactory = new InstanceOpFactoryImpl(config)(clock)
  private[this] val sufficientOffer = MarathonTestHelper.makeBasicOffer(
    cpus = 100,
    mem = 100000,
    disk = 100000
  ).build()

  def buildFrom(runSpec: RunSpec): Instance = {
    val app = runSpec match {
      case app: AppDefinition => app
      case _ => ???
    }

    // TODO: pass a case class containing hostname, agentId, attributes and frameworkId instead
    val attributes = Seq.empty[mesos.Protos.Attribute]
    val offer = mesos.Protos.Offer.newBuilder()
        .setHostname("hostname")
        .setSlaveId(mesos.Protos.SlaveID.newBuilder().setValue("agentId").build())
        .addAllAttributes(attributes)
        .setFrameworkId(mesos.Protos.FrameworkID.newBuilder().setValue("frameworkId"))
        .build()
    // TODO: this needs a proper ResourceMatch
    val resourceMatch: ResourceMatch = ResourceMatch(scalarMatches = Seq.empty, portsMatch = PortsMatch(Nil))
    val taskBuilder = new TaskBuilder(app, Task.Id.forRunSpec, config)
    val (taskInfo, networkInfo) = taskBuilder.build(offer, resourceMatch, volumeMatchOpt = None)
    val task = Task.LaunchedEphemeral(
      taskId = Task.Id(taskInfo.getTaskId),
      runSpecVersion = runSpec.version,
      status = Task.Status(
        stagedAt = clock.now(),
        condition = Condition.Created,
        networkInfo = networkInfo
      )
    )

    val maybeOp = opFactory.buildTaskOp(InstanceOpFactory.Request(
      runSpec,
      sufficientOffer,
      instanceMap = Map.empty[Instance.Id, Instance],
      additionalLaunches = 1))
    val maybeInstance = maybeOp.map { op =>
      op.stateOp match {
        case InstanceUpdateOperation.LaunchEphemeral(instance) => instance
        case _ => throw new RuntimeException(s"can't map ${op.stateOp} to an instance")
      }
    }

    maybeInstance.getOrElse(throw new RuntimeException("buildTaskOpFailed"))
  }
}
