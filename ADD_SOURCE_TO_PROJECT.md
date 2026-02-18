# Adding Source Directories to Existing CPG Projects

## Overview

Joern now supports adding additional source directories to existing projects by simply calling `importCode` with the same project name.

## Usage

### Basic Example

```scala
// Create initial project
joern> importCode("/path/to/app", "myproject")

// Add library code to the same project
joern> importCode("/path/to/library", "myproject")
```

When you import code using an existing project name, Joern will:
1. Detect that the project already exists
2. Add the new source path to the project's metadata
3. Provide instructions for regenerating the CPG with all source code

## How It Works

**Metadata Tracking**: When you add a new source directory, Joern updates the project metadata to track all source paths (separated by semicolons).

**CPG Regeneration**: The actual CPG is not automatically merged. Instead, you should:
1. Organize all source code under a common parent directory
2. Delete the project: `workspace.deleteProject("myproject")`
3. Re-import from the parent: `importCode("/path/to/parent", "myproject")`

## Example Workflow

```scala
// Step 1: Import application code
joern> importCode("~/projects/myapp/src", "myapp")
// Analyze...

// Step 2: Add library code 
joern> importCode("~/projects/myapp/lib", "myapp")
// System adds path to metadata and provides instructions

// Step 3: Reorganize and regenerate
// (organize code so both src/ and lib/ are under a common parent)
joern> workspace.deleteProject("myapp")
joern> importCode("~/projects/myapp", "myapp")
// Now CPG includes all code
```

## Why This Approach?

**Technical Limitation**: Most CPG frontends expect a single source directory. Merging CPGs at the graph level is complex and has limitations with the current flatgraph API.

**Best Practice**: The recommended approach is to organize your code so that all sources you want to analyze are under a common parent directory, then import that parent directory.

## Alternative: Multiple Projects

If you want to analyze code separately, you can still create multiple projects:

```scala
joern> importCode("/path/to/app", "app")
joern> importCode("/path/to/library", "lib")

// Switch between projects
joern> workspace.setActiveProject("app")
joern> workspace.setActiveProject("lib")
```

## Checking Project Metadata

To see what paths are tracked for a project:

```scala
joern> workspace.project("myproject").get.inputPath
// Returns paths separated by semicolons
```

## Future Enhancements

Future versions may support:
- Automatic CPG merging for frontends that support multiple input directories
- Better handling of distributed source code
- Incremental CPG updates
