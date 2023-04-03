package viewers.controllers.SFEI;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import models.SFEx_particular.SFEI_conveyor;
import models.SFEx_particular.SFEI_machine;
import models.base.SFEI;
import models.sensor_actuator;
import utils.utils;
import viewers.mediators.CM_SFEE;
import viewers.mediators.CM_SFEI;

import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;

public class C_SFEI extends CM_SFEI implements Initializable {

    private final TreeMap<Integer, sensor_actuator> io;

    public C_SFEI(TreeMap<Integer, sensor_actuator> io) {
        this.io = io;
    }

    @Override
    public ArrayList<C_SFEI_conveyor> getSfeiConveyors() {
        return super.getSfeiConveyors();
    }

    @Override
    public ArrayList<C_SFEI_machine> getSfeiMachines() {
        return super.getSfeiMachines();
    }

    @FXML
    private TextField sfeiName;
    @FXML
    private ComboBox<String> sfei_type;
    @FXML
    private ComboBox<String> input_sensor;
    @FXML
    private ComboBox<String> output_sensor;
    @FXML
    private DatePicker last_maintenance_date;
    @FXML
    private DatePicker manufacturing_date;

    @FXML
    private Pane failures_pane;

    @FXML
    private ToggleGroup simulation;

    @FXML
    private ToggleGroup failures_support;
    @FXML
    private ToggleGroup end_item;
    @FXML
    private ToggleGroup start_item;

    private C_SFEI_conveyor cSfeiConveyor;
    private C_SFEI_machine cSfeiMachine;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        utils utility = new utils();

        SFEI.SFEI_type[] values = SFEI.SFEI_type.values();
        ArrayList<String> values_str = new ArrayList<>();
        for (SFEI.SFEI_type value : values)
            values_str.add(value.toString());
        sfei_type.setItems(FXCollections.observableArrayList(values_str));


        values_str.clear();
        TreeMap<Integer, sensor_actuator> inputs = utility.getSearch().getSensorsOrActuators(io, true);
        for (Map.Entry<Integer, sensor_actuator> entry : inputs.entrySet())
            values_str.add(/*entry.getValue().getBit_offset() + " - " +*/ entry.getValue().getName());
        input_sensor.setItems(FXCollections.observableArrayList(values_str));
        output_sensor.setItems(FXCollections.observableArrayList(values_str));

        // Initialize both, then only one will be added in the arrays
        cSfeiConveyor = new C_SFEI_conveyor(io);
        cSfeiMachine = new C_SFEI_machine(io);

    }

    @FXML
    void comboBoxPressed(ActionEvent event) {
        ComboBox<String> temp = (ComboBox<String>) event.getSource();

        switch (temp.getId()) {
            case "sfei_type" -> {
                show_failure_support_pane(event);
            }

        }

    }

    @FXML
    void show_failure_support_pane(ActionEvent event) {
        try {
            if (((RadioButton) failures_support.getSelectedToggle()).getText().equalsIgnoreCase("yes")) {
                String type = sfei_type.getValue().toLowerCase();
                if (!type.equalsIgnoreCase("conveyor") && !type.equalsIgnoreCase("machine"))
                    return;

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SFEI_failures_support_" + type + ".fxml"));

                if (type.equalsIgnoreCase("conveyor")) {
//                    registerC_SFEI_conveyor(cSfeiConveyor);
                    loader.setController(cSfeiConveyor);
                } else {
//                    registerC_SFEI_machine(cSfeiMachine);
                    loader.setController(cSfeiMachine);
                }

                AnchorPane pane = loader.load();
                failures_pane.getChildren().setAll(pane);

            } else {
                cleanFailuresPane();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final utils utility = new utils();

    @FXML
    void buttonPressed(ActionEvent event) {
        Button temp = (Button) event.getSource();
        ToggleButton failures_btn = (ToggleButton) failures_support.getSelectedToggle();
        // Assumed to be the add button temp.getText()
        switch (sfei_type.getValue().toLowerCase()) {
            // Add new SFEI
            case "conveyor" -> {
                if (failures_btn.getText().equalsIgnoreCase("yes")) {
                    SFEI_conveyor newObj = new SFEI_conveyor(
                            sfeiName.getText(),
                            SFEI.SFEI_type.CONVEYOR,
                            utility.getSearch().getIObyName(input_sensor.getValue(), io),
                            utility.getSearch().getIObyName(output_sensor.getValue(), io),
                            Instant.from(manufacturing_date.getValue().atStartOfDay(ZoneId.systemDefault())),
                            Instant.from(last_maintenance_date.getValue().atStartOfDay(ZoneId.systemDefault())),
                            CM_SFEE.getInstance().getProperties().getEnvironment().equalsIgnoreCase("simulation"),
                            true,
                            ((ToggleButton) start_item.getSelectedToggle()).getText().equals("yes"),
                            ((ToggleButton) end_item.getSelectedToggle()).getText().equals("yes"),
                            cSfeiConveyor.getSensAct());

                    cSfeiConveyor.setSfeiConveyor(newObj);
                    registerC_SFEI_conveyor(cSfeiConveyor);

                } else {
                    SFEI_conveyor newObj = new SFEI_conveyor(
                            sfeiName.getText(),
                            SFEI.SFEI_type.CONVEYOR,
                            utility.getSearch().getIObyName(input_sensor.getValue(), io),
                            utility.getSearch().getIObyName(output_sensor.getValue(), io),
                            Instant.from(manufacturing_date.getValue().atStartOfDay(ZoneId.systemDefault())),
                            Instant.from(last_maintenance_date.getValue().atStartOfDay(ZoneId.systemDefault())),
                            CM_SFEE.getInstance().getProperties().getEnvironment().equalsIgnoreCase("simulation"),
                            false,
                            ((ToggleButton) start_item.getSelectedToggle()).getText().equals("yes"),
                            ((ToggleButton) end_item.getSelectedToggle()).getText().equals("yes"),
                            new sensor_actuator[5]);

                    cSfeiConveyor.setSfeiConveyor(newObj);
                    registerC_SFEI_conveyor(cSfeiConveyor);
                }
            }
            case "machine" -> {
                if (failures_btn.getText().equalsIgnoreCase("yes")) {
                    SFEI_machine newObj = new SFEI_machine(
                            sfeiName.getText(),
                            SFEI.SFEI_type.CONVEYOR,
                            utility.getSearch().getIObyName(input_sensor.getValue(), io),
                            utility.getSearch().getIObyName(output_sensor.getValue(), io),
                            Instant.from(manufacturing_date.getValue().atStartOfDay(ZoneId.systemDefault())),
                            Instant.from(last_maintenance_date.getValue().atStartOfDay(ZoneId.systemDefault())),
                            CM_SFEE.getInstance().getProperties().getEnvironment().equalsIgnoreCase("simulation"),
                            true,
                            ((ToggleButton) start_item.getSelectedToggle()).getText().equals("yes"),
                            ((ToggleButton) end_item.getSelectedToggle()).getText().equals("yes"),
                            cSfeiConveyor.getSensAct());

                    cSfeiMachine.setSfeiMachine(newObj);
                    registerC_SFEI_machine(cSfeiMachine);

                } else {
                    SFEI_conveyor newObj = new SFEI_conveyor(
                            sfeiName.getText(),
                            SFEI.SFEI_type.CONVEYOR,
                            utility.getSearch().getIObyName(input_sensor.getValue(), io),
                            utility.getSearch().getIObyName(output_sensor.getValue(), io),
                            Instant.from(manufacturing_date.getValue().atStartOfDay(ZoneId.systemDefault())),
                            Instant.from(last_maintenance_date.getValue().atStartOfDay(ZoneId.systemDefault())),
                            CM_SFEE.getInstance().getProperties().getEnvironment().equalsIgnoreCase("simulation"),
                            false,
                            ((ToggleButton) start_item.getSelectedToggle()).getText().equals("yes"),
                            ((ToggleButton) end_item.getSelectedToggle()).getText().equals("yes"),
                            new sensor_actuator[5]);

                    cSfeiMachine.setSfeiMachine(newObj);
                    registerC_SFEI_machine(cSfeiMachine);
                }
            }
        }

        cleanFailuresPane();


    }

    private void cleanFailuresPane() {
        AnchorPane pane = new AnchorPane();
        failures_pane.getChildren().setAll(pane);
    }
}



