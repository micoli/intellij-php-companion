package org.micoli.php

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.io.File
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.micoli.php.notes.NoteFilesystem
import org.micoli.php.notes.NoteFilesystem.StorageData
import org.micoli.php.notes.NotesException
import org.micoli.php.notes.models.NoteType

class NoteFilesystemTest : BasePlatformTestCase() {

    private lateinit var noteFilesystem: NoteFilesystem
    private lateinit var storageFile: File

    override fun setUp() {
        super.setUp()
        noteFilesystem = NoteFilesystem(project)
        storageFile = File(project.basePath, ".idea/notes.yaml")
        if (storageFile.exists()) {
            storageFile.delete()
        }
    }

    override fun tearDown() {
        try {
            if (storageFile.exists()) {
                storageFile.delete()
            }
        } finally {
            super.tearDown()
        }
    }

    fun testAddPathWithoutParent() {
        val pathId = noteFilesystem.addPath(null, "root")

        assertThat(pathId).isNotNull()
        val path = noteFilesystem.getPathById(pathId)
        assertThat(path).isNotNull()
        assertThat(path?.label).isEqualTo("root")
        assertThat(path?.parentId).isNull()
        assertThat(path?.isExpanded).isTrue()
    }

    fun testAddPathWithParent() {
        val parentId = noteFilesystem.addPath(null, "parent")
        val childId = noteFilesystem.addPath(parentId, "child")

        val child = noteFilesystem.getPathById(childId)
        assertThat(child).isNotNull()
        assertThat(child?.label).isEqualTo("child")
        assertThat(child?.parentId).isEqualTo(parentId)
    }

    fun testAddPathWithInvalidParentThrowsException() {
        assertThatThrownBy { noteFilesystem.addPath("invalid-id", "child") }
            .isInstanceOf(NotesException::class.java)
            .hasMessage("Parent path not found")
    }

    fun testGetFullPath() {
        val rootId = noteFilesystem.addPath(null, "root")
        val childId = noteFilesystem.addPath(rootId, "child")
        val grandchildId = noteFilesystem.addPath(childId, "grandchild")

        assertThat(noteFilesystem.getFullPath(rootId)).isEqualTo("root")
        assertThat(noteFilesystem.getFullPath(childId)).isEqualTo("root/child")
        assertThat(noteFilesystem.getFullPath(grandchildId)).isEqualTo("root/child/grandchild")
        assertThat(noteFilesystem.getFullPath(null)).isEqualTo("")
        assertThat(noteFilesystem.getFullPath("invalid-id")).isEqualTo("")
    }

    fun testFindPathIdByFullPath() {
        val rootId = noteFilesystem.addPath(null, "root")
        val childId = noteFilesystem.addPath(rootId, "child")

        assertThat(noteFilesystem.findPathIdByFullPath("root")).isEqualTo(rootId)
        assertThat(noteFilesystem.findPathIdByFullPath("root/child")).isEqualTo(childId)
        assertThat(noteFilesystem.findPathIdByFullPath("nonexistent")).isNull()
        assertThat(noteFilesystem.findPathIdByFullPath("")).isNull()
    }

    fun testGetPathByFullPath() {
        val rootId = noteFilesystem.addPath(null, "root")
        noteFilesystem.addPath(rootId, "child")

        val path = noteFilesystem.getPathByFullPath("root/child")
        assertThat(path).isNotNull()
        assertThat(path?.label).isEqualTo("child")
        assertThat(path?.parentId).isEqualTo(rootId)
    }

    fun testUpdatePathExpansion() {
        val pathId = noteFilesystem.addPath(null, "test")

        noteFilesystem.updatePathExpansion(pathId, false)
        assertThat(noteFilesystem.getPathExpansionState(pathId)).isFalse()

        noteFilesystem.updatePathExpansion(pathId, true)
        assertThat(noteFilesystem.getPathExpansionState(pathId)).isTrue()
    }

    fun testGetPathExpansionStateDefaultValue() {
        val pathId = noteFilesystem.addPath(null, "test")
        assertThat(noteFilesystem.getPathExpansionState(pathId)).isTrue()
    }

    fun testFindPathByLabel() {
        val rootId = noteFilesystem.addPath(null, "root")
        noteFilesystem.addPath(rootId, "child")

        val foundPath = noteFilesystem.findPathByLabel("child", rootId)
        assertThat(foundPath).isNotNull()
        assertThat(foundPath?.label).isEqualTo("child")
        assertThat(foundPath?.parentId).isEqualTo(rootId)

        val notFound = noteFilesystem.findPathByLabel("nonexistent", rootId)
        assertThat(notFound).isNull()
    }

    fun testGetChildPaths() {
        val rootId = noteFilesystem.addPath(null, "root")
        val child1Id = noteFilesystem.addPath(rootId, "child1")
        noteFilesystem.addPath(rootId, "child2")
        noteFilesystem.addPath(child1Id, "grandchild")

        val children = noteFilesystem.getChildPaths(rootId)
        assertThat(children).hasSize(2)
        assertThat(children.map { it.label }).containsExactlyInAnyOrder("child1", "child2")

        val rootPaths = noteFilesystem.getChildPaths(null)
        assertThat(rootPaths).hasSize(1)
        assertThat(rootPaths[0].label).isEqualTo("root")
    }

    fun testGetAllDescendantPathIds() {
        val rootId = noteFilesystem.addPath(null, "root")
        val child1Id = noteFilesystem.addPath(rootId, "child1")
        val child2Id = noteFilesystem.addPath(rootId, "child2")
        val grandchildId = noteFilesystem.addPath(child1Id, "grandchild")

        val descendants = noteFilesystem.getAllDescendantPathIds(rootId)
        assertThat(descendants).containsExactlyInAnyOrder(rootId, child1Id, child2Id, grandchildId)
    }

    fun testDeletePathById() {
        val rootId = noteFilesystem.addPath(null, "root")
        val childId = noteFilesystem.addPath(rootId, "child")
        val grandchildId = noteFilesystem.addPath(childId, "grandchild")

        noteFilesystem.addNote(childId, "note1", NoteType.PHP, "content1")
        noteFilesystem.addNote(grandchildId, "note2", NoteType.JS, "content2")

        noteFilesystem.deletePathById(childId)

        assertThat(noteFilesystem.getPathById(childId)).isNull()
        assertThat(noteFilesystem.getPathById(grandchildId)).isNull()
        assertThat(noteFilesystem.findNoteByName("note1")).isNull()
        assertThat(noteFilesystem.findNoteByName("note2")).isNull()
        assertThat(noteFilesystem.getPathById(rootId)).isNotNull()
    }

    fun testExistsByFullPath() {
        val pathTwo = noteFilesystem.addPath(null, "2")
        val pathOne = noteFilesystem.addPath(null, "1")
        val pathOneDotOne = noteFilesystem.addPath(pathOne, "1.1")

        noteFilesystem.addNote(null, "noteAtRoot", NoteType.PHP, "content1")
        noteFilesystem.addNote(pathOne, "noteAOne", NoteType.PHP, "content1")
        noteFilesystem.addNote(pathOneDotOne, "noteAOneDotOne", NoteType.PHP, "content1")
        // dumpFileSystemAsText(noteFilesystem.loadData())
        assertThat(noteFilesystem.exists("no-existing")).isFalse()
        assertThat(noteFilesystem.exists("noteAtRoot")).isTrue()
        assertThat(noteFilesystem.exists("1/noteAOne")).isTrue()
        assertThat(noteFilesystem.exists("1/1.1")).isTrue()
        assertThat(noteFilesystem.exists("1/1.1/noteAOneDotOne")).isTrue()
    }

    fun testRenamePathById() {
        val pathId = noteFilesystem.addPath(null, "oldname")

        noteFilesystem.renamePathById(pathId, "newname")

        val path = noteFilesystem.getPathById(pathId)
        assertThat(path?.label).isEqualTo("newname")
    }

    fun testAddNote() {
        val pathId = noteFilesystem.addPath(null, "path")
        val note = noteFilesystem.addNote(pathId, "test-note", NoteType.PHP, "<?php echo 'test';")

        assertThat(note.id).isNotNull()
        assertThat(note.name).isEqualTo("test-note")
        assertThat(note.type).isEqualTo(NoteType.PHP)
        assertThat(note.content).isEqualTo("<?php echo 'test';")
        assertThat(note.pathId).isEqualTo(pathId)
        assertThat(note.createdAt).isGreaterThan(0)
        assertThat(note.updatedAt).isGreaterThan(0)
    }

    fun testAddNoteWithoutPath() {
        val note = noteFilesystem.addNote(null, "root-note", NoteType.MD, "# Title")

        assertThat(note.pathId).isNull()
        assertThat(note.name).isEqualTo("root-note")
    }

    fun testAddNoteWithInvalidPathThrowsException() {
        assertThatThrownBy { noteFilesystem.addNote("invalid-id", "note", NoteType.PHP, "content") }
            .isInstanceOf(NotesException::class.java)
            .hasMessage("Path not found")
    }

    fun testGetNoteById() {
        val note = noteFilesystem.addNote(null, "test", NoteType.PHP, "content")

        val retrieved = noteFilesystem.getNoteById(note.id)
        assertThat(retrieved).isNotNull()
        assertThat(retrieved?.id).isEqualTo(note.id)
        assertThat(retrieved?.name).isEqualTo("test")
    }

    fun testGetNoteByIdReturnsNullForInvalidId() {
        assertThat(noteFilesystem.getNoteById("invalid-id")).isNull()
    }

    fun testFindNoteByName() {
        noteFilesystem.addNote(null, "unique-note", NoteType.PHP, "content")

        val found = noteFilesystem.findNoteByName("unique-note")
        assertThat(found).isNotNull()
        assertThat(found?.name).isEqualTo("unique-note")

        val notFound = noteFilesystem.findNoteByName("nonexistent")
        assertThat(notFound).isNull()
    }

    fun testUpdateNote() {
        val note = noteFilesystem.addNote(null, "original", NoteType.PHP, "original content")
        val updatedNote =
            note.copy(
                name = "updated",
                content = "updated content",
                updatedAt = System.currentTimeMillis())

        noteFilesystem.updateNote(updatedNote)

        val retrieved = noteFilesystem.getNoteById(note.id)
        assertThat(retrieved?.name).isEqualTo("updated")
        assertThat(retrieved?.content).isEqualTo("updated content")
    }

    fun testDeleteNoteById() {
        val note = noteFilesystem.addNote(null, "test", NoteType.PHP, "content")

        noteFilesystem.deleteNoteById(note.id)

        assertThat(noteFilesystem.getNoteById(note.id)).isNull()
    }

    fun testRenameNoteById() {
        val note = noteFilesystem.addNote(null, "oldname", NoteType.PHP, "content")

        noteFilesystem.renameNoteById(note.id, "newname")

        val updated = noteFilesystem.getNoteById(note.id)
        assertThat(updated?.name).isEqualTo("newname")
    }

    fun testLoadDataWithEmptyFile() {
        assertThat(noteFilesystem.loadData().paths).isEmpty()
        assertThat(noteFilesystem.loadData().notes).isEmpty()
    }

    fun testPersistenceOfPaths() {
        val pathId = noteFilesystem.addPath(null, "persistent")

        val newRepository = NoteFilesystem(project)
        val path = newRepository.getPathById(pathId)

        assertThat(path).isNotNull()
        assertThat(path?.label).isEqualTo("persistent")
    }

    fun testPersistenceOfNotes() {
        val note =
            noteFilesystem.addNote(null, "persistent-note", NoteType.PHP, "persistent content")

        val newRepository = NoteFilesystem(project)
        val retrieved = newRepository.getNoteById(note.id)

        assertThat(retrieved).isNotNull()
        assertThat(retrieved?.name).isEqualTo("persistent-note")
        assertThat(retrieved?.content).isEqualTo("persistent content")
    }

    fun testPersistenceOfComplexStructure() {
        val root1Id = noteFilesystem.addPath(null, "root1")
        val root2Id = noteFilesystem.addPath(null, "root2")
        val child1Id = noteFilesystem.addPath(root1Id, "child1")

        val note1 = noteFilesystem.addNote(root1Id, "note1", NoteType.PHP, "content1")
        val note2 = noteFilesystem.addNote(child1Id, "note2", NoteType.JS, "content2")
        val note3 = noteFilesystem.addNote(null, "root-note", NoteType.MD, "content3")

        noteFilesystem.updatePathExpansion(root1Id, false)

        val newRepository = NoteFilesystem(project)
        val data = newRepository.loadData()

        assertThat(data.paths).hasSize(3)
        assertThat(data.notes).hasSize(3)
        assertThat(newRepository.getPathExpansionState(root1Id)).isFalse()
        assertThat(newRepository.getPathExpansionState(root2Id)).isTrue()
        assertThat(newRepository.getNoteById(note1.id)?.name).isEqualTo("note1")
        assertThat(newRepository.getNoteById(note2.id)?.name).isEqualTo("note2")
        assertThat(newRepository.getNoteById(note3.id)?.pathId).isNull()
    }

    fun testYamlFileStructure() {
        val pathId = noteFilesystem.addPath(null, "test-path")
        noteFilesystem.addNote(pathId, "test-note", NoteType.PHP, "test content")

        assertThat(storageFile.exists()).isTrue()
        val content = storageFile.readText()
        assertThat(content).contains("paths:")
        assertThat(content).contains("notes:")
        assertThat(content).contains("test-path")
        assertThat(content).contains("test-note")
    }

    fun testAllNoteTypes() {
        NoteType.fileTypesEntries().forEach { noteType ->
            val note = noteFilesystem.addNote(null, "note-${noteType.name}", noteType, "content")
            assertThat(note.type).isEqualTo(noteType)

            val newRepository = NoteFilesystem(project)
            val retrieved = newRepository.getNoteById(note.id)
            assertThat(retrieved?.type).isEqualTo(noteType)
        }
    }

    fun testLoadDataWithInvalidNoteType() {
        storageFile.parentFile?.mkdirs()
        storageFile.writeText(
            """
            paths: {}
            notes:
              note-id-1:
                name: test
                content: content
                type: INVALID_TYPE
                createdAt: 1000000
                updatedAt: 1000000
            """
                .trimIndent())

        val data = noteFilesystem.loadData()
        assertThat(data.notes["note-id-1"]?.type).isEqualTo(NoteType.OTHER)
    }

    fun testLoadDataWithMissingFields() {
        storageFile.parentFile?.mkdirs()
        storageFile.writeText(
            """
            paths:
              path-id-1:
                label: test-path
            notes:
              note-id-1:
                name: test-note
            """
                .trimIndent())

        val data = noteFilesystem.loadData()
        assertThat(data.paths["path-id-1"]?.label).isEqualTo("test-path")
        assertThat(data.notes["note-id-1"]?.name).isEqualTo("test-note")
        assertThat(data.notes["note-id-1"]?.type).isEqualTo(NoteType.OTHER)
        assertThat(data.notes["note-id-1"]?.content).isEmpty()
    }

    fun testMoveNoteToValidPath() {
        val path1Id = noteFilesystem.addPath(null, "path1")
        val path2Id = noteFilesystem.addPath(null, "path2")
        val note = noteFilesystem.addNote(path1Id, "test-note", NoteType.PHP, "content")

        noteFilesystem.moveNote(note.id, path2Id)

        val movedNote = noteFilesystem.getNoteById(note.id)
        assertThat(movedNote?.pathId).isEqualTo(path2Id)
        assertThat(movedNote?.name).isEqualTo("test-note")
        assertThat(movedNote?.updatedAt).isGreaterThanOrEqualTo(note.updatedAt)
    }

    fun testMoveNoteToRoot() {
        val pathId = noteFilesystem.addPath(null, "path")
        val note = noteFilesystem.addNote(pathId, "test-note", NoteType.PHP, "content")

        noteFilesystem.moveNote(note.id, null)

        val movedNote = noteFilesystem.getNoteById(note.id)
        assertThat(movedNote?.pathId).isNull()
    }

    fun testMoveNoteToInvalidPathThrowsException() {
        val note = noteFilesystem.addNote(null, "test-note", NoteType.PHP, "content")

        assertThatThrownBy { noteFilesystem.moveNote(note.id, "invalid-id") }
            .isInstanceOf(NotesException::class.java)
            .hasMessage("Target path not found")
    }

    fun testMoveNoteWithDuplicateNameThrowsException() {
        val path1Id = noteFilesystem.addPath(null, "path1")
        val path2Id = noteFilesystem.addPath(null, "path2")
        val note1 = noteFilesystem.addNote(path1Id, "duplicate", NoteType.PHP, "content1")
        noteFilesystem.addNote(path2Id, "duplicate", NoteType.PHP, "content2")

        assertThatThrownBy { noteFilesystem.moveNote(note1.id, path2Id) }
            .isInstanceOf(NotesException::class.java)
            .hasMessage("Note with name 'duplicate' already exists in target path")
    }

    fun testMoveNoteWithInvalidNoteIdThrowsException() {
        val pathId = noteFilesystem.addPath(null, "path")

        assertThatThrownBy { noteFilesystem.moveNote("invalid-note-id", pathId) }
            .isInstanceOf(NotesException::class.java)
            .hasMessage("Note not found")
    }

    fun testMovePathToValidParent() {
        val parent1Id = noteFilesystem.addPath(null, "parent1")
        val parent2Id = noteFilesystem.addPath(null, "parent2")
        val childId = noteFilesystem.addPath(parent1Id, "child")

        noteFilesystem.movePath(childId, parent2Id)

        val movedPath = noteFilesystem.getPathById(childId)
        assertThat(movedPath?.parentId).isEqualTo(parent2Id)
        assertThat(movedPath?.label).isEqualTo("child")
        assertThat(noteFilesystem.getFullPath(childId)).isEqualTo("parent2/child")
    }

    fun testMovePathToRoot() {
        val parentId = noteFilesystem.addPath(null, "parent")
        val childId = noteFilesystem.addPath(parentId, "child")

        noteFilesystem.movePath(childId, null)

        val movedPath = noteFilesystem.getPathById(childId)
        assertThat(movedPath?.parentId).isNull()
        assertThat(noteFilesystem.getFullPath(childId)).isEqualTo("child")
    }

    fun testMovePathToItselfThrowsException() {
        val pathId = noteFilesystem.addPath(null, "path")

        assertThatThrownBy { noteFilesystem.movePath(pathId, pathId) }
            .isInstanceOf(NotesException::class.java)
            .hasMessage("Cannot move path into itself")
    }

    fun testMovePathToDescendantThrowsException() {
        val grandparentId = noteFilesystem.addPath(null, "grandparent")
        val parentId = noteFilesystem.addPath(grandparentId, "parent")
        val childId = noteFilesystem.addPath(parentId, "child")

        assertThatThrownBy { noteFilesystem.movePath(grandparentId, childId) }
            .isInstanceOf(NotesException::class.java)
            .hasMessage("Cannot move path into its own descendant")

        assertThatThrownBy { noteFilesystem.movePath(parentId, childId) }
            .isInstanceOf(NotesException::class.java)
            .hasMessage("Cannot move path into its own descendant")
    }

    fun testMovePathWithDuplicateNameThrowsException() {
        val parent1Id = noteFilesystem.addPath(null, "parent1")
        val parent2Id = noteFilesystem.addPath(null, "parent2")
        val child1Id = noteFilesystem.addPath(parent1Id, "duplicate")
        noteFilesystem.addPath(parent2Id, "duplicate")

        assertThatThrownBy { noteFilesystem.movePath(child1Id, parent2Id) }
            .isInstanceOf(NotesException::class.java)
            .hasMessage("Path with name 'duplicate' already exists in target location")
    }

    fun testMovePathToInvalidParentThrowsException() {
        val pathId = noteFilesystem.addPath(null, "path")

        assertThatThrownBy { noteFilesystem.movePath(pathId, "invalid-id") }
            .isInstanceOf(NotesException::class.java)
            .hasMessage("Target path not found")
    }

    fun testMovePathWithInvalidPathIdThrowsException() {
        val parentId = noteFilesystem.addPath(null, "parent")

        assertThatThrownBy { noteFilesystem.movePath("invalid-path-id", parentId) }
            .isInstanceOf(NotesException::class.java)
            .hasMessage("Path not found")
    }

    fun testMovePathWithNotesPreservesNotes() {
        val parent1Id = noteFilesystem.addPath(null, "parent1")
        val parent2Id = noteFilesystem.addPath(null, "parent2")
        val childId = noteFilesystem.addPath(parent1Id, "child")
        val note = noteFilesystem.addNote(childId, "test-note", NoteType.PHP, "content")

        noteFilesystem.movePath(childId, parent2Id)

        val retrievedNote = noteFilesystem.getNoteById(note.id)
        assertThat(retrievedNote?.pathId).isEqualTo(childId)
        assertThat(retrievedNote?.name).isEqualTo("test-note")
    }

    fun testMoveDeepPathStructure() {
        val root1Id = noteFilesystem.addPath(null, "root1")
        val root2Id = noteFilesystem.addPath(null, "root2")
        val level1Id = noteFilesystem.addPath(root1Id, "level1")
        val level2Id = noteFilesystem.addPath(level1Id, "level2")
        val level3Id = noteFilesystem.addPath(level2Id, "level3")

        noteFilesystem.movePath(level1Id, root2Id)

        assertThat(noteFilesystem.getFullPath(level1Id)).isEqualTo("root2/level1")
        assertThat(noteFilesystem.getFullPath(level2Id)).isEqualTo("root2/level1/level2")
        assertThat(noteFilesystem.getFullPath(level3Id)).isEqualTo("root2/level1/level2/level3")
    }

    fun testRenamePathByIdWithDuplicateNameThrowsException() {
        val parentId = noteFilesystem.addPath(null, "parent")
        val child1Id = noteFilesystem.addPath(parentId, "child1")
        noteFilesystem.addPath(parentId, "child2")

        assertThatThrownBy { noteFilesystem.renamePathById(child1Id, "child2") }
            .isInstanceOf(NotesException::class.java)
            .hasMessage("Path or note with full path 'parent/child2' already exists")
    }

    fun testRenameNoteByIdWithDuplicateNameThrowsException() {
        val pathId = noteFilesystem.addPath(null, "path")
        val note1 = noteFilesystem.addNote(pathId, "note1", NoteType.PHP, "content1")
        noteFilesystem.addNote(pathId, "note2", NoteType.PHP, "content2")

        assertThatThrownBy { noteFilesystem.renameNoteById(note1.id, "note2") }
            .isInstanceOf(NotesException::class.java)
            .hasMessage("Path or note with full path 'path/note2' already exists")
    }

    fun testCannotRenameIfTargetExists() {
        val pathOne = noteFilesystem.addPath(null, "1")
        val pathTwo = noteFilesystem.addPath(null, "2")
        val pathOneDotOne = noteFilesystem.addPath(pathOne, "1.1")
        val pathOneDotTwo = noteFilesystem.addPath(pathOne, "1.2")

        val noteAtRoot1 = noteFilesystem.addNote(null, "noteAtRoot1", NoteType.PHP, "")
        val noteAtRoot2 = noteFilesystem.addNote(null, "noteAtRoot2", NoteType.PHP, "")
        val noteAOne1 = noteFilesystem.addNote(pathOne, "noteAOne1", NoteType.PHP, "")
        val noteAOne2 = noteFilesystem.addNote(pathOne, "noteAOne2", NoteType.PHP, "")
        // dumpFileSystemAsText(noteFilesystem.loadData())

        assertThatThrownBy { noteFilesystem.renamePathById(pathOne, "2") }
            .isInstanceOf(NotesException::class.java)
            .hasMessage("Path or note with full path '2' already exists")

        assertThatThrownBy { noteFilesystem.renamePathById(pathOne, "1") }
            .isInstanceOf(NotesException::class.java)
            .hasMessage("Path or note with full path '1' already exists")

        assertThatThrownBy { noteFilesystem.renamePathById(pathOne, "noteAtRoot1") }
            .isInstanceOf(NotesException::class.java)
            .hasMessage("Path or note with full path 'noteAtRoot1' already exists")

        assertThatThrownBy { noteFilesystem.renameNoteById(noteAtRoot1.id, "1") }
            .isInstanceOf(NotesException::class.java)
            .hasMessage("Path or note with full path '1' already exists")

        assertThatThrownBy { noteFilesystem.renameNoteById(noteAtRoot1.id, "noteAtRoot1") }
            .isInstanceOf(NotesException::class.java)
            .hasMessage("Path or note with full path 'noteAtRoot1' already exists")

        assertThatThrownBy { noteFilesystem.renameNoteById(noteAtRoot1.id, "noteAtRoot2") }
            .isInstanceOf(NotesException::class.java)
            .hasMessage("Path or note with full path 'noteAtRoot2' already exists")

        assertThatThrownBy { noteFilesystem.renamePathById(pathOneDotOne, "1.2") }
            .isInstanceOf(NotesException::class.java)
            .hasMessage("Path or note with full path '1/1.2' already exists")

        assertThatThrownBy { noteFilesystem.renamePathById(pathOneDotOne, "1.1") }
            .isInstanceOf(NotesException::class.java)
            .hasMessage("Path or note with full path '1/1.1' already exists")

        assertThatThrownBy { noteFilesystem.renamePathById(pathOneDotOne, "noteAOne1") }
            .isInstanceOf(NotesException::class.java)
            .hasMessage("Path or note with full path '1/noteAOne1' already exists")

        assertThatThrownBy { noteFilesystem.renameNoteById(noteAOne1.id, "1.1") }
            .isInstanceOf(NotesException::class.java)
            .hasMessage("Path or note with full path '1/1.1' already exists")

        assertThatThrownBy { noteFilesystem.renameNoteById(noteAOne1.id, "noteAOne1") }
            .isInstanceOf(NotesException::class.java)
            .hasMessage("Path or note with full path '1/noteAOne1' already exists")

        assertThatThrownBy { noteFilesystem.renameNoteById(noteAOne1.id, "noteAOne2") }
            .isInstanceOf(NotesException::class.java)
            .hasMessage("Path or note with full path '1/noteAOne2' already exists")
    }

    fun testPersistenceAfterMoveNote() {
        val path1Id = noteFilesystem.addPath(null, "path1")
        val path2Id = noteFilesystem.addPath(null, "path2")
        val note = noteFilesystem.addNote(path1Id, "test-note", NoteType.PHP, "content")

        noteFilesystem.moveNote(note.id, path2Id)

        val newRepository = NoteFilesystem(project)
        val retrievedNote = newRepository.getNoteById(note.id)
        assertThat(retrievedNote?.pathId).isEqualTo(path2Id)
    }

    fun testPersistenceAfterMovePath() {
        val parent1Id = noteFilesystem.addPath(null, "parent1")
        val parent2Id = noteFilesystem.addPath(null, "parent2")
        val childId = noteFilesystem.addPath(parent1Id, "child")

        noteFilesystem.movePath(childId, parent2Id)

        val newRepository = NoteFilesystem(project)
        val retrievedPath = newRepository.getPathById(childId)
        assertThat(retrievedPath?.parentId).isEqualTo(parent2Id)
        assertThat(newRepository.getFullPath(childId)).isEqualTo("parent2/child")
    }

    private fun dumpFileSystemAsText(data: StorageData, indent: Int = 0, parentId: String? = null) {
        val indentString = " ".repeat(indent)
        data.paths
            .filter { (id, path) -> path.parentId == parentId }
            .toSortedMap(compareBy { data.paths[it]?.label })
            .forEach { (id, path) ->
                println(
                    "$indentString | ${path.label} (${noteFilesystem.loadData().paths.buildFullPath(id)})")
                dumpFileSystemAsText(data, indent + 2, path.id)
            }
        data.notes
            .filter { (id, note) -> note.pathId == parentId }
            .toSortedMap(compareBy { data.notes[it]?.name })
            .forEach { (id, note) ->
                var folder = noteFilesystem.loadData().paths.buildFullPath(note.pathId)
                if (folder.isNotEmpty()) folder = "$folder/"
                println("$indentString |> ${note.name}  ($folder${note.name})")
            }
    }
}
