package viewers.controllers.SFEE;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;

public class C_SFEE_failure {

    @FXML
    private TextField BD_a;

    @FXML
    private TextField BD_m;

    @FXML
    private TextField BD_n;

    @FXML
    private TextField BDwR_a;

    @FXML
    private TextField BDwR_m;

    @FXML
    private TextField BDwR_n;

    @FXML
    private TextField BDwR_repair;

    @FXML
    private TextField PF_a;

    @FXML
    private TextField PF_m;

    @FXML
    private TextField PF_n;

    @FXML
    private TextField PM_a;

    @FXML
    private TextField PM_m;

    @FXML
    private TextField PM_n;

    @FXML
    private TextField formula;

    @FXML
    private Button info_btn;
    @FXML
    private Label infoOperation_t;

    @FXML
    private ToggleGroup opMode;

    @FXML
    private Pane paneFailuresMode;

    @FXML
    void buttonPressed(ActionEvent event) {
        if (((ToggleButton) opMode.getSelectedToggle()).getText().equals("Failures")) {
            paneFailuresMode.setVisible(true);
        } else if (((ToggleButton) opMode.getSelectedToggle()).getText().equals("Normal")) {
            paneFailuresMode.setVisible(false);
        }
    }

    @FXML
    void infoButtonPressed(ActionEvent event) {

        infoOperation_t.setText(
                """
                        Valid variables: n - number of pieces moved / a - age of the machine in minutes / m - time since last maintenance in minutes\s
                         Valid operator: + - * / % or gauss[ mean ; dev ] linear[ value ]
                        Please add a space between each character/number (p.e: gauss [ 65 + ( 0.001 * n) ; 3.5 + 0.1 * a ]""");

        infoOperation_t.setVisible(true);

    }



}
