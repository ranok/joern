# CPG Extension and Merging Feature

## Status: EXPERIMENTAL / NOT YET FULLY FUNCTIONAL

This feature aims to add the ability to extend existing CPGs with additional source code and merge multiple CPGs together. However, the implementation is currently incomplete due to technical limitations with the flatgraph API.

## Current Status

### What's Implemented
- ✅ API structure and method signatures
- ✅ Console commands (`addToProject` and `mergeProjects`)
- ✅ Documentation and usage examples
- ✅ Unit tests for API structure
- ✅ Error handling and validation

### What's Not Yet Working
- ❌ Actual CPG merging at the binary level
- ❌ Adding source files to existing projects
- ❌ Merging two separate projects

## Technical Challenge

The main challenge is that the flatgraph API (used by Joern 4.x+) does not provide a straightforward way to copy nodes from one graph to another. The CPG schema has many specialized node types (File, Method, Call, etc.), each with specific properties. Proper merging would require:

1. Type-aware node copying for all CPG node types
2. Handling of node ID remapping
3. Edge recreation with proper references
4. Metadata conflict resolution
5. Or, access to flatgraph's graph union/merge operations (not currently exposed)

## Workaround

Until this feature is fully implemented, you can combine multiple source directories by:

### Option 1: Create a Combined Project from the Start

```bash
# Instead of:
joern> importCode("app/", "app")
joern> addToProject("app", "lib/")  # Not yet working

# Do this:
joern> importCode(".", "combined")  # Where . contains both app/ and lib/
```

### Option 2: Use Symbolic Links

```bash
# Create a directory structure that includes all code
mkdir combined
ln -s /path/to/app combined/app
ln -s /path/to/lib combined/lib

# Then import the combined directory
joern> importCode("combined/", "myproject")
```

### Option 3: Copy Files to a Temporary Location

```bash
# Create a temporary directory with all code
mkdir /tmp/combined
cp -r app/* /tmp/combined/
cp -r lib/* /tmp/combined/

# Import the combined directory
joern> importCode("/tmp/combined", "myproject")
```

## API Reference

The API methods exist and can be called, but they will return `None` with a helpful error message explaining that the feature is not yet implemented.

### addToProject

```scala
def addToProject(
  projectName: String,
  inputPath: String,
  language: String = ""
): Option[Cpg]
```

**Current Behavior:** Returns `None` with a message explaining the feature is not implemented.

**Future Behavior:** Will add the source code at `inputPath` to the existing project's CPG.

### mergeProjects

```scala
def mergeProjects(
  targetProjectName: String,
  sourceProjectName: String,
  deleteSource: Boolean = false
): Option[Cpg]
```

**Current Behavior:** Returns `None` with a message explaining the feature is not implemented.

**Future Behavior:** Will merge the source project into the target project.

## Future Implementation

To complete this feature, one of the following approaches is needed:

### Approach 1: flatgraph Library Enhancement
Wait for or contribute to the flatgraph library to add:
- Graph union operations
- Node copying utilities
- Graph merging API

### Approach 2: Serialization-Based Merging
- Export both CPGs to a serialization format
- Merge at the file level
- Re-import the merged CPG
- Requires handling of node ID conflicts

### Approach 3: Type-Aware Node Copying
- Implement a comprehensive node copying mechanism
- Handle all CPG node types explicitly
- Map between old and new node IDs
- Recreate all edges with updated references
- Very complex due to the number of node types in the CPG schema

### Approach 4: Source-Level Integration
- Instead of merging binary CPGs, regenerate from source
- This is actually what would be most correct but requires:
  - Access to original source code
  - Language frontend availability
  - Potentially long regeneration times

## Contributing

If you're interested in implementing this feature, please:
1. Review the code in `CpgMerger.scala`, `WorkspaceManager.scala`, and `Console.scala`
2. Understand the flatgraph API and CPG schema
3. Choose an implementation approach
4. Submit a PR with the implementation

The API structure is already in place, so the main work is implementing the actual merging logic.

## Files

- `console/src/main/scala/io/joern/console/cpgcreation/CpgMerger.scala`: CPG merging utility (placeholder)
- `console/src/main/scala/io/joern/console/workspacehandling/WorkspaceManager.scala`: Workspace management methods
- `console/src/main/scala/io/joern/console/Console.scala`: User-facing API
- `console/src/test/scala/io/joern/console/workspacehandling/WorkspaceManagerExtensionTests.scala`: Unit tests

