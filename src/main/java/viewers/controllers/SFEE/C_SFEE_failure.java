package viewers.controllers.SFEE;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.List;

public class C_SFEE_failure {

    private final ArrayList<TextField> textFields;
    private final ArrayList<Object> savedValues;

    public C_SFEE_failure() {
        this.savedValues = new ArrayList<>();
        this.textFields = new ArrayList<>();
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
        if (savedValues.size() > 0) {
            System.out.println("Loading FAILURES");
            for (int i = 0; i < opMode.getToggles().size(); i++) {
                if (((ToggleButton) opMode.getToggles().get(i)).getId().equals(savedValues.get(0)))
                    opMode.selectToggle(opMode.getToggles().get(i));
            }

            if (((ToggleButton) opMode.getSelectedToggle()).getId().equals("failures")) {
                paneFailuresMode.setVisible(true);
                for (int i = 0; i < textFields.size(); i++) {
                    textFields.get(i).setText((String) savedValues.get(i + 1));
                }
            }
        }

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
        if (opMode.getSelectedToggle() == null) {
            errorMsg = "Select all fields !";
            error = true;
        } else if (((ToggleButton) opMode.getSelectedToggle()).getId().equals("failures")) {
            if (formula.getText().isBlank()) {
                errorMsg = "For the failures mode, formulas is mandatory! \n";
                error = true;
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
        System.out.println("SAVING FAILURES");
        savedValues.add(0, ((ToggleButton) opMode.getSelectedToggle()).getId());

        if (((ToggleButton) opMode.getSelectedToggle()).getId().equals("failures")) {
            for (int i = 0; i < textFields.size(); i++) {
                savedValues.add(i + 1, textFields.get(i).getText());
            }
        }

    }
}
