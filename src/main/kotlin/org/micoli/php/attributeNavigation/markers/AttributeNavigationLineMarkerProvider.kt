package org.micoli.php.attributeNavigation.markers

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.find.FindModel
import com.intellij.ide.DataManager
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereManager
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereManagerImpl
import com.intellij.openapi.actionSystem.ActionUiKind
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Computable
import com.intellij.openapi.util.IconLoader.getIcon
import com.intellij.psi.PsiElement
import com.intellij.usageView.UsageInfo
import com.intellij.usages.UsageInfo2UsageAdapter
import com.jetbrains.php.lang.psi.elements.PhpAttribute
import java.awt.event.MouseEvent
import java.time.Duration
import javax.swing.Icon
import kotlinx.collections.immutable.toImmutableList
import org.micoli.php.attributeNavigation.service.AttributeNavigationService
import org.micoli.php.service.intellij.psi.PhpUtil.normalizeNonRootFQN
import org.micoli.php.service.intellij.psi.PsiElementUtil
import org.micoli.php.service.intellij.psi.PsiElementUtil.findFirstLeafElement
import org.micoli.php.service.intellij.search.ConcurrentSearchManager
import org.micoli.php.service.intellij.search.SearchWithCompletionIndicator
import org.micoli.php.ui.Notification.Companion.getInstance
import org.micoli.php.ui.popup.NavigableItem
import org.micoli.php.ui.popup.NavigableListPopup.showNavigablePopup
import org.micoli.php.ui.popup.NavigableListPopupItem

class AttributeNavigationLineMarkerProvider : LineMarkerProvider {
    var concurrentSearchManager: ConcurrentSearchManager =
        ConcurrentSearchManager(Duration.ofSeconds(20))

    var navigateIcon: Icon =
        getIcon("icons/xml.svg", AttributeNavigationLineMarkerProvider::class.java)

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        return null
    }

    override fun collectSlowLineMarkers(
        elements: MutableList<out PsiElement?>,
        result: MutableCollection<in LineMarkerInfo<*>?>
    ) {
        if (elements.isEmpty()) {
            return
        }
        val firstElement = elements.first() ?: return
        val attributeNavigationService: AttributeNavigationService =
            AttributeNavigationService.getInstance(firstElement.project)
        if (attributeNavigationService.configurationIsEmpty()) {
            return
        }
        for (element in elements) {
            if (element is PhpAttribute) {
                processPhpAttribute(attributeNavigationService, element, result)
            }
        }
    }

    private fun processPhpAttribute(
        attributeNavigationService: AttributeNavigationService,
        phpAttribute: PhpAttribute,
        result: MutableCollection<in LineMarkerInfo<*>?>
    ) {
        for (rule in attributeNavigationService.rules) {
            val fqn = phpAttribute.fqn ?: continue
            if (normalizeNonRootFQN(fqn) != normalizeNonRootFQN(rule.attributeFQCN)) {
                continue
            }

            for (attributeArgument in phpAttribute.arguments) {
                if (!(attributeArgument.name == rule.propertyName ||
                    (rule.isDefault && attributeArgument.name.isEmpty()))) {
                    continue
                }
                val leafElement = findFirstLeafElement(phpAttribute)

                val value = attributeArgument.argument.value.replace("^[\"']|[\"']$".toRegex(), "")
                result.add(
                    LineMarkerInfo(
                        leafElement,
                        leafElement.textRange,
                        navigateIcon,
                        { "Search for [$value]" },
                        { mouseEvent: MouseEvent?, _: PsiElement? ->
                            when (rule.actionType) {
                                "find_in_file" ->
                                    openGlobalSearchWithRouteExpression(
                                        phpAttribute.project,
                                        mouseEvent!!,
                                        attributeNavigationService.getFormattedValue(
                                            value, rule.formatterScript),
                                        rule.fileMask)

                                "search_everywhere" ->
                                    openSearchEveryWhereWithRouteExpression(
                                        phpAttribute.project,
                                        mouseEvent!!,
                                        attributeNavigationService.getFormattedValue(
                                            value, rule.formatterScript))
                            }
                        },
                        GutterIconRenderer.Alignment.CENTER,
                        { "Search for [$value]" }))
            }
        }
    }

    private fun openGlobalSearchWithRouteExpression(
        project: Project,
        mouseEvent: MouseEvent,
        searchText: String?,
        fileMask: String?
    ) {
        if (searchText == null) {
            return
        }
        if (concurrentSearchManager.isSearchInProgress(searchText)) {
            getInstance(project).messageWithTimeout("Search already in progress", 1000)
            return
        }

        ApplicationManager.getApplication().invokeLater {
            val findModel: FindModel = getFindModel(searchText, fileMask)
            concurrentSearchManager.addSearch(searchText)
            SearchWithCompletionIndicator.findUsagesWithProgress(findModel, project, 1500) {
                results: MutableList<UsageInfo> ->
                concurrentSearchManager.removeSearch(searchText)
                if (results.isEmpty()) {
                    getInstance(project).messageWithTimeout("No Usage found", 1500)
                    return@findUsagesWithProgress
                }
                showNavigablePopup(
                    mouseEvent,
                    results
                        .stream()
                        .map {
                            ApplicationManager.getApplication()
                                .runReadAction<NavigableItem?>(
                                    Computable {
                                        it.file ?: return@Computable null

                                        val fileExtract = PsiElementUtil.getFileExtract(it, 1)
                                        NavigableItem(
                                            fileExtract, UsageInfo2UsageAdapter(it), it.icon)
                                    })
                        }
                        .map { it as NavigableListPopupItem }
                        .filter { it != null }
                        .toList()
                        .toImmutableList())
            }
        }
    }

    private fun openSearchEveryWhereWithRouteExpression(
        project: Project,
        mouseEvent: MouseEvent,
        searchText: String?
    ) {
        ApplicationManager.getApplication().invokeLater {
            SearchEverywhereManager.getInstance(project)
                .show(
                    SearchEverywhereManagerImpl.ALL_CONTRIBUTORS_GROUP_ID,
                    searchText,
                    AnActionEvent.createEvent(
                        DataManager.getInstance().getDataContext(mouseEvent.component),
                        null,
                        "",
                        ActionUiKind.NONE,
                        mouseEvent))
        }
    }

    private fun getFindModel(searchText: String, fileMask: String?): FindModel {
        val findModel = FindModel()

        findModel.isGlobal = true
        findModel.isProjectScope = true
        findModel.isRegularExpressions = true
        findModel.isWithSubdirectories = true
        findModel.fileFilter = null
        findModel.stringToFind = searchText
        findModel.fileFilter = fileMask

        return findModel
    }
}
