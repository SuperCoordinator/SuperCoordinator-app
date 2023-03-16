package failures.newVersion;

import java.time.Instant;

public class failure_occurrence {

    public enum activationVariable {
        N,
        A,
        M
    }

    private activationVariable actVariable;
    private failure2.type failureType;

    private int nPartsMoved_at_time;
    private Instant start_t;
    private Instant end_t;

    public failure_occurrence() {
    }

    public failure_occurrence(failure2.type failureType, activationVariable activationVariable, int part_id, Instant start_t) {
        super();
        this.failureType = failureType;
        this.actVariable = activationVariable;
        this.nPartsMoved_at_time = part_id;
        this.start_t = start_t;
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
}
