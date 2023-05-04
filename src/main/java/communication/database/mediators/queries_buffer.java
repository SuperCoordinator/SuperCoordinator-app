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

    public void runQueries(Connection con, boolean print) {
        try {
            Iterator<String> iterator = storedQueries.iterator();
            while (iterator.hasNext()) {
                String query = iterator.next();
                int affectedRows = con.prepareStatement(query).executeUpdate();
                if (affectedRows > 0) {
                    // Query successfully executed
                    iterator.remove();
                } else {
                    System.out.println("Affected 0 rows!");
                    System.out.println(query);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
