package failures.newVersion;

import java.time.Duration;
import java.time.Instant;

public class timeValidation {

    private enum formulaType {
        GAUSSIAN,
        LINEAR,
        PROBABILITY
    }

    private final formulaType formulaPrefix;

    private final gaussFormula gaussFormula;
    private final linearFormula linearFormula;
    private final probFormula probFormula;

    public timeValidation(gaussFormula gaussFormula) {
        this.gaussFormula = gaussFormula;
        this.formulaPrefix = formulaType.GAUSSIAN;

        this.linearFormula = new linearFormula();
        this.probFormula = new probFormula();
    }

    public timeValidation(linearFormula linearFormula) {
        this.linearFormula = linearFormula;
        this.formulaPrefix = formulaType.LINEAR;

        this.gaussFormula = new gaussFormula();
        this.probFormula = new probFormula();
    }

    public timeValidation(probFormula probFormula) {
        this.probFormula = probFormula;
        this.formulaPrefix = formulaType.PROBABILITY;

        this.gaussFormula = new gaussFormula();
        this.linearFormula = new linearFormula();
    }

    private Instant start_t = Instant.now();

    public boolean validation(int var) {
        boolean res = false;
        if (Duration.between(start_t, Instant.now()).toMinutes() >= 1) {

            if (formulaPrefix.equals(formulaType.GAUSSIAN)) {
                res = gaussFormula.getCurrentValue() == var;
                if (res) {
                    gaussFormula.setNextValue();
                    System.out.println("Activated by timeValidation: " + formulaPrefix);
                }
            } else if (formulaPrefix.equals(formulaType.LINEAR)) {

                // Verification of linear formula
                res = linearFormula.getCurrentValue() == var;
                if (res)
                    System.out.println("Activated by timeValidation: " + formulaPrefix);

            } else if (formulaPrefix.equals(formulaType.PROBABILITY)) {
                // verification of prob formula - 2 steps
                //    Verificate the condition, that is, the variable with the threshold
                //    IF true -> return the currentValue and calculate another one
                //    ELSE -> return false
                if (var == probFormula.getCurrentValue()) {
                    res = probFormula.getState();
                    if (res)
                        System.out.println("Activated by timeValidation: " + formulaPrefix);
                }
            }
            start_t = Instant.now();
        }
        return res;
    }
}
