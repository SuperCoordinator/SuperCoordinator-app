package models;

import models.base.part;

import java.io.*;
import java.util.Objects;

public final class part_prodTime /*implements Externalizable*/ {
   /* public static final long serialVersionUID = 1234L;

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(part);
        out.write(production_time);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.part = (part) in.readObject();
        this.production_time = in.readInt();
    }*/
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