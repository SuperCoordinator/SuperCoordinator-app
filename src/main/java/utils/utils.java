package utils;

public class utils {

    private static final csv_reader reader = new csv_reader();
    private static final search search = new search();
    private static final logicalOperators logicalOp = new logicalOperators();

    public csv_reader getReader() {
        return reader;
    }

    public search getSearch() {
        return search;
    }

    public logicalOperators getLogicalOperator() {
        return logicalOp;
    }
}
