package communication.database.mediators;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class queries_buffer {

    private ArrayList<String> storedQueries = new ArrayList<>();

    public queries_buffer() {
    }

    public synchronized ArrayList<String> getStoredQueries() {
        return storedQueries;
    }

    public void runQueries(Connection con) {
        try {
            Iterator<String> iterator = storedQueries.iterator();
            Statement statement = con.createStatement();
            while (iterator.hasNext()) {
                statement.addBatch(iterator.next());
            }
            int[] affectedRows = statement.executeBatch();
            con.commit();
            for (int i = 0; i < affectedRows.length; i++) {
                if (affectedRows[i] > 0) {
                    storedQueries.set(i, "");
                } else {
                    System.out.println("Affected 0 rows!");
                    System.out.println(storedQueries.get(i));
                }
            }
            storedQueries.removeIf(String::isEmpty);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
