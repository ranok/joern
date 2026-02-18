package io.joern.console.workspacehandling

import io.shiftleft.codepropertygraph.generated.Cpg
import io.shiftleft.semanticcpg.utils.FileUtil
import io.shiftleft.semanticcpg.utils.FileUtil.*
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.nio.file.{Files, Path}

class WorkspaceManagerExtensionTests extends AnyWordSpec with Matchers {

  private val tmpDirPrefix = "workspace-extension-tests"

  "WorkspaceManager extension features" should {

    "have addToProject method that takes correct parameters" in {
      FileUtil.usingTemporaryDirectory(tmpDirPrefix) { workspaceDir =>
        val manager = new WorkspaceManager(workspaceDir.toString)
        val inputFile1 = FileUtil.newTemporaryFile("project1")
        val inputFile2 = FileUtil.newTemporaryFile("additional")
        
        // Create initial project
        manager.createProject(inputFile1.toString, "testproject")
        manager.numberOfProjects shouldBe 1
        
        // Mock CPG generator that returns None (simulates CPG generation without actual frontends)
        val mockGenerator: (String, String, String) => Option[Cpg] = (_, _, _) => None
        
        // Test that addToProject method exists and can be called
        // We expect it to return None since we're using a mock generator
        val result = manager.addToProject("testproject", inputFile2.toString, "", mockGenerator)
        result shouldBe None  // Expected because mock generator returns None
        
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
        
        // Mock CPG generator
        val mockGenerator: (String, String, String) => Option[Cpg] = (_, _, _) => None
        
        // Test that mergeProjects method exists and can be called
        val result = manager.mergeProjects("project1", "project2", deleteSource = false, mockGenerator)
        // The result will depend on the implementation, but the method should exist
        
        FileUtil.delete(inputFile1)
        FileUtil.delete(inputFile2)
      }
    }

    "reject merge of a project into itself" in {
      FileUtil.usingTemporaryDirectory(tmpDirPrefix) { workspaceDir =>
        val manager = new WorkspaceManager(workspaceDir.toString)
        val inputFile = FileUtil.newTemporaryFile("project")
        
        manager.createProject(inputFile.toString, "testproject")
        
        val mockGenerator: (String, String, String) => Option[Cpg] = (_, _, _) => None
        val result = manager.mergeProjects("testproject", "testproject", deleteSource = false, mockGenerator)
        
        result shouldBe None
        FileUtil.delete(inputFile)
      }
    }

    "handle non-existent projects gracefully in addToProject" in {
      FileUtil.usingTemporaryDirectory(tmpDirPrefix) { workspaceDir =>
        val manager = new WorkspaceManager(workspaceDir.toString)
        val inputFile = FileUtil.newTemporaryFile("additional")
        
        val mockGenerator: (String, String, String) => Option[Cpg] = (_, _, _) => None
        val result = manager.addToProject("nonexistent", inputFile.toString, "", mockGenerator)
        
        result shouldBe None
        FileUtil.delete(inputFile)
      }
    }

    "handle non-existent projects gracefully in mergeProjects" in {
      FileUtil.usingTemporaryDirectory(tmpDirPrefix) { workspaceDir =>
        val manager = new WorkspaceManager(workspaceDir.toString)
        
        val mockGenerator: (String, String, String) => Option[Cpg] = (_, _, _) => None
        
        // Test with non-existent target
        val result1 = manager.mergeProjects("nonexistent", "alsoNonexistent", deleteSource = false, mockGenerator)
        result1 shouldBe None
        
        // Test with existing target but non-existent source
        val inputFile = FileUtil.newTemporaryFile("project")
        manager.createProject(inputFile.toString, "existing")
        val result2 = manager.mergeProjects("existing", "nonexistent", deleteSource = false, mockGenerator)
        result2 shouldBe None
        
        FileUtil.delete(inputFile)
      }
    }
  }
}
