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
import models.partsAspect;
import models.sensor_actuator;
import utils.utils;

import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;

public class C_SFEI_machine implements Initializable {

    private SFEI_machine sfeiMachine;
    private C_SFEI_machine_failures sfeiMachineFailures;
    private final TreeMap<Integer, sensor_actuator> io;

    public C_SFEI_machine(TreeMap<Integer, sensor_actuator> io) {
        this.io = io;
    }

    public SFEI_machine getSfeiMachine() {
        return sfeiMachine;
    }

    public void setSfeiMachine(SFEI_machine sfeiMachine) {
        this.sfeiMachine = sfeiMachine;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        utils utility = new utils();

        TreeMap<Integer, sensor_actuator> outputs = utility.getSearch().getSensorsOrActuators(io, false);
        ArrayList<String> values_str = new ArrayList<>();
        for (Map.Entry<Integer, sensor_actuator> entry : outputs.entrySet())
            values_str.add(/*entry.getValue().getBit_offset() + " - " +*/ entry.getValue().getName());
        aProduce.setItems(FXCollections.observableArrayList(values_str));

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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SFEI_failures_support_machine.fxml"));

            sfeiMachineFailures = new C_SFEI_machine_failures(io);
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


    public partsAspect.form getPart_type() {
        RadioButton radioButton = (RadioButton) part_type.getSelectedToggle();
        return radioButton.getText().equalsIgnoreCase("lid") ? partsAspect.form.LID : partsAspect.form.BASE;
    }
    public boolean getFailure_support() {
        ToggleButton toggleButton = (ToggleButton) failures_support.getSelectedToggle();
        return toggleButton.getText().equalsIgnoreCase("yes");
    }

    public sensor_actuator[] getSensAct() {

        sensor_actuator[] ret = new sensor_actuator[3];
        if(sfeiMachineFailures != null){
            ret[0] = search_sensor_actuator(sfeiMachineFailures.getsDoor());
            ret[1] = search_sensor_actuator(sfeiMachineFailures.getaStop());
        }else{
            ret[0] = null;
            ret[1] = null;
        }
        ret[2] = search_sensor_actuator(aProduce);
        return ret;

    }

    private sensor_actuator search_sensor_actuator(ComboBox<String> comboBox) {
        utils utils = new utils();
        if (comboBox.getValue() == null)
            return null;
        else
            return utils.getSearch().getIObyName(comboBox.getValue(), io);
    }


}
