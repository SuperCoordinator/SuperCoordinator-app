package viewers.controllers.SFEI;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import models.SFEx_particular.SFEI_conveyor;
import models.sensor_actuator;
import utility.utils;

import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;

public class C_SFEI_conveyor_failures implements Initializable {

    private final TreeMap<Integer, sensor_actuator> io;
    private boolean editMode;
    private SFEI_conveyor sfeiConveyor;

    public void activeEditMode(SFEI_conveyor sfeiConveyor) {
        this.editMode = true;
        this.sfeiConveyor = sfeiConveyor;
    }

    public C_SFEI_conveyor_failures(TreeMap<Integer, sensor_actuator> io) {
        this.io = io;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        if (!editMode) {
            utils utility = new utils();

            TreeMap<Integer, sensor_actuator> inputs = utility.getSearch().getSensorsOrActuators(io, true);
            ArrayList<String> values_str = new ArrayList<>();
            for (Map.Entry<Integer, sensor_actuator> entry : inputs.entrySet())
                values_str.add(entry.getValue().getName());
            sEmitter.setItems(FXCollections.observableArrayList(values_str));
            sRemover.setItems(FXCollections.observableArrayList(values_str));

            TreeMap<Integer, sensor_actuator> outputs = utility.getSearch().getSensorsOrActuators(io, false);
            values_str.clear();
            for (Map.Entry<Integer, sensor_actuator> entry : outputs.entrySet())
                values_str.add(entry.getValue().getName());

            aEmitter.setItems(FXCollections.observableArrayList(values_str));
            aRemover.setItems(FXCollections.observableArrayList(values_str));
        } else {
            sEmitter.setValue(sfeiConveyor.getsEmitter().getName());
            sRemover.setValue(sfeiConveyor.getsRemover().getName());
            aEmitter.setValue(sfeiConveyor.getaEmitter().getName());
            aRemover.setValue(sfeiConveyor.getaRemover().getName());

        }
    }

    @FXML
    private ComboBox<String> aEmitter;

    @FXML
    private ComboBox<String> aRemover;

    @FXML
    private ComboBox<String> sEmitter;

    @FXML
    private ComboBox<String> sRemover;


    public ComboBox<String> getaEmitter() {
        return aEmitter;
    }

    public ComboBox<String> getaRemover() {
        return aRemover;
    }

    public ComboBox<String> getsEmitter() {
        return sEmitter;
    }

    public ComboBox<String> getsRemover() {
        return sRemover;
    }
}
