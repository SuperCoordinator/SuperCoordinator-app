package viewers.controllers.SFEE;

import controllers.transport.cSFEE_transport;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import models.SFEx_particular.SFEI_transport;
import org.apache.commons.math3.util.Pair;
import utility.utils;
import viewers.controllers.C_ShopFloor;
import viewers.controllers.SFEI.C_SFEI_machine;
import viewers.controllers.SFEI.C_SFEI_transport;
import viewers.controllers.SFEM.C_SFEM_transport;

public class C_SFEE_transport {

    private String inSFEI, outSFEI;
    private cSFEE_transport cSFEETransport;

    public C_SFEE_transport() {
    }

    public C_SFEE_transport(String inSFEI, String outSFEI) {
        this.inSFEI = inSFEI;
        this.outSFEI = outSFEI;
    }

    public void setcSFEETransport(cSFEE_transport cSFEETransport) {
        this.cSFEETransport = cSFEETransport;
        loadData();
    }

    private C_SFEI_transport transport;

    public void registerC_SFEI_transport(C_SFEI_transport controller) {
        this.transport = controller;
    }


    @FXML
    private TextField formula;
    @FXML
    private TextField inSFEI_name;
    @FXML
    private Text infoOperationTime;

    @FXML
    private Button info_btn;

    @FXML
    private TextField outSFEI_name;

    @FXML
    private TextField sfee_transport_name;

    @FXML
    private Pane sfei_transport_pane;

    @FXML
    private Button swap_SFEI_order;

    public void initialize() {
        if (!C_ShopFloor.getInstance().isLoadedConfig()) {
            C_SFEI_transport cSfeiTransport = new C_SFEI_transport();
            cSfeiTransport.setIo();
            registerC_SFEI_transport(cSfeiTransport);

        } else {
            C_SFEI_transport cSfeiTransport = new C_SFEI_transport();
            cSfeiTransport.setSfeiTransport((SFEI_transport) cSFEETransport.getSfee().getSFEIbyIndex(0));
            cSfeiTransport.setIo(cSFEETransport.getSfee().getIo());
            registerC_SFEI_transport(cSfeiTransport);

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SFEI/SFEI_transport.fxml"));
                loader.setController(cSfeiTransport);
                AnchorPane pane = loader.load();
                sfei_transport_pane.getChildren().setAll(pane);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Pair<Pair<String, String>, Pair<String, String>> pair = cSFEETransport.prevNextSFEE();

            inSFEI_name.setText(pair.getFirst().getSecond());
            outSFEI_name.setText(pair.getSecond().getSecond());
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

    private void loadData() {

/*        C_SFEI_transport cSfeiTransport = new C_SFEI_transport(cSFEETransport.getSfee().getIo());
        cSfeiTransport.setSfeiTransport((SFEI_transport) cSFEETransport.getSfee().getSFEIbyIndex(0));
        registerC_SFEI_transport(cSfeiTransport);*/

    }

    private String errorMsg = "Missing fields: \n";

    public boolean validateMoveOn() {
        boolean error = false;

        if (transport.validateMoveOn()) {
            error = true;
            errorMsg = errorMsg.concat(transport.getErrorMsg());
        }

        if (sfee_transport_name.getText().isBlank()) {
            error = true;
            errorMsg = errorMsg.concat("  - SFEM Transport name \n");
        }
        if (formula.getText().isBlank()) {
            errorMsg = "Formula is mandatory to define transport time! \n";
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
