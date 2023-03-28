package viewers.controllers.SFEE;

import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;

public class C_SFEE_properties {
    @FXML
    private ToggleGroup env;
    @FXML
    private ToggleGroup func;

    public C_SFEE_properties() {
    }

    public String getEnvironment() {
        ToggleButton toggleButton = (ToggleButton) env.getSelectedToggle();
        return toggleButton.getText();
    }

    public String getFunction() {
        ToggleButton toggleButton = (ToggleButton) func.getSelectedToggle();
        return toggleButton.getText();
    }
}
