package com.martinkurz.confluence2wiki.beans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang.WordUtils;

import com.google.common.base.Joiner;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Page {
    @XmlAttribute
    private int id;
    @XmlAttribute
    private String creator;
    @XmlAttribute
    private String lastModifier;
    @XmlAttribute
    @XmlJavaTypeAdapter(JaxbDateSerializer.class)
    private Date creationDate;
    @XmlAttribute
    @XmlJavaTypeAdapter(JaxbDateSerializer.class)
    private Date lastModificationDate;
    @XmlElement
    private String title;
    @XmlElement(name = "page")
    @XmlElementWrapper(name = "children")
    private List<Page> children;
    @XmlElement(name = "body")
    @XmlElementWrapper(name = "bodies")
    private List<Body> bodies;
    @XmlElement(name = "attachment")
    @XmlElementWrapper(name = "attachments")
    private List<Attachment> attachments;
    @XmlTransient
    private Page parent;
    @XmlTransient
    private Space space;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getLastModifier() {
        return lastModifier;
    }

    public void setLastModifier(String lastModifier) {
        this.lastModifier = lastModifier;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getLastModificationDate() {
        return lastModificationDate;
    }

    public void setLastModificationDate(Date lastModificationDate) {
        this.lastModificationDate = lastModificationDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Page> getChildren() {
        return children;
    }

    public void setChildren(List<Page> children) {
        this.children = children;
    }

    public List<Body> getBodies() {
        return bodies;
    }

    public void setBodies(List<Body> bodies) {
        this.bodies = bodies;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }

    public Page getParent() {
        return parent;
    }

    public void setParent(Page parent) {
        this.parent = parent;
    }

    public Space getSpace() {
        return space;
    }

    public void setSpace(Space space) {
        this.space = space;
    }

    /**
     * create relative link to other page.
     * @param linkTarget
     * @return
     */
    public String createRelativeLink(final Page linkTarget) {
        if (linkTarget == null) {
            return null;
        }
        final List<String> srcPathFragements = getPathElements();
        final List<String> trgPathFragements = linkTarget.getPathElements();
        int common = -1;
        for (int i = 0; i < srcPathFragements.size() && i < trgPathFragements.size(); i++) {
            if (!srcPathFragements.get(i).equals(trgPathFragements.get(i))) {
                break;
            }
            common = i;
        }
        final StringBuilder sb = new StringBuilder();
        for (int i = common + 1; i < srcPathFragements.size(); i++) {
            sb.append("../");
        }
        if (sb.length() == 0) {
            sb.append("./");
        }
        for (int i = common + 1; i < trgPathFragements.size(); i++) {
            sb.append(trgPathFragements.get(i));
            sb.append("/");
        }
        sb.append(linkTarget.getFileName());
        sb.append(".html");
        return sb.toString();
    }

    /**
     * Filename for actual page, "index" when pages title is "Home" or page has children,
     * cleand up page title otherwise (without suffix).
     * @return
     */
    public String getFileName() {
        final String tmptFilename = titleToFilename();
        return hasChildren() || tmptFilename == null ? "index" : tmptFilename;
    }

    /**
     * create filename from page title, "Home" is ignored because that's the automatically generated title for spaces homepage.
     * @return
     */
    private String titleToFilename() {
        return "Home".equals(title) ? null : WordUtils.capitalize(title.replaceAll("[^a-zA-Z0-9_. -]", "")).replace(" ", "");
    }

    /**
     * List of directory names (from space to parent pages) to current page.
     * @return
     */
    public List<String> getPathElements() {
        final List<String> path = new ArrayList<String>();
        Page act = hasChildren() ? this : parent;
        while (act != null) {
            final String tmpFileName = act.titleToFilename();
            if (tmpFileName != null) {
                path.add(tmpFileName);
            }
            act = act.parent;
        }
        path.add(space.getFilesystemPath());
        Collections.reverse(path);
        return path;
    }

    /**
     * create directory path to current page.
     * @return
     */
    public String getPath() {
        return Joiner.on(System.getProperty("file.separator")).join(getPathElements());
    }

    /**
     * create path and filename for current page (without suffix).
     * @return
     */
    public String getPathAndFilename() {
        return getPath() + System.getProperty("file.separator") + getFileName();
    }

    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    @Override
    public String toString() {
        return "Page [id=" + id + ", creator=" + creator + ", lastModifier=" + lastModifier + ", creationDate=" + creationDate + ", lastModificationDate="
            + lastModificationDate + ", title=" + title + ", children=" + children + ", bodies=" + bodies + ", attachments=" + attachments + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((attachments == null) ? 0 : attachments.hashCode());
        result = prime * result + ((bodies == null) ? 0 : bodies.hashCode());
        result = prime * result + ((children == null) ? 0 : children.hashCode());
        result = prime * result + ((creationDate == null) ? 0 : creationDate.hashCode());
        result = prime * result + ((creator == null) ? 0 : creator.hashCode());
        result = prime * result + id;
        result = prime * result + ((lastModificationDate == null) ? 0 : lastModificationDate.hashCode());
        result = prime * result + ((lastModifier == null) ? 0 : lastModifier.hashCode());
        result = prime * result + ((title == null) ? 0 : title.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Page other = (Page) obj;
        if (attachments == null) {
            if (other.attachments != null)
                return false;
        } else if (!attachments.equals(other.attachments))
            return false;
        if (bodies == null) {
            if (other.bodies != null)
                return false;
        } else if (!bodies.equals(other.bodies))
            return false;
        if (children == null) {
            if (other.children != null)
                return false;
        } else if (!children.equals(other.children))
            return false;
        if (creationDate == null) {
            if (other.creationDate != null)
                return false;
        } else if (!creationDate.equals(other.creationDate))
            return false;
        if (creator == null) {
            if (other.creator != null)
                return false;
        } else if (!creator.equals(other.creator))
            return false;
        if (id != other.id)
            return false;
        if (lastModificationDate == null) {
            if (other.lastModificationDate != null)
                return false;
        } else if (!lastModificationDate.equals(other.lastModificationDate))
            return false;
        if (lastModifier == null) {
            if (other.lastModifier != null)
                return false;
        } else if (!lastModifier.equals(other.lastModifier))
            return false;
        if (title == null) {
            if (other.title != null)
                return false;
        } else if (!title.equals(other.title))
            return false;
        return true;
    }

}
