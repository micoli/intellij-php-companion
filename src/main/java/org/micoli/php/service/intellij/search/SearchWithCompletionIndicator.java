package org.micoli.php.service.intellij.search;

import com.intellij.find.FindModel;
import com.intellij.find.impl.FindInProjectUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.usageView.UsageInfo;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;

public class SearchWithCompletionIndicator {
    @SuppressWarnings("BusyWait")
    public static void findUsagesWithProgress(
            FindModel findModel, Project project, int maxTimeSearchWithoutResult, Consumer<List<UsageInfo>> callback) {
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Finding usages", true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                List<UsageInfo> results = Collections.synchronizedList(new ArrayList<>());
                AtomicBoolean processingStarted = new AtomicBoolean(false);
                AtomicLong lastUpdateTime = new AtomicLong(System.currentTimeMillis());

                FindInProjectUtil.findUsages(
                        findModel,
                        project,
                        usageInfo -> {
                            if (indicator.isCanceled()) {
                                return false;
                            }

                            processingStarted.set(true);
                            results.add(usageInfo);
                            lastUpdateTime.set(System.currentTimeMillis());
                            indicator.setText("Found " + results.size() + " usages");
                            return true;
                        },
                        FindInProjectUtil.setupProcessPresentation(FindInProjectUtil.setupViewPresentation(findModel)));

                while (!indicator.isCanceled() && !indicator.isRunning()) {
                    try {
                        Thread.sleep(100);

                        if (processingStarted.get()
                                && (System.currentTimeMillis() - lastUpdateTime.get()) > maxTimeSearchWithoutResult) {
                            break;
                        }

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }

                ApplicationManager.getApplication().invokeLater(() -> {
                    callback.accept(new ArrayList<>(results));
                });
            }
        });
    }
}
