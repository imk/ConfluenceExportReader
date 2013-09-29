package com.martinkurz.confluence2wiki.doxia.markdown;

import java.io.Writer;

import org.apache.maven.doxia.sink.AbstractTextSinkFactory;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.sink.SinkFactory;
import org.codehaus.plexus.component.annotations.Component;

/**
 * Markdown implementation of the Sink factory.
 */
@Component(role = SinkFactory.class, hint = "markdown")
public class MarkdownSinkFactory extends AbstractTextSinkFactory {
    /** {@inheritDoc} */
    protected Sink createSink(Writer writer, String encoding) {
        // encoding can safely be ignored since it isn't written into the generated APT source
        return new MarkdownSink(writer);
    }

}
