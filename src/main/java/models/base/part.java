package models.base;

import models.partsAspect;
import org.apache.commons.math3.util.Pair;

import java.time.Instant;
import java.util.TreeMap;
import java.util.TreeSet;

public class part extends TreeSet<part> {
    public enum materialType {
        BLUE,
        GREEN,
        METAL
    }

    public enum targetType {
        RAW,
        LID,
        BASE
    }


    private final int id;
    private final partsAspect expectation;
    private final TreeMap<String, Instant> itemTimestamps;

    private partsAspect reality;

    private boolean defect;
    private boolean waitTransport;
    private boolean produced;

    public part(int id, partsAspect expectedPart) {
        this.id = id;
        this.expectation = expectedPart;

        itemTimestamps = new TreeMap<>();
        this.defect = false;
        this.waitTransport = false;
        this.produced = false;
    }

    public int getId() {
        return id;
    }

    public TreeMap<String, Instant> getTimestamps() {
        return itemTimestamps;
    }

    public partsAspect getExpectation() {
        return expectation;
    }

    public void addTimestamp(String itemName) {
        itemTimestamps.put(itemName, Instant.now());
    }

    public void setReality(partsAspect aspect) {
        reality = aspect;
    }

    public partsAspect getReality() {
        return reality;
    }

    public boolean isWaitTransport() {
        return waitTransport;
    }

    public void setWaitTransport(boolean waitTransport) {
        this.waitTransport = waitTransport;
    }

    public boolean isDefect() {
        return defect;
    }

    public void setDefect() {
        this.defect = true;
    }

    public boolean isProduced() {
        return produced;
    }

    public void setProduced(boolean produced) {
        this.produced = produced;
    }
}
