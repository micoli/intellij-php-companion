package org.micoli.php

import java.io.FileInputStream
import junit.framework.TestCase
import org.micoli.php.symfony.profiler.ProfilerParser
import org.micoli.php.symfony.profiler.parsers.DBData
import org.micoli.php.symfony.profiler.parsers.LoggerData
import org.micoli.php.symfony.profiler.parsers.MessengerData
import org.micoli.php.symfony.profiler.parsers.RequestData

class ProfilerParserTest : TestCase() {
    private fun readFile(filename: String): String =
        FileInputStream(filename).readAllBytes().toString(Charsets.UTF_8)

    fun testItParseLogProfilePageInDTOs() {
        val logs =
            ProfilerParser()
                .loadProfilerPage(
                    LoggerData::class.java, readFile("src/test/resources/profiler/log.html"))
        assertEquals(160, logs.logs.size)
        assertEquals("2025-10-11T15:57:16.314Z", logs.logs[0].time.toString())
        assertEquals("request", logs.logs[0].channel)
        assertEquals("info", logs.logs[0].severity)
        assertEquals("Matched route \"blog_post\" .", logs.logs[0].message)
        assertTrue(logs.logs[0].context.length > 20)
    }

    fun testItParseDBProfilePageInDTOs() {
        val db =
            ProfilerParser()
                .loadProfilerPage(
                    DBData::class.java, readFile("src/test/resources/profiler/db.html"))
        assertEquals(12, db.queries.size)
        assertEquals(1, db.queries[0].index)
        assertEquals(
            "SELECT t0.id AS id_1, t0.title AS title_2, t0.slug AS slug_3, t0.summary AS summary_4, t0.content AS content_5, t0.published_at AS published_at_6, t0.author_id AS author_id_7 FROM symfony_demo_post t0 WHERE t0.slug = ? LIMIT 1",
            db.queries[0].sql)
        assertEquals(
            "SELECT t0.id AS id_1, t0.title AS title_2, t0.slug AS slug_3, t0.summary AS summary_4, t0.content AS content_5, t0.published_at AS published_at_6, t0.author_id AS author_id_7 FROM symfony_demo_post t0 WHERE t0.slug = 'mauris-dapibus-risus-quis-suscipit-vulputate' LIMIT 1;",
            db.queries[0].runnableSql)
        assertEquals(
            "<pre class=\"highlight highlight-sql\"><span class=\"keyword\">SELECT</span> <span class=\"word\">t0</span><span class=\"symbol\">.</span><span class=\"word\">id</span> <span class=\"keyword\">AS</span> <span class=\"word\">id_1</span><span class=\"symbol\">,</span> <span class=\"word\">t0</span><span class=\"symbol\">.</span><span class=\"word\">title</span> <span class=\"keyword\">AS</span> <span class=\"word\">title_2</span><span class=\"symbol\">,</span> <span class=\"word\">t0</span><span class=\"symbol\">.</span><span class=\"word\">slug</span> <span class=\"keyword\">AS</span> <span class=\"word\">slug_3</span><span class=\"symbol\">,</span> <span class=\"word\">t0</span><span class=\"symbol\">.</span><span class=\"word\">summary</span> <span class=\"keyword\">AS</span> <span class=\"word\">summary_4</span><span class=\"symbol\">,</span> <span class=\"word\">t0</span><span class=\"symbol\">.</span><span class=\"word\">content</span> <span class=\"keyword\">AS</span> <span class=\"word\">content_5</span><span class=\"symbol\">,</span> <span class=\"word\">t0</span><span class=\"symbol\">.</span><span class=\"word\">published_at</span> <span class=\"keyword\">AS</span> <span class=\"word\">published_at_6</span><span class=\"symbol\">,</span> <span class=\"word\">t0</span><span class=\"symbol\">.</span><span class=\"word\">author_id</span> <span class=\"keyword\">AS</span> <span class=\"word\">author_id_7</span> <span class=\"keyword\">FROM</span> <span class=\"word\">symfony_demo_post</span> <span class=\"word\">t0</span> <span class=\"keyword\">WHERE</span> <span class=\"word\">t0</span><span class=\"symbol\">.</span><span class=\"word\">slug</span> <span class=\"symbol\">=</span> <span class=\"word\">?</span> <span class=\"keyword\">LIMIT</span> <span class=\"number\">1</span></pre>",
            db.queries[0].htmlSql)
        assertEquals(0.2, db.queries[0].executionMS)
        assertEquals(13, db.queries[0].backtrace.size)
    }

    fun testItParseMessengerProfilePageInDTOs() {
        val messenger =
            ProfilerParser()
                .loadProfilerPage(
                    MessengerData::class.java,
                    readFile("src/test/resources/profiler/messenger.html"))
        assertEquals(3, messenger.stats.messageCount)
        assertTrue(
            messenger.dispatches[0]
                .dispatch
                ?.file
                ?.contains("src/Infrastructure/Bus/Messenger/MessengerDomainEventDispatcher.php")
                ?: false)
        assertTrue(
            messenger.dispatches[0]
                .messageLocation
                ?.file
                ?.contains("src/UseCase/ArticleViewed/Event.php") ?: false)
        assertEquals(23, messenger.dispatches[0].dispatch?.line)
        assertEquals("event.bus", messenger.dispatches[0].busName)
        assertEquals("App\\UseCase\\ArticleViewed\\Event", messenger.dispatches[0].messageName)
        assertTrue(
            messenger.dispatches[0]
                .messageLocation
                ?.file
                ?.contains("src/UseCase/ArticleViewed/Event.php") ?: false)
        assertEquals(7, messenger.dispatches[0].messageLocation?.line)
    }

    fun testItParseRequestProfilePageInDTOs() {
        val requestResponse =
            ProfilerParser()
                .loadProfilerPage(
                    RequestData::class.java, readFile("src/test/resources/profiler/request.html"))
        assertEquals("App\\Controller\\BlogController::postShow", requestResponse.controller)
        assertEquals("blog_post", requestResponse.route)
    }
}
