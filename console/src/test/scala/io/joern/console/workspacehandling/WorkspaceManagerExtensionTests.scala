package io.joern.console.workspacehandling

import io.shiftleft.codepropertygraph.generated.Cpg
import io.shiftleft.semanticcpg.utils.FileUtil
import io.shiftleft.semanticcpg.utils.FileUtil.*
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.nio.file.{Files, Path}

class WorkspaceManagerExtensionTests extends AnyWordSpec with Matchers {

  private val tmpDirPrefix = "workspace-extension-tests"
  
  // Shared mock CPG generator for tests
  private val mockCpgGenerator: (String, String, String) => Option[Cpg] = (_, _, _) => None

  "WorkspaceManager extension features" should {

    "have addToProject method that takes correct parameters" in {
      FileUtil.usingTemporaryDirectory(tmpDirPrefix) { workspaceDir =>
        val manager = new WorkspaceManager(workspaceDir.toString)
        val inputFile1 = FileUtil.newTemporaryFile("project1")
        val inputFile2 = FileUtil.newTemporaryFile("additional")
        
        // Create initial project
        manager.createProject(inputFile1.toString, "testproject")
        manager.numberOfProjects shouldBe 1
        
        // Test that addToProject method exists and can be called
        // We expect it to return None since the feature is not yet implemented
        val result = manager.addToProject("testproject", inputFile2.toString, "", mockCpgGenerator)
        result shouldBe None  // Expected because feature is not yet implemented
        
        FileUtil.delete(inputFile1)
        FileUtil.delete(inputFile2)
      }
    }

    "have mergeProjects method that takes correct parameters" in {
      FileUtil.usingTemporaryDirectory(tmpDirPrefix) { workspaceDir =>
        val manager = new WorkspaceManager(workspaceDir.toString)
        val inputFile1 = FileUtil.newTemporaryFile("project1")
        val inputFile2 = FileUtil.newTemporaryFile("project2")
        
        // Create two projects
        manager.createProject(inputFile1.toString, "project1")
        manager.createProject(inputFile2.toString, "project2")
        manager.numberOfProjects shouldBe 2
        
        // Test that mergeProjects method exists and can be called
        val result = manager.mergeProjects("project1", "project2", deleteSource = false, mockCpgGenerator)
        result shouldBe None  // Expected because feature is not yet implemented
        
        FileUtil.delete(inputFile1)
        FileUtil.delete(inputFile2)
      }
    }

    "reject merge of a project into itself" in {
      FileUtil.usingTemporaryDirectory(tmpDirPrefix) { workspaceDir =>
        val manager = new WorkspaceManager(workspaceDir.toString)
        val inputFile = FileUtil.newTemporaryFile("project")
        
        manager.createProject(inputFile.toString, "testproject")
        
        val result = manager.mergeProjects("testproject", "testproject", deleteSource = false, mockCpgGenerator)
        result shouldBe None
        
        FileUtil.delete(inputFile)
      }
    }

    "handle non-existent projects gracefully in addToProject" in {
      FileUtil.usingTemporaryDirectory(tmpDirPrefix) { workspaceDir =>
        val manager = new WorkspaceManager(workspaceDir.toString)
        val inputFile = FileUtil.newTemporaryFile("additional")
        
        val result = manager.addToProject("nonexistent", inputFile.toString, "", mockCpgGenerator)
        result shouldBe None
        
        FileUtil.delete(inputFile)
      }
    }

    "handle non-existent projects gracefully in mergeProjects" in {
      FileUtil.usingTemporaryDirectory(tmpDirPrefix) { workspaceDir =>
        val manager = new WorkspaceManager(workspaceDir.toString)
        
        // Test with non-existent target
        val result1 = manager.mergeProjects("nonexistent", "alsoNonexistent", deleteSource = false, mockCpgGenerator)
        result1 shouldBe None
        
        // Test with existing target but non-existent source
        val inputFile = FileUtil.newTemporaryFile("project")
        manager.createProject(inputFile.toString, "existing")
        val result2 = manager.mergeProjects("existing", "nonexistent", deleteSource = false, mockCpgGenerator)
        result2 shouldBe None
        
        FileUtil.delete(inputFile)
      }
    }
  }
}
