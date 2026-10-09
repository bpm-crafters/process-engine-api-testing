package dev.bpmcrafters.processengineapi.testing.contract.fixture

/**
 * Logical names and resources for the minimal user-task process exercised by the compatibility
 * contract.
 *
 * The adapter owns the BPMN resource named by [classpathResource]. The values returned by this
 * fixture must describe that resource without exposing engine-specific model types.
 */
interface UserTaskProcessFixture : ProcessModelFixture {

  /**
   * Returns the classpath location of the BPMN resource to deploy.
   *
   * @return classpath location understood by the adapter deployment implementation
   */
  override fun classpathResource(): String

  /**
   * Returns the process definition key used to start the fixture process.
   *
   * @return engine-agnostic process definition key
   */
  override fun definitionKey(): String

  /**
   * Returns the logical key of the process's single user task.
   *
   * @return task description key accepted by the testing API
   */
  fun userTaskDescriptionKey(): String

  /**
   * Returns the BPMN identifier of the start event.
   *
   * @return start-event BPMN identifier
   */
  fun startElementId(): String

  /**
   * Returns the BPMN identifier of the user task.
   *
   * @return user-task BPMN identifier
   */
  fun userTaskElementId(): String

  /**
   * Returns the BPMN identifier of the end event.
   *
   * @return end-event BPMN identifier
   */
  fun endElementId(): String
}
