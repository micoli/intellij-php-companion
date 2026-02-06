package org.micoli.php

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.jetbrains.php.lang.PhpFileType
import com.jetbrains.php.lang.psi.elements.Method
import org.assertj.core.api.Assertions.assertThat
import org.micoli.php.symfony.messenger.annotator.AddMissingThrowsQuickFix

class AddMissingThrowsQuickFixTest : BasePlatformTestCase() {

    private fun applyQuickFixAndGetResult(
        phpContent: String,
        missingThrows: Set<String>,
        methodName: String = "bar",
    ): String {
        val psiFile = myFixture.configureByText(PhpFileType.INSTANCE, phpContent)
        val method =
            PsiTreeUtil.findChildrenOfType(psiFile, Method::class.java).first {
                it.name == methodName
            }

        val quickFix = AddMissingThrowsQuickFix(method, missingThrows)

        WriteCommandAction.runWriteCommandAction(project) {
            quickFix.invoke(project, myFixture.editor, psiFile)
        }

        PsiDocumentManager.getInstance(project).commitAllDocuments()
        return myFixture.editor.document.text
    }

    fun testAddThrowsToExistingDoc_4SpaceIndent() {
        val input =
            """
            |<?php
            |class Foo {
            |    /**
            |     * @throws \RuntimeException
            |     */
            |    public function bar(): void {}
            |}
            """
                .trimMargin()

        val expected =
            """
            |<?php
            |class Foo {
            |    /**
            |     * @throws \RuntimeException
            |     * @throws \App\Exception\NotFoundException
            |     */
            |    public function bar(): void {}
            |}
            """
                .trimMargin()

        val result = applyQuickFixAndGetResult(input, setOf("\\App\\Exception\\NotFoundException"))

        assertThat(result).isEqualTo(expected)
    }

    fun testAddThrowsToExistingDoc_8SpaceIndent() {
        val input =
            """
            |<?php
            |class Foo {
            |        /**
            |         * @throws \RecipientNotFoundException
            |         */
            |        private function bar(): void {}
            |}
            """
                .trimMargin()

        val expected =
            """
            |<?php
            |class Foo {
            |        /**
            |         * @throws \RecipientNotFoundException
            |         * @throws \Domain\SignatureRequestNotFoundException
            |         */
            |        private function bar(): void {}
            |}
            """
                .trimMargin()

        val result =
            applyQuickFixAndGetResult(
                input,
                setOf("\\Domain\\SignatureRequestNotFoundException"),
            )

        assertThat(result).isEqualTo(expected)
    }

    fun testAddThrowsToExistingDoc_MultipleThrows() {
        val input =
            """
            |<?php
            |class Foo {
            |    /**
            |     * @throws \RuntimeException
            |     */
            |    public function bar(): void {}
            |}
            """
                .trimMargin()

        val expected =
            """
            |<?php
            |class Foo {
            |    /**
            |     * @throws \RuntimeException
            |     * @throws \App\Exception\NotFoundException
            |     * @throws \App\Exception\PermissionDenied
            |     */
            |    public function bar(): void {}
            |}
            """
                .trimMargin()

        val result =
            applyQuickFixAndGetResult(
                input,
                setOf(
                    "\\App\\Exception\\PermissionDenied",
                    "\\App\\Exception\\NotFoundException",
                ),
            )

        assertThat(result).isEqualTo(expected)
    }

    fun testCreateNewDocComment_NoExistingDoc() {
        val input =
            """
            |<?php
            |class Foo {
            |    public function bar(): void {}
            |}
            """
                .trimMargin()

        val expected =
            """
            |<?php
            |class Foo {
            |    /**
            |     * @throws \App\Exception\NotFoundException
            |     */
            |    public function bar(): void {}
            |}
            """
                .trimMargin()

        val result = applyQuickFixAndGetResult(input, setOf("\\App\\Exception\\NotFoundException"))

        assertThat(result).isEqualTo(expected)
    }

    fun testAddThrowsToDocWithParamAndReturn() {
        val input =
            """
            |<?php
            |class Foo {
            |    /**
            |     * @param int ${'$'}id
            |     * @return string
            |     * @throws \RuntimeException
            |     */
            |    public function bar(int ${'$'}id): string { return ''; }
            |}
            """
                .trimMargin()

        val expected =
            """
            |<?php
            |class Foo {
            |    /**
            |     * @param int ${'$'}id
            |     * @return string
            |     * @throws \RuntimeException
            |     * @throws \App\Exception\NotFoundException
            |     */
            |    public function bar(int ${'$'}id): string { return ''; }
            |}
            """
                .trimMargin()

        val result = applyQuickFixAndGetResult(input, setOf("\\App\\Exception\\NotFoundException"))

        assertThat(result).isEqualTo(expected)
    }

    fun testAddThrowsToEmptyDocComment() {
        val input =
            """
            |<?php
            |class Foo {
            |    /**
            |     */
            |    public function bar(): void {}
            |}
            """
                .trimMargin()

        val expected =
            """
            |<?php
            |class Foo {
            |    /**
            |     * @throws \App\Exception\NotFoundException
            |     */
            |    public function bar(): void {}
            |}
            """
                .trimMargin()

        val result = applyQuickFixAndGetResult(input, setOf("\\App\\Exception\\NotFoundException"))

        assertThat(result).isEqualTo(expected)
    }

    fun testCreateNewDocComment_8SpaceIndent() {
        val input =
            """
            |<?php
            |class Foo {
            |        public function bar(): void {}
            |}
            """
                .trimMargin()

        val expected =
            """
            |<?php
            |class Foo {
            |        /**
            |         * @throws \App\Exception\NotFoundException
            |         */
            |        public function bar(): void {}
            |}
            """
                .trimMargin()

        val result = applyQuickFixAndGetResult(input, setOf("\\App\\Exception\\NotFoundException"))

        assertThat(result).isEqualTo(expected)
    }

    fun testAddThrowsToExistingDoc_TabIndent() {
        val input =
            "<?php\nclass Foo {\n\t/**\n\t * @throws \\RuntimeException\n\t */\n\tpublic function bar(): void {}\n}"

        val expected =
            "<?php\nclass Foo {\n\t/**\n\t * @throws \\RuntimeException\n\t * @throws \\App\\Exception\\NotFoundException\n\t */\n\tpublic function bar(): void {}\n}"

        val result = applyQuickFixAndGetResult(input, setOf("\\App\\Exception\\NotFoundException"))

        assertThat(result).isEqualTo(expected)
    }
}
