package viewers.UIcontrollers.SFEI;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import models.SFEx.SFEI_conveyor;
import models.sensor_actuator;
import utility.utils;

import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;

public class C_SFEI_conveyor implements Initializable {

    private SFEI_conveyor sfeiConveyor;
    private C_SFEI_conveyor_failures sfeiConveyorFailures;
    private final TreeMap<Integer, sensor_actuator> io;

    private boolean editMode;

    public C_SFEI_conveyor(TreeMap<Integer, sensor_actuator> io) {
        this.io = io;
    }

    public void activeEditMode() {
        this.editMode = true;
    }

    public SFEI_conveyor getSfeiConveyor() {
        return sfeiConveyor;
    }

    public void setSfeiConveyor(SFEI_conveyor sfeiConveyor) {
        this.sfeiConveyor = sfeiConveyor;
    }

    @FXML
    private ComboBox<String> aConveyor;
    @FXML
    private ToggleGroup failures_support;
    @FXML
    private Pane failure_pane;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (!editMode) {
            ArrayList<String> values_str = new ArrayList<>();

            TreeMap<Integer, sensor_actuator> outputs = utils.getInstance().getSearch().getSensorsOrActuators(io, false);

            for (Map.Entry<Integer, sensor_actuator> entry : outputs.entrySet())
                values_str.add(/*entry.getValue().getBit_offset() + " - " +*/ entry.getValue().getName());
            aConveyor.setItems(FXCollections.observableArrayList(values_str));
        } else {
            aConveyor.setValue(sfeiConveyor.getaConveyorMotor().getName());

            if (sfeiConveyor.isSupportsFailures()) {
                loadFailuresPane();

                if (((RadioButton) failures_support.getToggles().get(0)).getText().equalsIgnoreCase("yes"))
                    failures_support.selectToggle(failures_support.getToggles().get(0));
                else
                    failures_support.selectToggle(failures_support.getToggles().get(1));

            }
        }

    }

    @FXML
    void show_failure_support_pane(ActionEvent event) {

        if (getFailures_support())
            loadFailuresPane();
        else
            cleanFailuresPane();
    }

    public boolean getFailures_support() {
        ToggleButton toggleButton = (ToggleButton) failures_support.getSelectedToggle();
        return toggleButton.getText().equalsIgnoreCase("yes");
    }

    private void loadFailuresPane() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SFEI/SFEI_failures_support_conveyor.fxml"));

            sfeiConveyorFailures = new C_SFEI_conveyor_failures(io);
            if (editMode && sfeiConveyor.isSupportsFailures()) {
                sfeiConveyorFailures.activeEditMode(sfeiConveyor);
            }

            loader.setController(sfeiConveyorFailures);

            AnchorPane pane = loader.load();
            failure_pane.getChildren().setAll(pane);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void cleanFailuresPane() {
        sfeiConveyorFailures = null;
        failure_pane.getChildren().clear();
    }


    public sensor_actuator[] getSensAct() {
        sensor_actuator[] ret = new sensor_actuator[7];
        if (sfeiConveyorFailures != null) {
            ret[0] = search_sensor_actuator(sfeiConveyorFailures.getaRemover());
            ret[1] = search_sensor_actuator(sfeiConveyorFailures.getaEmitter());
            ret[2] = null; // for Emit Part
            ret[3] = null; // for Emit Base
            ret[4] = search_sensor_actuator(sfeiConveyorFailures.getsRemover());
            ret[5] = search_sensor_actuator(sfeiConveyorFailures.getsEmitter());
        } else {
            ret[0] = null;
            ret[1] = null;
            ret[2] = null;
            ret[3] = null;
            ret[4] = null;
            ret[5] = null;
        }
        ret[6] = search_sensor_actuator(aConveyor);

        return ret;
    }

    private sensor_actuator search_sensor_actuator(ComboBox<String> comboBox) {
        if (comboBox.getValue() == null)
            return null;
        else
            return utils.getInstance().getSearch().getIObyName(comboBox.getValue(), io);
    }


}
