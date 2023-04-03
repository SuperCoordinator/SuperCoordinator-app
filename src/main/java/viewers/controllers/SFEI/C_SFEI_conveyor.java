package viewers.controllers.SFEI;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import models.sensor_actuator;
import utils.utils;

import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;

public class C_SFEI_conveyor implements Initializable {

    private final TreeMap<Integer, sensor_actuator> io;

    public C_SFEI_conveyor(TreeMap<Integer, sensor_actuator> io) {
        this.io = io;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        utils utility = new utils();

        TreeMap<Integer, sensor_actuator> inputs = utility.getSearch().getSensorsOrActuators(io, true);
        ArrayList<String> values_str = new ArrayList<>();
        for (Map.Entry<Integer, sensor_actuator> entry : inputs.entrySet())
            values_str.add(entry.getValue().getBit_offset() + " - " + entry.getValue().getName());
        sEmitter.setItems(FXCollections.observableArrayList(values_str));
        sRemover.setItems(FXCollections.observableArrayList(values_str));

        TreeMap<Integer, sensor_actuator> outputs = utility.getSearch().getSensorsOrActuators(io, false);
        values_str.clear();
        for (Map.Entry<Integer, sensor_actuator> entry : outputs.entrySet())
            values_str.add(entry.getValue().getBit_offset() + " - " + entry.getValue().getName());
        aConveyor.setItems(FXCollections.observableArrayList(values_str));
        aEmitter.setItems(FXCollections.observableArrayList(values_str));
        aRemover.setItems(FXCollections.observableArrayList(values_str));
    }

    @FXML
    private ComboBox<String> aConveyor;

    @FXML
    private ComboBox<String> aEmitter;

    @FXML
    private ComboBox<String> aRemover;

    @FXML
    private ComboBox<String> sEmitter;

    @FXML
    private ComboBox<String> sRemover;


}
