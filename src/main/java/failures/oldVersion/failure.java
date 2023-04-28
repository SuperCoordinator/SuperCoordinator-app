package failures.oldVersion;

import utility.*;

import java.util.Random;

public class failure {

    public enum type {
        BREAKDOWN,
        BREAKDOWN_WITH_REPAIR,
        PRODUCE_FAULTY,
        PRODUCE_MORE
    }

    private final type type;
    private final String formula;
    private final boolean isProbability;

    public failure(failure.type type, String formula) {
        this.type = type;
        this.formula = formula;

        if (formula.contains("prob"))
            this.isProbability = true;
        else
            this.isProbability = false;
    }

    public failure.type getType() {
        return type;
    }

    public boolean isProbability() {
        return isProbability;
    }

    public boolean evalFormula(int nParts, int age_inDays, int maintenance_inDays) {
        return eval_f(formula, nParts, age_inDays, maintenance_inDays);
    }

    public boolean evalGivenFormula(String given_f, int nParts, int age_inDays, int maintenance_inDays) {
        return eval_f(given_f, nParts, age_inDays, maintenance_inDays);
    }

    private boolean eval_f(String f, int nParts, int age_inDays, int maintenance_inDays) {

        if (isProbability) {
            return eval_prob(f, nParts, age_inDays, maintenance_inDays);
        }

        String[] members;
        String op;
        if (f.contains(" > ")) {
            op = "> ";
        } else if (f.contains(" < ")) {
            op = "< ";
        } else if (f.contains(" = ")) {
            op = "= ";
        } else if (f.contains(" <= ")) {
            op = "<= ";
        } else if (f.contains(" >= ")) {
            op = ">= ";
        } else {
            return false;
        }
        members = f.split(op);
        String[] operator = op.split(" ");
        double leftMember = utils.getInstance().getCustomCalculator().calcExpression(
                members[0],
                nParts,
                age_inDays,
                maintenance_inDays);
        double rightMember = utils.getInstance().getCustomCalculator().calcExpression(
                members[1],
                nParts,
                age_inDays,
                maintenance_inDays);

        return switch (operator[0]) {
            case ">" -> leftMember > rightMember;
            case "<" -> leftMember < rightMember;
            case "=" -> leftMember == rightMember;
            case "<=" -> leftMember <= rightMember;
            case ">=" -> leftMember >= rightMember;
            default -> false;
        };
    }

    private boolean eval_prob(String f, int nParts, int age_inDays, int maintenance_inDays) {

        String[] members;
        String op;
        if (f.contains(" > ")) {
            op = "> ";
        } else if (f.contains(" < ")) {
            op = "< ";
        } else if (f.contains(" = ")) {
            op = "= ";
        } else if (f.contains(" <= ")) {
            op = "<= ";
        } else if (f.contains(" >= ")) {
            op = ">= ";
        } else {
            return false;
        }
        members = f.split(op);
        String[] operator = op.split(" ");
        boolean leftMember = members[0].contains("prob");
        double val = utils.getInstance().getCustomCalculator().calcExpression(
                leftMember ? members[1] : members[0],
                nParts,
                age_inDays,
                maintenance_inDays);

        int prob = utils.getInstance().getRandom().nextInt(1, 101);
        System.out.println("prob : " + prob);
        return switch (operator[0]) {
            case ">" -> leftMember ? prob > val : val > prob;
            case "<" -> leftMember ? prob < val : val < prob;
            case "=" -> prob == val;
            case "<=" -> leftMember ? prob <= val : val <= prob;
            case ">=" -> leftMember ? prob >= val : val >= prob;
            default -> false;
        };
    }

}
