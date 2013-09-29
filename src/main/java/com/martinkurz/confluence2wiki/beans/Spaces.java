package com.martinkurz.confluence2wiki.beans;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Spaces {
    @XmlElement(name = "space")
    private List<Space> spaces;

    public List<Space> getSpaces() {
        return spaces;
    }

    public void setSpaces(List<Space> spaces) {
        this.spaces = spaces;
    }

    @Override
    public String toString() {
        return "Spaces [spaces=" + spaces + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((spaces == null) ? 0 : spaces.hashCode());
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
        Spaces other = (Spaces) obj;
        if (spaces == null) {
            if (other.spaces != null)
                return false;
        } else if (!spaces.equals(other.spaces))
            return false;
        return true;
    }
}
