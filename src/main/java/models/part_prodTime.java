package models;

import models.base.part;

import java.io.*;
import java.util.Objects;

public final class part_prodTime  {

    private models.base.part part;
    private int production_time;

    public part_prodTime() {
    }
    public part_prodTime(models.base.part part, int production_time) {
        this.part = part;
        this.production_time = production_time;
    }

    public models.base.part part() {
        return part;
    }

    public int production_time() {
        return production_time;
    }


}