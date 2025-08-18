package org.micoli.php.ui.popup;

import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.usages.UsageTarget;

class SimpleUsageTarget implements UsageTarget {
    private final PsiElement element;
    private final String name;

    public SimpleUsageTarget(PsiElement element, String name) {
        this.element = element;
        this.name = name != null ? name : "Usage Target";
    }

    @Override
    public String getName() {
        if (element instanceof PsiNamedElement) {
            String elementName = ((PsiNamedElement) element).getName();
            return elementName != null ? elementName : name;
        }
        return name;
    }

    @Override
    public void findUsages() {}

    @Override
    public boolean isValid() {
        return element == null || element.isValid();
    }

    @Override
    public boolean canNavigate() {
        return element != null && element.isValid();
    }

    @Override
    public void navigate(boolean requestFocus) {
        if (canNavigate()) {
            // element.navigate(requestFocus);
        }
    }

    @Override
    public boolean canNavigateToSource() {
        return canNavigate();
    }

    @Override
    public ItemPresentation getPresentation() {
        return new ItemPresentation() {
            @Override
            public String getPresentableText() {
                return getName();
            }

            @Override
            public String getLocationString() {
                return null;
            }

            @Override
            public javax.swing.Icon getIcon(boolean unused) {
                return null;
            }
        };
    }

    public boolean isReadOnly() {
        return false;
    }

    public PsiElement getElement() {
        return element;
    }
}
