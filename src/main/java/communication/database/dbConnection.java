package communication.database;

import com.sun.xml.txw2.annotation.XmlAttribute;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class dbConnection {

    private static Connection con = null;
    @XmlElement
    private static String url = "jdbc:mysql://localhost:3306/supercoordinator";
    @XmlElement
    private static String user = "root";
    @XmlElement
    private static String pass = "";

/*    static {

        try {
//            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(url, user, pass);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    public dbConnection() {
    }

    public static Connection getConnection() {

        try {
            if (con == null) {
                return DriverManager.getConnection(url, user, pass);
            }
            return con;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
