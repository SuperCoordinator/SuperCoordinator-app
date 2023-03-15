package failures.newVersion;

public class failures_conditions {

    private condition_variable nCondition; // N of parts
    private condition_variable aCondition; // Age of the equipment
    private condition_variable mCondition; // Time since last maintenance


    public failures_conditions(String[] conditions) {
        this.nCondition = new condition_variable(conditions[0], condition_variable.validationMethod.EVENT);
        this.aCondition = new condition_variable(conditions[1], condition_variable.validationMethod.TIME);
        this.mCondition = new condition_variable(conditions[2], condition_variable.validationMethod.TIME);
    }

    public boolean evalConditions(int n, int a, int m) {
        return nCondition.evalFormula(n) || aCondition.evalFormula(a) || mCondition.evalFormula(m);
    }
}
