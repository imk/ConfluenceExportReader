conversion of conlfluence storage export to apt or markdown wiki in maven site layout
(quick and dirty solution for migration of rometools from http://rometools.jira.com to http://github.com/rometools/).

For exectuion, daves confluence export ist required, the zip file must be extracted and com.martinkurz.confluence2wiki.Confluence2Wiki
must be called with markup type (apt or markdown) and path to confluence export directory.

The confluence export xml is transformed to a simplified xml structure by xslt, the result is converted to beans by jaxb,
then all the pages content is read with Jsoup, the wiki markup is generated with cumstomized doxia markup sinks in a
Jsoup NodeVisitor that traverses each document.