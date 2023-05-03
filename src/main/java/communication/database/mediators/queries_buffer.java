package communication.database.mediators;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class queries_buffer {

    private ArrayList<String[]> storedQueries = new ArrayList<>();

    public queries_buffer() {
    }

    public ArrayList<String[]> getStoredQueries() {
        return storedQueries;
    }

    public void runQueries(Connection con) {
        try {
            for (String[] query : storedQueries) {
                Statement st = con.createStatement();
                for (String subQuery : query) {
                    st.addBatch(subQuery);
                }
                for (int val : st.executeBatch()) {
                    if (val < 0)
                        throw new RuntimeException("SQL error");
                }
                storedQueries.clear();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
