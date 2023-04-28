package models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.time.Instant;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class inboundOrder {
    @XmlAttribute
    private int id;
    @XmlAttribute
    private int metal_qty;
    @XmlAttribute
    private int green_qty;
    @XmlAttribute
    private int blue_qty;

    public inboundOrder() {
    }

    public inboundOrder(int id, int metal_qty, int green_qty, int blue_qty) {
        this.id = id;
        this.metal_qty = metal_qty;
        this.green_qty = green_qty;
        this.blue_qty = blue_qty;
    }

    public int getId() {
        return id;
    }

    public int getMetal_qty() {
        return metal_qty;
    }

    public int getGreen_qty() {
        return green_qty;
    }

    public int getBlue_qty() {
        return blue_qty;
    }

    @Override
    public String toString() {
        return "[id=" + id + ", metal_qty=" + metal_qty + ", green_qty=" + green_qty + ", blue_qty=" + blue_qty + "]";
    }
}
