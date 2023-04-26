package viewers.controllers.SFEE.transport;

import controllers.production.cSFEE_production;
import controllers.transport.cSFEE_transport;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import models.SFEx_particular.SFEI_transport;
import models.base.SFEI;
import models.sensor_actuator;
import utility.utils;
import viewers.controllers.C_ShopFloor;
import viewers.controllers.SFEI.C_SFEI_transport;
import viewers.controllers.SFEM.C_SFEM_production;

import java.util.*;

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

    private void loadData() {
        C_SFEI_transport cSfeiTransport = new C_SFEI_transport();
        cSfeiTransport.setIO(getIOsBySFEIs(cSFEETransport.getPrevSFEI_name()), getIOsBySFEIs(cSFEETransport.getNextSFEI_name()));
        cSfeiTransport.setSfeiTransport((SFEI_transport) cSFEETransport.getSfee().getSFEIbyIndex(0));
        registerC_SFEI_transport(cSfeiTransport);
    }

    public String getInSFEI() {
        return inSFEI;
    }

    public String getOutSFEI() {
        return outSFEI;
    }

    /**
     * To simulate the CM_SFEI_transport
     */
    private C_SFEI_transport transport;

    public void registerC_SFEI_transport(C_SFEI_transport controller) {
        this.transport = controller;
    }

    public C_SFEI_transport getC_SFEI_Transport() {
        return transport;
    }

    /**
     * END !
     */

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
        if (cSFEETransport == null) {
            C_SFEI_transport cSfeiTransport = new C_SFEI_transport();
            cSfeiTransport.setIO(getIOsBySFEIs(inSFEI), getIOsBySFEIs(outSFEI));
            registerC_SFEI_transport(cSfeiTransport);
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SFEI/SFEI_transport.fxml"));
                loader.setController(cSfeiTransport);
                AnchorPane pane = loader.load();
                sfei_transport_pane.getChildren().setAll(pane);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SFEI/SFEI_transport.fxml"));
//                C_ShopFloor.getInstance().getCurrent_C_SFEMTransport().getcSfeeTransport().getC_SFEI_Transport().setEditMode(true);
                loader.setController(C_ShopFloor.getInstance().getCurrent_C_SFEMTransport().getcSfeeTransport().getC_SFEI_Transport());
                AnchorPane pane = loader.load();
                sfei_transport_pane.getChildren().setAll(pane);
            } catch (Exception e) {
                e.printStackTrace();
            }
//            sfee_transport_name.setText(savedValues.get(0));
//            sfee_transport_name.setEditable(false);
//            formula.setText(savedValues.get(1));
            sfee_transport_name.setText(cSFEETransport.getSfee().getName());
            sfee_transport_name.setEditable(false);
            formula.setText(cSFEETransport.getSavedFormula());
        }

        inSFEI_name.setText(inSFEI);
        outSFEI_name.setText(outSFEI);

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

    private TreeMap<Integer, sensor_actuator> getIOsBySFEIs(String sfeiName) {
        System.out.println(sfeiName);
        TreeMap<Integer, sensor_actuator> treeMap = new TreeMap<>();
        for (C_SFEM_production cSFEMProduction : C_ShopFloor.getInstance().getcSfemProductions()) {
            for (cSFEE_production cSFEEProduction : cSFEMProduction.getcSFEMProduction().getSfeeControllers()) {
                for (Map.Entry<Integer, SFEI> entry : cSFEEProduction.getSFEE().getSFEIs().entrySet()) {
                    if (entry.getValue().getName().equals(sfeiName)) {
                        treeMap = cSFEEProduction.getSFEE().getIo();
                        break;
                    }
                }
            }
        }
        return treeMap;
    }

    public TextField getSfee_transport_name() {
        return sfee_transport_name;
    }

    public TextField getInSFEI_name() {
        return inSFEI_name;
    }

    public ArrayList<Object> getFormulaSplit() {
        ArrayList<Object> ret = new ArrayList<>();
        ret.add(0, formula.getText().contains("gauss") ? "gauss" : "linear");
        utils.getInstance().getCustomCalculator().evalStochasticTimeExpression(formula.getText());
        ret.addAll(List.of(utils.getInstance().getCustomCalculator().getStochasticTimeFormulaElements()));
        return ret;
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
            error = true;
            errorMsg = utils.getInstance().getCustomCalculator().errorMsg(formula.getText());
        }
        if (!error)
            setSavedValues();

        return error;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    private final ArrayList<String> savedValues = new ArrayList<>();

    private void setSavedValues() {
        savedValues.add(0, sfee_transport_name.getText());
        savedValues.add(1, formula.getText());
    }


}
