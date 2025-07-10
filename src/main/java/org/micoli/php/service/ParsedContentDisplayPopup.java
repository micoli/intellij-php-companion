package org.micoli.php.service;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.util.ui.JBUI;
import org.micoli.php.ui.Notification;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;

public class ParsedContentDisplayPopup extends DialogWrapper {

    public enum PopupType {
        JSON, PHP
    }

    private final PopupType popupType;
    private final String title;
    private final String jsonContent;
    private final Project project;
    private Editor editor;

    public ParsedContentDisplayPopup(@NotNull PopupType popupType, @Nullable Project project, @NotNull String title, @NotNull String jsonContent) {
        super(project);
        this.popupType = popupType;
        this.project = project;
        this.title = title;
        this.jsonContent = jsonContent;

        setTitle(title);
        setModal(true);
        setResizable(true);

        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setPreferredSize(JBUI.size(600, 400));
        editor = createJsonEditor(popupType);
        JComponent editorComponent = editor.getComponent();

        JScrollPane scrollPane = new JScrollPane(editorComponent);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        mainPanel.add(scrollPane, BorderLayout.CENTER);

        return mainPanel;
    }

    private Editor createJsonEditor(PopupType popupType) {
        FileType fileType = FileTypeManager.getInstance().getFileTypeByExtension(popupType == PopupType.PHP ? "php" : "json");

        EditorFactory editorFactory = EditorFactory.getInstance();
        com.intellij.openapi.editor.Document document = editorFactory.createDocument(jsonContent);
        Editor editor = editorFactory.createViewer(document, project);

        EditorSettings settings = editor.getSettings();
        settings.setLineNumbersShown(true);
        settings.setFoldingOutlineShown(false);
        settings.setLineMarkerAreaShown(false);
        settings.setIndentGuidesShown(true);
        settings.setVirtualSpace(false);
        settings.setWheelFontChangeEnabled(false);

        if (editor instanceof EditorEx) {
            EditorEx editorEx = (EditorEx) editor;
            EditorHighlighter highlighter = EditorHighlighterFactory.getInstance().createEditorHighlighter(project, fileType);
            editorEx.setHighlighter(highlighter);
        }

        return editor;
    }

    @Override
    protected Action @NotNull [] createActions() {
        return new Action[] { new CopyAction(), getCancelAction() };
    }

    private class CopyAction extends AbstractAction {

        public CopyAction() {
            super("Copy");
            putValue(Action.SHORT_DESCRIPTION, "Copy " + (popupType == PopupType.PHP ? "PHP" : "JSON"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            StringSelection stringSelection = new StringSelection(jsonContent);
            CopyPasteManager.getInstance().setContents(stringSelection);
            Notification.messageWithTimeout("Content copied to clipboard", 500);
            close(OK_EXIT_CODE);
        }
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();
    }

    @Override
    public void doCancelAction() {
        super.doCancelAction();
    }

    @Override
    protected void dispose() {
        if (editor != null) {
            EditorFactory.getInstance().releaseEditor(editor);
        }
        super.dispose();
    }

    public static void showJsonPopup(@Nullable Project project, @NotNull String title, @NotNull String jsonContent) {
        SwingUtilities.invokeLater(() -> {
            ParsedContentDisplayPopup dialog = new ParsedContentDisplayPopup(PopupType.JSON, project, title, jsonContent);
            dialog.show();
        });
    }

    public static void showPhpPopup(@Nullable Project project, @NotNull String title, @NotNull String jsonContent) {
        SwingUtilities.invokeLater(() -> {
            ParsedContentDisplayPopup dialog = new ParsedContentDisplayPopup(PopupType.PHP, project, title, jsonContent);
            dialog.show();
        });
    }
}
