package io.joern.console.cpgcreation

import io.shiftleft.codepropertygraph.generated.Cpg

import scala.util.{Failure, Try}

/** Utility for merging CPGs together.
  *
  * This object provides functionality to merge one CPG into another, combining all nodes and edges. 
  *
  * Note: Direct CPG merging at the graph level is not straightforward with the current flatgraph API
  * because it requires creating NewNode instances for each node type, and the CPG schema has many
  * specialized node types (File, Method, Call, etc.) each with their own properties.
  *
  * The recommended approach is to use the addToProject functionality which regenerates CPGs from
  * source code and merges them, rather than merging already-generated CPGs at the binary level.
  */
object CpgMerger {

  /** Merge the source CPG into the target CPG.
    *
    * This method attempts to copy all nodes and edges from the source CPG into the target CPG.
    *
    * IMPORTANT: This is a placeholder implementation. True CPG merging requires either:
    * 1. Access to the original source code to regenerate and merge properly
    * 2. A sophisticated node-by-node copying mechanism that handles all CPG node types
    * 3. flatgraph library support for graph union operations (not currently available)
    *
    * For now, this method returns a failure indicating that direct CPG merging is not yet implemented.
    * Users should use the addToProject method instead, which generates a new CPG from source code
    * and merges it properly.
    *
    * Future implementation could:
    * - Use flatgraph's serialization format to merge CPGs at the binary level
    * - Implement a type-aware node copying mechanism using the CPG schema
    * - Leverage flatgraph's graph algebra operations if they become available
    *
    * @param target
    *   The CPG to merge into (will be modified)
    * @param source
    *   The CPG to merge from (will not be modified)
    * @return
    *   Failure indicating this operation is not yet supported
    */
  def mergeCpg(target: Cpg, source: Cpg): Try[Cpg] = {
    Failure(new UnsupportedOperationException(
      "Direct CPG merging is not yet implemented. " +
      "To add code to an existing project, use addToProject(projectName, sourcePath) " +
      "which generates a new CPG from source code and merges it properly. " +
      "To combine two projects created from source code, use mergeProjects() which " +
      "uses addToProject internally."
    ))
  }

}
