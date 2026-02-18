# CPG Extension and Merging Feature

This feature adds the ability to extend existing CPGs with additional source code and merge multiple CPGs together.

## Overview

The implementation provides two main capabilities:

1. **Add Source Files to Existing Project** (`addToProject`): Add new source code to an existing project/CPG
2. **Merge Projects** (`mergeProjects`): Combine two separate projects into one

## Usage

### Adding Source Code to an Existing Project

```scala
// Create an initial project
joern> importCode("/path/to/app", "myapp")

// Later, add library code to the same project
joern> addToProject("myapp", "/path/to/library")

// The CPG now contains both application and library code
joern> cpg.file.name.l  // Lists files from both sources
```

### Merging Two Separate Projects

```scala
// Create two separate projects
joern> importCode("/path/to/app", "app")
joern> importCode("/path/to/library", "lib")

// Merge the library project into the app project
joern> mergeProjects("app", "lib", deleteSource = true)

// The app project now contains code from both projects
joern> workspace  // Shows app project with merged content
```

## Implementation Details

### Architecture

The implementation consists of three main components:

1. **WorkspaceManager Methods**:
   - `addToProject(projectName, inputPath, language, cpgGenerator)`: Core logic for adding code to existing projects
   - `mergeProjects(targetProjectName, sourceProjectName, deleteSource, cpgGenerator)`: Core logic for merging projects

2. **Console API Methods**:
   - `addToProject(projectName, inputPath, language)`: User-facing API for adding code
   - `mergeProjects(targetProjectName, sourceProjectName, deleteSource)`: User-facing API for merging projects

3. **CpgMerger Utility**:
   - `mergeCpg(target, source)`: Low-level utility (currently returns UnsupportedOperationException)
   - Note: Direct binary CPG merging is complex and not yet fully implemented

### How It Works

Both features work by:
1. Generating a new CPG from the source code being added
2. Merging the new CPG with the existing CPG using Joern's DiffGraph mechanism
3. Updating project metadata to reflect the merged sources

**Key Point**: The merging happens at the source code level, not at the binary CPG level. This means:
- The original source code must still be accessible
- Projects must have been created from source code (not imported from binary CPGs)
- The merging process regenerates CPGs to ensure consistency

### Limitations

1. **Source Code Required**: Both features require access to the original source code. You cannot merge pre-generated binary CPGs that were imported without source.

2. **Same Language**: For best results, merged code should be in the same programming language. Mixed-language projects may work but are not extensively tested.

3. **Metadata Conflicts**: If both projects have conflicting metadata (e.g., different language settings), the behavior is undefined.

4. **Performance**: Regenerating CPGs for large codebases can be time-consuming.

5. **Binary CPG Merging**: Direct merging of binary CPGs (without source code) is not yet implemented due to complexity of the flatgraph API and the need to handle all CPG node types correctly.

## Testing

Tests are provided in `WorkspaceManagerExtensionTests.scala` covering:
- API structure and parameter validation
- Error handling for non-existent projects
- Basic functionality checks

## Future Enhancements

Potential improvements include:

1. **Binary CPG Merging**: Implement true binary CPG merging using flatgraph's serialization format or graph algebra operations
2. **Conflict Resolution**: Better handling of duplicate nodes and conflicting metadata
3. **Incremental Updates**: Ability to update only changed files rather than regenerating entire CPGs
4. **Performance Optimization**: Caching and parallel processing for large codebases
5. **Mixed-Language Support**: Better handling of projects with multiple programming languages

## Examples

### Example 1: Adding Test Code to Production Code

```scala
joern> importCode("src/main/java", "myapp")
joern> addToProject("myapp", "src/test/java")
joern> cpg.method.name("test.*").size  // Count test methods
```

### Example 2: Merging Application and Library

```scala
// Analyze application code
joern> importCode("app/", "app")
joern> cpg.call.name("libraryFunction").size  // 0 - no definition found

// Add library code
joern> addToProject("app", "lib/")
joern> cpg.call.name("libraryFunction").size  // Now finds calls
joern> cpg.method.name("libraryFunction").size  // And the definition
```

### Example 3: Separate Analysis Then Merge

```scala
// Analyze components separately first
joern> importCode("component1/", "c1")
joern> importCode("component2/", "c2")
joern> open("c1")
joern> cpg.call.name(".*").size  // Analyze c1
joern> open("c2")
joern> cpg.call.name(".*").size  // Analyze c2

// Now merge for combined analysis
joern> mergeProjects("c1", "c2", deleteSource = true)
joern> cpg.call.name(".*").size  // Analyze combined code
```

## API Reference

### addToProject

```scala
def addToProject(
  projectName: String,
  inputPath: String,
  language: String = ""
): Option[Cpg]
```

**Parameters:**
- `projectName`: Name of the existing project to add code to
- `inputPath`: Path to the additional source code
- `language`: Programming language (optional, auto-detected if empty)

**Returns:** The updated CPG, or None if the operation failed

### mergeProjects

```scala
def mergeProjects(
  targetProjectName: String,
  sourceProjectName: String,
  deleteSource: Boolean = false
): Option[Cpg]
```

**Parameters:**
- `targetProjectName`: Name of the project to merge into
- `sourceProjectName`: Name of the project to merge from
- `deleteSource`: If true, delete the source project after merging

**Returns:** The merged CPG, or None if the operation failed
