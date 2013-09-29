package com.martinkurz.confluence2wiki.doxia.markdown;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.Stack;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.maven.doxia.sink.AbstractTextSink;
import org.apache.maven.doxia.sink.SinkEventAttributes;
import org.codehaus.plexus.util.StringUtils;

public class MarkdownSink extends AbstractTextSink {
    private static final String LIST_INDENT = "    ";

    /** markup in table as html **/
    private boolean tableFlag;
    /** itemFlag. */
    private boolean itemFlag;
    private String href;
    private int indentLevel;
    /** The writer to use. */
    private final PrintWriter writer;
    /** listStyles. */
    private final Stack<String> listStyles;
    /** buffer stack **/
    private final Stack<StringBuilder> buffers;

    /**
     * Constructor, initialize the Writer and the variables.
     * 
     * @param writer not null writer to write the result. <b>Should</b> be an UTF-8 Writer.
     *            You could use <code>newWriter</code> methods from {@link org.codehaus.plexus.util.WriterFactory}.
     */
    protected MarkdownSink(Writer writer) {
        this.writer = new PrintWriter(writer);
        listStyles = new Stack<String>();
        buffers = new Stack<StringBuilder>();
        init();
    }

    /** {@inheritDoc} */
    protected void init() {
        super.init();
        buffers.clear();
        buffers.add(new StringBuilder());
        listStyles.clear();
        itemFlag = false;
        tableFlag = false;
        indentLevel = 0;
        href = null;
    }

    /** {@inheritDoc} */
    public void head() {
        pushElementBuffer();
    }

    /** {@inheritDoc} */
    public void head_() {
        getElementContent();
    }

    /** {@inheritDoc} */
    public void body() {
        pushElementBuffer();
    }

    /** {@inheritDoc} */
    public void body_() {
        write(getElementContent().trim());
    }

    private void startSectionTitle(final int level) {
        pushElementBuffer();
    }

    private void endSectionTitel(final int level) {
        final String content = getElementContent();
        if (!StringUtils.isBlank(content)) {
            if (tableFlag) {
                rawText("<h" + level + ">" + content + "</h" + level + ">");
            } else {
                rawText(EOL + StringUtils.repeat("#", level) + SPACE + content + EOL + EOL);
            }
        }
    }

    /** {@inheritDoc} */
    public void sectionTitle1() {
        startSectionTitle(1);
    }

    /** {@inheritDoc} */
    public void sectionTitle1_() {
        endSectionTitel(1);
    }

    /** {@inheritDoc} */
    public void sectionTitle2() {
        startSectionTitle(2);
    }

    /** {@inheritDoc} */
    public void sectionTitle2_() {
        endSectionTitel(2);
    }

    /** {@inheritDoc} */
    public void sectionTitle3() {
        startSectionTitle(3);
    }

    /** {@inheritDoc} */
    public void sectionTitle3_() {
        endSectionTitel(3);
    }

    /** {@inheritDoc} */
    public void sectionTitle4() {
        startSectionTitle(4);
    }

    /** {@inheritDoc} */
    public void sectionTitle4_() {
        endSectionTitel(4);
    }

    /** {@inheritDoc} */
    public void sectionTitle5() {
        startSectionTitle(5);
    }

    /** {@inheritDoc} */
    public void sectionTitle5_() {
        endSectionTitel(5);
    }

    /** {@inheritDoc} */
    public void paragraph() {
        pushElementBuffer();
    }

    /** {@inheritDoc} */
    public void paragraph_() {
        final String content = getElementContent();
        if (StringUtils.isNotBlank(content)) {
            if (tableFlag) {
                rawText("<p>" + content + "</p>");
            } else if (itemFlag) {
                rawText(EOL + EOL + StringUtils.repeat(LIST_INDENT, indentLevel) + content + EOL + EOL);
            } else {
                rawText(EOL + content + EOL + EOL);
            }
        }
    }

    /** {@inheritDoc} */
    public void list() {
        startList("*");
    }

    /** {@inheritDoc} */
    public void list_() {
        endList();
    }

    /** {@inheritDoc} */
    public void numberedList(int numbering) {
        startList("1.");
    }

    /** {@inheritDoc} */
    public void numberedList_() {
        endList();
    }

    private void startList(final String listStyle) {
        indentLevel++;
        listStyles.push(listStyle);
        pushElementBuffer();
    }

    private void endList() {
        indentLevel--;
        final String listStyle = listStyles.pop();
        final String content = getElementContent();
        if (StringUtils.isNotBlank(content)) {
            if (tableFlag) {
                if ("*".equals(listStyle)) {
                    rawText(EOL + "<ul>" + content + "</ul>" + EOL);
                } else {
                    rawText(EOL + "<ol>" + content + "</ol>" + EOL);
                }
            } else {
                rawText(EOL + content + EOL);
            }
        }
    }

    /** {@inheritDoc} */
    public void listItem() {
        startListItem();
    }

    /** {@inheritDoc} */
    public void listItem_() {
        endListItem();
    }

    /** {@inheritDoc} */
    public void numberedListItem() {
        startListItem();
    }

    /** {@inheritDoc} */
    public void numberedListItem_() {
        endListItem();
    }

    private void startListItem() {
        itemFlag = true;
        pushElementBuffer();
    }

    private void endListItem() {
        final String content = getElementContent();
        if (StringUtils.isNotBlank(content)) {
            if (tableFlag) {
                rawText(EOL + "<li>" + content + "</li>" + EOL);
            } else {
                rawText(EOL + StringUtils.repeat(LIST_INDENT, indentLevel - 1) + listStyles.peek() + SPACE + content + EOL);
            }
        }
        itemFlag = false;
    }

    /** {@inheritDoc} */
    public void link(String name) {
        this.link(name, (String) null);
    }

    /**
     * A link with a target.
     * 
     * @param name The name of the link.
     * @param target The link target.
     */
    public void link(String name, String target) {
        pushElementBuffer();
        href = name;
    }

    /** {@inheritDoc} */
    public void link_() {
        final String content = getElementContent();
        if (StringUtils.isNotBlank(content)) {
            if (tableFlag) {
                rawText("<a href=\"" + StringEscapeUtils.escapeHtml(href) + "\">" + content + "</a>");
            } else {
                rawText("[" + content + "](" + escapeMarkdown(StringEscapeUtils.escapeHtml(href)) + ")");
            }
        }
    }

    /** {@inheritDoc} */
    public void italic() {
        pushElementBuffer();
    }

    /** {@inheritDoc} */
    public void italic_() {
        final String content = getElementContent();
        if (StringUtils.isNotBlank(content)) {
            if (tableFlag) {
                rawText("<em>" + content + "</em>");
            } else {
                rawText("_" + content + "_");
            }
        }
    }

    /** {@inheritDoc} */
    public void bold() {
        pushElementBuffer();
    }

    /** {@inheritDoc} */
    public void bold_() {
        final String content = getElementContent();
        if (StringUtils.isNotBlank(content)) {
            if (tableFlag) {
                rawText("<strong>" + content + "</strong>");
            } else {
                rawText("**" + content + "**");
            }
        }
    }

    /** {@inheritDoc} */
    public void monospaced() {
        pushElementBuffer();
    }

    /** {@inheritDoc} */
    public void monospaced_() {
        final String content = getElementContent();
        if (StringUtils.isNotBlank(content)) {
            if (tableFlag) {
                rawText("<code>" + content + "</code>");
            } else {
                rawText("`" + content + "`");
            }
        }
    }

    /** {@inheritDoc} */
    public void lineBreak() {
        if (tableFlag) {
            rawText("<br/>");
        } else {
            rawText("  " + EOL);
        }
    }

    /** {@inheritDoc} */
    public void horizontalRule() {
        if (tableFlag) {
            rawText("<hr/>");
        } else {
            rawText(EOL + "---" + EOL);
        }
    }

    /** {@inheritDoc} */
    public void figureGraphics(String name) {
        if (tableFlag) {
            rawText("<img src=\"" + StringEscapeUtils.escapeHtml(name) + "\" alt=\"\"/>");
        } else {
            rawText("![ ](" + escapeMarkdown(name) + ")");
        }
    }

    /** {@inheritDoc} */
    public void verbatim(boolean boxed) {
        pushElementBuffer();
    }

    /** {@inheritDoc} */
    public void verbatim_() {
        final String content = getElementContent();
        if (StringUtils.isNotBlank(content)) {
            if (tableFlag) {
                rawText("<pre>" + content + "</pre>");
            } else {
                rawText(EOL + EOL + "```" + EOL + content + EOL + "```" + EOL);
            }
        }
    }

    /** {@inheritDoc} */
    public void table() {
        pushElementBuffer();
        tableFlag = true;
    }

    /** {@inheritDoc} */
    public void table_() {
        tableFlag = false;
        final String content = getElementContent();
        if (StringUtils.isNotBlank(content)) {
            rawText("<table>" + content + "</table>" + EOL);
        }
    }

    /** {@inheritDoc} */
    public void tableRows(int justification[], boolean grid) {
        pushElementBuffer();
    }

    /** {@inheritDoc} */
    public void tableRows_() {
        final String content = getElementContent();
        if (StringUtils.isNotBlank(content)) {
            rawText("<tbody>" + content + "</tbody>" + EOL);
        }
    }

    /** {@inheritDoc} */
    public void tableRow() {
        pushElementBuffer();
    }

    /** {@inheritDoc} */
    public void tableRow_() {
        final String content = getElementContent();
        if (StringUtils.isNotBlank(content)) {
            rawText("<tr>" + content + "</tr>" + EOL);
        }
    }

    /** {@inheritDoc} */
    public void tableCell() {
        pushElementBuffer();
    }

    /** {@inheritDoc} */
    public void tableCell_() {
        final String content = getElementContent();
        if (StringUtils.isNotBlank(content)) {
            rawText("<td>" + content + "</td>" + EOL);
        }
    }

    /** {@inheritDoc} */
    public void tableHeaderCell() {
        pushElementBuffer();
    }

    /** {@inheritDoc} */
    public void tableHeaderCell_() {
        final String content = getElementContent();
        if (StringUtils.isNotBlank(content)) {
            rawText("<th>" + content + "</th>" + EOL);
        }
    }

    /** {@inheritDoc} */
    public void text(String text) {
        buffers.peek().append(WordUtils.wrap(escape(text), 100));
    }

    /** {@inheritDoc} */
    public void rawText(String text) {
        buffers.peek().append(text);
    }

    /**
     * {@inheritDoc}
     * 
     * Unkown events just log a warning message but are ignored otherwise.
     * 
     * @see org.apache.maven.doxia.sink.Sink#unknown(String,Object[],SinkEventAttributes)
     */
    public void unknown(String name, Object[] requiredParams, SinkEventAttributes attributes) {
        getLog().warn("[Apt Sink] Unknown Sink event: '" + name + "', ignoring!");
    }

    protected String getElementContent() {
        return buffers.pop().toString();
    }

    protected void pushElementBuffer() {
        buffers.push(new StringBuilder());
    }

    /**
     * Write text to output.
     * 
     * @param text The text to write.
     */
    protected void write(String text) {
        writer.write(unifyEOLs(text));
    }

    /**
     * Write Apt escaped text to output.
     * 
     * @param text The text to write.
     */
    protected void verbatimContent(String text) {
        write(escape(text));
    }

    /** {@inheritDoc} */
    public void flush() {
        writer.flush();
    }

    /** {@inheritDoc} */
    public void close() {
        writer.close();
        init();
    }

    private String escape(final String text) {
        if (tableFlag) {
            return StringEscapeUtils.escapeHtml(text);
        } else {
            return escapeMarkdown(text);
        }
    }

    // ----------------------------------------------------------------------
    // Private methods
    // ----------------------------------------------------------------------

    /**
     * Escape special characters in a text in APT:
     * 
     * <pre>
     * \~, \=, \-, \+, \*, \[, \], \<, \>, \{, \}, \\
     * </pre>
     * 
     * @param text the String to escape, may be null
     * @return the text escaped, "" if null String input
     */
    private String escapeMarkdown(String text) {
        if (text == null) {
            return "";
        }

        int length = text.length();
        StringBuilder buffer = new StringBuilder(length);

        for (int i = 0; i < length; ++i) {
            char c = text.charAt(i);
            switch (c) {
                case '\\':
                case '`':
                case '*':
                case '_':
                case '{':
                case '}':
                case '<':
                case '>':
                case '[':
                case ']':
                case '(':
                case ')':
                case '#':
                case '+':
                case '-':
                case '!':
                    buffer.append('\\');
                    buffer.append(c);
                    break;
                default:
                    buffer.append(c);
            }
        }

        return buffer.toString();
    }
}
