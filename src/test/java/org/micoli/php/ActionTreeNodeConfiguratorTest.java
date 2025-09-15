package org.micoli.php;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.ui.treeStructure.Tree;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import org.jetbrains.annotations.NotNull;
import org.micoli.php.builders.*;
import org.micoli.php.tasks.configuration.AbstractNode;
import org.micoli.php.tasks.configuration.Path;
import org.micoli.php.tasks.configuration.Task;
import org.micoli.php.tasks.configuration.runnableTask.Script;
import org.micoli.php.tasks.configuration.runnableTask.Shell;
import org.micoli.php.ui.components.tasks.tree.*;

public class ActionTreeNodeConfiguratorTest extends BasePlatformTestCase {

    private ActionTreeNodeConfigurator configurator;
    private Tree tree;
    private DefaultMutableTreeNode root;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        root = new DefaultMutableTreeNode("Root");
        tree = new Tree(new DefaultTreeModel(root));
        configurator = new ActionTreeNodeConfigurator(getProject(), tree);
    }

    public void testConfigureTree() {
        Shell shell = ShellBuilder.create()
                .withId("shellTask")
                .withLabel("Shell Task")
                .withIcon("/icons/shell.svg")
                .build();

        Script script = ScriptBuilder.create()
                .withId("scriptTask")
                .withLabel("Script Task")
                .withIcon("/icons/script.svg")
                .build();

        Task task1 = TaskBuilder.create().withTaskId("shellTask").build();

        Task task2 = TaskBuilder.create()
                .withTaskId("scriptTask")
                .withLabel("Custom Script Label")
                .build();

        Path path1 = PathBuilder.create()
                .withLabel("a Path")
                .withTasks(new AbstractNode[] {task1, task2})
                .build();

        Path path2 = PathBuilder.create()
                .withLabel("an emptyPath")
                .withTasks(new AbstractNode[] {})
                .build();

        configurator.configureTree(
                Map.of(shell.id, shell, script.id, script), new AbstractNode[] {task1, task2, path1, path2});

        assertFalse(tree.isRootVisible());
        assertTrue(tree.getShowsRootHandles());

        assertTreeEquals(
                """
            00-(1)=>Root
            01--(2)=>Shell Task
            02--(2)=>Custom Script Label
            03--(2)=>a Path
            04---(3)=>Shell Task
            05---(3)=>Custom Script Label
            06--(2)=>an emptyPath
            """);
    }

    private void assertTreeEquals(String s) {
        assertEquals(s.trim(), dumpTree());
    }

    private @NotNull String dumpTree() {
        List<String> expectedLabels = new ArrayList<>();
        TreeIterator.forEach(tree, (node, isLeaf, path, level, index) -> {
            String indent = "-".repeat(level);
            expectedLabels.add(
                    switch (node) {
                        case PathNode pathNode -> String.format(
                                "%02d%s(%d)=>%s", index, indent, level, pathNode.getLabel());
                        case LabeledTreeNode labeledTreeNode -> String.format(
                                "%02d%s(%d)=>%s", index, indent, level, labeledTreeNode.getLabel());
                        default -> String.format("%02d%s(%d)=>%s", index, indent, level, node.toString());
                    });
        });
        return String.join("\n", expectedLabels).trim();
    }
}
