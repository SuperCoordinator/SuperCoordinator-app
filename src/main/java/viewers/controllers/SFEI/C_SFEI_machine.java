package viewers.controllers.SFEI;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ToggleGroup;
import models.SFEx_particular.SFEI_machine;
import models.sensor_actuator;
import utils.utils;

import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;

public class C_SFEI_machine implements Initializable {

    private SFEI_machine sfeiMachine;
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

        TreeMap<Integer, sensor_actuator> inputs = utility.getSearch().getSensorsOrActuators(io, true);
        ArrayList<String> values_str = new ArrayList<>();
        for (Map.Entry<Integer, sensor_actuator> entry : inputs.entrySet())
            values_str.add(/*entry.getValue().getBit_offset() + " - " +*/ entry.getValue().getName());
        sDoor.setItems(FXCollections.observableArrayList(values_str));

        TreeMap<Integer, sensor_actuator> outputs = utility.getSearch().getSensorsOrActuators(io, false);
        values_str.clear();
        for (Map.Entry<Integer, sensor_actuator> entry : outputs.entrySet())
            values_str.add(/*entry.getValue().getBit_offset() + " - " +*/ entry.getValue().getName());
        aStop.setItems(FXCollections.observableArrayList(values_str));

    }

    @FXML
    private ComboBox<String> aStop;

    @FXML
    private ToggleGroup part_type;

    @FXML
    private ComboBox<String> sDoor;


}
