package org.micoli.php.configuration.documentation;

public class Helper {
    public static boolean isCustomClass(Class<?> clazz) {
        if (clazz == null || clazz.isPrimitive()) {
            return false;
        }

        if (clazz.equals(String.class)
                || clazz.equals(Boolean.class)
                || clazz.equals(boolean.class)
                || clazz.equals(Integer.class)
                || clazz.equals(int.class)
                || clazz.equals(Long.class)
                || clazz.equals(long.class)
                || clazz.equals(Double.class)
                || clazz.equals(double.class)
                || clazz.equals(Float.class)
                || clazz.equals(float.class)) {
            return false;
        }

        if (clazz.getPackage() != null && clazz.getPackage().getName().startsWith("java.")) {
            return false;
        }

        return true;
    }
}
