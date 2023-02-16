package utils;

public class logicalOperators {

    public boolean RE_detector(boolean now, boolean before) {
        return !before && now;
    }

    public boolean FE_detector(boolean now, boolean before) {
        return before && !now;
    }
}
