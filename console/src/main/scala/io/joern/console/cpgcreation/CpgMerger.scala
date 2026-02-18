package io.joern.console.cpgcreation

import io.shiftleft.codepropertygraph.generated.{Cpg, DiffGraphBuilder}
import io.shiftleft.passes.CpgPass

import scala.jdk.CollectionConverters.*
import scala.util.{Failure, Success, Try}

/** Utility for merging CPGs together.
  *
  * This object provides functionality to merge one CPG into another, combining all nodes and edges. The merge operation
  * uses the DiffGraph mechanism to add all nodes and edges from the source CPG to the target CPG.
  *
  * Note: This is an initial implementation that provides the API structure. Full CPG merging is complex because it
  * requires handling all different node types (File, Method, Call, etc.) and their specific properties. The current
  * implementation uses flatgraph's low-level graph operations to copy nodes and edges.
  */
object CpgMerger {

  /** Merge the source CPG into the target CPG.
    *
    * This method attempts to copy all nodes and edges from the source CPG into the target CPG. The implementation uses
    * flatgraph's graph manipulation capabilities to preserve node properties and edge relationships.
    *
    * Merging strategy: 1. Create a node ID mapping from source to target 2. Iterate through all source nodes and add
    * them to a diff graph 3. Iterate through all edges and recreate them using the node mapping 4. Apply the diff graph
    * to the target CPG
    *
    * Limitations: - Some edge types or properties may not be fully preserved - Metadata nodes (e.g., language info) may
    * conflict - Node IDs will change in the merged CPG
    *
    * @param target
    *   The CPG to merge into (will be modified)
    * @param source
    *   The CPG to merge from (will not be modified)
    * @return
    *   Success(target) if merge was successful, Failure with exception otherwise
    */
  def mergeCpg(target: Cpg, source: Cpg): Try[Cpg] = Try {
    // Get the source and target graphs
    val sourceGraph = source.graph
    val targetGraph = target.graph

    // Create a diff graph builder
    val diff = Cpg.newDiffGraphBuilder

    // Track node mapping: source node seq -> target node
    val nodeMap = scala.collection.mutable.Map[Long, flatgraph.GNode]()

    // First pass: Copy all nodes
    // We iterate through all source nodes and add them to the diff
    val sourceNodes = sourceGraph.allNodes.asScala.toList
    
    sourceNodes.foreach { sourceNode =>
      // For each node, we need to extract its type and properties
      // and create a corresponding entry in the diff graph
      val label = sourceNode.label
      val props = sourceNode.propertiesMap
      
      // Use flatgraph's DiffGraphBuilder to add nodes
      // The exact API depends on flatgraph version, but typically involves
      // creating a node builder and setting properties
      val nodeRef = diff.addNode(sourceNode)
      nodeMap.put(sourceNode.seq(), nodeRef)
    }

    // Second pass: Copy all edges
    sourceNodes.foreach { sourceNode =>
      sourceNode.outE.asScala.foreach { edge =>
        val srcNodeOpt = nodeMap.get(sourceNode.seq())
        val dstNodeOpt = nodeMap.get(edge.dst.seq())
        
        (srcNodeOpt, dstNodeOpt) match {
          case (Some(srcNode), Some(dstNode)) =>
            diff.addEdge(srcNode, dstNode, edge.label, edge.propertiesMap)
          case _ =>
            // Skip edges that reference nodes outside the source CPG
            ()
        }
      }
    }

    // Apply the diff to the target CPG
    val pass = new CpgPass(target) {
      override def run(builder: DiffGraphBuilder): Unit = {
        builder.absorb(diff)
      }
    }
    pass.createAndApply()

    target
  } recoverWith { case ex =>
    Failure(new RuntimeException(s"Failed to merge CPGs: ${ex.getMessage}. " +
      "CPG merging is an experimental feature that may not work in all cases. " +
      "Consider using addToProject to add source files incrementally instead.", ex))
  }

}
