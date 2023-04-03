package viewers.controllers.SFEE;

import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import net.synedra.validatorfx.Validator;

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

    public boolean validation_moveON() {
        if(env.getSelectedToggle() == null || func.getSelectedToggle() == null){

            return false;
        }


        Validator validator = new Validator();

        validator.createCheck()
                .dependsOn("env",  env.selectedToggleProperty())
                .dependsOn("func", func.selectedToggleProperty())
                .withMethod(c -> {
                    String environment = c.get("env");
                    String func = c.get("env");
                    if (environment.isBlank() || func.isBlank())
                        c.error("All fields must be completed !");
                })
                .decorates((Node) env.getSelectedToggle())
                .decorates((Node) func.getSelectedToggle())
                .immediate();

        return validator.validate();

    }
}
