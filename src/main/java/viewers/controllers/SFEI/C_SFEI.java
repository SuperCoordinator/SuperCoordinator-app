package viewers.controllers.SFEI;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
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

    private C_SFEI_conveyor cSfeiConveyor;
    private C_SFEI_machine cSfeiMachine;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        title.setText("New Shop Floor Educational Item");

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

        if (temp.getId().equals("sfei_type")) {
            show_failure_support_pane(event);
        }

    }

    @FXML
    void show_failure_support_pane(ActionEvent event) {
        try {

            // Load the sfei_type pane
            String type = sfei_type.getValue().toLowerCase();
            if (!type.equalsIgnoreCase("conveyor") && !type.equalsIgnoreCase("machine"))
                return;

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SFEI_type_" + type + ".fxml"));
            if (type.equalsIgnoreCase("conveyor")) {
//                    registerC_SFEI_conveyor(cSfeiConveyor);
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

    private final utils utility = new utils();

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
                if (vect[4] == null) {
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
                        errorMsg = errorMsg.concat("  - Emitter \n");
                    }
                    if (vect[2] == null) {
                        error = true;
                        errorMsg = errorMsg.concat("  - Remover sensor \n");
                    }
                    if (vect[3] == null) {
                        error = true;
                        errorMsg = errorMsg.concat("  - Emitter sensor \n");
                    }
                }

            } else if (sfei_type.getValue().equalsIgnoreCase("machine")) {

                sensor_actuator[] vect = cSfeiMachine.getSensAct();
                if (vect[2] == null) {
                    error = true;
                    errorMsg = errorMsg.concat("  - Produce Bit \n");
                }

                if (cSfeiMachine.getFailures_support()) {
                    if (vect[0] == null) {
                        error = true;
                        errorMsg = errorMsg.concat("  - Machine Door sensor \n");
                    }
                    if (vect[1] == null) {
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

    public void addSFEI() {
        switch (sfei_type.getValue().toLowerCase()) {
            // Add new SFEI
            case "conveyor" -> {

                SFEI_conveyor newObj = new SFEI_conveyor(
                        sfeiName.getText(),
                        SFEI.SFEI_type.CONVEYOR,
                        utility.getSearch().getIObyName(input_sensor.getValue(), io),
                        utility.getSearch().getIObyName(output_sensor.getValue(), io),
                        Instant.from(manufacturing_date.getValue().atStartOfDay(ZoneId.systemDefault())),
                        Instant.from(last_maintenance_date.getValue().atStartOfDay(ZoneId.systemDefault())),
                        CM_SFEE.getInstance().getProperties().getEnvironment().equalsIgnoreCase("simulation"),
                        cSfeiConveyor.getFailures_support(),
                        ((ToggleButton) start_item.getSelectedToggle()).getText().equals("yes"),
                        ((ToggleButton) end_item.getSelectedToggle()).getText().equals("yes"),
                        cSfeiConveyor.getSensAct());

                cSfeiConveyor.setSfeiConveyor(newObj);
                registerC_SFEI_conveyor(cSfeiConveyor);


            }
            case "machine" -> {

                SFEI_machine newObj = new SFEI_machine(
                        sfeiName.getText(),
                        SFEI.SFEI_type.MACHINE,
                        cSfeiMachine.getPart_type(),
                        utility.getSearch().getIObyName(input_sensor.getValue(), io),
                        utility.getSearch().getIObyName(output_sensor.getValue(), io),
                        Instant.from(manufacturing_date.getValue().atStartOfDay(ZoneId.systemDefault())),
                        Instant.from(last_maintenance_date.getValue().atStartOfDay(ZoneId.systemDefault())),
                        CM_SFEE.getInstance().getProperties().getEnvironment().equalsIgnoreCase("simulation"),
                        cSfeiMachine.getFailure_support(),
                        ((ToggleButton) start_item.getSelectedToggle()).getText().equals("yes"),
                        ((ToggleButton) end_item.getSelectedToggle()).getText().equals("yes"),
                        cSfeiMachine.getSensAct());

                cSfeiMachine.setSfeiMachine(newObj);
                registerC_SFEI_machine(cSfeiMachine);

            }
        }

    }


}



