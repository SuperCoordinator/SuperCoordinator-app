package models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public final class partsAspect {
    public enum material {
        BLUE, GREEN, METAL
    }

    public enum form {
        RAW, LID, BASE
    }

    @XmlAttribute
    private material material;
    @XmlAttribute
    private form form;

    public partsAspect() {
    }

    public partsAspect(material material, form form) {
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
