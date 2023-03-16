package failures.newVersion;

import utils.utils;

import java.util.Random;

public class failure2 {

    public enum type {
        BREAKDOWN,
        BREAKDOWN_WITH_REPAIR,
        PRODUCE_FAULTY,
        PRODUCE_MORE
    }

    private final type type;

    public failure2(failure2.type type) {
        this.type = type;

    }
    public failure2.type getType() {
        return type;
    }


}
