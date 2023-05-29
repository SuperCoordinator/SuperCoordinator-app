package viewers.UIcontrollers.SFEI;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import models.SFEx.SFEI_conveyor;
import models.SFEx.SFEI_machine;
import models.base.SFEE;
import models.base.SFEI;
import models.partDescription;
import models.sensor_actuator;
import viewers.UIcontrollers.C_ShopFloor;
import viewers.UIcontrollers.mediators.CM_SFEI;
import utility.utils;

import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class C_SFEIs extends CM_SFEI implements Initializable {

    private final TreeMap<Integer, sensor_actuator> io;
    private C_SFEI_conveyor cSfeiConveyor;
    private C_SFEI_machine cSfeiMachine;

    private SFEE.SFEE_environment sfee_environment;

    public C_SFEIs(TreeMap<Integer, sensor_actuator> io, SFEE.SFEE_environment sfee_environment) {
        this.io = io;
        this.sfee_environment = sfee_environment;
        this.editMode = false;
    }

    private boolean editMode;
    private String sfei2updateName;

    public void activeEditMode(String sfei2updateName) {
        this.editMode = true;
        this.sfei2updateName = sfei2updateName;
    }

    public void deactivateEditMode() {
        this.editMode = false;
    }


    @FXML
    private TextField sfeiName;
    @FXML
    private Text title;
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
    private Pane sfeiType_pane;

    @FXML
    private ToggleGroup end_item;
    @FXML
    private ToggleGroup start_item;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        if (editMode)
            title.setText("Update Shop Floor Educational Item");
        else
            title.setText("New Shop Floor Educational Item");


        SFEI.SFEI_type[] values = SFEI.SFEI_type.values();
        ArrayList<String> values_str = new ArrayList<>();
        for (SFEI.SFEI_type value : values)
            values_str.add(value.toString());
        sfei_type.setItems(FXCollections.observableArrayList(values_str));


        values_str.clear();
        TreeMap<Integer, sensor_actuator> inputs = utils.getInstance().getSearch().getSensorsOrActuators(io, true);
        for (Map.Entry<Integer, sensor_actuator> entry : inputs.entrySet())
            values_str.add(/*entry.getValue().getBit_offset() + " - " +*/ entry.getValue().getName());
        input_sensor.setItems(FXCollections.observableArrayList(values_str));
        output_sensor.setItems(FXCollections.observableArrayList(values_str));

        if (!editMode) {
            // Initialize both, then only one will be added in the arrays
            cSfeiConveyor = new C_SFEI_conveyor(io);
            cSfeiMachine = new C_SFEI_machine(io);
        } else {
            // From "load config"
            if (C_ShopFloor.getInstance().isLoadedConfig()) {
                //search for cSfeiConveyor/cSfeiMachine correct instance

                for (C_SFEI_conveyor conveyor : getSfeiConveyors()) {
                    if (conveyor.getSfeiConveyor().getName().equalsIgnoreCase(sfei2updateName)) {
                        cSfeiConveyor = conveyor;
                        break;
                    }
                }
                if (cSfeiConveyor == null) {
                    for (C_SFEI_machine machine : getSfeiMachines()) {
                        if (machine.getSfeiMachine().getName().equalsIgnoreCase(sfei2updateName)) {
                            cSfeiMachine = machine;
                            break;
                        }
                    }
                }
            }
            // Search in the conveyors
            SFEI_conveyor sfeiConveyor = null;
            SFEI_machine sfeiMachine = null;

            for (C_SFEI_conveyor conveyor : getSfeiConveyors()) {
                if (conveyor.getSfeiConveyor().getName().equalsIgnoreCase(sfei2updateName)) {
                    sfeiConveyor = conveyor.getSfeiConveyor();
                    break;
                }
            }
            if (sfeiConveyor == null) {
                // and then in the machine
                for (C_SFEI_machine machine : getSfeiMachines()) {
                    if (machine.getSfeiMachine().getName().equalsIgnoreCase(sfei2updateName)) {
                        sfeiMachine = machine.getSfeiMachine();
                        break;
                    }
                }
            }
            editModeInitialize(sfeiConveyor, sfeiMachine);
        }

    }

    private void editModeInitialize(SFEI_conveyor sfeiConveyor, SFEI_machine sfeiMachine) {

        if (sfeiConveyor == null && sfeiMachine == null)
            throw new RuntimeException("(" + C_SFEIs.class + ") sfeiMachine and sfeiConveyor both null");

        SFEI sfei = Objects.requireNonNullElse(sfeiConveyor, sfeiMachine);

        sfeiName.setText(sfei.getName());
        sfei_type.setValue(sfei.getSfeiType().equals(SFEI.SFEI_type.CONVEYOR) ? "CONVEYOR" : "MACHINE");
        sfei_type.setDisable(true);
        input_sensor.setValue(sfei.getInSensor().getName());
        output_sensor.setValue(sfei.getOutSensor().getName());
        manufacturing_date.setValue(LocalDate.ofInstant(sfei.getDayOfBirth(), ZoneId.systemDefault()));
        last_maintenance_date.setValue(LocalDate.ofInstant(sfei.getDayOfLastMaintenance(), ZoneId.systemDefault()));
        if (sfei.isLine_start()) {
            if (((RadioButton) start_item.getToggles().get(0)).getText().equalsIgnoreCase("yes"))
                start_item.selectToggle(start_item.getToggles().get(0));
            else
                start_item.selectToggle(start_item.getToggles().get(1));
        }

        if (sfei.isLine_end()) {
            if (((RadioButton) end_item.getToggles().get(0)).getText().equalsIgnoreCase("yes"))
                end_item.selectToggle(end_item.getToggles().get(0));
            else
                end_item.selectToggle(end_item.getToggles().get(1));
        }
        if (sfei.getSfeiType().equals(SFEI.SFEI_type.CONVEYOR)) {
            cSfeiConveyor.activeEditMode();
        } else {
            cSfeiMachine.activeEditMode();
        }
        showSFEITypePane(new ActionEvent());

    }

    @FXML
    void comboBoxPressed(ActionEvent event) {
        ComboBox<String> temp = (ComboBox<String>) event.getSource();

        if (temp.getId().equals("sfei_type")) {
            showSFEITypePane(event);
        }

    }

    private void showSFEITypePane(ActionEvent event) {
        try {

            // Load the sfei_type pane
            String type = sfei_type.getValue().toLowerCase();
            if (!type.equalsIgnoreCase("conveyor") && !type.equalsIgnoreCase("machine"))
                return;

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SFEI/SFEI_type_" + type + ".fxml"));
            if (type.equalsIgnoreCase("conveyor")) {
//                    registerC_SFEI_conveyor(cSfeiConveyor);
                if (editMode)
                    cSfeiConveyor.activeEditMode();
                loader.setController(cSfeiConveyor);
            } else {
//                    registerC_SFEI_machine(cSfeiMachine);
                loader.setController(cSfeiMachine);
            }

            AnchorPane pane = loader.load();
            sfeiType_pane.getChildren().setAll(pane);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String errorMsg = "Missing fields: \n";

    public boolean verifyFields() {
        boolean error = false;
        if (sfeiName.getText().isBlank()) {
            error = true;
            errorMsg = errorMsg.concat("  - SFEI name \n");
        }
        if (sfei_type.getValue() == null) {
            error = true;
            errorMsg = errorMsg.concat("  - SFEI Type \n");
        }
        if (input_sensor.getValue() == null) {
            error = true;
            errorMsg = errorMsg.concat("  - Input Sensor \n");
        }
        if (output_sensor.getValue() == null) {
            error = true;
            errorMsg = errorMsg.concat("  - Output Sensor \n");
        }
        if (manufacturing_date.getValue() == null) {
            error = true;
            errorMsg = errorMsg.concat("  - Manufacturing Date \n");
        }
        if (last_maintenance_date.getValue() == null) {
            error = true;
            errorMsg = errorMsg.concat("  - Last Maintenance Date \n");
        }

        // Compose verifications
        if (sfei_type.getValue() != null) {
            if (sfei_type.getValue().equalsIgnoreCase("conveyor")) {

                sensor_actuator[] vect = cSfeiConveyor.getSensAct();
                if (vect[6] == null) {
                    error = true;
                    errorMsg = errorMsg.concat("  - Conveyor Motor \n");
                }

                if (cSfeiConveyor.getFailures_support()) {
                    if (vect[0] == null) {
                        error = true;
                        errorMsg = errorMsg.concat("  - Remover \n");
                    }
                    if (vect[1] == null) {
                        error = true;
                        errorMsg = errorMsg.concat("  - Emit \n");
                    }
                    if (vect[2] == null) {
                        error = true;
                        errorMsg = errorMsg.concat("  - Emit Part \n");
                    }
                    if (vect[3] == null) {
                        error = true;
                        errorMsg = errorMsg.concat("  - Emit Base \n");
                    }
                    if (vect[4] == null) {
                        error = true;
                        errorMsg = errorMsg.concat("  - Remover sensor \n");
                    }
                    if (vect[5] == null) {
                        error = true;
                        errorMsg = errorMsg.concat("  - Emitter sensor \n");
                    }
                }

            } else if (sfei_type.getValue().equalsIgnoreCase("machine")) {

                sensor_actuator[] vect = cSfeiMachine.getSensAct();
                if (vect[0] == null) {
                    error = true;
                    errorMsg = errorMsg.concat("  - Produce Bit \n");
                }

                if (cSfeiMachine.getFailures_support()) {
                    if (vect[1] == null) {
                        error = true;
                        errorMsg = errorMsg.concat("  - Machine Door sensor \n");
                    }
                    if (vect[2] == null) {
                        error = true;
                        errorMsg = errorMsg.concat("  - Stop Actuator \n");
                    }
                }
            }
        }

        return error;
    }

    public String getErrorMsg() {
        String e = errorMsg;
        errorMsg = "Missing fields: \n";
        return e;
    }

    public void add_updateSFEI(String name) {

        switch (sfei_type.getValue().toLowerCase()) {
            // Add new SFEI
            case "conveyor" -> {

//                SFEI_conveyor newObj = new SFEI_conveyor(
//                        sfeiName.getText(),
//                        utils.getInstance().getSearch().getIObyName(input_sensor.getValue(), io),
//                        utils.getInstance().getSearch().getIObyName(output_sensor.getValue(), io),
//                        Instant.from(manufacturing_date.getValue().atStartOfDay(ZoneId.systemDefault())),
//                        Instant.from(last_maintenance_date.getValue().atStartOfDay(ZoneId.systemDefault())),
//                        sfee_environment.equals(SFEE.SFEE_environment.SIMULATION),
//                        cSfeiConveyor.getFailures_support(),
//                        ((RadioButton) start_item.getSelectedToggle()).getText().equalsIgnoreCase("yes"),
//                        ((RadioButton) end_item.getSelectedToggle()).getText().equalsIgnoreCase("yes"),
//                        cSfeiConveyor.getSensAct());

//                cSfeiConveyor.setSfeiConveyor(newObj);
                if (name.isBlank()) {
                    registerC_SFEI_conveyor(cSfeiConveyor);
                } else {
                    int pos = -1;
                    for (int i = 0; i < getSfeiConveyors().size(); i++) {
                        if (getSfeiConveyors().get(i).getSfeiConveyor().getName().equalsIgnoreCase(name)) {
                            pos = i;
                            break;
                        }
                    }
                    if (pos > -1) {
                        updateC_SFEI_conveyor(pos, cSfeiConveyor);
                    }

                }
            }
            case "machine" -> {

//                SFEI_machine newObj = new SFEI_machine(
//                        sfeiName.getText(),
//                        new partDescription(partDescription.material.UNKNOWN, cSfeiMachine.getPart_type()),
//                        utils.getInstance().getSearch().getIObyName(input_sensor.getValue(), io),
//                        utils.getInstance().getSearch().getIObyName(output_sensor.getValue(), io),
//                        Instant.from(manufacturing_date.getValue().atStartOfDay(ZoneId.systemDefault())),
//                        Instant.from(last_maintenance_date.getValue().atStartOfDay(ZoneId.systemDefault())),
//                        sfee_environment.equals(SFEE.SFEE_environment.SIMULATION),
//                        cSfeiMachine.getFailure_support(),
//                        ((RadioButton) start_item.getSelectedToggle()).getText().equalsIgnoreCase("yes"),
//                        ((RadioButton) end_item.getSelectedToggle()).getText().equalsIgnoreCase("yes"),
//                        cSfeiMachine.getSensAct());
//
//                cSfeiMachine.setSfeiMachine(newObj);
                if (name.isBlank()) {
                    registerC_SFEI_machine(cSfeiMachine);
                } else {
                    int pos = -1;
                    for (int i = 0; i < getSfeiConveyors().size(); i++) {
                        if (getSfeiMachines().get(i).getSfeiMachine().getName().equalsIgnoreCase(name)) {
                            pos = i;
                            break;
                        }
                    }
                    if (pos > -1)
                        updateC_SFEI_machine(pos, cSfeiMachine);
                }
            }
        }

    }

    public void removeSFEI(String sfeiName) {

        if (getSfeiConveyors().removeIf(conveyor -> conveyor.getSfeiConveyor().getName().equalsIgnoreCase(sfeiName)))
            return;

        getSfeiMachines().removeIf(conveyor -> conveyor.getSfeiMachine().getName().equalsIgnoreCase(sfeiName));

    }


}



