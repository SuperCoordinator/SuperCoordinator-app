package viewers.controllers.SFEE;

import controllers.production.cSFEE_production;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import models.base.SFEE;
import utility.customCalculator;
import viewers.controllers.C_ShopFloor;

import java.util.ArrayList;
import java.util.List;

public class C_SFEE_failure {

    private final ArrayList<TextField> textFields;
    private final ArrayList<Object> savedValues;

    private cSFEE_production.operationMode operationMode;
    private SFEE.SFEE_type sfeeType;
    private ArrayList<String> loadedData;

    public C_SFEE_failure() {
        this.savedValues = new ArrayList<>();
        this.textFields = new ArrayList<>();
    }

    public void setSfeeType(SFEE.SFEE_type sfeeType) {
        this.sfeeType = sfeeType;
    }

    @FXML
    private ToggleGroup opMode;
    @FXML
    private TextField formula;

    @FXML
    private Button info_btn;
    @FXML
    private Label infoOperation_t;

    @FXML
    private TextField BDwR_n;
    @FXML
    private TextField BDwR_a;
    @FXML
    private TextField BDwR_m;
    @FXML
    private TextField BDwR_repair;

    @FXML
    private TextField BD_n;
    @FXML
    private TextField BD_a;
    @FXML
    private TextField BD_m;


    @FXML
    private TextField PF_n;
    @FXML
    private TextField PF_a;

    @FXML
    private TextField PF_m;

    @FXML
    private TextField PM_n;
    @FXML
    private TextField PM_a;
    @FXML
    private TextField PM_m;
    @FXML
    private Pane paneFailuresMode;


    public void initialize() {
        textFields.clear();
        textFields.addAll(List.of(formula, BDwR_n, BDwR_a, BDwR_m, BDwR_repair, BD_n, BD_a, BD_m, PF_n, PF_a, PF_m, PM_n, PM_a, PM_m));
        if (!C_ShopFloor.getInstance().isLoadedConfig()) {
            // Load data from "new configuration"
            if (savedValues.size() > 0) {
                for (int i = 0; i < opMode.getToggles().size(); i++) {
                    if (((ToggleButton) opMode.getToggles().get(i)).getId().equals(savedValues.get(0)))
                        opMode.selectToggle(opMode.getToggles().get(i));
                }

                if (((ToggleButton) opMode.getSelectedToggle()).getId().equals("prog_failures")) {
                    paneFailuresMode.setVisible(true);
                    for (int i = 0; i < textFields.size(); i++) {
                        textFields.get(i).setText((String) savedValues.get(i + 1));
                    }
                }
            }
        } else {
            // Load data from "load configuration"
            for (int i = 0; i < opMode.getToggles().size(); i++) {
                if (((ToggleButton) opMode.getToggles().get(i)).getId().contains(operationMode.name().toLowerCase()))
                    opMode.selectToggle(opMode.getToggles().get(i));
            }

            if (((ToggleButton) opMode.getSelectedToggle()).getId().equals("prog_failures")) {
                paneFailuresMode.setVisible(true);
                for (int i = 0; i < textFields.size(); i++) {
                    textFields.get(i).setText(loadedData.get(i));
                }
            }
        }
        // In case of real modules, not support failures
        if (sfeeType.equals(SFEE.SFEE_type.REAL)) {
            for (int i = 0; i < opMode.getToggles().size(); i++) {
                if (((ToggleButton) opMode.getToggles().get(i)).getId().equals("prog_failures")) {
                    ((ToggleButton) opMode.getToggles().get(i)).setDisable(true);
                }
            }
        }
    }

    public void loadData(cSFEE_production.operationMode operationMode, ArrayList<String> loadedData) {
        this.operationMode = operationMode;
        this.loadedData = loadedData;
    }

    @FXML
    public void buttonPressed(ActionEvent event) {
        if (((ToggleButton) opMode.getSelectedToggle()).getText().equals("Failures")) {
            paneFailuresMode.setVisible(true);
        } else if (((ToggleButton) opMode.getSelectedToggle()).getText().equals("Normal")) {
            paneFailuresMode.setVisible(false);
        }
    }

    @FXML
    public void infoButtonPressed(ActionEvent event) {

        infoOperation_t.setText(
                """
                        Valid variables: n - number of pieces moved / a - age of the machine in minutes / m - time since last maintenance in minutes\s
                        Valid operator: + - * / % or gauss[ mean ; dev ] linear[ value ]
                        Please add a space between each character/number (p.e: gauss [ 65 + ( 0.001 * n) ; 3.5 + 0.1 * a ]""");

        infoOperation_t.setVisible(true);

    }

    private String errorMsg;

    public boolean validateMoveOn() {
        boolean error = false;
        customCalculator customCalculator = new customCalculator();
        if (opMode.getSelectedToggle() == null) {
            errorMsg = "Select all fields !";
            error = true;
        } else if (((ToggleButton) opMode.getSelectedToggle()).getId().equals("prog_failures")) {
            if (formula.getText().isBlank()) {
                errorMsg = "For the failures mode, formulas is mandatory! \n";
                error = true;
            } else if (customCalculator.evalStochasticTimeExpression(formula.getText())) {
                errorMsg = customCalculator.errorMsg(formula.getText());
            }
            if (!BDwR_n.getText().isBlank() || !BDwR_a.getText().isBlank() || !BDwR_m.getText().isBlank()) {
                if (BDwR_repair.getText().isBlank()) {
                    errorMsg = errorMsg.concat(" Breakdown with repair must have a repair condition!");
                    error = true;
                }
            }
        }
        return !error;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setSaveValues() {
//        System.out.println("SAVING FAILURES");
        savedValues.add(0, ((ToggleButton) opMode.getSelectedToggle()).getId());

        if (((ToggleButton) opMode.getSelectedToggle()).getId().equals("prog_failures")) {
            for (int i = 0; i < textFields.size(); i++) {
                savedValues.add(i + 1, textFields.get(i).getText());
            }
        }

    }


}
