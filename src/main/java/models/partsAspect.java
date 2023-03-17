package models;

import org.apache.commons.math3.util.Pair;

public record partsAspect(material material, form form) {

    public enum material {
        BLUE, GREEN, METAL
    }

    public enum form {
        RAW, LID, BASE
    }

}
