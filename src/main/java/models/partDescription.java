package models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public final class partDescription {
    public enum material {
        BLUE, GREEN, METAL, UNKNOWN
    }

    public enum form {
        RAW, LID, BASE, UNKNOWN
    }

    @XmlAttribute
    private material material;
    @XmlAttribute
    private form form;

    public partDescription() {
    }

    public partDescription(material material, form form) {
        this.material = material;
        this.form = form;
    }

    public material material() {
        return material;
    }

    public form form() {
        return form;
    }


}
