package viewers.controllers.SFEM;

import controllers.transport.cSFEM_transport;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import viewers.controllers.C_ShopFloor;
import viewers.controllers.SFEE.C_SFEE_transport;
import viewers.drawables.MyRectangle;
import viewers.drawables.connectionLines.boundLine;
import viewers.mediators.CM_SFEM_transport;

public class C_SFEM_transport extends CM_SFEM_transport {

    private String sfemName;
    private cSFEM_transport cSFEMTransport;

    private String inSFEI, outSFEI;


    public C_SFEM_transport() {
    }

    public C_SFEM_transport(String sfemName) {
        this.sfemName = sfemName;
    }

    public C_SFEM_transport(String sfemName, String inSFEI, String outSFEI) {
        this.sfemName = sfemName;
        this.inSFEI = inSFEI;
        this.outSFEI = outSFEI;
    }

    public C_SFEM_transport(cSFEM_transport cSfemTransport) {
        this.cSFEMTransport = cSfemTransport;
        this.sfemName = cSfemTransport.getSfem().getName();
    }


    public String getSfemName() {
        return sfemName;
    }

    public cSFEM_transport getcSFEMTransport() {
        return cSFEMTransport;
    }

    public void setSfemName(String sfemName) {
        this.sfemName = sfemName;
        cSFEMTransport.getSfem().setName(sfemName);
    }

    @FXML
    private Pane sfee_transport_pane;

    public void initialize() {
        if (!C_ShopFloor.getInstance().isLoadedConfig()) {
            C_SFEE_transport cSfeeTransport = new C_SFEE_transport(inSFEI, outSFEI);
            registerC_SFEE_transport(cSfeeTransport);
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SFEE/SFEE_transport.fxml"));
                loader.setController(cSfeeTransport);
                AnchorPane pane = loader.load();
                sfee_transport_pane.getChildren().setAll(pane);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {

            // load configuration
            C_SFEE_transport cSfeeTransport = new C_SFEE_transport();
            cSfeeTransport.setcSFEETransport(cSFEMTransport.getSfeeTransportController());
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SFEE/SFEE_transport.fxml"));
                loader.setController(cSfeeTransport);
                AnchorPane pane = loader.load();
                sfee_transport_pane.getChildren().setAll(pane);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    private String errorMsg = "";

    public boolean validateMoveOn() {
        boolean error = false;
        if (!getcSfeeTransport().validateMoveOn()) {
            error = true;
            errorMsg = errorMsg.concat(getcSfeeTransport().getErrorMsg());
        }

        return !error;
    }

    public String getErrorMsg() {
        return errorMsg;
    }
}
