package viewers.controllers.SFEE;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;


public class C_SFEE_properties {

    public C_SFEE_properties() {
        this.saveValues = new ArrayList<>();

    }

    private final ArrayList<Object> saveValues;

    @FXML
    private ToggleGroup env;
    @FXML
    private ToggleGroup func;


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


    public void initialize() {

        if (saveValues.size() > 0) {
            for (int i = 0; i < env.getToggles().size(); i++) {
                if (((ToggleButton) env.getToggles().get(i)).getId().equals(saveValues.get(0)))
                    env.selectToggle(env.getToggles().get(i));
            }
            for (int i = 0; i < func.getToggles().size(); i++) {
                if (((ToggleButton) func.getToggles().get(i)).getId().equals(saveValues.get(1)))
                    func.selectToggle(func.getToggles().get(i));
            }
//            saveValues.clear();
        }
    }

    public void setSaveValues() {
        if (validation_moveON()) {
            saveValues.add(0, ((ToggleButton) env.getSelectedToggle()).getId());
            saveValues.add(1, ((ToggleButton) func.getSelectedToggle()).getId());
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
