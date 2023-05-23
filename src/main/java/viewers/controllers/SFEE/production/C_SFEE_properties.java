package viewers.controllers.SFEE.production;

import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import models.base.SFEE;
import viewers.controllers.C_ShopFloor;

import java.util.ArrayList;


public class C_SFEE_properties {

    public C_SFEE_properties() {
        this.savedValues = new ArrayList<>();
    }

    private final ArrayList<Object> savedValues;

    private SFEE.SFEE_environment sfeeType;
    private SFEE.SFEE_role sfeeFunction;

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
        if (!C_ShopFloor.getInstance().isLoadedConfig()) {
            // Load data from "new configuration"
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
        } else {
            // Load data from "load configuration"
            for (int i = 0; i < env.getToggles().size(); i++) {
                if (((ToggleButton) env.getToggles().get(i)).getText().equalsIgnoreCase(sfeeType.name().toLowerCase()))
                    env.selectToggle(env.getToggles().get(i));
            }
            for (int i = 0; i < func.getToggles().size(); i++) {
                if (((ToggleButton) func.getToggles().get(i)).getText().equalsIgnoreCase(sfeeFunction.name().toLowerCase()))
                    func.selectToggle(func.getToggles().get(i));
            }
        }
    }

    public void loadData(SFEE.SFEE_environment sfeeType, SFEE.SFEE_role sfeeFunction) {
        this.sfeeType = sfeeType;
        this.sfeeFunction = sfeeFunction;
    }

    public void setSaveValues() {
        if (validateMoveOn()) {
            savedValues.add(0, ((ToggleButton) env.getSelectedToggle()).getId());
            savedValues.add(1, ((ToggleButton) func.getSelectedToggle()).getId());
        }
    }

    private String errorMsg;

    public boolean validateMoveOn() {
        if (env.getSelectedToggle() == null || func.getSelectedToggle() == null)
            errorMsg = "Select all fields !";

        return env.getSelectedToggle() != null && func.getSelectedToggle() != null;
    }

    public String getErrorMsg() {
        return errorMsg;
    }


}
