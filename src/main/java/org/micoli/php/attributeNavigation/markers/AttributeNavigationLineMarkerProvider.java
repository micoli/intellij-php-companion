package org.micoli.php.attributeNavigation.markers;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.find.FindManager;
import com.intellij.find.FindModel;
import com.intellij.ide.DataManager;
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereManager;
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereManagerImpl;
import com.intellij.openapi.actionSystem.ActionUiKind;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.PhpAttribute;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.micoli.php.attributeNavigation.configuration.NavigationByAttributeRule;
import org.micoli.php.attributeNavigation.service.AttributeNavigationService;
import org.micoli.php.service.PsiElementUtil;
import org.micoli.php.service.PhpUtil;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.*;

public class AttributeNavigationLineMarkerProvider implements LineMarkerProvider {

    Icon navigateIcon = IconLoader.getIcon("icons/xml.svg", AttributeNavigationLineMarkerProvider.class);

    public AttributeNavigationLineMarkerProvider() {
    }

    @Override
    public @Nullable LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<? extends PsiElement> elements, @NotNull Collection<? super LineMarkerInfo<?>> result) {
        if (AttributeNavigationService.configurationIsEmpty()) {
            return;
        }
        for (PsiElement element : elements) {
            if (element instanceof PhpAttribute phpAttribute) {
                processPhpAttribute(phpAttribute, result);
            }
        }
    }

    private void processPhpAttribute(PhpAttribute phpAttribute, Collection<? super LineMarkerInfo<?>> result) {
        for (NavigationByAttributeRule rule : AttributeNavigationService.getRules()) {
            String fqn = phpAttribute.getFQN();
            if (fqn == null) {
                continue;
            }
            if (!PhpUtil.normalizeNonRootFQN(fqn).equals(PhpUtil.normalizeNonRootFQN(rule.attributeFQCN))) {
                continue;
            }

            for (@NotNull
            PhpAttribute.PhpAttributeArgument attributeArgument : phpAttribute.getArguments()) {
                if (!(attributeArgument.getName().equals(rule.propertyName) || (rule.isDefault && attributeArgument.getName().isEmpty()))) {
                    continue;
                }
                PsiElement leafElement = PsiElementUtil.findFirstLeafElement(phpAttribute);
                // spotless:off
                String value = attributeArgument
                    .getArgument()
                    .getValue()
                    .replaceAll("^[\"']|[\"']$", "");
                result.add(new LineMarkerInfo<>(leafElement, leafElement.getTextRange(), navigateIcon, element -> "Search for [" + value + "]", (mouseEvent, psiElement) -> {
                    switch (rule.actionType) {
                        case "find_in_file":
                            openGlobalSearchWithRouteExpression(
                                phpAttribute.getProject(),
                                AttributeNavigationService.getFormattedValue(value, rule.formatterScript),
                                rule.fileMask
                            );
                        break;
                        case "search_everywhere":
                            openSearchEveryWhereWithRouteExpression(
                                phpAttribute.getProject(),
                                mouseEvent,
                                AttributeNavigationService.getFormattedValue(value, rule.formatterScript)
                            );
                        break;
                    }
                }, GutterIconRenderer.Alignment.CENTER, () -> "Search for [" + value + "]"));
                // spotless:on
            }
        }
    }

    private void openGlobalSearchWithRouteExpression(Project project, String searchText, String fileMask) {
        ApplicationManager.getApplication().invokeLater(() -> {
            FindManager findManager = FindManager.getInstance(project);
            FindModel findModel = findManager.getFindInProjectModel().clone();

            findModel.setStringToFind(searchText);
            findModel.setRegularExpressions(true);
            findModel.setGlobal(true);
            findModel.setProjectScope(true);
            findModel.setFileFilter(fileMask);

            findManager.showFindDialog(findModel, () -> {
            });
        });
    }

    private void openSearchEveryWhereWithRouteExpression(Project project, MouseEvent mouseEvent, String searchText) {
        // spotless:off
        ApplicationManager.getApplication().invokeLater(() -> {
            SearchEverywhereManager
                .getInstance(project)
                .show(
                    SearchEverywhereManagerImpl.ALL_CONTRIBUTORS_GROUP_ID,
                    searchText,
                    AnActionEvent.createEvent(
                        DataManager.getInstance().getDataContext(mouseEvent.getComponent()),
                        null,
                        "",
                        ActionUiKind.NONE,
                        mouseEvent
                    )
                );
        });
        // spotless:on
    }
}
