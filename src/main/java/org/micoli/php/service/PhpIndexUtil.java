package org.micoli.php.service;

import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import java.util.ArrayList;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;

public class PhpIndexUtil {
    public static Collection<PhpClass> getAllSubclasses(@NotNull PhpIndex phpIndex, @NotNull String clazz) {
        Collection<PhpClass> phpClasses = new ArrayList<>();

        phpIndex.processAllSubclasses(clazz, phpClass -> {
            phpClasses.add(phpClass);
            return true;
        });

        return phpClasses;
    }
}
