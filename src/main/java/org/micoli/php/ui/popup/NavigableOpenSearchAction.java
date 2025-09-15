package org.micoli.php.ui.popup;

import com.intellij.openapi.project.Project;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.usageView.UsageInfo;
import com.intellij.usages.*;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class NavigableOpenSearchAction implements NavigableListPopupItem {
    private final List<Navigatable> navigables;
    private final String findWindowTitle;
    private final String messageClassName;
    private final Project project;

    public NavigableOpenSearchAction(
            Project project, List<Navigatable> navigables, String findWindowTitle, String messageClassName) {
        this.project = project;
        this.navigables = navigables;
        this.findWindowTitle = findWindowTitle;
        this.messageClassName = messageClassName;
    }

    @Override
    public void navigate(boolean requestFocus) {

        if (this.navigables == null) {
            return;
        }
        List<Usage> usages = new ArrayList<>();
        PsiElement element = null;
        for (Navigatable navigatable : navigables) {
            element = extractPsiElement(navigatable);
            if (element != null && element.isValid()) {
                usages.add(new UsageInfo2UsageAdapter(new UsageInfo(element)));
            }
        }
        if (usages.isEmpty()) {
            return;
        }

        UsageTarget target = new SimpleUsageTarget(element, messageClassName);
        UsageTarget[] targets = {target};
        UsageViewPresentation presentation = new UsageViewPresentation();
        presentation.setTabText(findWindowTitle);
        presentation.setToolwindowTitle(findWindowTitle);
        presentation.setShowCancelButton(true);

        UsageViewManager.getInstance(project).showUsages(targets, usages.toArray(new Usage[0]), presentation);
    }

    @Override
    public boolean canNavigate() {
        return true;
    }

    public boolean canNavigateToSource() {
        return false;
    }

    @NotNull public String getText() {
        return String.format(
                """
                <html>
                    <div style="padding:5px">
                        <i style="color: orange;font-weight:bold">%s</i>
                    </div>
                </html>
                """,
                "Open results in Find window");
    }

    private static PsiElement extractPsiElement(Navigatable navigatable) {
        if (navigatable instanceof PsiElement element) {
            return element;
        }

        if (navigatable instanceof SmartPsiElementPointer<?> smartPsiElementPointer) {
            return smartPsiElementPointer.getElement();
        }

        return null;
    }
}
