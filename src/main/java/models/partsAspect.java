package models;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;

public final class partsAspect implements Externalizable {

    public static final long serialVersionUID = 1234L;

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(material);
        out.writeObject(form);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.material = (partsAspect.material) in.readObject();
        this.form = (partsAspect.form) in.readObject();
    }

    public enum material {
        BLUE, GREEN, METAL
    }

    public enum form {
        RAW, LID, BASE
    }

    private material material;
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
