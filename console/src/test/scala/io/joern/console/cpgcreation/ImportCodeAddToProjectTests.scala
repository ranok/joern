package io.joern.console.cpgcreation

import io.joern.console.testing.ConsoleFixture
import io.shiftleft.semanticcpg.utils.FileUtil
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.nio.file.Files

class ImportCodeAddToProjectTests extends AnyWordSpec with Matchers {

  "ImportCode when adding to existing project" should {

    "detect existing project and update metadata" in ConsoleFixture() { (console, project) =>
      val sourceDir1 = FileUtil.newTemporaryDirectory("source1")
      val sourceDir2 = FileUtil.newTemporaryDirectory("source2")
      
      try {
        // Create a simple C file in first directory
        val file1 = sourceDir1.resolve("test1.c")
        Files.writeString(file1, "int main() { return 0; }")
        
        // Create a simple C file in second directory
        val file2 = sourceDir2.resolve("test2.c")
        Files.writeString(file2, "int foo() { return 1; }")
        
        // Import first directory
        val projectName = "test_project"
        val importCode = new ImportCode(console)
        importCode.apply(sourceDir1.toString, projectName, "c")
        
        // Verify project was created
        console.workspace.project(projectName) should not be empty
        val project = console.workspace.project(projectName).get
        project.inputPath shouldBe sourceDir1.toString
        
        // Import second directory with same project name
        // This should detect the existing project and update metadata
        importCode.apply(sourceDir2.toString, projectName, "c")
        
        // Verify project metadata was updated
        val updatedProject = console.workspace.project(projectName).get
        val inputPaths = updatedProject.inputPath.split(";").map(_.trim)
        
        inputPaths should have length 2
        inputPaths should contain(sourceDir1.toString)
        inputPaths should contain(sourceDir2.toString)
        
      } finally {
        FileUtil.delete(sourceDir1)
        FileUtil.delete(sourceDir2)
      }
    }

    "not add duplicate paths to project metadata" in ConsoleFixture() { (console, project) =>
      val sourceDir = FileUtil.newTemporaryDirectory("source")
      
      try {
        // Create a simple C file
        val file = sourceDir.resolve("test.c")
        Files.writeString(file, "int main() { return 0; }")
        
        // Import once
        val projectName = "test_dup_project"
        val importCode = new ImportCode(console)
        importCode.apply(sourceDir.toString, projectName, "c")
        
        // Import same path again
        importCode.apply(sourceDir.toString, projectName, "c")
        
        // Verify path is not duplicated
        val project = console.workspace.project(projectName).get
        val inputPaths = project.inputPath.split(";").map(_.trim).filter(_.nonEmpty)
        
        inputPaths should have length 1
        inputPaths(0) shouldBe sourceDir.toString
        
      } finally {
        FileUtil.delete(sourceDir)
      }
    }
  }
}
