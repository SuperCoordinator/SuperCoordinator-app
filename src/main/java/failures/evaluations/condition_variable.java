package failures.evaluations;

import failures.formulas.gaussFormula;
import failures.formulas.deterministicFormula;
import failures.formulas.probFormula;

public class condition_variable {

    private validation validation;

    public condition_variable(String condition, validation.method method) {
        initialize(condition, method);
    }

    public boolean evalFormula(int var) {
        return validation.validate(var);
    }

    private void initialize(String condition, validation.method method) {
        if (condition.contains("gauss")) {
            this.validation = new validation(newGaussFormula(condition), method);

        } else if (condition.contains("det")) {
            this.validation = new validation(newDeterministicFormula(condition), method);

        } else if (condition.contains("prob")) {
            this.validation = new validation(newProbFormula(condition), method);

        } else
            throw new RuntimeException("[condition_variable] initialize(): unknown prefix");
    }

    private gaussFormula newGaussFormula(String condition) {

        String mean = condition.substring(condition.indexOf('[') + 1, condition.indexOf(';'));
        String dev = condition.substring(condition.indexOf(';') + 1, condition.indexOf(']'));
        return new gaussFormula(removeSpaces(mean), removeSpaces(dev));
    }

    private deterministicFormula newDeterministicFormula(String condition) {
        String value = condition.substring(condition.indexOf('[') + 1, condition.indexOf(']'));
        return new deterministicFormula(removeSpaces(value));
    }

    private probFormula newProbFormula(String condition) {

        String value = condition.substring(condition.indexOf('[') + 1, condition.indexOf(']'));
        probFormula.signal signal = getProbFormulaSignal(condition);

        String[] members = condition.split(operator);

        String thres = members[1].substring(1);

        return new probFormula(removeSpaces(value),
                removeSpaces(thres),
                signal);
    }

    private int removeSpaces(String str) {
        if (str.charAt(0) == ' ')
            str = str.substring(1);
        if (str.charAt(str.length() - 1) == ' ')
            str = str.substring(0, str.length() - 1);

        return Integer.parseInt(str);
    }

    private String operator = "";

    private probFormula.signal getProbFormulaSignal(String condition) {
        probFormula.signal op = null;
        if (condition.contains("<=")) {
            operator = "<=";
            op = probFormula.signal.LESS_EQUAL;
        } else if (condition.contains(">=")) {
            operator = ">=";
            op = probFormula.signal.MORE_EQUAL;
        } else if (condition.contains(">")) {
            operator = ">";
            op = probFormula.signal.MORE;
        } else if (condition.contains("<")) {
            operator = "<";
            op = probFormula.signal.LESS;
        } else if (condition.contains("=")) {
            operator = "=";
            op = probFormula.signal.EQUAL;
        }
        return op;
    }
}
