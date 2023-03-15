package failures.newVersion;

public class eventsValidation {

    private enum formulaType {
        GAUSSIAN,
        LINEAR,
        PROBABILITY
    }

    private final formulaType formulaPrefix;

    private gaussFormula gaussFormula;
    private final linearFormula linearFormula;
    private final probFormula probFormula;

    public eventsValidation(gaussFormula gaussFormula) {
        this.gaussFormula = gaussFormula;
        this.formulaPrefix = formulaType.GAUSSIAN;

        this.linearFormula = new linearFormula();
        this.probFormula = new probFormula();
    }

    public eventsValidation(linearFormula linearFormula) {
        this.linearFormula = linearFormula;
        this.formulaPrefix = formulaType.LINEAR;

        this.gaussFormula = new gaussFormula();
        this.probFormula = new probFormula();
    }

    public eventsValidation(probFormula probFormula) {
        this.probFormula = probFormula;
        this.formulaPrefix = formulaType.PROBABILITY;

        this.gaussFormula = new gaussFormula();
        this.linearFormula = new linearFormula();
    }

    private int old_var = -1;

    public boolean validation(int var) {
        boolean res = false;
        if (old_var != var) {

            if (formulaPrefix.equals(formulaType.GAUSSIAN)) {
                res = gaussFormula.getCurrentValue() == var;
                if (res) {
                    gaussFormula.setNextValue();
                    System.out.println("Activated by eventValidation: " + formulaPrefix);
                }
            } else if (formulaPrefix.equals(formulaType.LINEAR)) {

                // Verification of linear formula
                res = linearFormula.getCurrentValue() == var;
                if (res)
                    System.out.println("Activated by eventValidation: " + formulaPrefix);

            } else if (formulaPrefix.equals(formulaType.PROBABILITY)) {
                // verification of prob formula - 2 steps
                //    Verificate the condition, that is, the variable with the threshold
                //    IF true -> return the currentValue and calculate another one
                //    ELSE -> return false
                if (var == probFormula.getCurrentValue()) {
                    res = probFormula.getState();
                    if (res)
                        System.out.println("Activated by eventValidation: " + formulaPrefix);
                }
            }
        }

        old_var = var;
        return res;

    }
}
