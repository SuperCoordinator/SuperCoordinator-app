package monitor;

import java.time.Duration;
import java.time.Instant;

public class timestamp_pair {

    private final Instant[] pair;

    private Duration duration;

    public timestamp_pair(Instant firstValue) {
        this.pair = new Instant[2];
        this.pair[0] = firstValue;
    }

    public Instant[] getPair() {
        return pair;
    }

    public void setSecondValue(Instant secondValue) {
        this.pair[1] = secondValue;
        this.duration = Duration.between(pair[0], pair[1]);
    }

    public Duration getDuration() {
        return duration;
    }


}