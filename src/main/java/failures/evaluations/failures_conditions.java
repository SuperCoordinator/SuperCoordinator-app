package failures.evaluations;

public class failures_conditions extends failure {

    private condition_variable nCondition; // N of parts
    private condition_variable aCondition; // Age of the equipment
    private condition_variable mCondition; // Time since last maintenance


    public failures_conditions(String[] conditions, failure.type type) {
        super(type);
        this.nCondition = conditions[0].equalsIgnoreCase("no") ? null : new condition_variable(conditions[0], validation.method.EVENT);
        this.aCondition = conditions[1].equalsIgnoreCase("no") ? null : new condition_variable(conditions[1], validation.method.TIME);
        this.mCondition = conditions[2].equalsIgnoreCase("no") ? null : new condition_variable(conditions[2], validation.method.TIME);
    }

    private boolean activated_by_N = false, activated_by_A = false, activated_by_M = false;

    public boolean evalConditions(int n, int a, int m) {
        activated_by_N = false;
        activated_by_A = false;
        activated_by_M = false;

        if (nCondition != null) activated_by_N = nCondition.evalFormula(n);
        if (aCondition != null) activated_by_A = aCondition.evalFormula(a);
        if (mCondition != null) activated_by_M = mCondition.evalFormula(m);

//        if (activated_by_N || activated_by_A || activated_by_M)
//            System.out.println("Failure activated by variable n:" + activated_by_N + " a:" + activated_by_A + " m:" + activated_by_M);

        return activated_by_N || activated_by_A || activated_by_M;
    }

    public boolean wasActivated_by_N() {
        if (activated_by_N) {
            activated_by_N = false;
            return true;
        }
        return false;
    }

    public boolean wasActivated_by_A() {
        if (activated_by_A) {
            activated_by_A = false;
            return true;
        }
        return false;
    }

    public boolean wasActivated_by_M() {
        if (activated_by_M) {
            activated_by_M = false;
            return true;
        }
        return false;
    }

    public condition_variable getnCondition() {
        return nCondition;
    }

    public condition_variable getaCondition() {
        return aCondition;
    }

    public condition_variable getmCondition() {
        return mCondition;
    }
}
