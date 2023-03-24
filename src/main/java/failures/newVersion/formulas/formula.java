package failures.newVersion.formulas;

import java.util.Random;

public class formula {

    private int currentValue;
    private final Random random = new Random();

    public formula(int currentValue) {
        this.currentValue = currentValue;
        random.setSeed(3587214);
    }

    public int getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(int currentValue) {
        this.currentValue = currentValue;
    }

    public Random getRandom() {
        return random;
    }
}
