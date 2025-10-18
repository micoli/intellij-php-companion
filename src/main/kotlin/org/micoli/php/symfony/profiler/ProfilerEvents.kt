package org.micoli.php.symfony.profiler

import com.intellij.util.messages.Topic

interface ProfilerEvents {
    fun indexUpdated()

    companion object {
        @JvmField
        val INDEX_UPDATED: Topic<ProfilerEvents> =
            Topic.create<ProfilerEvents>("Profiler Index update", ProfilerEvents::class.java)
    }
}
