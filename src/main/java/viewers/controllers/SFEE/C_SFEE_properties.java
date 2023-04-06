package viewers.controllers.SFEE;

import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;

import java.util.ArrayList;


public class C_SFEE_properties {

    public C_SFEE_properties() {
        this.savedValues = new ArrayList<>();

    }

    private final ArrayList<Object> savedValues;

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

        if (savedValues.size() > 0) {
            for (int i = 0; i < env.getToggles().size(); i++) {
                if (((ToggleButton) env.getToggles().get(i)).getId().equals(savedValues.get(0)))
                    env.selectToggle(env.getToggles().get(i));
            }
            for (int i = 0; i < func.getToggles().size(); i++) {
                if (((ToggleButton) func.getToggles().get(i)).getId().equals(savedValues.get(1)))
                    func.selectToggle(func.getToggles().get(i));
            }
//            saveValues.clear();
        }
    }

    public void setSaveValues() {
        if (validation_moveON()) {
            savedValues.add(0, ((ToggleButton) env.getSelectedToggle()).getId());
            savedValues.add(1, ((ToggleButton) func.getSelectedToggle()).getId());
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
