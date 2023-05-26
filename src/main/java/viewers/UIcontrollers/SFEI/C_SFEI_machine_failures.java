package viewers.UIcontrollers.SFEI;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import models.SFEx.SFEI_machine;
import models.sensor_actuator;
import utility.utils;

import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;

public class C_SFEI_machine_failures implements Initializable {

    private final TreeMap<Integer, sensor_actuator> io;
    private boolean editMode;
    private SFEI_machine sfeiMachine;

    public void activeEditMode(SFEI_machine sfeiMachine) {
        this.editMode = true;
        this.sfeiMachine = sfeiMachine;
    }

    public C_SFEI_machine_failures(TreeMap<Integer, sensor_actuator> io) {
        this.io = io;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (!editMode) {

            TreeMap<Integer, sensor_actuator> inputs = utils.getInstance().getSearch().getSensorsOrActuators(io, true);
            ArrayList<String> values_str = new ArrayList<>();
            for (Map.Entry<Integer, sensor_actuator> entry : inputs.entrySet())
                values_str.add(/*entry.getValue().getBit_offset() + " - " +*/ entry.getValue().getName());
            sDoor.setItems(FXCollections.observableArrayList(values_str));

            TreeMap<Integer, sensor_actuator> outputs = utils.getInstance().getSearch().getSensorsOrActuators(io, false);
            values_str.clear();
            for (Map.Entry<Integer, sensor_actuator> entry : outputs.entrySet())
                values_str.add(/*entry.getValue().getBit_offset() + " - " +*/ entry.getValue().getName());
            aStop.setItems(FXCollections.observableArrayList(values_str));
        } else {
            sDoor.setValue(sfeiMachine.getsDoor().getName());
            aStop.setValue(sfeiMachine.getaStop().getName());
        }
    }

    @FXML
    private ComboBox<String> sDoor;
    @FXML
    private ComboBox<String> aStop;

    public ComboBox<String> getsDoor() {
        return sDoor;
    }

    public ComboBox<String> getaStop() {
        return aStop;
    }


}
