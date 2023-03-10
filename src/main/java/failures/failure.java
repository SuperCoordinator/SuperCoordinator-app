package failures;

public class failure {

    public enum type {
        BREAKDOWN,
        BREAKDOWN_WITH_REPAIR,
        PRODUCE_FAULTY,
        PRODUCE_MORE
    }

    private final type type;
    private final String formula;

    private final int idx_SFEI;

    public failure(failure.type type, String formula) {
        this.type = type;
        this.formula = formula;

        this.idx_SFEI = getSFEI_idx();
    }

    public failure.type getType() {
        return type;
    }

    public String getFormula() {
        return formula;
    }

    public int getIdx_SFEI() {
        return idx_SFEI;
    }

    private int getSFEI_idx() {
        return 0;
    }
}
