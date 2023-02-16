package models;

import java.time.Instant;

public class missingParts extends part {


    private Instant t_init;
    /**
     * 2s- Minimum time for the remover/emitter pair do not accelerate the part movement
     */
    private int delay;
    private boolean disappear;

    public missingParts(part part, int delay, Instant t_init, boolean disappear) {
        super(part.getId());
        this.delay = delay;
        this.t_init = t_init;
        this.disappear = disappear;
    }

    public missingParts(part part, int delay, Instant t_init) {
        super(part.getId());
        this.delay = delay;
        this.t_init = t_init;
        this.disappear = false;
    }

    public missingParts(part part, Instant t_init) {
        super(part.getId());
        this.delay = 2;
        this.t_init = t_init;
        this.disappear = false;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public boolean isDisappear() {
        return disappear;
    }

    public void setDisappear(boolean disappear) {
        this.disappear = disappear;
    }

    public Instant getT_init() {
        return t_init;
    }

    public void setT_init(Instant t_init) {
        this.t_init = t_init;
    }
}
