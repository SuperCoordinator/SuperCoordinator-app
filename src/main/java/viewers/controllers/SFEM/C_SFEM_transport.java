package viewers.controllers.SFEM;

import controllers.transport.cSFEM_transport;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import utility.utils;
import viewers.controllers.C_ShopFloor;
import viewers.mediators.CM_SFEM_transport;

public class C_SFEM_transport extends CM_SFEM_transport {

    private String sfemTransportName;
    private cSFEM_transport cSFEMTransport;

    private String inSFEI;
    private String outSFEI;

    public C_SFEM_transport() {
    }

    public C_SFEM_transport(String sfemName) {
        this.sfemTransportName = sfemName;
    }

    public C_SFEM_transport(String sfemName, String inSFEI, String outSFEI) {
        this.sfemTransportName = sfemName;
        this.inSFEI = inSFEI;
        this.outSFEI = outSFEI;
    }

    public C_SFEM_transport(cSFEM_transport cSfemTransport) {
        this.cSFEMTransport = cSfemTransport;
        this.sfemTransportName = cSfemTransport.getSfem().getName();
    }

    public String getSfemTransportName() {
        return sfemTransportName;
    }

    public cSFEM_transport getcSFEMTransport() {
        return cSFEMTransport;
    }

    public void setSfemTransportName(String sfemTransportName) {
        this.sfemTransportName = sfemTransportName;
    }


    @FXML
    private TextField SFEM_TransportName;

    @FXML
    private ComboBox<String> emit_base_bit;

    @FXML
    private ComboBox<String> emit_bit;

    @FXML
    private ComboBox<String> emit_part_bit;

    @FXML
    private TextField inSFEI_name;

    @FXML
    private TextField outSFEI_name;

    @FXML
    private ComboBox<String> remover_bit;

    @FXML
    private Button swap_SFEI_order;

    @FXML
    private TextField formula;

    @FXML
    private Text infoOperationTime;

    @FXML
    private Button info_btn;

    public void initialize() {

        if (C_ShopFloor.getInstance().isLoadedConfig()) {
            if (sfemTransportName != null)
                SFEM_TransportName.setText(sfemTransportName);


            if (inSFEI != null && outSFEI != null) {
                inSFEI_name.setText(inSFEI);
                outSFEI_name.setText(outSFEI);
            }
        } else {

        }

        infoOperationTime.setText("""
                Valid variables: n - number of pieces moved / a - age of the machine in minutes / m - time since last maintenance in minutes\\s
                Valid operator: + - * / % or gauss[ mean ; dev ] linear[ value ]
                Please add a space between each character/number (p.e: gauss [ 65 + ( 0.001 * n) ; 3.5 + 0.1 * a ]                     
                """);

        info_btn.setOnMouseClicked(event -> {
            infoOperationTime.setVisible(!infoOperationTime.isVisible());
        });

        swap_SFEI_order.setOnMouseClicked(event -> {
            String old = inSFEI_name.getText();
            inSFEI_name.setText(outSFEI_name.getText());
            outSFEI_name.setText(old);
        });


    }

    private String errorMsg = "Missing fields: \n";

    public boolean validateMoveOn() {
        boolean error = false;

        if (SFEM_TransportName.getText().isBlank()) {
            error = true;
            errorMsg = errorMsg.concat("  - SFEM Transport name \n");
        }
        if (remover_bit.getValue() == null) {
            error = true;
            errorMsg = errorMsg.concat("  - Remover bit \n");
        }
        if (emit_bit.getValue() == null) {
            error = true;
            errorMsg = errorMsg.concat("  - Emitter emit bit \n");
        }
        if (emit_part_bit.getValue() == null) {
            error = true;
            errorMsg = errorMsg.concat("  - Emitter part bit \n");
        }
        if (emit_base_bit.getValue() == null) {
            error = true;
            errorMsg = errorMsg.concat("  - Emitter base bit \n");
        }
        if (formula.getText().isBlank()) {
            errorMsg = "For the failures mode, formulas is mandatory! \n";
            error = true;
        } else if (utils.getInstance().getCustomCalculator().evalStochasticTimeExpression(formula.getText())) {
            errorMsg = utils.getInstance().getCustomCalculator().errorMsg(formula.getText());
        }
        return !error;
    }

    public String getErrorMsg() {
        return errorMsg;
    }
}
