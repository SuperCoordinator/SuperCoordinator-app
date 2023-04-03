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
import models.base.SFEI;
import models.sensor_actuator;
import utils.utils;
import viewers.mediators.CM_SFEI;

import java.net.URL;
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
    private ToggleGroup end_item;
    @FXML
    private ToggleGroup failures_support;
    @FXML
    private ToggleGroup start_item;

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
            values_str.add(entry.getValue().getBit_offset() + " - " + entry.getValue().getName());
        input_sensor.setItems(FXCollections.observableArrayList(values_str));
        output_sensor.setItems(FXCollections.observableArrayList(values_str));
    }

    @FXML
    void comboBoxPressed(ActionEvent event) {
        ComboBox<String> temp = (ComboBox<String>) event.getSource();

        switch (temp.getId()) {
            case "sfei_type" -> {
                System.out.println(temp.getId());
                show_failure_support_pane(event);
            }
            case "input_sensor" -> {
                System.out.println(temp.getId());
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
                    System.out.println("ENTROU NO CONVEYOR COMO È SUPOSTO");
                    C_SFEI_conveyor controller = new C_SFEI_conveyor(io);
                    registerC_SFEI_conveyor(controller);
                    loader.setController(controller);
                } else {
                    C_SFEI_machine controller = new C_SFEI_machine(io);
                    registerC_SFEI_machine(controller);
                    loader.setController(controller);
                }

                AnchorPane pane = loader.load();
                failures_pane.getChildren().setAll(pane);

            } else {
                AnchorPane pane = new AnchorPane();
                failures_pane.getChildren().setAll(pane);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    void buttonPressed(ActionEvent event){
        Button temp = (Button) event.getSource();
        if(temp.getId().equalsIgnoreCase("add")){
            // Add new SFEI
        }
    }

}
