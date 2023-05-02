package models.base;

import models.partDescription;

import java.time.Instant;
import java.util.TreeMap;

public class part {

    public enum status {
        IN_STOCK,
        IN_PRODUCTION,
        PRODUCED
    }

    private int id;

    private status state;
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
        this.state = status.IN_STOCK;
        this.itemTimestamps = new TreeMap<>();
        this.defect = false;
        this.waitTransport = false;
        this.produced = false;
    }

    public part(int id, status status,partDescription partAppearance) {
        this.id = id;
        this.reality = partAppearance;
        this.state = status;
        this.itemTimestamps = new TreeMap<>();
        this.defect = false;
        this.waitTransport = false;
        this.produced = false;
    }


    public int getId() {
        return id;
    }

    public status getState() {
        return state;
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
        return "db_part [id=" + id + ", reality=" + reality.toString() + "]";
    }
}
