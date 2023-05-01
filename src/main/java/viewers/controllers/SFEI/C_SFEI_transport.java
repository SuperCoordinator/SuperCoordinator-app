package viewers.controllers.SFEI;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import models.SFEx_particular.SFEI_transport;
import models.sensor_actuator;
import utility.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

public class C_SFEI_transport {

    private SFEI_transport sfeiTransport;
    private TreeMap<Integer, sensor_actuator> inSFEI_io;
    private TreeMap<Integer, sensor_actuator> outSFEI_io;

//    private boolean editMode;

    public C_SFEI_transport() {
    }


    public void setIO(TreeMap<Integer, sensor_actuator> ioIO, TreeMap<Integer, sensor_actuator> outIO) {
        this.inSFEI_io = ioIO;
        this.outSFEI_io = outIO;
    }

//    public void setEditMode(boolean editMode) {
//        this.editMode = editMode;
//    }

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

        ArrayList<String> values_str = new ArrayList<>();
        values_str.add("none");
        TreeMap<Integer, sensor_actuator> outputs = utils.getInstance().getSearch().getSensorsOrActuators(inSFEI_io, false);
        for (Map.Entry<Integer, sensor_actuator> entry : outputs.entrySet())
            values_str.add(entry.getValue().getName());
        remover_bit.setItems(FXCollections.observableArrayList(values_str));

        values_str.clear();
        values_str.add("none");
        outputs = utils.getInstance().getSearch().getSensorsOrActuators(outSFEI_io, false);
        for (Map.Entry<Integer, sensor_actuator> entry : outputs.entrySet())
            values_str.add(entry.getValue().getName());
        emit_bit.setItems(FXCollections.observableArrayList(values_str));
        emit_part_bit.setItems(FXCollections.observableArrayList(values_str));
        emit_base_bit.setItems(FXCollections.observableArrayList(values_str));


        if (sfeiTransport != null) {
            remover_bit.setValue(sfeiTransport.getaRemover().getName());
            emit_bit.setValue(sfeiTransport.getaEmitter().getName());
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
        if (!error) {
            setSavedValues();
        }
        return error;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    private ArrayList<String> savedValues = new ArrayList<>();

    private void setSavedValues() {
        savedValues.add(0, remover_bit.getValue());
        savedValues.add(1, emit_bit.getValue());
        savedValues.add(2, emit_part_bit.getValue());
        savedValues.add(3, emit_base_bit.getValue());
    }

    public ArrayList<Object> getSensAct() {
        return new ArrayList<>(savedValues);
    }

}
