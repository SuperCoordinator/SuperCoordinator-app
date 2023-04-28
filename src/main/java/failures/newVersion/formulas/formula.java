package failures.newVersion.formulas;

import java.util.Random;

public class formula {

    private int currentValue;

    public formula(int currentValue) {
        this.currentValue = currentValue;
    }
    public int getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(int currentValue) {
        this.currentValue = currentValue;
    }

}
