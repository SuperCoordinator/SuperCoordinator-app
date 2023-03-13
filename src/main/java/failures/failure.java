package failures;

import models.SFEI.SFEI_conveyor;
import models.SFEI.SFEI_machine;
import utils.*;

import java.time.Duration;
import java.time.Instant;

public class failure {

    public enum type {
        BREAKDOWN,
        BREAKDOWN_WITH_REPAIR,
        PRODUCE_FAULTY,
        PRODUCE_MORE
    }

    private final type type;
    private final String formula;
    private final utils utility;

    public failure(failure.type type, String formula) {
        this.type = type;
        this.formula = formula;

        this.utility = new utils();

    }

    public failure.type getType() {
        return type;
    }

    public utils getUtility() {
        return utility;
    }

    public boolean evalFormula(int nParts, int age_inDays, int maintenance_inDays) {
        return eval_f(formula, nParts, age_inDays, maintenance_inDays);
    }

    public boolean evalGivenFormula(String given_f, int nParts, int age_inDays, int maintenance_inDays) {
        return eval_f(given_f, nParts, age_inDays, maintenance_inDays);
    }

    private boolean eval_f(String f, int nParts, int age_inDays, int maintenance_inDays) {
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
        double leftMember = utility.getCustomCalc().calcExpression(
                members[0],
                nParts,
                age_inDays,
                maintenance_inDays);
        double rightMember = utility.getCustomCalc().calcExpression(
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


}
