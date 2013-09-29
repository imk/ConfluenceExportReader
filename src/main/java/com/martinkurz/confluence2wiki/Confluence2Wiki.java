package com.martinkurz.confluence2wiki;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.maven.doxia.sink.SinkFactory;
import org.codehaus.plexus.util.FileUtils;

import com.martinkurz.confluence2wiki.beans.Attachment;
import com.martinkurz.confluence2wiki.beans.Page;
import com.martinkurz.confluence2wiki.beans.Space;
import com.martinkurz.confluence2wiki.beans.Spaces;
import com.martinkurz.confluence2wiki.doxia.apt.AptSinkFactory;
import com.martinkurz.confluence2wiki.doxia.markdown.MarkdownSinkFactory;

public class Confluence2Wiki {
    private static final String SEP = System.getProperty("file.separator");

    public static enum MarkupType {
        apt(".apt"), markdown(".md");
        private final String extension;

        private MarkupType(final String extension) {
            this.extension = extension;
        }

        public String getExtension() {
            return extension;
        }
    }

    private final MarkupType markupType;

    public Confluence2Wiki(final MarkupType markupType) {
        this.markupType = markupType;
    }

    /**
     * read confluence export and generate documents in required markup type in maven site structure.
     * @param baseDir
     * @throws TransformerConfigurationException
     * @throws TransformerException
     * @throws TransformerFactoryConfigurationError
     * @throws JAXBException
     * @throws IOException
     */
    public void parse(final File baseDir) throws TransformerConfigurationException, TransformerException, TransformerFactoryConfigurationError, JAXBException,
        IOException {
        final Map<String, Map<String, Page>> pageBySpaceAndTitle = getSourceData(baseDir);
        final ConfluenceHtmlNodeVisitor chnv = new ConfluenceHtmlNodeVisitor(pageBySpaceAndTitle);
        final SinkFactory factory = createSinkFactory();
        for (final Map<String, Page> pagesBySpace : pageBySpaceAndTitle.values()) {
            for (final Page p : pagesBySpace.values()) {
                System.out.println("converting " + p.toString());
                final File outputFile = new File("src" + SEP + "site" + SEP + markupType.name() + SEP + p.getPathAndFilename() + markupType.extension);
                if (!outputFile.getParentFile().exists()) {
                    outputFile.getParentFile().mkdirs();
                }
                chnv.convertPage(p, factory.createSink(new FileOutputStream(outputFile)));
                if (p.getAttachments() != null) {
                    for (final Attachment a : p.getAttachments()) {
                        FileUtils.copyFile(
                            new File(baseDir.getAbsolutePath() + SEP + "attachments" + SEP + a.getPageId() + SEP + a.getId() + SEP + a.getVersion()), new File(
                                "src" + SEP + "site" + SEP + "resources" + SEP + p.getPath() + SEP + a.getFileName()));
                    }
                }
            }
        }
    }

    /**
     * doxia sink depending on selected output markup type.
     * @return
     */
    private SinkFactory createSinkFactory() {
        final SinkFactory factory;
        switch (markupType) {
            case markdown:
                factory = new MarkdownSinkFactory();
                break;
            default:
                factory = new AptSinkFactory();
                break;
        }
        return factory;
    }

    /**
     * read confluence export and prepare conversion to markup.
     * @param baseDir
     * @return
     * @throws TransformerConfigurationException
     * @throws TransformerException
     * @throws TransformerFactoryConfigurationError
     * @throws JAXBException
     * @throws IOException
     */
    private Map<String, Map<String, Page>> getSourceData(final File baseDir) throws TransformerConfigurationException, TransformerException,
        TransformerFactoryConfigurationError, JAXBException, IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        transformConfluenceExport(baseDir, baos);
        final Spaces spaces = parseTransformedConfluenceExport(new ByteArrayInputStream(baos.toByteArray()));
        final Map<String, Map<String, Page>> pageBySpaceAndTitle = new HashMap<String, Map<String, Page>>();
        for (final Space space : spaces.getSpaces()) {
            FileUtils.deleteDirectory("src" + SEP + "site" + SEP + markupType.name() + SEP + space.getFilesystemPath());
            FileUtils.deleteDirectory("src" + SEP + "site" + SEP + "resources" + SEP + space.getFilesystemPath());
            prepareSpaceInfo(pageBySpaceAndTitle, space);
        }
        return pageBySpaceAndTitle;
    }

    /**
     * preparation of space beans for easier transformation to markup.
     * @param pageBySpaceAndTitle
     * @param space
     */
    private void prepareSpaceInfo(final Map<String, Map<String, Page>> pageBySpaceAndTitle, final Space space) {
        pageBySpaceAndTitle.put(space.getKey().toUpperCase(), new HashMap<String, Page>());
        preparePageInfo(pageBySpaceAndTitle, space, space.getHomepage());
    }

    /**
     * preparation of page beans for easier transformation to markup.
     * @param pageBySpaceAndTitle
     * @param space
     * @param page
     */
    private void preparePageInfo(final Map<String, Map<String, Page>> pageBySpaceAndTitle, final Space space, final Page page) {
        if (page == null) {
            return;
        }
        page.setSpace(space);
        pageBySpaceAndTitle.get(space.getKey().toUpperCase()).put(page.getTitle(), page);
        if (page.getChildren() != null) {
            for (final Page child : page.getChildren()) {
                child.setParent(page);
                preparePageInfo(pageBySpaceAndTitle, space, child);
            }
        }
    }

    /**
     * get beans from simplified structure.
     * 
     * @param in
     * @return
     * @throws JAXBException
     */
    private Spaces parseTransformedConfluenceExport(final InputStream in) throws JAXBException {
        System.out.println("loading data");
        return (Spaces) JAXBContext.newInstance(Spaces.class).createUnmarshaller().unmarshal(new StreamSource(in));
    }

    /**
     * transform confluence storage format to simplified structure.
     * 
     * @param baseDir path to confluence export files
     * @param out for capturing the tranformation result
     * @throws TransformerConfigurationException
     * @throws TransformerException
     * @throws TransformerFactoryConfigurationError
     */
    private void transformConfluenceExport(final File baseDir, final OutputStream out) throws TransformerConfigurationException, TransformerException,
        TransformerFactoryConfigurationError {
        final File confluenceExportXml = new File(baseDir, "entities.xml");
        System.out.println("transforming " + confluenceExportXml.getAbsolutePath());
        if (!confluenceExportXml.exists() || !confluenceExportXml.canRead()) {
            throw new RuntimeException("can't read " + confluenceExportXml.getAbsolutePath());
        }
        final TransformerFactory tf = TransformerFactory.newInstance();
        final Transformer t = tf.newTransformer(new StreamSource(this.getClass().getResourceAsStream("/confluence.xslt")));
        t.transform(new StreamSource(confluenceExportXml), new StreamResult(out));
    }

    /**
     * @param args
     * @throws TransformerFactoryConfigurationError
     * @throws TransformerException
     * @throws TransformerConfigurationException
     * @throws JAXBException
     * @throws IOException
     */
    public static void main(String[] args) throws TransformerConfigurationException, TransformerException, TransformerFactoryConfigurationError, JAXBException,
        IOException {
        if (args == null || args.length == 0) {
            args = new String[]{"apt", "/home/martink/Desktop/rome/Confluence-backup-20130922"};
        }
        if (args.length < 2) {
            throw new IllegalArgumentException("two arguments required: markup type [apt|markdown] and confluence export directory");
        }
        new Confluence2Wiki(MarkupType.valueOf(args[0])).parse(new File(args[1]));
    }

}
