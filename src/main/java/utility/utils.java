package utility;

public class utils {

    private static final csv_reader reader = new csv_reader();
    private static final search search = new search();
    private static final logicalOperators logicalOp = new logicalOperators();

    private static final customCalculator customCalc = new customCalculator();

    public csv_reader getReader() {
        return reader;
    }

    public search getSearch() {
        return search;
    }

    public logicalOperators getLogicalOperator() {
        return logicalOp;
    }

    public customCalculator getCustomCalculator() {
        return customCalc;
    }

    public utils() {
    }

    public static utils getInstance() {
        return utils.utilsHolder.INSTANCE;
    }


    private static class utilsHolder {
        private static final utils INSTANCE = new utils();
    }
}
