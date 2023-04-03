package viewers.controllers.SFEI;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import models.SFEx_particular.SFEI_conveyor;
import models.sensor_actuator;
import utils.utils;

import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;

public class C_SFEI_conveyor implements Initializable {

    private SFEI_conveyor sfeiConveyor;

    private final TreeMap<Integer, sensor_actuator> io;

    public C_SFEI_conveyor(TreeMap<Integer, sensor_actuator> io) {
        this.io = io;
    }

    public SFEI_conveyor getSfeiConveyor() {
        return sfeiConveyor;
    }

    public void setSfeiConveyor(SFEI_conveyor sfeiConveyor) {
        this.sfeiConveyor = sfeiConveyor;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        utils utility = new utils();

        TreeMap<Integer, sensor_actuator> inputs = utility.getSearch().getSensorsOrActuators(io, true);
        ArrayList<String> values_str = new ArrayList<>();
        for (Map.Entry<Integer, sensor_actuator> entry : inputs.entrySet())
            values_str.add(/*entry.getValue().getBit_offset() + " - " +*/ entry.getValue().getName());
        sEmitter.setItems(FXCollections.observableArrayList(values_str));
        sRemover.setItems(FXCollections.observableArrayList(values_str));

        TreeMap<Integer, sensor_actuator> outputs = utility.getSearch().getSensorsOrActuators(io, false);
        values_str.clear();
        for (Map.Entry<Integer, sensor_actuator> entry : outputs.entrySet())
            values_str.add(/*entry.getValue().getBit_offset() + " - " +*/ entry.getValue().getName());
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


    public sensor_actuator[] getSensAct() {
        utils utils = new utils();
        sensor_actuator[] ret = new sensor_actuator[5];
        ret[0] = utils.getSearch().getIObyName(aRemover.getValue(), io);
        ret[1] = utils.getSearch().getIObyName(aEmitter.getValue(), io);
        ret[2] = utils.getSearch().getIObyName(sRemover.getValue(), io);
        ret[3] = utils.getSearch().getIObyName(sEmitter.getValue(), io);
        ret[4] = utils.getSearch().getIObyName(aConveyor.getValue(), io);

        return ret;
    }


}
