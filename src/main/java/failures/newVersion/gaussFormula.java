package failures.newVersion;

import java.util.ArrayList;
import java.util.Random;

public class gaussFormula extends formula {

    private int mean;
    private int dev;

    public gaussFormula() {
        super(-1);
    }

    public gaussFormula(int mean, int dev) {
        super(0);
        this.mean = mean;
        this.dev = dev;
        init();
        setNextValue();
    }

    private final ArrayList<Double> array = new ArrayList<>();

    private void init() {
        for (int i = 0; i < 1024 * 2; i++) {
            double gauss = getRandom().nextGaussian() * dev + mean;
            double r = (double) Math.round(gauss);
            array.add(r);
        }
    }

    private int array_index = -1;

    public void setNextValue() {
        if (array_index < array.size())
            array_index++;
        else
            array_index = 0;

        System.out.println("GAUSS currentValue: " + array.get(array_index).intValue());
        setCurrentValue(array.get(array_index).intValue());
    }


}
