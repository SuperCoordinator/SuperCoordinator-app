package communication.database.mediators;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

public class queries_buffer {

    private final CopyOnWriteArrayList<String> storedQueries = new CopyOnWriteArrayList<>();

    public queries_buffer() {
    }

    public synchronized CopyOnWriteArrayList<String> getStoredQueries() {
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
