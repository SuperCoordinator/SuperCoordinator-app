package failures.evaluations;

import utility.InstantAdapter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.Instant;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class failure_occurrence {

    public enum activationVariable {
        N,
        A,
        M
    }

    @XmlAttribute(name = "SFEI")
    private String sfeiName;
    @XmlAttribute(name = "activation_variable")
    private activationVariable actVariable;
    @XmlAttribute
    private failure.type failureType;
    @XmlAttribute
    private int nPartsMoved_at_time;
    @XmlAttribute
    @XmlJavaTypeAdapter(InstantAdapter.class)
    private Instant start_t;
    @XmlAttribute
    @XmlJavaTypeAdapter(InstantAdapter.class)
    private Instant end_t;

    public failure_occurrence() {
    }

    public failure_occurrence(String sfeiName, failure.type failureType, activationVariable activationVariable, int part_id, Instant start_t) {
        super();
        this.sfeiName = sfeiName;
        this.failureType = failureType;
        this.actVariable = activationVariable;
        this.nPartsMoved_at_time = part_id;
        this.start_t = start_t;
    }

    public String getSfeiName() {
        return sfeiName;
    }

    public void setEnd_t(Instant end_t) {
        this.end_t = end_t;
    }

    public int getnPartsMoved_at_time() {
        return nPartsMoved_at_time;
    }

    public Instant getStart_t() {
        return start_t;
    }

    public Instant getEnd_t() {
        return end_t;
    }

    public activationVariable getActVariable() {
        return actVariable;
    }

    @Override
    public String toString() {
        return "[" + failureType + " activated by " + actVariable + " at " + start_t + " when item was moved " + nPartsMoved_at_time + " parts]";
    }
}
