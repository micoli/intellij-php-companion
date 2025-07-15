package org.micoli.php.attributeNavigation.markers;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.find.FindModel;
import com.intellij.ide.DataManager;
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereManager;
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereManagerImpl;
import com.intellij.openapi.actionSystem.ActionUiKind;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.usages.UsageInfo2UsageAdapter;
import com.jetbrains.php.lang.psi.elements.PhpAttribute;
import java.awt.event.MouseEvent;
import java.time.Duration;
import java.util.*;
import javax.swing.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.micoli.php.attributeNavigation.configuration.NavigationByAttributeRule;
import org.micoli.php.attributeNavigation.service.AttributeNavigationService;
import org.micoli.php.service.*;
import org.micoli.php.ui.Notification;
import org.micoli.php.ui.popup.FileExtract;
import org.micoli.php.ui.popup.NavigableItem;
import org.micoli.php.ui.popup.NavigatableListPopup;

public class AttributeNavigationLineMarkerProvider implements LineMarkerProvider {
    ConcurrentSearchManager concurrentSearchManager = new ConcurrentSearchManager(Duration.ofSeconds(20));

    Icon navigateIcon = IconLoader.getIcon("icons/xml.svg", AttributeNavigationLineMarkerProvider.class);

    public AttributeNavigationLineMarkerProvider() {}

    @Override
    public @Nullable LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        return null;
    }

    @Override
    public void collectSlowLineMarkers(
            @NotNull List<? extends PsiElement> elements, @NotNull Collection<? super LineMarkerInfo<?>> result) {
        if (elements.isEmpty()) {
            return;
        }
        AttributeNavigationService attributeNavigationService =
                AttributeNavigationService.getInstance(elements.getFirst().getProject());
        if (attributeNavigationService.configurationIsEmpty()) {
            return;
        }
        for (PsiElement element : elements) {
            if (element instanceof PhpAttribute phpAttribute) {
                processPhpAttribute(attributeNavigationService, phpAttribute, result);
            }
        }
    }

    private void processPhpAttribute(
            AttributeNavigationService attributeNavigationService,
            PhpAttribute phpAttribute,
            Collection<? super LineMarkerInfo<?>> result) {
        for (NavigationByAttributeRule rule : attributeNavigationService.getRules()) {
            String fqn = phpAttribute.getFQN();
            if (fqn == null) {
                continue;
            }
            if (!PhpUtil.normalizeNonRootFQN(fqn).equals(PhpUtil.normalizeNonRootFQN(rule.attributeFQCN))) {
                continue;
            }

            for (@NotNull PhpAttribute.PhpAttributeArgument attributeArgument : phpAttribute.getArguments()) {
                if (!(attributeArgument.getName().equals(rule.propertyName)
                        || (rule.isDefault && attributeArgument.getName().isEmpty()))) {
                    continue;
                }
                PsiElement leafElement = PsiElementUtil.findFirstLeafElement(phpAttribute);

                String value = attributeArgument.getArgument().getValue().replaceAll("^[\"']|[\"']$", "");
                result.add(new LineMarkerInfo<>(
                        leafElement,
                        leafElement.getTextRange(),
                        navigateIcon,
                        element -> "Search for [" + value + "]",
                        (mouseEvent, psiElement) -> {
                            switch (rule.actionType) {
                                case "find_in_file":
                                    openGlobalSearchWithRouteExpression(
                                            phpAttribute.getProject(),
                                            mouseEvent,
                                            attributeNavigationService.getFormattedValue(value, rule.formatterScript),
                                            rule.fileMask);
                                    break;
                                case "search_everywhere":
                                    openSearchEveryWhereWithRouteExpression(
                                            phpAttribute.getProject(),
                                            mouseEvent,
                                            attributeNavigationService.getFormattedValue(value, rule.formatterScript));
                                    break;
                            }
                        },
                        GutterIconRenderer.Alignment.CENTER,
                        () -> "Search for [" + value + "]"));
            }
        }
    }

    private void openGlobalSearchWithRouteExpression(
            Project project, MouseEvent mouseEvent, String searchText, String fileMask) {
        if (concurrentSearchManager.isSearchInProgress(searchText)) {
            Notification.messageWithTimeout("Search already in progress", 1000);
            return;
        }

        ApplicationManager.getApplication().invokeLater(() -> {
            FindModel findModel = getFindModel(searchText, fileMask);
            concurrentSearchManager.addSearch(searchText);
            SearchWithCompletionIndicator.findUsagesWithProgress(findModel, project, 1500, results -> {
                concurrentSearchManager.removeSearch(searchText);
                if (results == null || results.isEmpty()) {
                    Notification.messageWithTimeout("No Usage found", 1500);
                    return;
                }
                NavigatableListPopup.showNavigablePopup(
                        mouseEvent,
                        results.stream()
                                .map(usageInfo -> ApplicationManager.getApplication()
                                        .runReadAction((Computable<NavigableItem>) () -> {
                                            PsiFile file = usageInfo.getFile();
                                            if (file == null) {
                                                return null;
                                            }
                                            FileExtract fileExtract = PsiElementUtil.getFileExtract(usageInfo);
                                            return new NavigableItem(
                                                    PathUtil.getPathWithParent(file, 2),
                                                    fileExtract,
                                                    new UsageInfo2UsageAdapter(usageInfo),
                                                    usageInfo.getIcon());
                                        }))
                                .filter(Objects::nonNull)
                                .toList());
            });
        });
    }

    private void openSearchEveryWhereWithRouteExpression(Project project, MouseEvent mouseEvent, String searchText) {

        ApplicationManager.getApplication().invokeLater(() -> {
            SearchEverywhereManager.getInstance(project)
                    .show(
                            SearchEverywhereManagerImpl.ALL_CONTRIBUTORS_GROUP_ID,
                            searchText,
                            AnActionEvent.createEvent(
                                    DataManager.getInstance().getDataContext(mouseEvent.getComponent()),
                                    null,
                                    "",
                                    ActionUiKind.NONE,
                                    mouseEvent));
        });
    }

    private static @NotNull FindModel getFindModel(String searchText, String fileMask) {
        FindModel findModel = new FindModel();

        findModel.setGlobal(true);
        findModel.setProjectScope(true);
        findModel.setRegularExpressions(true);
        findModel.setWithSubdirectories(true);
        findModel.setFileFilter(null);
        findModel.setStringToFind(searchText);
        findModel.setFileFilter(fileMask);

        return findModel;
    }
}
