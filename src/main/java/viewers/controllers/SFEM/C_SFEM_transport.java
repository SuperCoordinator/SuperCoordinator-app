package viewers.controllers.SFEM;

import controllers.transport.cSFEM_transport;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import viewers.controllers.C_ShopFloor;
import viewers.controllers.SFEE.transport.C_SFEE_transport;
import viewers.mediators.CM_SFEM_transport;

public class C_SFEM_transport extends CM_SFEM_transport {

    private String sfemName;
    private cSFEM_transport cSFEMTransport;
    private String inSFEI, outSFEI;

//    private boolean editMode;

    public C_SFEM_transport() {
    }

    public C_SFEM_transport(String sfemName, String inSFEI, String outSFEI) {
        this.sfemName = sfemName;
        this.inSFEI = inSFEI;
        this.outSFEI = outSFEI;
    }

    public C_SFEM_transport(cSFEM_transport cSfemTransport) {
        this.cSFEMTransport = cSfemTransport;
        this.sfemName = cSfemTransport.getSfem().getName();
        loadData();
    }

    private void loadData() {
        C_SFEE_transport cSfeeTransport = new C_SFEE_transport(cSFEMTransport.getSfeeTransportController().getPrevSFEI_name(), cSFEMTransport.getSfeeTransportController().getNextSFEI_name());
        cSfeeTransport.setcSFEETransport(cSFEMTransport.getSfeeTransportController());
        registerC_SFEE_transport(cSfeeTransport);
    }


    public String getSfemName() {
        return sfemName;
    }

    public cSFEM_transport getcSFEMTransport() {
        return cSFEMTransport;
    }

    public void setSfemName(String sfemName) {
        this.sfemName = sfemName;
    }

/*    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }*/

    @FXML
    private Pane sfee_transport_pane;

    public void initialize() {

        if (/*C_ShopFloor.getInstance().getCurrent_C_SFEMTransport().*/getcSfeeTransport() == null) {
            System.out.println(inSFEI + " " + outSFEI);
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
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SFEE/SFEE_transport.fxml"));
//                C_ShopFloor.getInstance().getCurrent_C_SFEMTransport().getcSfeeTransport().setEditMode(true);
                loader.setController(C_ShopFloor.getInstance().getCurrent_C_SFEMTransport().getcSfeeTransport());
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
        if (getcSfeeTransport().validateMoveOn()) {
            error = true;
            errorMsg = errorMsg.concat(getcSfeeTransport().getErrorMsg());
        }

        return error;
    }

    public String getErrorMsg() {
        return errorMsg;
    }
}
