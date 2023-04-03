package viewers.controllers.SFEE;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;

import java.net.URL;
import java.util.ResourceBundle;


public class C_SFEE_properties {
    @FXML
    private ToggleGroup env;
    @FXML
    private ToggleButton sim_tb;
    @FXML
    private ToggleButton real_tb;

    @FXML
    private ToggleGroup func;
    @FXML
    private ToggleButton prod_tb;
    @FXML
    private ToggleButton trans_tb;


    public C_SFEE_properties() {
    }

    public String getEnvironment() {
        if (env == null)
            return null;
        ToggleButton toggleButton = (ToggleButton) env.getSelectedToggle();
        return toggleButton.getText();
    }

    public String getFunction() {
        if (func == null)
            return null;

        ToggleButton toggleButton = (ToggleButton) func.getSelectedToggle();
        return toggleButton.getText();
    }

    public void activatePreviousValues() {
        if (env != null) {
            ToggleButton btn = (ToggleButton) env.getSelectedToggle();
            if (btn != null)
                if (btn.getText().equalsIgnoreCase(sim_tb.getText())) {
                    env.selectToggle(sim_tb);
                    sim_tb.requestFocus();
                } else {
                    env.selectToggle(real_tb);
                    real_tb.requestFocus();
                }
        }
        if (func != null) {
            ToggleButton btn = (ToggleButton) func.getSelectedToggle();
            if (btn != null) {
                btn.requestFocus();
                func.selectToggle(btn);
            }
        }
    }

    private String errorMsg;

    public boolean validation_moveON() {
        if (env.getSelectedToggle() == null || func.getSelectedToggle() == null)
            errorMsg = "Select all fields !";

        return env.getSelectedToggle() != null && func.getSelectedToggle() != null;
      /*  Validator validator = new Validator();

        validator.createCheck()
                .dependsOn("env", env.selectedToggleProperty())
//                .dependsOn("func", func.selectedToggleProperty())
                .withMethod(c -> {
                    ToggleButton env = c.get("env");
//                    ToggleButton func = c.get("func");
                    if (!(env.isSelected() *//*&& func.isSelected()*//*))
                        c.error("All fields must be completed !");
                })
                .decorates((Node) env.getToggles())
//                .decorates(func.getSelectedToggle())
                .immediate();

        return validator.validate();*/

    }

    public String getErrorMsg() {
        return errorMsg;
    }
}
