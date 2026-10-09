package dev.bpmcrafters.processengineapi.testing.jgiven.format

import com.tngtech.jgiven.format.ArgumentFormatter

/**
 * Formats a BPMN element-id vararg for a JGiven report.
 *
 * Each element identifier is quoted individually, avoiding the JVM array identity text that a
 * scalar formatter such as `@Quoted` would render for a `String[]`.
 */
class QuotedElementIdsFormatter : ArgumentFormatter<Array<String>> {

  /**
   * Formats [elementIds] as a comma-separated list of individually quoted identifiers.
   *
   * @param elementIds BPMN element identifiers supplied to a path assertion
   * @param formatterArguments unused JGiven formatter arguments
   * @return a report-ready list such as `"start", "review", "end"`
   */
  override fun format(elementIds: Array<String>, vararg formatterArguments: String): String =
    elementIds.joinToString(", ") { "\"$it\"" }
}
