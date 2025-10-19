package org.micoli.php.configuration.documentation.markdown

sealed class MarkdownElement {
    abstract fun render(): String

    override fun toString() = render()
}

class MarkdownElementBuilder {
    private val elements = mutableListOf<MarkdownElement>()

    fun text(content: String) = apply { elements.add(Text(content)) }

    fun paragraph(content: String) = apply { elements.add(Paragraph(content)) }

    fun heading(level: Int, content: String) = apply { elements.add(Heading(level, content)) }

    fun bold(content: String) = apply { elements.add(Bold(content)) }

    fun italic(content: String) = apply { elements.add(Italic(content)) }

    fun boldItalic(content: String) = apply { elements.add(BoldItalic(content)) }

    fun strikethrough(content: String) = apply { elements.add(Strikethrough(content)) }

    fun code(content: String) = apply { elements.add(InlineCode(content)) }

    fun codeBlock(content: String, language: String = "") = apply {
        elements.add(CodeBlock(content, language))
    }

    fun link(text: String, url: String, title: String? = null) = apply {
        elements.add(Link(text, url, title))
    }

    fun image(alt: String, url: String, title: String? = null) = apply {
        elements.add(Image(alt, url, title))
    }

    fun bulletList(builder: BulletListBuilder.() -> Unit) = apply {
        elements.add(BulletListBuilder().apply(builder).build())
    }

    fun orderedList(builder: OrderedListBuilder.() -> Unit) = apply {
        elements.add(OrderedListBuilder().apply(builder).build())
    }

    fun checklist(builder: ChecklistBuilder.() -> Unit) = apply {
        elements.add(ChecklistBuilder().apply(builder).build())
    }

    fun blockquote(content: String) = apply { elements.add(Blockquote(content)) }

    fun horizontalRule() = apply { elements.add(HorizontalRule()) }

    fun table(builder: TableBuilder.() -> Unit) = apply {
        elements.add(TableBuilder().apply(builder).build())
    }

    fun footnote(text: String, reference: String) = apply {
        elements.add(Footnote(text, reference))
    }

    fun footnoteReference(reference: String) = apply { elements.add(FootnoteReference(reference)) }

    fun subscript(content: String) = apply { elements.add(Subscript(content)) }

    fun superscript(content: String) = apply { elements.add(Superscript(content)) }

    fun highlight(content: String) = apply { elements.add(Highlight(content)) }

    fun lineBreak() = apply { elements.add(LineBreak()) }

    fun taskListItem(completed: Boolean, content: String) = apply {
        elements.add(TaskListItem(completed, content))
    }

    fun details(summary: String, content: String) = apply {
        elements.add(Details(summary, content))
    }

    fun build() = elements
}

class MarkdownBuilder {

    private val elements = mutableListOf<MarkdownElement>()

    fun add(element: MarkdownElement) = apply { elements.add(element) }

    fun add(elements: List<MarkdownElement>) = apply { this.elements.addAll(elements) }

    fun add(builder: MarkdownElementBuilder.() -> Unit) = apply {
        val builtElements = MarkdownElementBuilder().apply(builder).build()
        this.elements.addAll(builtElements)
        return this
    }

    fun build() = elements.joinToString("\n\n") { it.render() }
}

class BulletListBuilder {
    val items = mutableListOf<Any>()

    fun item(content: String) = apply { items.add(content) }

    fun items(content: List<Any>) = apply { items.addAll(content) }

    fun item(element: MarkdownElement) = apply { items.add(element) }

    fun item(builder: InlineBuilder.() -> Unit) = apply {
        items.add(InlineBuilder().apply(builder).build())
    }

    fun subList(builder: BulletListBuilder.() -> Unit) = apply {
        items.add(BulletListBuilder().apply(builder).build())
    }

    fun addSubList(builder: BulletListBuilder) = apply { items.add(builder.build()) }

    fun orderedSubList(builder: OrderedListBuilder.() -> Unit) = apply {
        items.add(OrderedListBuilder().apply(builder).build())
    }

    fun addOrderedSubList(builder: OrderedListBuilder) = apply { items.add(builder.build()) }

    fun build() = BulletList(items.toList())
}

class OrderedListBuilder {
    private val items = mutableListOf<Any>()

    fun item(content: String) = apply { items.add(content) }

    fun item(element: MarkdownElement) = apply { items.add(element) }

    fun item(builder: InlineBuilder.() -> Unit) = apply {
        items.add(InlineBuilder().apply(builder).build())
    }

    fun subList(builder: BulletListBuilder.() -> Unit) = apply {
        items.add(BulletListBuilder().apply(builder).build())
    }

    fun orderedSubList(builder: OrderedListBuilder.() -> Unit) = apply {
        items.add(OrderedListBuilder().apply(builder).build())
    }

    fun addOrderedSubList(builder: OrderedListBuilder) = apply { items.add(builder.build()) }

    fun addSubList(builder: BulletListBuilder) = apply { items.add(builder.build()) }

    fun build() = OrderedList(items.toList())
}

class ChecklistBuilder {
    private val items = mutableListOf<Pair<Boolean, String>>()

    fun completed(content: String) = apply { items.add(true to content) }

    fun pending(content: String) = apply { items.add(false to content) }

    fun build() = Checklist(items.toList())
}

class TableBuilder {
    private var headers = listOf<String>()
    private val rows = mutableListOf<List<String>>()
    private var alignment = listOf<TableAlignment>()

    fun headers(vararg headers: String) = apply { this.headers = headers.toList() }

    fun row(vararg cells: String) = apply { rows.add(cells.toList()) }

    fun rows(rows: List<List<String>>) = apply { this.rows.addAll(rows) }

    fun alignment(vararg align: TableAlignment) = apply { this.alignment = align.toList() }

    fun build() = Table(headers, rows.toList(), alignment)
}

class InlineBuilder {
    private val elements = mutableListOf<MarkdownElement>()

    fun text(content: String) = apply { elements.add(Text(content)) }

    fun bold(content: String) = apply { elements.add(Bold(content)) }

    fun italic(content: String) = apply { elements.add(Italic(content)) }

    fun boldItalic(content: String) = apply { elements.add(BoldItalic(content)) }

    fun strikethrough(content: String) = apply { elements.add(Strikethrough(content)) }

    fun code(content: String) = apply { elements.add(InlineCode(content)) }

    fun link(text: String, url: String, title: String? = null) = apply {
        elements.add(Link(text, url, title))
    }

    fun superscript(content: String) = apply { elements.add(Superscript(content)) }

    fun subscript(content: String) = apply { elements.add(Subscript(content)) }

    fun highlight(content: String) = apply { elements.add(Highlight(content)) }

    fun build(): MarkdownElement =
        if (elements.size == 1) elements.first() else InlineGroup(elements.toList())
}

class InlineGroup(val elements: List<MarkdownElement>) : MarkdownElement() {
    override fun render() = elements.joinToString("") { it.render() }
}

class Text(val content: String) : MarkdownElement() {
    override fun render() = content
}

class Paragraph(val content: String) : MarkdownElement() {
    override fun render() = content
}

class Heading(val level: Int, val content: String) : MarkdownElement() {
    override fun render() = "${"#".repeat(level.coerceIn(1, 6))} $content"
}

class Bold(val content: String) : MarkdownElement() {
    override fun render() = "**$content**"
}

class Italic(val content: String) : MarkdownElement() {
    override fun render() = "*$content*"
}

class BoldItalic(val content: String) : MarkdownElement() {
    override fun render() = "***$content***"
}

class Strikethrough(val content: String) : MarkdownElement() {
    override fun render() = "~~$content~~"
}

class InlineCode(val content: String) : MarkdownElement() {
    override fun render() = "`$content`"
}

class CodeBlock(val content: String, val language: String = "") : MarkdownElement() {
    override fun render() = "```$language\n$content\n```"
}

class Link(val text: String, val url: String, val title: String? = null) : MarkdownElement() {
    override fun render() = if (title != null) "[$text]($url \"$title\")" else "[$text]($url)"
}

class Image(val alt: String, val url: String, val title: String? = null) : MarkdownElement() {
    override fun render() = if (title != null) "![$alt]($url \"$title\")" else "![$alt]($url)"
}

class BulletList(val items: List<Any>) : MarkdownElement() {
    override fun render(): String = renderWithIndent(0)

    fun renderWithIndent(indent: Int): String {
        val prefix = " ".repeat(indent)
        return items
            .flatMap { item ->
                when (item) {
                    is BulletList -> item.renderWithIndent(indent + 2).split("\n")
                    is OrderedList -> item.renderWithIndent(indent + 2).split("\n")
                    is MarkdownElement -> {
                        val rendered = item.render()
                        if (rendered.contains("\n")) {
                            val lines = rendered.split("\n")
                            listOf("$prefix- ${lines[0]}") + lines.drop(1).map { "$prefix  $it" }
                        } else {
                            listOf("$prefix- $rendered")
                        }
                    }
                    else -> listOf("$prefix- $item")
                }
            }
            .joinToString("\n")
    }
}

class OrderedList(val items: List<Any>) : MarkdownElement() {
    override fun render(): String = renderWithIndent(0)

    fun renderWithIndent(indent: Int): String {
        val prefix = " ".repeat(indent)
        return items
            .flatMapIndexed { i, item ->
                when (item) {
                    is BulletList -> item.renderWithIndent(indent + 2).split("\n")
                    is OrderedList -> item.renderWithIndent(indent + 2).split("\n")
                    is MarkdownElement -> {
                        val rendered = item.render()
                        if (rendered.contains("\n")) {
                            val lines = rendered.split("\n")
                            listOf("$prefix${i + 1}. ${lines[0]}") +
                                lines.drop(1).map { "$prefix   $it" }
                        } else {
                            listOf("$prefix${i + 1}. $rendered")
                        }
                    }
                    else -> listOf("$prefix${i + 1}. $item")
                }
            }
            .joinToString("\n")
    }
}

class Checklist(val items: List<Pair<Boolean, String>>) : MarkdownElement() {
    override fun render() =
        items.joinToString("\n") { (completed, item) ->
            if (completed) "- [x] $item" else "- [ ] $item"
        }
}

class Blockquote(val content: String) : MarkdownElement() {
    override fun render() = content.lines().joinToString("\n") { "> $it" }
}

class HorizontalRule : MarkdownElement() {
    override fun render() = "---"
}

enum class TableAlignment(val symbol: String) {
    LEFT(":---"),
    CENTER(":---:"),
    RIGHT("---:")
}

class Table(
    val headers: List<String>,
    val rows: List<List<String>>,
    val alignment: List<TableAlignment> = emptyList()
) : MarkdownElement() {
    override fun render(): String {
        val columnWidths =
            headers.indices.map { colIndex ->
                val headerWidth = headers.getOrNull(colIndex)?.length ?: 0
                val maxRowWidth = rows.maxOfOrNull { it.getOrNull(colIndex)?.length ?: 0 } ?: 0
                maxOf(headerWidth, maxRowWidth)
            }

        val headerRow =
            headers.mapIndexed { i, h -> h.padEnd(columnWidths[i]) }.joinToString(" | ", "| ", " |")
        val separatorRow =
            columnWidths
                .mapIndexed { i, width ->
                    val symbol = alignment.getOrNull(i)?.symbol ?: "---"
                    symbol.padEnd(width, '-')
                }
                .joinToString(" | ", "| ", " |")
        val dataRows =
            rows.joinToString("\n") { row ->
                row.mapIndexed { i, cell -> cell.padEnd(columnWidths[i]) }
                    .joinToString(" | ", "| ", " |")
            }
        return "$headerRow\n$separatorRow\n$dataRows"
    }
}

class Footnote(val text: String, val reference: String) : MarkdownElement() {
    override fun render() = "[$text][^$reference]"
}

class FootnoteReference(val reference: String) : MarkdownElement() {
    override fun render() = "[^$reference]: Reference content"
}

class Subscript(val content: String) : MarkdownElement() {
    override fun render() = "~$content~"
}

class Superscript(val content: String) : MarkdownElement() {
    override fun render() = "^$content^"
}

class Highlight(val content: String) : MarkdownElement() {
    override fun render() = "==$content=="
}

class LineBreak : MarkdownElement() {
    override fun render() = "  "
}

class TaskListItem(val completed: Boolean, val content: String) : MarkdownElement() {
    override fun render() = if (completed) "- [x] $content" else "- [ ] $content"
}

class Details(val summary: String, val content: String) : MarkdownElement() {
    override fun render() = "<details>\n<summary>$summary</summary>\n\n$content\n</details>"
}
