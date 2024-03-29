package utility.serialize;

import communication.database.dbConnection;
import controllers.production.cSFEM_production;
import controllers.transport.cSFEM_transport;
import controllers.warehouse.cSFEM_warehouse;
import utility.utils;


import javax.xml.bind.annotation.*;
import java.util.ArrayList;

@XmlRootElement(name = "config")
@XmlAccessorType(XmlAccessType.NONE)
public class serializable {
    private cSFEM_warehouse C_Warehouse = new cSFEM_warehouse();
    private ArrayList<cSFEM_production> C_Production = new ArrayList<>();
    private ArrayList<cSFEM_transport> C_Transport = new ArrayList<>();

    @XmlElement(name = "failureOccurrencesPath")
    private String failuresHistoryPath;
    @XmlElement(name = "inboundOrdersPath")
    private String inboundOrdersPath;
    @XmlElement(name = "databasePath")
    private String databasePath;
    @XmlElement(name ="randomSeed")
    private long randSeed;
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

    @XmlElement
    public dbConnection getConnection() {
        return dbConnection.getInstance();
    }

    public void setFailuresHistoryPath(String failuresHistoryPath) {
        this.failuresHistoryPath = failuresHistoryPath;
    }

    public String getFailuresHistoryPath() {
        return failuresHistoryPath;
    }

    public String getInboundOrdersPath() {
        return inboundOrdersPath;
    }

    public void setInboundOrdersPath(String inboundOrdersPath) {
        this.inboundOrdersPath = inboundOrdersPath;
    }

    public String getDatabasePath() {
        return databasePath;
    }

    public void setDatabasePath(String databasePath) {
        this.databasePath = databasePath;
    }

    public long getRandSeed() {
        randSeed = utils.getInstance().getRnd_seed();
        return randSeed;
    }

    public void setRandSeed(long randSeed) {
        this.randSeed = randSeed;
    }
}
