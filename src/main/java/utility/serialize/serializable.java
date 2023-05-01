package utility.serialize;

import communication.database.dbConnection;
import controllers.production.cSFEM_production;
import controllers.transport.cSFEM_transport;
import controllers.warehouse.cSFEM_warehouse;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class serializable {

    private dbConnection connection = new dbConnection();
    private cSFEM_warehouse C_Warehouse = new cSFEM_warehouse();
    private ArrayList<cSFEM_production> C_Production = new ArrayList<>();
    private ArrayList<cSFEM_transport> C_Transport = new ArrayList<>();

    public serializable() {
    }

    public void setC_Warehouse(cSFEM_warehouse c_Warehouse) {
        C_Warehouse = c_Warehouse;
    }

    @XmlElement
    public cSFEM_warehouse getC_Warehouse() {
        return C_Warehouse;
    }

    public void setC_Production(ArrayList<cSFEM_production> c_Production) {
        C_Production = c_Production;
    }

    @XmlElement
    public ArrayList<cSFEM_production> getC_Production() {
        return C_Production;
    }

    public void setC_Transport(ArrayList<cSFEM_transport> c_Transport) {
        C_Transport = c_Transport;
    }

    @XmlElement
    public ArrayList<cSFEM_transport> getC_Transport() {
        return C_Transport;
    }

    public void setConnection(dbConnection connection) {
        this.connection = connection;
    }

    @XmlElement
    public dbConnection getConnection() {
        return connection;
    }

}
