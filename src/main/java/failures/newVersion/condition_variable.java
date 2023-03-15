package failures.newVersion;

public class condition_variable {

    public enum validationMethod {
        EVENT,
        TIME
    }

    private final validationMethod method;
    private eventsValidation eventsValidation;
    private timeValidation timeValidation;

    public condition_variable(String condition, validationMethod method) {
        this.method = method;
        initialize(condition);

/*        this.eventsValidation = new eventsValidation(gaussFormula);
        this.timeValidation = new timeValidation(linearFormula, probFormula);*/
    }

    public boolean evalFormula(int var) {
        if (method.equals(validationMethod.EVENT))
            return eventsValidation.validation(var);
        return timeValidation.validation(var);
    }

    private void initialize(String condition) {
        if (condition.contains("gauss")) {
            if (method.equals(validationMethod.EVENT))
                this.eventsValidation = new eventsValidation(newGaussFormula(condition));
            else
                this.timeValidation = new timeValidation(newGaussFormula(condition));

        } else if (condition.contains("linear")) {
            if (method.equals(validationMethod.EVENT))
                this.eventsValidation = new eventsValidation(newLinearFormula(condition));
            else
                this.timeValidation = new timeValidation(newLinearFormula(condition));

        } else if (condition.contains("prob")) {
            if (method.equals(validationMethod.EVENT))
                this.eventsValidation = new eventsValidation(newProbFormula(condition));
            else
                this.timeValidation = new timeValidation(newProbFormula(condition));

        } else
            throw new RuntimeException("[condition_variable] initialize(): unknown prefix");
    }

    private gaussFormula newGaussFormula(String condition) {

        String mean = condition.substring(condition.indexOf('[') + 1, condition.indexOf(';'));
        String dev = condition.substring(condition.indexOf(';') + 1, condition.indexOf(']'));
        return new gaussFormula(removeSpaces(mean), removeSpaces(dev));
    }

    private linearFormula newLinearFormula(String condition) {
        String value = condition.substring(condition.indexOf('[') + 1, condition.indexOf(']'));
        return new linearFormula(removeSpaces(value));
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
        if (condition.contains(">")) {
            operator = ">";
            op = probFormula.signal.MORE;
        } else if (condition.contains("<")) {
            operator = "<";
            op = probFormula.signal.LESS;
        } else if (condition.contains("=")) {
            operator = "=";
            op = probFormula.signal.EQUAL;
        } else if (condition.contains("<=")) {
            operator = "<=";
            op = probFormula.signal.LESS_EQUAL;
        } else if (condition.contains(">=")) {
            operator = ">=";
            op = probFormula.signal.MORE_EQUAL;
        }
        return op;
    }
}
