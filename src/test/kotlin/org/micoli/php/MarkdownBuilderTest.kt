package org.micoli.php

import junit.framework.TestCase
import org.assertj.core.api.Assertions.*
import org.micoli.php.configuration.documentation.markdown.*

class MarkdownBuilderTest : TestCase() {

    fun testText() {
        val result = MarkdownBuilder().add(Text("Hello World")).build()
        assertThat(result).isEqualTo("Hello World")
    }

    fun testParagraph() {
        val result = MarkdownBuilder().add(Paragraph("This is a paragraph")).build()
        assertThat(result).isEqualTo("This is a paragraph")
    }

    fun testHeading() {
        val result = MarkdownBuilder().add(Heading(1, "Title")).build()
        assertThat(result).isEqualTo("# Title")
    }

    fun testHeadingLevel2() {
        val result = MarkdownBuilder().add(Heading(2, "Subtitle")).build()
        assertThat(result).isEqualTo("## Subtitle")
    }

    fun testHeadingMaxLevel() {
        val result = MarkdownBuilder().add(Heading(10, "Too many hashes")).build()
        assertThat(result).isEqualTo("###### Too many hashes")
    }

    fun testBold() {
        val result = MarkdownBuilder().add(Bold("bold text")).build()
        assertThat(result).isEqualTo("**bold text**")
    }

    fun testItalic() {
        val result = MarkdownBuilder().add(Italic("italic text")).build()
        assertThat(result).isEqualTo("*italic text*")
    }

    fun testBoldItalic() {
        val result = MarkdownBuilder().add(BoldItalic("bold and italic")).build()
        assertThat(result).isEqualTo("***bold and italic***")
    }

    fun testStrikethrough() {
        val result = MarkdownBuilder().add(Strikethrough("deleted text")).build()
        assertThat(result).isEqualTo("~~deleted text~~")
    }

    fun testInlineCode() {
        val result = MarkdownBuilder().add(InlineCode("var x = 5")).build()
        assertThat(result).isEqualTo("`var x = 5`")
    }

    fun testCodeBlock() {
        val result = MarkdownBuilder().add(CodeBlock("val x = 5\nval y = 10", "kotlin")).build()
        assertThat(result).isEqualTo("```kotlin\nval x = 5\nval y = 10\n```")
    }

    fun testCodeBlockWithoutLanguage() {
        val result = MarkdownBuilder().add(CodeBlock("some code")).build()
        assertThat(result).isEqualTo("```\nsome code\n```")
    }

    fun testLink() {
        val result = MarkdownBuilder().add(Link("Click here", "https://example.com")).build()
        assertThat(result).isEqualTo("[Click here](https://example.com)")
    }

    fun testLinkWithTitle() {
        val result =
            MarkdownBuilder().add(Link("Click here", "https://example.com", "Example Site")).build()
        assertThat(result).isEqualTo("[Click here](https://example.com \"Example Site\")")
    }

    fun testImage() {
        val result =
            MarkdownBuilder().add(Image("alt text", "https://example.com/image.jpg")).build()
        assertThat(result).isEqualTo("![alt text](https://example.com/image.jpg)")
    }

    fun testImageWithTitle() {
        val result =
            MarkdownBuilder()
                .add(Image("alt text", "https://example.com/image.jpg", "My Image"))
                .build()
        assertThat(result).isEqualTo("![alt text](https://example.com/image.jpg \"My Image\")")
    }

    fun testBulletList() {
        val result =
            MarkdownBuilder()
                .add {
                    bulletList {
                        item("Item 1")
                        item("Item 2")
                        item("Item 3")
                    }
                }
                .build()
        assertThat(result).isEqualTo("- Item 1\n- Item 2\n- Item 3")
    }

    fun testBulletListWithList() {
        val result = MarkdownBuilder().add(BulletList(listOf("Item 1", "Item 2"))).build()
        assertThat(result).isEqualTo("- Item 1\n- Item 2")
    }

    fun testOrderedList() {
        val result =
            MarkdownBuilder()
                .add(
                    OrderedList(
                        listOf(
                            "First",
                            "Second",
                            "Third",
                        )))
                .build()
        assertThat(result).isEqualTo("1. First\n2. Second\n3. Third")
    }

    fun testOrderedListWithListFluent() {
        val result =
            MarkdownBuilder()
                .add {
                    orderedList {
                        item("First")
                        item("Second")
                    }
                }
                .build()
        assertThat(result).isEqualTo("1. First\n2. Second")
    }

    fun testOrderedListWithListNonFluent() {
        val result = MarkdownBuilder().add(OrderedList(listOf("First", "Second"))).build()
        assertThat(result).isEqualTo("1. First\n2. Second")
    }

    fun testChecklist() {
        val result =
            MarkdownBuilder()
                .add {
                    checklist {
                        completed("Completed task")
                        pending("Pending task")
                    }
                }
                .build()

        assertThat(result).isEqualTo("- [x] Completed task\n- [ ] Pending task")
    }

    fun testChecklistWithList() {
        val result =
            MarkdownBuilder()
                .add(
                    Checklist(
                        listOf(Pair(true, "Task 1"), Pair(false, "Task 2"), Pair(true, "Task 3"))))
                .build()
        assertThat(result).isEqualTo("- [x] Task 1\n- [ ] Task 2\n- [x] Task 3")
    }

    fun testBlockquote() {
        val result = MarkdownBuilder().add(Blockquote("This is a quote")).build()
        assertThat(result).isEqualTo("> This is a quote")
    }

    fun testBlockquoteMultiline() {
        val result = MarkdownBuilder().add(Blockquote("Line 1\nLine 2\nLine 3")).build()
        assertThat(result).isEqualTo("> Line 1\n> Line 2\n> Line 3")
    }

    fun testHorizontalRule() {
        val result = MarkdownBuilder().add(HorizontalRule()).build()
        assertThat(result).isEqualTo("---")
    }

    fun testTable() {
        val headers = listOf("Name", "Age")
        val rows = listOf(listOf("Alice", "30"), listOf("Bob", "25"))
        val result = MarkdownBuilder().add(Table(headers, rows)).build()
        assertThat(result)
            .contains("| Name  | Age |")
            .contains("| ----- | --- |")
            .contains("| Alice | 30  |")
            .contains("| Bob   | 25  |")
    }

    fun testTableWithAlignment() {
        val headers = listOf("Left", "Center", "Right")
        val rows = listOf(listOf("L", "C", "R"))
        val alignment = listOf(TableAlignment.LEFT, TableAlignment.CENTER, TableAlignment.RIGHT)
        val result = MarkdownBuilder().add(Table(headers, rows, alignment)).build()
        assertThat(result).contains(":---").contains(":---:").contains("---:")
    }

    fun testFootnote() {
        val result = MarkdownBuilder().add(Footnote("See note", "ref1")).build()
        assertThat(result).isEqualTo("[See note][^ref1]")
    }

    fun testFootnoteReference() {
        val result = MarkdownBuilder().add(FootnoteReference("ref1")).build()
        assertThat(result).isEqualTo("[^ref1]: Reference content")
    }

    fun testSubscript() {
        val result = MarkdownBuilder().add(Subscript("subscript text")).build()
        assertThat(result).isEqualTo("~subscript text~")
    }

    fun testSuperscript() {
        val result = MarkdownBuilder().add(Superscript("superscript text")).build()
        assertThat(result).isEqualTo("^superscript text^")
    }

    fun testHighlight() {
        val result = MarkdownBuilder().add(Highlight("highlighted text")).build()
        assertThat(result).isEqualTo("==highlighted text==")
    }

    fun testLineBreak() {
        val result = MarkdownBuilder().add(LineBreak()).build()
        assertThat(result).isEqualTo("  ")
    }

    fun testTaskListItem() {
        val result = MarkdownBuilder().add(TaskListItem(true, "Done")).build()
        assertThat(result).isEqualTo("- [x] Done")
    }

    fun testTaskListItemIncomplete() {
        val result = MarkdownBuilder().add(TaskListItem(false, "Todo")).build()
        assertThat(result).isEqualTo("- [ ] Todo")
    }

    fun testDetails() {
        val result = MarkdownBuilder().add(Details("Summary", "Content details")).build()
        assertThat(result)
            .isEqualTo("<details>\n<summary>Summary</summary>\n\nContent details\n</details>")
    }

    fun testMultipleElementsFluent() {
        val result =
            MarkdownBuilder()
                .add {
                    heading(1, "Title")
                    paragraph("Introduction")
                    bold("Important")
                }
                .build()

        assertThat(result).isEqualTo("# Title\n\nIntroduction\n\n**Important**")
    }

    fun testMultipleElements() {
        val result =
            MarkdownBuilder()
                .add(listOf(Heading(1, "Title"), Paragraph("Introduction"), Bold("Important")))
                .build()

        assertThat(result).isEqualTo("# Title\n\nIntroduction\n\n**Important**")
    }

    fun testBuilderChaining() {
        val builder = MarkdownBuilder()
        val result =
            builder
                .add { heading(2, "Section") }
                .add {
                    bulletList {
                        item("Item 1")
                        item("Item 2")
                    }
                }
                .add { paragraph("Footer") }
                .build()

        assertThat(result).contains("## Section").contains("- Item 1").contains("Footer")
    }

    fun testEmptyBuilder() {
        val result = MarkdownBuilder().build()
        assertThat(result).isEqualTo("")
    }

    fun testNestedLists() {
        val innerList = BulletList(listOf("Nested 1", "Nested 2"))
        val result =
            MarkdownBuilder().add(BulletList(listOf("Item 1", innerList, "Item 2"))).build()
        assertThat(result).startsWith("- Item 1").contains("  - Nested 1")
    }

    fun testBulletListWithSubListFluent() {
        val result =
            MarkdownBuilder()
                .add {
                    bulletList {
                        item("Item 1")
                        subList {
                            item("Nested 1")
                            item("Nested 2")
                        }
                        item("Item 2")
                    }
                }
                .build()
        assertThat(result)
            .startsWith("- Item 1")
            .contains("  - Nested 1")
            .contains("  - Nested 2")
            .contains("- Item 2")
    }

    fun testBulletListWithSubList() {
        val result =
            MarkdownBuilder()
                .add(
                    BulletListBuilder()
                        .item("Item 1")
                        .addSubList(BulletListBuilder().item("First").item("Second"))
                        .build())
                .build()
        assertThat(result).startsWith("- Item 1").contains("  - First").contains("  - Second")
    }

    fun testBulletListWithOrderedSubList() {
        val result =
            MarkdownBuilder()
                .add {
                    bulletList {
                        item("Item 1")
                        orderedSubList {
                            item("First")
                            item("Second")
                        }
                    }
                }
                .build()
        assertThat(result).startsWith("- Item 1").contains("  1. First").contains("  2. Second")
    }

    fun testOrderedListWithBulletSubList() {
        val result =
            MarkdownBuilder()
                .add {
                    orderedList {
                        item("Item 1")
                        subList {
                            item("Nested bullet 1")
                            item("Nested bullet 2")
                        }
                    }
                }
                .build()
        assertThat(result)
            .startsWith("1. Item 1")
            .contains("  - Nested bullet 1")
            .contains("  - Nested bullet 2")
    }

    fun testOrderedListWithOrderedSubList() {
        val result =
            MarkdownBuilder()
                .add {
                    orderedList {
                        item("Item 1")
                        orderedSubList {
                            item("First")
                            item("Second")
                        }
                    }
                }
                .build()
        assertThat(result).startsWith("1. Item 1").contains("  1. First").contains("  2. Second")
    }

    fun testBulletListWithMarkdownElement() {
        val result =
            MarkdownBuilder()
                .add {
                    bulletList {
                        item(Bold("Bold item"))
                        item(Italic("Italic item"))
                    }
                }
                .build()
        assertThat(result).contains("- **Bold item**").contains("- *Italic item*")
    }

    fun testOrderedListWithMarkdownElement() {
        val result =
            MarkdownBuilder()
                .add {
                    orderedList {
                        item(Bold("Bold item"))
                        item(Italic("Italic item"))
                    }
                }
                .build()
        assertThat(result).contains("1. **Bold item**").contains("2. *Italic item*")
    }

    fun testBulletListWithInlineBuilder() {
        val result =
            MarkdownBuilder()
                .add {
                    bulletList {
                        item {
                            text("Normal ")
                            bold("bold ")
                            italic("italic")
                        }
                    }
                }
                .build()
        assertThat(result).contains("- Normal **bold ***italic*")
    }

    fun testOrderedListWithInlineBuilder() {
        val result =
            MarkdownBuilder()
                .add {
                    orderedList {
                        item {
                            text("Text with ")
                            code("code")
                        }
                    }
                }
                .build()
        assertThat(result).contains("1. Text with `code`")
    }

    fun testInlineBuilderWithLink() {
        val result =
            MarkdownBuilder()
                .add {
                    bulletList {
                        item {
                            text("Visit ")
                            link("example", "https://example.com")
                        }
                    }
                }
                .build()
        assertThat(result).contains("- Visit [example](https://example.com)")
    }

    fun testInlineBuilderWithMultipleElements() {
        val result =
            MarkdownBuilder()
                .add {
                    bulletList {
                        item {
                            text("This is ")
                            bold("bold")
                            text(" and ")
                            italic("italic")
                            text(" and ")
                            strikethrough("deleted")
                        }
                    }
                }
                .build()
        assertThat(result).isEqualTo("- This is **bold** and *italic* and ~~deleted~~")
    }

    fun testInlineBuilderWithSuperscriptAndSubscript() {
        val result =
            MarkdownBuilder()
                .add {
                    bulletList {
                        item {
                            text("H")
                            subscript("2")
                            text("O or x")
                            superscript("2")
                        }
                    }
                }
                .build()
        assertThat(result).isEqualTo("- H~2~O or x^2^")
    }

    fun testInlineBuilderWithHighlight() {
        val result =
            MarkdownBuilder()
                .add {
                    bulletList {
                        item {
                            text("This is ")
                            highlight("important")
                        }
                    }
                }
                .build()
        assertThat(result).isEqualTo("- This is ==important==")
    }

    fun testInlineBuilderSingleElement() {
        val result =
            MarkdownBuilder().add { bulletList { item { bold("Single element") } } }.build()
        assertThat(result).isEqualTo("- **Single element**")
    }

    fun testListAndSublist() {
        val result =
            "\n" +
                MarkdownBuilder()
                    .add {
                        bulletList {
                            item("Item 1")
                            subList {
                                item("Sub 1.1")
                                item("Sub 1.2")
                            }
                            item("Item 2")
                            subList {
                                item("Sub 2.1")
                                item("Sub 2.2")
                            }
                        }
                    }
                    .build() +
                "\n"

        assertThat(result)
            .contains("\n- Item 1")
            .contains("\n  - Sub 1.1\n")
            .contains("\n  - Sub 1.2\n")
            .contains("\n- Item 2")
            .contains("\n  - Sub 2.1\n")
            .contains("\n  - Sub 2.2\n")
    }

    fun testTableBuilderFluent() {
        val result =
            MarkdownBuilder()
                .add {
                    table {
                        headers("Column 1", "Column 2")
                        row("A", "B")
                        row("C", "D")
                    }
                }
                .build()
        assertThat(result)
            .contains("| Column 1 | Column 2 |")
            .contains("| A        | B        |")
            .contains("| C        | D        |")
    }

    fun testTableBuilderWithAlignment() {
        val result =
            MarkdownBuilder()
                .add {
                    table {
                        headers("Left", "Center", "Right")
                        alignment(TableAlignment.LEFT, TableAlignment.CENTER, TableAlignment.RIGHT)
                        row("L", "C", "R")
                    }
                }
                .build()
        assertThat(result).contains(":---").contains(":---:").contains("---:")
    }

    fun testFluentBuilderText() {
        val result = MarkdownBuilder().add { text("Hello") }.build()
        assertThat(result).isEqualTo("Hello")
    }

    fun testFluentBuilderParagraph() {
        val result = MarkdownBuilder().add { paragraph("Paragraph") }.build()
        assertThat(result).isEqualTo("Paragraph")
    }

    fun testFluentBuilderHeading() {
        val result = MarkdownBuilder().add { heading(3, "Title") }.build()
        assertThat(result).isEqualTo("### Title")
    }

    fun testFluentBuilderBold() {
        val result = MarkdownBuilder().add { bold("Bold") }.build()
        assertThat(result).isEqualTo("**Bold**")
    }

    fun testFluentBuilderItalic() {
        val result = MarkdownBuilder().add { italic("Italic") }.build()
        assertThat(result).isEqualTo("*Italic*")
    }

    fun testFluentBuilderBoldItalic() {
        val result = MarkdownBuilder().add { boldItalic("BoldItalic") }.build()
        assertThat(result).isEqualTo("***BoldItalic***")
    }

    fun testFluentBuilderStrikethrough() {
        val result = MarkdownBuilder().add { strikethrough("Strikethrough") }.build()
        assertThat(result).isEqualTo("~~Strikethrough~~")
    }

    fun testFluentBuilderCode() {
        val result = MarkdownBuilder().add { code("code") }.build()
        assertThat(result).isEqualTo("`code`")
    }

    fun testFluentBuilderCodeBlock() {
        val result = MarkdownBuilder().add { codeBlock("code", "kotlin") }.build()
        assertThat(result).isEqualTo("```kotlin\ncode\n```")
    }

    fun testFluentBuilderLink() {
        val result = MarkdownBuilder().add { link("text", "url") }.build()
        assertThat(result).isEqualTo("[text](url)")
    }

    fun testFluentBuilderLinkWithTitle() {
        val result = MarkdownBuilder().add { link("text", "url", "title") }.build()
        assertThat(result).isEqualTo("[text](url \"title\")")
    }

    fun testFluentBuilderImage() {
        val result = MarkdownBuilder().add { image("alt", "url") }.build()
        assertThat(result).isEqualTo("![alt](url)")
    }

    fun testFluentBuilderImageWithTitle() {
        val result = MarkdownBuilder().add { image("alt", "url", "title") }.build()
        assertThat(result).isEqualTo("![alt](url \"title\")")
    }

    fun testFluentBuilderBlockquote() {
        val result = MarkdownBuilder().add { blockquote("Quote") }.build()
        assertThat(result).isEqualTo("> Quote")
    }

    fun testFluentBuilderHorizontalRule() {
        val result = MarkdownBuilder().add { horizontalRule() }.build()
        assertThat(result).isEqualTo("---")
    }

    fun testFluentBuilderFootnote() {
        val result = MarkdownBuilder().add { footnote("text", "ref") }.build()
        assertThat(result).isEqualTo("[text][^ref]")
    }

    fun testFluentBuilderFootnoteReference() {
        val result = MarkdownBuilder().add { footnoteReference("ref") }.build()
        assertThat(result).isEqualTo("[^ref]: Reference content")
    }

    fun testFluentBuilderSubscript() {
        val result = MarkdownBuilder().add { subscript("sub") }.build()
        assertThat(result).isEqualTo("~sub~")
    }

    fun testFluentBuilderSuperscript() {
        val result = MarkdownBuilder().add { superscript("sup") }.build()
        assertThat(result).isEqualTo("^sup^")
    }

    fun testFluentBuilderHighlight() {
        val result = MarkdownBuilder().add { highlight("highlight") }.build()
        assertThat(result).isEqualTo("==highlight==")
    }

    fun testFluentBuilderLineBreak() {
        val result = MarkdownBuilder().add { lineBreak() }.build()
        assertThat(result).isEqualTo("  ")
    }

    fun testFluentBuilderTaskListItem() {
        val result = MarkdownBuilder().add { taskListItem(true, "Done") }.build()
        assertThat(result).isEqualTo("- [x] Done")
    }

    fun testFluentBuilderDetails() {
        val result = MarkdownBuilder().add { details("Summary", "Content") }.build()
        assertThat(result).isEqualTo("<details>\n<summary>Summary</summary>\n\nContent\n</details>")
    }

    fun testHeadingMinLevel() {
        val result = MarkdownBuilder().add(Heading(0, "Title")).build()
        assertThat(result).isEqualTo("# Title")
    }

    fun testTableEmptyRows() {
        val result = MarkdownBuilder().add(Table(listOf("Header"), emptyList())).build()
        assertThat(result).contains("| Header |")
    }

    fun testComplexDocumentWithMultipleLevelsAndInlineBlocks() {
        val result =
            MarkdownBuilder()
                .add {
                    heading(1, "Complete Documentation")
                    paragraph(
                        "This is a comprehensive example demonstrating the MarkdownBuilder capabilities.")

                    heading(2, "Features Overview")
                    paragraph("The following sections describe the main features:")

                    heading(3, "Text Formatting")
                    bulletList {
                        item {
                            bold("Bold text")
                            text(" can be used for ")
                            italic("emphasis")
                        }
                        item {
                            text("Combine ")
                            boldItalic("bold and italic")
                            text(" for stronger emphasis")
                        }
                        item {
                            strikethrough("Deleted content")
                            text(" shows what was removed")
                        }
                        item {
                            text("Inline ")
                            code("code snippets")
                            text(" for technical terms")
                        }
                        item {
                            text("Use ")
                            highlight("highlighting")
                            text(" for important notes")
                        }
                    }

                    heading(3, "Scientific Notation")
                    paragraph("Mathematical formulas can use subscripts and superscripts:")
                    bulletList {
                        item {
                            text("Water molecule: H")
                            subscript("2")
                            text("O")
                        }
                        item {
                            text("Einstein's equation: E = mc")
                            superscript("2")
                        }
                        item {
                            text("Complex: x")
                            superscript("2")
                            text(" + y")
                            subscript("1")
                        }
                    }

                    heading(2, "Lists and Nesting")

                    heading(3, "Ordered Lists with Nested Content")
                    orderedList {
                        item {
                            text("First item with ")
                            bold("bold text")
                        }
                        item {
                            text("Second item with ")
                            link("a link", "https://example.com")
                        }
                        orderedSubList {
                            item {
                                text("Nested ordered item with ")
                                italic("italic")
                            }
                            item {
                                text("Another nested with ")
                                code("code")
                            }
                        }
                        item {
                            text("Third item with ")
                            strikethrough("crossed out text")
                        }
                        subList {
                            item {
                                text("Mixed nested bullet with ")
                                boldItalic("bold italic")
                            }
                            item {
                                text("Check the ")
                                link("documentation", "https://docs.example.com", "Official Docs")
                            }
                        }
                    }

                    heading(3, "Bullet Lists with Deep Nesting")
                    bulletList {
                        item {
                            bold("Main category")
                            text(": Description here")
                        }
                        subList {
                            item { italic("Subcategory 1") }
                            subList {
                                item {
                                    text("Deep nested item with ")
                                    code("technical.details")
                                }
                                item { text("Another deep item") }
                            }
                            item { italic("Subcategory 2") }
                        }
                        item { text("Another main item") }
                        orderedSubList {
                            item { text("Ordered nested under bullet #1") }
                            item { text("Ordered nested under bullet #2") }
                        }
                    }

                    heading(3, "Task Lists")
                    checklist {
                        completed("Initial setup with dependencies")
                        completed("Configure build system")
                        pending("Write unit tests")
                        pending("Deploy to production")
                    }

                    heading(2, "Code Examples")
                    paragraph("Here's a Kotlin code example:")
                    codeBlock(
                        """
                        fun greet(name: String): String {
                            return "Hello, ${'$'}name!"
                        }

                        val message = greet("World")
                        println(message)
                        """
                            .trimIndent(),
                        "kotlin")

                    heading(2, "Tables with Complex Content")
                    table {
                        headers("Feature", "Status", "Priority")
                        alignment(TableAlignment.LEFT, TableAlignment.CENTER, TableAlignment.RIGHT)
                        row("Authentication", "Done", "High")
                        row("Database", "In Progress", "High")
                        row("UI Polish", "Pending", "Low")
                    }

                    heading(2, "Additional Elements")

                    heading(3, "Blockquotes")
                    blockquote(
                        """
                        This is a multi-line blockquote.
                        It can span several lines.
                        Each line will be properly prefixed.
                        """
                            .trimIndent())

                    heading(3, "Horizontal Rule")
                    paragraph("A separator below:")
                    horizontalRule()

                    heading(3, "Details Section")
                    details(
                        "Click to expand advanced configuration",
                        """
                        Advanced settings:
                        - Setting A: value1
                        - Setting B: value2
                        - Setting C: value3
                        """
                            .trimIndent())

                    heading(2, "References and Footnotes")
                    paragraph("This document includes footnotes for additional information.")
                    footnote("reference 1", "note1")
                    footnote("reference 2", "note2")
                    bulletList {
                        item("See footnote for more details")
                        item("Check additional references for examples")
                    }
                    footnoteReference("note1")
                    footnoteReference("note2")
                }
                .build()

        // Verify headings
        assertThat(result)
            .contains("# Complete Documentation")
            .contains("## Features Overview")
            .contains("### Text Formatting")
            .contains("### Scientific Notation")
            .contains("## Lists and Nesting")
            .contains("### Ordered Lists with Nested Content")
            .contains("### Bullet Lists with Deep Nesting")
            .contains("### Task Lists")
            .contains("## Code Examples")
            .contains("## Tables with Complex Content")
            .contains("## Additional Elements")
            .contains("### Blockquotes")
            .contains("### Horizontal Rule")
            .contains("### Details Section")
            .contains("## References and Footnotes")

        // Verify inline formatting
        assertThat(result)
            .contains("**Bold text**")
            .contains("*emphasis*")
            .contains("***bold and italic***")
            .contains("~~Deleted content~~")
            .contains("`code snippets`")
            .contains("==highlighting==")

        // Verify scientific notation
        assertThat(result).contains("H~2~O").contains("mc^2^").contains("x^2^").contains("y~1~")

        // Verify lists
        assertThat(result)
            .contains("1. First item with **bold text**")
            .contains("2. Second item with [a link](https://example.com)")
        // Note: Nested lists appear as separate lines between parent items
        assertThat(result)
            .contains("Nested ordered item with *italic*")
            .contains("Another nested with `code`")
            .contains("crossed out text")
            .contains("Mixed nested bullet with ***bold italic***")
            .contains("[documentation](https://docs.example.com \"Official Docs\")")

        // Verify bullet list nesting
        assertThat(result)
            .contains("- **Main category**: Description here")
            .contains("  - *Subcategory 1*")
            .contains("    - Deep nested item with `technical.details`")
            .contains("Ordered nested under bullet #1")

        // Verify checklist
        assertThat(result)
            .contains("- [x] Initial setup with dependencies")
            .contains("- [x] Configure build system")
            .contains("- [ ] Write unit tests")
            .contains("- [ ] Deploy to production")

        // Verify code block
        assertThat(result).contains("```kotlin").contains("fun greet(name: String): String {")

        // Verify table
        assertThat(result)
            .contains("| Feature        | Status      | Priority |")
            .contains("| :------------- | :---:------ | ---:---- |")
            .contains("| Authentication | Done        | High     |")

        // Verify blockquote
        assertThat(result)
            .contains("> This is a multi-line blockquote.")
            .contains("> It can span several lines.")

        // Verify horizontal rule
        assertThat(result).contains("---")

        // Verify details
        assertThat(result)
            .contains("<details>")
            .contains("<summary>Click to expand advanced configuration</summary>")

        // Verify footnotes
        assertThat(result)
            .contains("[reference 1][^note1]")
            .contains("[reference 2][^note2]")
            .contains("- See footnote for more details")
            .contains("- Check additional references for examples")
            .contains("[^note1]: Reference content")
            .contains("[^note2]: Reference content")
    }
}
