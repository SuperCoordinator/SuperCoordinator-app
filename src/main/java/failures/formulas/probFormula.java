package failures.formulas;

import utility.utils;

public class probFormula extends formula {

    public enum signal {
        LESS,
        LESS_EQUAL,
        MORE,
        MORE_EQUAL,
        EQUAL
    }

    private int thres;
    private signal signal;

    private boolean state;

    public probFormula() {
        super(-1);
    }

    public probFormula(int value, int thres, signal signal) {
        super(value);
        this.thres = thres;
        this.signal = signal;

        this.state = false;
        nextState();
    }

    public boolean getState() {
        boolean old_state = state;
        nextState();
        return old_state;
    }

    private void nextState() {
        int n = utils.getInstance().getRandom().nextInt(1, 100);

        switch (signal) {
            case LESS -> state = n < thres;
            case LESS_EQUAL -> state = n <= thres;
            case MORE -> state = n > thres;
            case MORE_EQUAL -> state = n >= thres;
            case EQUAL -> state = n == thres;
        }

//        System.out.println("PROB currentValue: " + state + " n: " + n + signal + " " + thres);

    }
}
