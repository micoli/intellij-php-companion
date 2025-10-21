package org.micoli.php

import java.io.FileInputStream
import junit.framework.TestCase
import org.assertj.core.api.Assertions.*
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
        assertThat(logs.logs.size).isEqualTo(160)
        assertThat(logs.logs[0].time.toString()).isEqualTo("2025-10-11T15:57:16.314Z")
        assertThat(logs.logs[0].channel).isEqualTo("request")
        assertThat(logs.logs[0].severity).isEqualTo("info")
        assertThat(logs.logs[0].message).isEqualTo("Matched route \"blog_post\" .")
        assertThat(logs.logs[0].context.length > 20).isTrue
    }

    fun testItParseDBProfilePageInDTOs() {
        val db =
            ProfilerParser()
                .loadProfilerPage(
                    DBData::class.java, readFile("src/test/resources/profiler/db.html"))
        assertThat(db.queries.size).isEqualTo(12)
        assertThat(db.queries[0].index).isEqualTo(1)
        assertThat(db.queries[0].sql)
            .isEqualTo(
                "SELECT t0.id AS id_1, t0.title AS title_2, t0.slug AS slug_3, t0.summary AS summary_4, t0.content AS content_5, t0.published_at AS published_at_6, t0.author_id AS author_id_7 FROM symfony_demo_post t0 WHERE t0.slug = ? LIMIT 1")
        assertThat(db.queries[0].runnableSql)
            .isEqualTo(
                "SELECT t0.id AS id_1, t0.title AS title_2, t0.slug AS slug_3, t0.summary AS summary_4, t0.content AS content_5, t0.published_at AS published_at_6, t0.author_id AS author_id_7 FROM symfony_demo_post t0 WHERE t0.slug = 'mauris-dapibus-risus-quis-suscipit-vulputate' LIMIT 1;")
        assertThat(db.queries[0].htmlSql)
            .isEqualTo(
                "<pre class=\"highlight highlight-sql\"><span class=\"keyword\">SELECT</span> <span class=\"word\">t0</span><span class=\"symbol\">.</span><span class=\"word\">id</span> <span class=\"keyword\">AS</span> <span class=\"word\">id_1</span><span class=\"symbol\">,</span> <span class=\"word\">t0</span><span class=\"symbol\">.</span><span class=\"word\">title</span> <span class=\"keyword\">AS</span> <span class=\"word\">title_2</span><span class=\"symbol\">,</span> <span class=\"word\">t0</span><span class=\"symbol\">.</span><span class=\"word\">slug</span> <span class=\"keyword\">AS</span> <span class=\"word\">slug_3</span><span class=\"symbol\">,</span> <span class=\"word\">t0</span><span class=\"symbol\">.</span><span class=\"word\">summary</span> <span class=\"keyword\">AS</span> <span class=\"word\">summary_4</span><span class=\"symbol\">,</span> <span class=\"word\">t0</span><span class=\"symbol\">.</span><span class=\"word\">content</span> <span class=\"keyword\">AS</span> <span class=\"word\">content_5</span><span class=\"symbol\">,</span> <span class=\"word\">t0</span><span class=\"symbol\">.</span><span class=\"word\">published_at</span> <span class=\"keyword\">AS</span> <span class=\"word\">published_at_6</span><span class=\"symbol\">,</span> <span class=\"word\">t0</span><span class=\"symbol\">.</span><span class=\"word\">author_id</span> <span class=\"keyword\">AS</span> <span class=\"word\">author_id_7</span> <span class=\"keyword\">FROM</span> <span class=\"word\">symfony_demo_post</span> <span class=\"word\">t0</span> <span class=\"keyword\">WHERE</span> <span class=\"word\">t0</span><span class=\"symbol\">.</span><span class=\"word\">slug</span> <span class=\"symbol\">=</span> <span class=\"word\">?</span> <span class=\"keyword\">LIMIT</span> <span class=\"number\">1</span></pre>")
        assertThat(db.queries[0].executionMS).isEqualTo(0.2)
        assertThat(db.queries[0].backtrace.size).isEqualTo(13)
    }

    fun testItParseMessengerProfilePageInDTOs() {
        val messenger =
            ProfilerParser()
                .loadProfilerPage(
                    MessengerData::class.java,
                    readFile("src/test/resources/profiler/messenger.html"))
        assertThat(messenger.stats.messageCount).isEqualTo(3)
        assertThat(
                messenger.dispatches[0]
                    .dispatch
                    ?.file
                    ?.contains(
                        "src/Infrastructure/Bus/Messenger/MessengerDomainEventDispatcher.php")
                    ?: false)
            .isTrue
        assertThat(
                messenger.dispatches[0]
                    .messageLocation
                    ?.file
                    ?.contains("src/UseCase/ArticleViewed/Event.php") ?: false)
            .isTrue
        assertThat(messenger.dispatches[0].dispatch?.line).isEqualTo(23)
        assertThat(messenger.dispatches[0].busName).isEqualTo("event.bus")
        assertThat(messenger.dispatches[0].messageName)
            .isEqualTo("App\\UseCase\\ArticleViewed\\Event")
        assertThat(
                messenger.dispatches[0]
                    .messageLocation
                    ?.file
                    ?.contains("src/UseCase/ArticleViewed/Event.php") ?: false)
            .isTrue
        assertThat(messenger.dispatches[0].messageLocation?.line).isEqualTo(7)
    }

    fun testItParseRequestProfilePageInDTOs() {
        val requestResponse =
            ProfilerParser()
                .loadProfilerPage(
                    RequestData::class.java, readFile("src/test/resources/profiler/request.html"))
        assertThat(requestResponse.controller)
            .isEqualTo("App\\Controller\\BlogController::postShow")
        assertThat(requestResponse.route).isEqualTo("blog_post")
    }
}
