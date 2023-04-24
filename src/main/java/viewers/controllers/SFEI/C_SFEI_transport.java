package viewers.controllers.SFEI;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import models.SFEx_particular.SFEI_transport;
import models.sensor_actuator;
import utility.utils;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class C_SFEI_transport {

    private SFEI_transport sfeiTransport;
    private TreeMap<Integer, sensor_actuator> io;

    private boolean editMode;

    public C_SFEI_transport() {
    }

    public C_SFEI_transport(TreeMap<Integer, sensor_actuator> io) {
        this.io = io;
    }

    public void setIo(TreeMap<Integer, sensor_actuator> io) {
        this.io = io;
    }

    public void activeEditMode() {
        this.editMode = true;
    }

    public SFEI_transport getSfeiTransport() {
        return sfeiTransport;
    }

    public void setSfeiTransport(SFEI_transport sfeiTransport) {
        this.sfeiTransport = sfeiTransport;
    }

    @FXML
    private ComboBox<String> remover_bit;
    @FXML
    private ComboBox<String> emit_base_bit;

    @FXML
    private ComboBox<String> emit_bit;

    @FXML
    private ComboBox<String> emit_part_bit;

    public void initialize() {
        if (!editMode) {
            ArrayList<String> values_str = new ArrayList<>();
            values_str.add("none");
            TreeMap<Integer, sensor_actuator> outputs = utils.getInstance().getSearch().getSensorsOrActuators(io, false);
            for (Map.Entry<Integer, sensor_actuator> entry : outputs.entrySet())
                values_str.add(entry.getValue().getName());
            remover_bit.setItems(FXCollections.observableArrayList(values_str));
            emit_bit.setItems(FXCollections.observableArrayList(values_str));
            emit_part_bit.setItems(FXCollections.observableArrayList(values_str));
            emit_base_bit.setItems(FXCollections.observableArrayList(values_str));
        } else {
            remover_bit.setValue(sfeiTransport.getaRemover().getName());
            emit_part_bit.setValue(sfeiTransport.getaEmitterPart().getName());
            emit_base_bit.setValue(sfeiTransport.getaEmitterBase().getName());
        }
    }

    private String errorMsg = "";

    public boolean validateMoveOn() {
        boolean error = false;
        if (remover_bit.getValue() == null) {
            error = true;
            errorMsg = errorMsg.concat("  - Remover bit \n");
        }
        if (emit_bit.getValue() == null) {
            error = true;
            errorMsg = errorMsg.concat("  - Emitter emit bit \n");
        }
        if (emit_part_bit.getValue() == null) {
            error = true;
            errorMsg = errorMsg.concat("  - Emitter part bit \n");
        }
        if (emit_base_bit.getValue() == null) {
            error = true;
            errorMsg = errorMsg.concat("  - Emitter base bit \n");
        }

        return error;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public sensor_actuator[] getSensAct() {
        sensor_actuator[] ret = new sensor_actuator[3];
        ret[0] = search_sensor_actuator(remover_bit);
        ret[1] = search_sensor_actuator(emit_base_bit);
        ret[2] = search_sensor_actuator(emit_base_bit);
        return ret;
    }

    private sensor_actuator search_sensor_actuator(ComboBox<String> comboBox) {
        if (comboBox.getValue() == null)
            return null;
        else
            return utils.getInstance().getSearch().getIObyName(comboBox.getValue(), io);
    }

}
