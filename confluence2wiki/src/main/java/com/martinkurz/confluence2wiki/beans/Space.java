package com.martinkurz.confluence2wiki.beans;

import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.ImmutableMap;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Space {
    /**
     * Mapping of rometools confluence spaces to rometools github repository names.
     */
    private static final Map<String, String> SPACES = new ImmutableMap.Builder<String, String>().put("OPML", "rome-opml").put("MANO", "rome-mano")
        .put("MODULES", "rome-modules").put("PROPONO", "rome-propono").put("INCUBATOR", "rome-incubator").put("FETCHER", "rome-fetcher").put("ROME", "rome")
        .build();
    @XmlAttribute
    private int id;
    @XmlAttribute
    private String name;
    @XmlAttribute
    private String key;
    @XmlElement(name = "page")
    private Page homepage;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Page getHomepage() {
        return homepage;
    }

    public void setHomepage(Page homepage) {
        this.homepage = homepage;
    }

    public String getFilesystemPath() {
        return SPACES.get(key);
    }

    @Override
    public String toString() {
        return "Space [id=" + id + ", name=" + name + ", key=" + key + ", homepage=" + homepage + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((homepage == null) ? 0 : homepage.hashCode());
        result = prime * result + id;
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        Space other = (Space) obj;
        if (homepage == null) {
            if (other.homepage != null)
                return false;
        } else if (!homepage.equals(other.homepage))
            return false;
        if (id != other.id)
            return false;
        if (key == null) {
            if (other.key != null)
                return false;
        } else if (!key.equals(other.key))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }
}
