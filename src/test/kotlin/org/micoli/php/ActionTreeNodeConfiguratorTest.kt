package org.micoli.php

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.ui.treeStructure.Tree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import org.assertj.core.api.Assertions.*
import org.micoli.php.builders.PathBuilder
import org.micoli.php.builders.ScriptBuilder
import org.micoli.php.builders.ShellBuilder
import org.micoli.php.builders.TaskBuilder
import org.micoli.php.ui.components.tasks.tree.ActionTreeNodeConfigurator
import org.micoli.php.ui.components.tasks.tree.LabeledTreeNode
import org.micoli.php.ui.components.tasks.tree.PathNode
import org.micoli.php.ui.components.tasks.tree.TreeIterator

class ActionTreeNodeConfiguratorTest : BasePlatformTestCase() {
    lateinit var configurator: ActionTreeNodeConfigurator
    lateinit var tree: Tree

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()

        val root = DefaultMutableTreeNode("Root")
        tree = Tree(DefaultTreeModel(root))
        configurator = ActionTreeNodeConfigurator(project, tree)
    }

    fun testConfigureTree() {
        val shell =
            ShellBuilder.create()
                .withId("shellTask")
                .withLabel("Shell Task")
                .withIcon("/icons/shell.svg")
                .build()

        val script =
            ScriptBuilder.create()
                .withId("scriptTask")
                .withLabel("Script Task")
                .withIcon("/icons/script.svg")
                .build()

        val task1 = TaskBuilder.create().withTaskId("shellTask").build()

        val task2 =
            TaskBuilder.create().withTaskId("scriptTask").withLabel("Custom Script Label").build()

        val path1 =
            PathBuilder.create().withLabel("a Path").withTasks(arrayOf(task1, task2)).build()

        val path2 = PathBuilder.create().withLabel("an emptyPath").withTasks(arrayOf()).build()

        configurator.configureTree(
            mapOf(shell.id!! to shell, script.id!! to script),
            arrayOf(task1, task2, path1, path2),
        )

        assertThat(tree.isRootVisible).isFalse
        assertThat(tree.showsRootHandles).isTrue

        assertTreeEquals()
    }

    private fun assertTreeEquals() {
        assertThat(dumpTree())
            .isEqualTo(
                """
        00-(1)=>Root
        01--(2)=>Shell Task
        02--(2)=>Custom Script Label
        03--(2)=>a Path
        04---(3)=>Shell Task
        05---(3)=>Custom Script Label
        06--(2)=>an emptyPath
        """
                    .trimIndent(),
            )
    }

    private fun dumpTree(): String {
        val expectedLabels = ArrayList<String>()
        TreeIterator.forEach(tree) { node, _, _, level, index ->
            val indent = "-".repeat(level)
            expectedLabels.add(
                when (node) {
                    is PathNode ->
                        String.format("%02d%s(%d)=>%s", index, indent, level, node.getLabel())
                    is LabeledTreeNode ->
                        String.format("%02d%s(%d)=>%s", index, indent, level, node.getLabel())
                    else -> String.format("%02d%s(%d)=>%s", index, indent, level, node.toString())
                })
        }
        return expectedLabels.joinToString("\n").trim()
    }
}
