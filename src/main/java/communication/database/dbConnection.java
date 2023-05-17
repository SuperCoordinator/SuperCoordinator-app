package communication.database;

import communication.database.mediators.*;
import org.apache.ibatis.jdbc.ScriptRunner;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

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
            this.con.setCatalog(database);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized Connection getConnection() {
        try {

            if (con == null || con.isClosed()) {
                con = DriverManager.getConnection(url, user, pass);
                con.setAutoCommit(true);
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

    private M_outbound_orders outbound_orders = new M_outbound_orders();

    public M_outbound_orders getOutbound_orders() {
        return outbound_orders;
    }

    private M_part parts = new M_part();

    public M_part getParts() {
        return parts;
    }

    private M_production_history production_history = new M_production_history();

    public M_production_history getProduction_history() {
        return production_history;
    }

    private M_sfem sfems = new M_sfem();

    public M_sfem getSfems() {
        return sfems;
    }

    private M_sfee sfees = new M_sfee();

    public M_sfee getSfees() {
        return sfees;
    }

    private M_sfei sfeis = new M_sfei();

    public M_sfei getSfeis() {
        return sfeis;
    }

    private M_sensor sensors = new M_sensor();

    public M_sensor getSensors() {
        return sensors;
    }

    private boolean firstRun = true;
    private ArrayList<queries_buffer> tables = new ArrayList<>();
    private int table_idx = 0;

//    public void initializeDB() {
//        // The order is important, mainly in the first execution as the static tables (until sensors, including)
//        tables.add(sf_configuration);
//        tables.add(sfems);
//        tables.add(sfees);
//        tables.add(sfeis);
//        tables.add(sensors);
//        tables.add(inbound_orders);
//        tables.add(outbound_orders);
//        tables.add(parts);
//        tables.add(production_history);
//
//        for (queries_buffer buffer : tables) {
//            if (buffer.getStoredQueries().size() > 0) {
//                buffer.runQueries(getConnection());
//            }
//        }
//
//    }

    @Override
    public void run() {
        try {
            if (firstRun) {
                // The order is important, mainly in the first execution as the static tables (until sensors, including)
                tables.add(sf_configuration);
                tables.add(sfems);
                tables.add(sfees);
                tables.add(sfeis);
                tables.add(sensors);
                tables.add(inbound_orders);
                tables.add(outbound_orders);
                tables.add(parts);
                tables.add(production_history);
                firstRun = false;
            }

            int i = 0;
//            System.out.print("buffer sizes: ");
            for (queries_buffer buffer : tables) {
//                System.out.print("[" + i + "]" + buffer.getStoredQueries().size() + ", ");
                if (buffer.getStoredQueries().size() > 0) {
//                    if (i == 7) {
//                        for (String str_vec : buffer.getStoredQueries()) {
//                            System.out.println(str_vec);
//                        }
//                    }
                    buffer.runQueries(con);
                }
                i++;
            }

        } catch (Exception e) {
            // In child thread, it must print the Exception because the main thread do not catch Runtime Exception from the others
            e.printStackTrace();
        }
    }

}
