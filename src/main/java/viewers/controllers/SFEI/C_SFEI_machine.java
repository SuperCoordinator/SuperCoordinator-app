package viewers.controllers.SFEI;

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
import models.SFEx_particular.SFEI_machine;
import models.partDescription;
import models.sensor_actuator;
import utility.utils;

import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;

public class C_SFEI_machine implements Initializable {

    private SFEI_machine sfeiMachine;
    private C_SFEI_machine_failures sfeiMachineFailures;
    private final TreeMap<Integer, sensor_actuator> io;
    private boolean editMode;

    public C_SFEI_machine(TreeMap<Integer, sensor_actuator> io) {
        this.io = io;
    }

    public void activeEditMode() {
        this.editMode = true;
    }

    public SFEI_machine getSfeiMachine() {
        return sfeiMachine;
    }

    public void setSfeiMachine(SFEI_machine sfeiMachine) {
        this.sfeiMachine = sfeiMachine;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (!editMode) {
            TreeMap<Integer, sensor_actuator> outputs = utils.getInstance().getSearch().getSensorsOrActuators(io, false);
            ArrayList<String> values_str = new ArrayList<>();
            for (Map.Entry<Integer, sensor_actuator> entry : outputs.entrySet())
                values_str.add(entry.getValue().getName());
            aProduce.setItems(FXCollections.observableArrayList(values_str));
        } else {

            aProduce.setValue(sfeiMachine.getaProduce().getName());

            if (((RadioButton) part_type.getToggles().get(0)).getText().equalsIgnoreCase("lid")) {
                if (sfeiMachine.getPartForm().equals(partDescription.form.LID)) {
                    part_type.selectToggle(part_type.getToggles().get(0));
                } else {
                    part_type.selectToggle(part_type.getToggles().get(1));
                }
            } else {
                if (sfeiMachine.getPartForm().equals(partDescription.form.BASE)) {
                    part_type.selectToggle(part_type.getToggles().get(0));
                } else {
                    part_type.selectToggle(part_type.getToggles().get(1));
                }
            }


            if (sfeiMachine.isSupportsFailures()) {
                loadFailuresPane();

                if (((RadioButton) failures_support.getToggles().get(0)).getText().equalsIgnoreCase("yes"))
                    failures_support.selectToggle(failures_support.getToggles().get(0));
                else
                    failures_support.selectToggle(failures_support.getToggles().get(1));
            }
        }
    }

    @FXML
    private ToggleGroup part_type;
    @FXML
    private ComboBox<String> aProduce;

    @FXML
    private ToggleGroup failures_support;
    @FXML
    private Pane failure_pane;


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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SFEI/SFEI_failures_support_machine.fxml"));

            sfeiMachineFailures = new C_SFEI_machine_failures(io);
            if (editMode && sfeiMachine.isSupportsFailures())
                sfeiMachineFailures.activeEditMode(sfeiMachine);
            loader.setController(sfeiMachineFailures);

            AnchorPane pane = loader.load();
            failure_pane.getChildren().setAll(pane);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void cleanFailuresPane() {
        sfeiMachineFailures = null;
        failure_pane.getChildren().clear();
    }


    public partDescription.form getPart_type() {
        RadioButton radioButton = (RadioButton) part_type.getSelectedToggle();
        return radioButton.getText().equalsIgnoreCase("lid") ? partDescription.form.LID : partDescription.form.BASE;
    }

    public boolean getFailure_support() {
        RadioButton toggleButton = (RadioButton) failures_support.getSelectedToggle();
        return toggleButton.getText().equalsIgnoreCase("yes");
    }

    public sensor_actuator[] getSensAct() {

        sensor_actuator[] ret = new sensor_actuator[3];
        ret[0] = search_sensor_actuator(aProduce);
        if (sfeiMachineFailures != null) {
            ret[1] = search_sensor_actuator(sfeiMachineFailures.getsDoor());
            ret[2] = search_sensor_actuator(sfeiMachineFailures.getaStop());
        } else {
            ret[1] = null;
            ret[2] = null;
        }

        return ret;

    }

    private sensor_actuator search_sensor_actuator(ComboBox<String> comboBox) {

        if (comboBox.getValue() == null)
            return null;
        else
            return utils.getInstance().getSearch().getIObyName(comboBox.getValue(), io);
    }


}
