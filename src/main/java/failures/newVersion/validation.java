package failures.newVersion;

import java.time.Duration;
import java.time.Instant;

public class validation {

    public enum method {
        EVENT,
        TIME
    }

    private final method validationMethod;

    private enum formulaType {
        GAUSSIAN,
        LINEAR,
        PROBABILITY
    }

    private final formulaType formulaPrefix;

    private final gaussFormula gaussFormula;
    private final linearFormula linearFormula;
    private final probFormula probFormula;


    public validation(gaussFormula gaussFormula, method validationMethod) {
        this.validationMethod = validationMethod;
        this.gaussFormula = gaussFormula;
        this.formulaPrefix = formulaType.GAUSSIAN;

        this.linearFormula = new linearFormula();
        this.probFormula = new probFormula();
    }

    public validation(linearFormula linearFormula, method validationMethod) {
        this.validationMethod = validationMethod;
        this.linearFormula = linearFormula;
        this.formulaPrefix = formulaType.LINEAR;

        this.gaussFormula = new gaussFormula();
        this.probFormula = new probFormula();
    }

    public validation(probFormula probFormula, method validationMethod) {
        this.validationMethod = validationMethod;
        this.probFormula = probFormula;
        this.formulaPrefix = formulaType.PROBABILITY;

        this.gaussFormula = new gaussFormula();
        this.linearFormula = new linearFormula();
    }


    private int old_var = -1;
    private Instant start_t = Instant.now();

    public boolean validate(int var) {

        boolean res = false;
        if ((old_var != var && validationMethod.equals(method.EVENT)) ||
                (Duration.between(start_t, Instant.now()).toMinutes() >= 1 && validationMethod.equals(method.TIME))) {

            if (formulaPrefix.equals(formulaType.GAUSSIAN)) {
                System.out.println(formulaPrefix + "currVal: " + gaussFormula.getCurrentValue() + " var: " + var);
                res = gaussFormula.getCurrentValue() == var;
                if (res) {
                    gaussFormula.setNextValue();
                    System.out.println("Activated by " + validationMethod + ": " + formulaPrefix);
                }
            } else if (formulaPrefix.equals(formulaType.LINEAR)) {

                // Verification of linear formula
                res = linearFormula.getCurrentValue() == var;
                if (res)
                    System.out.println("Activated by " + validationMethod + ": " + formulaPrefix);

            } else if (formulaPrefix.equals(formulaType.PROBABILITY)) {
                // verification of prob formula - 2 steps
                //    Verificate the condition, that is, the variable with the threshold
                //    IF true -> return the currentValue and calculate another one
                //    ELSE -> return false
                if (var == probFormula.getCurrentValue()) {
                    res = probFormula.getState();
                    if (res)
                        System.out.println("Activated by " + validationMethod + ": " + formulaPrefix);
                }
            }

            if (validationMethod.equals(method.TIME))
                start_t = Instant.now();

        }
        if (validationMethod.equals(method.EVENT))
            old_var = var;

        return res;

    }


}
