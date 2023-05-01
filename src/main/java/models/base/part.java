package models.base;

import models.partDescription;

import java.time.Instant;
import java.util.TreeMap;

public class part {
    private int id;
    private partDescription expectation;
    private TreeMap<String, Instant> itemTimestamps;

    private partDescription reality;

    private boolean defect;
    private boolean waitTransport;
    private boolean produced;

    public part() {
    }

    public part(int id, partDescription partAppearance) {
        this.id = id;
        this.reality = partAppearance;

        this.itemTimestamps = new TreeMap<>();
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

    public partDescription getExpectation() {
        return expectation;
    }

    public void addTimestamp(String itemName) {
        itemTimestamps.put(itemName, Instant.now());
    }

    public void setReality(partDescription aspect) {
        reality = aspect;
    }

    public partDescription getReality() {
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

    @Override
    public String toString() {
        return "part [id=" + id + ", reality=" + reality.toString() + "]";
    }
}
