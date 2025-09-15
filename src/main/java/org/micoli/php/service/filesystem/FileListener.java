package org.micoli.php.service.filesystem;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class FileListener<Id> {
    protected static final Logger LOGGER = Logger.getInstance(FileListener.class.getSimpleName());
    boolean enabled;

    public interface VfsHandler<T> {
        void vfsHandle(T id, VirtualFile file);
    }

    private final BulkFileListener vfsListener;
    Map<Id, List<PathMatcher>> patterns = new HashMap<>();

    public FileListener(VfsHandler<Id> handler) {
        this.vfsListener = new BulkFileListener() {
            @Override
            public void after(@NotNull List<? extends VFileEvent> events) {
                events.forEach(event -> {
                    if (!enabled) {
                        return;
                    }
                    VirtualFile file = event.getFile();
                    if (file == null || file.isDirectory()) {
                        return;
                    }
                    Path path = Paths.get(file.getPath());
                    patterns.forEach((id, pathMatchers) -> {
                        pathMatchers.forEach(pathMatcher -> {
                            if (pathMatcher.matches(path)) {
                                handler.vfsHandle(id, file);
                            }
                        });
                    });
                });
            }
        };
    }

    public void setPatterns(Map<Id, List<PathMatcher>> patterns) {
        this.enabled = true;
        this.patterns = patterns;
    }

    public void reset() {
        this.enabled = false;
        this.patterns = new HashMap<>();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Map<Id, List<PathMatcher>> getPatterns() {
        return patterns;
    }

    public BulkFileListener getVfsListener() {
        return this.vfsListener;
    }
}
