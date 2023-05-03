package communication.database;

import communication.database.mediators.M_inbound_orders;
import communication.database.mediators.M_sf_configuration;
import communication.database.mediators.queries_buffer;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class dbConnection implements Runnable {

    private Connection con = null;
    @XmlElement
    private String url = "jdbc:mysql://localhost:3306/";
    @XmlElement
    private String database = "";
    @XmlElement
    private String user = "root";
    @XmlElement
    private String pass = "";

    public dbConnection() {
    }

    public static dbConnection getInstance() {
        return dbConnection.dbConnectionHolder.INSTANCE;
    }


    private static class dbConnectionHolder {
        private static final dbConnection INSTANCE = new dbConnection();
    }


    public void setDatabase(String database) {
        try {
            this.database = database;
            DriverManager.getConnection(url + database, user, pass);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Connection getConnection() {
        try {
            if (con == null) {
                return DriverManager.getConnection(url + database, user, pass);
            }
            return con;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private M_sf_configuration sf_configuration = new M_sf_configuration();

    public M_sf_configuration getSf_configuration() {
        return sf_configuration;
    }

    private M_inbound_orders inbound_orders = new M_inbound_orders();

    public M_inbound_orders getInbound_orders() {
        return inbound_orders;
    }

    private boolean firstRun = true;
    private ArrayList<queries_buffer> tables = new ArrayList<>();

    @Override
    public void run() {
        if (firstRun) {
            tables.add(sf_configuration);
            tables.add(inbound_orders);

            firstRun = false;
        }
        for (queries_buffer buffer : tables) {
            buffer.runQueries(con);
        }
    }

}
