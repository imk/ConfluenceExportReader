package com.martinkurz.confluence2wiki;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.sink.SinkEventAttributeSet;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeVisitor;

import com.martinkurz.confluence2wiki.beans.Attachment;
import com.martinkurz.confluence2wiki.beans.Body;
import com.martinkurz.confluence2wiki.beans.Page;

public class ConfluenceHtmlNodeVisitor implements NodeVisitor {
    private static final List<String> HEADINGS = Arrays.asList("h1", "h2", "h3", "h4", "h5", "h6");
    private static final List<String> STRONG = Arrays.asList("strong", "b");
    private static final List<String> EMPHASIS = Arrays.asList("em", "i");
    private static final List<String> MONOSPACE = Arrays.asList("tt", "code");
    private static final List<String> VERBATIM = Arrays.asList("pre", "ac:plain-text-body");
    // left align table content, fails if table contains more than 25 cols!
    private static final int[] TABLE_LEFT_ALIGNED = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
    private final Map<String, Map<String, Page>> pageBySpaceAndTitle;
    private Page page;
    private Sink sink;
    private boolean verbatim;
    private boolean ignoreContent;
    private boolean inLink;
    private boolean linkedText;
    private boolean image;
    private String href;
    private String hrefTitle;
    private int maxHeadingLevel;
    private int actHeadingLevel;

    /**
     * @param pageBySpaceAndTitle Map with all confluence spaces pages by title for generation of links
     */
    public ConfluenceHtmlNodeVisitor(final Map<String, Map<String, Page>> pageBySpaceAndTitle) {
        this.pageBySpaceAndTitle = pageBySpaceAndTitle;
    }

    private void reset() {
        page = null;
        sink = null;
        verbatim = false;
        ignoreContent = false;
        inLink = false;
        linkedText = false;
        image = false;
        href = null;
        hrefTitle = null;
        maxHeadingLevel = 1;
        actHeadingLevel = 1;
    }

    /**
     * convert confluence page to markup.
     * @param page the page to convert
     * @param sink doxia sink for markup creation
     */
    public void convertPage(final Page page, final Sink sink) {
        reset();
        this.page = page;
        this.sink = sink;
        printHead();
        sink.body();
        if (!"Home".equals(page.getTitle())) {
            sink.sectionTitle1();
            sink.text(page.getTitle());
            sink.sectionTitle1_();
            maxHeadingLevel++;
        }
        for (final Body body : page.getBodies()) {
            if (body.getBodyType() == 2) {
                Jsoup.parseBodyFragment(body.getContent()).body().traverse(this);
            }
        }
        sink.body_();
        sink.flush();
        sink.close();
    }

    /**
     * print doc header.
     */
    private void printHead() {
        sink.head();
        sink.title();
        sink.text(page.getTitle());
        sink.title_();
        sink.author();
        sink.text(page.getCreator());
        sink.author_();
        sink.date();
        sink.text(new SimpleDateFormat("yyyy-MM-dd").format(page.getCreationDate()));
        sink.date_();
        sink.head_();
    }

    /**
     * @see NodeVisitor#head(Node, int)
     */
    @Override
    public void head(Node node, int depth) {
        if (node instanceof TextNode && !ignoreContent) {
            final TextNode tn = (TextNode) node;
            if (verbatim) {
                sink.rawText(tn.getWholeText());
            } else {
                final String textContent = tn.text();
                if (inLink && !linkedText && StringUtils.isNotBlank(textContent)) {
                    linkedText = true;
                }
                sink.text(textContent);
            }
        } else if (node instanceof Element) {
            final Element e = (Element) node;
            final String name = e.nodeName();
            if (HEADINGS.contains(name)) {
                // compute act heading level instead of using the provided level
                // for ensuring consistent heading level structure 
                actHeadingLevel = Integer.parseInt(name.substring(1));
                if (actHeadingLevel > maxHeadingLevel) {
                    actHeadingLevel = maxHeadingLevel;
                    maxHeadingLevel++;
                }
                switch (actHeadingLevel) {
                    case 1:
                        sink.sectionTitle1();
                        break;
                    case 2:
                        sink.sectionTitle2();
                        break;
                    case 3:
                        sink.sectionTitle3();
                        break;
                    case 4:
                        sink.sectionTitle4();
                        break;
                    case 5:
                        sink.sectionTitle5();
                        break;
                }
            } else if (STRONG.contains(name)) {
                sink.bold();
            } else if (EMPHASIS.contains(name)) {
                sink.italic();
            } else if (MONOSPACE.contains(name)) {
                sink.monospaced();
            } else if (VERBATIM.contains(name)) {
                verbatim = true;
                sink.verbatim(SinkEventAttributeSet.BOXED);
            } else if ("dl".equals(name)) {
                sink.definitionList();
            } else if ("dt".equals(name)) {
                sink.definedTerm();
            } else if ("dd".equals(name)) {
                sink.definitionListItem();
            } else if ("ul".equals(name)) {
                sink.list();
            } else if ("ol".equals(name)) {
                sink.numberedList(Sink.NUMBERING_DECIMAL);
            } else if ("li".equals(name)) {
                sink.listItem();
            } else if ("p".equals(name)) {
                sink.paragraph();
            } else if ("a".equals(name)) {
                href = e.attr("href");
                if (StringUtils.isNotBlank(href)) {
                    inLink = true;
                    linkedText = false;
                    sink.link(href);
                }
            } else if ("ac:link".equals(name)) {
                ignoreContent = true;
                if (StringUtils.isBlank(e.attr("ac:anchor"))) {
                    inLink = true;
                    linkedText = false;
                }
            } else if ("ac:image".equals(name)) {
                image = true;
            } else if ("ri:attachment".equals(name)) {
                final String attachmentFileName = e.attr("ri:filename");
                if (image) {
                    sink.figureGraphics(attachmentFileName);
                } else if (inLink) {
                    hrefTitle = attachmentFileName;
                    sink.link("./" + hrefTitle);
                }
            } else if ("ri:url".equals(name) && image) {
                sink.figureGraphics(e.attr("ri:value"));
            } else if ("ri:page".equals(name) && inLink) {
                hrefTitle = e.attr("ri:content-title");
                final String spaceKey = e.attr("ri:space-key");
                sink.link(page.createRelativeLink(pageBySpaceAndTitle.get(StringUtils.isNotBlank(spaceKey) ? spaceKey.toUpperCase() : page.getSpace().getKey().toUpperCase()).get(hrefTitle)));
            } else if (("ac:plain-text-link-body".equals(name) || "".equals(name)) && inLink) {
                ignoreContent = false;
            } else if ("table".equals(name)) {
                ignoreContent = true;
                sink.table();
            } else if ("tbody".equals(name)) {
                sink.tableRows(TABLE_LEFT_ALIGNED, true);
            } else if ("tr".equals(name)) {
                sink.tableRow();
            } else if ("th".equals(name)) {
                ignoreContent = false;
                sink.tableHeaderCell();
            } else if ("td".equals(name)) {
                ignoreContent = false;
                sink.tableCell();
            } else if ("hr".equals(name)) {
                sink.horizontalRule();
            } else if ("br".equals(name)) {
                sink.lineBreak();
            } else if ("ac:structured-macro".equals(name)) {
                final String macroName = e.attr("ac:name");
                if ("attachments".equals(macroName)) {
                    printAttachments();
                } else if ("pagetree".equals(macroName)) {
                    printChildPages(page);
                }
            } else  if ("ac:parameter".equals(name)) {
                ignoreContent = true;
            }
        }
    }

    /**
     * generate linked list of pages attchments.
     */
    private void printAttachments() {
        if (page.getAttachments() != null && !page.getAttachments().isEmpty()) {
            sink.list();
            for (final Attachment a : page.getAttachments()) {
                sink.listItem();
                sink.link("./" + a.getFileName());
                sink.text(a.getFileName());
                sink.link_();
                sink.listItem_();
            }
            sink.list_();
        }
    }

    /**
     * generate linked list of child pages (recursive).
     * @param pa
     */
    private void printChildPages(final Page pa) {
        if (pa.getChildren() != null && !pa.getChildren().isEmpty()) {
            sink.list();
            for (final Page p : page.getChildren()) {
                sink.listItem();
                sink.link(page.createRelativeLink(p));
                sink.text(p.getTitle());
                sink.link_();
                printChildPages(p);
                sink.listItem_();
            }
            sink.list_();
        }
    }

    /**
     * @see NodeVisitor#tail(Node, int)
     */
    @Override
    public void tail(Node node, int depth) {
        if (node instanceof Element) {
            final Element e = (Element) node;
            final String name = e.nodeName();
            if (HEADINGS.contains(name)) {
                switch (actHeadingLevel) {
                    case 1:
                        sink.sectionTitle1_();
                        break;
                    case 2:
                        sink.sectionTitle2_();
                        break;
                    case 3:
                        sink.sectionTitle3_();
                        break;
                    case 4:
                        sink.sectionTitle4_();
                        break;
                    case 5:
                        sink.sectionTitle5_();
                        break;
                }
            } else if (STRONG.contains(name)) {
                sink.bold_();
            } else if (EMPHASIS.contains(name)) {
                sink.italic_();
            } else if (MONOSPACE.contains(name)) {
                sink.monospaced_();
            } else if (VERBATIM.contains(name)) {
                verbatim = false;
                sink.verbatim_();
            } else if ("dl".equals(name)) {
                sink.definitionList_();
            } else if ("dt".equals(name)) {
                sink.definedTerm_();
            } else if ("dd".equals(name)) {
                sink.definitionListItem_();
            } else if ("ul".equals(name)) {
                sink.list_();
            } else if ("ol".equals(name)) {
                sink.numberedList_();
            } else if ("li".equals(name)) {
                sink.listItem_();
            } else if ("p".equals(name)) {
                sink.paragraph_();
            } else if ("a".equals(name) && inLink) {
                if (!linkedText) {
                    sink.text(href);
                }
                sink.link_();
            } else if ("ac:link".equals(name)) {
                ignoreContent = false;
                if (inLink) {
                    if (!linkedText) {
                        sink.text(hrefTitle);
                    }
                    sink.link_();
                }
            } else if ("ac:image".equals(name)) {
                image = false;
            } else if ("table".equals(name)) {
                ignoreContent = false;
                sink.table_();
            } else if ("tbody".equals(name)) {
                sink.tableRows_();
            } else if ("tr".equals(name)) {
                sink.tableRow_();
            } else if ("th".equals(name)) {
                ignoreContent = true;
                sink.tableHeaderCell_();
            } else if ("td".equals(name)) {
                ignoreContent = true;
                sink.tableCell_();
            } else if (("ac:plain-text-link-body".equals(name) || "".equals(name)) && inLink) {
                ignoreContent = true;
            } else  if ("ac:parameter".equals(name)) {
                ignoreContent = false;
            }
        }
    }

}
