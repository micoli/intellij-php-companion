package org.micoli.php.attributeNavigation.markers;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.find.FindManager;
import com.intellij.find.FindModel;
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
                result.add(new LineMarkerInfo<>(leafElement, leafElement.getTextRange(), navigateIcon, element -> "Search for [" + attributeArgument.getArgument().getValue() + "]", (e, elt) -> openGlobalSearchWithRouteExpression(phpAttribute.getProject(), AttributeNavigationService.getFormattedValue(attributeArgument.getArgument().getValue(), rule.formatterScript), rule.fileMask), GutterIconRenderer.Alignment.CENTER, () -> "Search for [" + attributeArgument.getArgument().getValue() + "]"));
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

}
