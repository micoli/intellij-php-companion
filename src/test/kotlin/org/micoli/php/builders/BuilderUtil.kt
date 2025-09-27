package org.micoli.php.builders

fun <T> appendToArray(originalArray: Array<T>?, newElement: T, componentType: Class<T>): Array<T> {
    if (originalArray == null) {
        @Suppress("UNCHECKED_CAST")
        val newArray = java.lang.reflect.Array.newInstance(componentType, 1) as Array<T>
        newArray[0] = newElement
        return newArray
    }

    @Suppress("UNCHECKED_CAST")
    val newArray =
        java.lang.reflect.Array.newInstance(componentType, originalArray.size + 1) as Array<T>
    System.arraycopy(originalArray, 0, newArray, 0, originalArray.size)
    newArray[originalArray.size] = newElement
    return newArray
}
