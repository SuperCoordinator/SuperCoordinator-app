package models;

import java.time.Instant;
import java.util.TreeMap;

public class part{

    private final int id;
    private final String targetType;
    private final TreeMap<String, Instant> itemTimestamps;
    private boolean produced;

    public part(int id, String targetType) {
        this.id = id;
        this.targetType = targetType;
        itemTimestamps = new TreeMap<>();
        this.produced = false;
    }

    public int getId() {
        return id;
    }

    public TreeMap<String, Instant> getTimestamps() {
        return itemTimestamps;
    }

    public String getTargetType() {
        return targetType;
    }

    public void addTimestamp(String itemName) {
        itemTimestamps.put(itemName, Instant.now());
    }

    public boolean isProduced() {
        return produced;
    }

    public void setProduced() {
        this.produced = true;
    }
}
