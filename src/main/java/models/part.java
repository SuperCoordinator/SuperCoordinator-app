package models;

import java.time.Instant;

public class part {

    private final int id;
    /**
     *  Following the layout of CMC Factory IO block,
     *  there are 8 presence sensor that will be monitoring the parts times
     */
    private final Instant[] timestamps;

    public part(int id) {
        this.id = id;
        timestamps = new Instant[8];
    }

    public int getId() {
        return id;
    }

    public Instant[] getTimestamps() {
        return timestamps;
    }

    public void addTimestamp(Instant timestamp, int idx) {
        this.timestamps[idx] = timestamp;
    }
}
