package dev.bpmcrafters.processengineapi.testing.contract.fixture

/**
 * Common deployment identity for an adapter-owned contract process model.
 */
interface ProcessModelFixture {

  /**
   * Returns the classpath resource containing the model.
   *
   * @return deployable adapter-owned BPMN resource
   */
  fun classpathResource(): String

  /**
   * Returns the definition key of the process under test.
   *
   * @return process definition key
   */
  fun definitionKey(): String
}

/**
 * Describes a user task whose assignment can be changed and observed again.
 */
interface UserTaskAssignmentFixture : ProcessModelFixture {

  /**
   * @return user-task description key
   */
  fun userTaskDescriptionKey(): String

  /**
   * @return assignee to set through the public modification API
   */
  fun assignee(): String

  /**
   * @return assignee configured by the deployed model before the suite overwrites it
   */
  fun initialAssignee(): String

  /**
   * @return metadata key exposing the assignee in task snapshots
   */
  fun assigneeMetadataKey(): String
}

/**
 * Describes a process that waits first for a message and then for a signal.
 */
interface MessageAndSignalFixture : ProcessModelFixture {

  /**
   * @return intermediate message name
   */
  fun messageName(): String

  /**
   * @return correlation variable name
   */
  fun correlationVariable(): String

  /**
   * @return correlation-key value
   */
  fun correlationKey(): String

  /**
   * @return intermediate signal name
   */
  fun signalName(): String

  /**
   * @return element that waits for the message
   */
  fun messageCatchElementId(): String

  /**
   * @return element that waits for the signal
   */
  fun signalCatchElementId(): String

  /**
   * @return end-event identifier
   */
  fun endElementId(): String
}

/**
 * Describes a process whose timer moves it to a user task.
 */
interface TimerProcessFixture : ProcessModelFixture {

  /**
   * @return timer catch-event identifier
   */
  fun timerElementId(): String

  /**
   * @return user-task description key after timer execution
   */
  fun userTaskDescriptionKey(): String
}

/**
 * Describes a process containing an externally delivered service task.
 */
interface ExternalTaskProcessFixture : ProcessModelFixture {

  /**
   * @return external-task description key
   */
  fun externalTaskDescriptionKey(): String

  /**
   * @return end-event identifier
   */
  fun endElementId(): String
}

/**
 * Describes message and element-entry starts for one process model.
 */
interface AlternateStartFixture : ProcessModelFixture {

  /**
   * @return message name for the message start event
   */
  fun startMessageName(): String

  /**
   * @return element identifier accepted for an element-entry start
   */
  fun entryElementId(): String

  /**
   * @return user-task description key reached by each start mode
   */
  fun userTaskDescriptionKey(): String
}
