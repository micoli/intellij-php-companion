package org.micoli.php.configuration

import com.google.gson.JsonElement
import com.google.gson.JsonObject

object GsonTools {
    @Throws(JsonObjectExtensionConflictException::class)
    fun extendJsonObject(
        destinationObject: JsonObject,
        conflictResolutionStrategy: ConflictStrategy,
        vararg objs: JsonObject,
    ) {
        for (obj in objs) {
            extendJsonObject(destinationObject, obj, conflictResolutionStrategy)
        }
    }

    @Throws(JsonObjectExtensionConflictException::class)
    private fun extendJsonObject(
        leftObj: JsonObject,
        rightObj: JsonObject,
        conflictStrategy: ConflictStrategy
    ) {
        for (rightEntry in rightObj.entrySet()) {
            val rightKey = rightEntry.key
            val rightVal = rightEntry.value
            if (leftObj.has(rightKey)) {
                val leftVal = leftObj.get(rightKey)
                if (leftVal.isJsonArray && rightVal.isJsonArray) {
                    val leftArr = leftVal.asJsonArray
                    val rightArr = rightVal.asJsonArray
                    for (i in 0 until rightArr.size()) {
                        if (leftArr.contains(rightArr.get(i))) {
                            continue
                        }
                        leftArr.add(rightArr.get(i))
                    }
                } else if (leftVal.isJsonObject && rightVal.isJsonObject) {
                    extendJsonObject(leftVal.asJsonObject, rightVal.asJsonObject, conflictStrategy)
                } else {
                    handleMergeConflict(rightKey, leftObj, leftVal, rightVal, conflictStrategy)
                }
            } else {
                leftObj.add(rightKey, rightVal)
            }
        }
    }

    @Throws(JsonObjectExtensionConflictException::class)
    private fun handleMergeConflict(
        key: String,
        leftObj: JsonObject,
        leftVal: JsonElement,
        rightVal: JsonElement,
        conflictStrategy: ConflictStrategy,
    ) {
        run {
            when (conflictStrategy) {
                ConflictStrategy.PREFER_FIRST_OBJ -> {}
                ConflictStrategy.PREFER_SECOND_OBJ -> leftObj.add(key, rightVal)
                ConflictStrategy.PREFER_NON_NULL ->
                    if (leftVal.isJsonNull && !rightVal.isJsonNull) {
                        leftObj.add(key, rightVal)
                    }
                ConflictStrategy.THROW_EXCEPTION ->
                    throw JsonObjectExtensionConflictException(
                        ("Key $key exists in both objects and the conflict resolution strategy is $conflictStrategy"))
            }
        }
    }

    enum class ConflictStrategy {
        THROW_EXCEPTION,
        PREFER_FIRST_OBJ,
        PREFER_SECOND_OBJ,
        PREFER_NON_NULL,
    }

    class JsonObjectExtensionConflictException(message: String?) : Exception(message)
}
