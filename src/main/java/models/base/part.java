package models.base;

import models.partsAspect;
import org.apache.commons.math3.util.Pair;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.time.Instant;
import java.util.TreeMap;
import java.util.TreeSet;

public class part /*implements Externalizable*/ {

   /* public static final long serialVersionUID = 1234L;
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(id);
        out.writeObject(expectation);
        out.writeObject(itemTimestamps);
        out.writeBoolean(defect);
        out.writeBoolean(waitTransport);
        out.writeBoolean(produced);

    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.id = in.readInt();
        this.expectation = (partsAspect) in.readObject();

        this.itemTimestamps = (TreeMap<String, Instant>) in.readObject();
        this.defect = in.readBoolean();
        this.waitTransport = in.readBoolean();
        this.produced = in.readBoolean();
    }*/


    private int id;
    private partsAspect expectation;
    private TreeMap<String, Instant> itemTimestamps;

    private partsAspect reality;

    private boolean defect;
    private boolean waitTransport;
    private boolean produced;

    public part() {
    }

    public part(int id, partsAspect expectedPart) {
        this.id = id;
        this.expectation = expectedPart;

        this.itemTimestamps = new TreeMap<>();
        this.defect = false;
        this.waitTransport = false;
        this.produced = false;
    }

//    public part(part forCopy) {
//        this.id = forCopy.getId();
//        this.expectation = forCopy.getExpectation();
//        this.itemTimestamps = forCopy.getTimestamps();
//        this.reality = forCopy.getReality();
//        this.defect = forCopy.isDefect();
//        this.waitTransport = forCopy.isWaitTransport();
//        this.produced = forCopy.isProduced();
//    }

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
