package utility.serialize;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import failures.evaluations.failure_occurrence;

import java.util.ArrayList;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class FailureOccurrenceArray {

    private final ArrayList<failure_occurrence> failuresOccurrences;

    public FailureOccurrenceArray() {
        this.failuresOccurrences = new ArrayList<>();
    }

    @XmlElement
    public ArrayList<failure_occurrence> getFailuresOccurrences() {
        return failuresOccurrences;
    }
}
