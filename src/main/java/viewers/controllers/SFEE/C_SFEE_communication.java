package viewers.controllers.SFEE;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;

import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.base.SFEE;
import models.sensor_actuator;
import utils.utils;
import viewers.controllers.C_ShopFloor;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;

public class C_SFEE_communication implements Initializable {

    private File file;
    private ArrayList<Object> savedValues;
    private TreeMap<Integer, sensor_actuator> io;

    private SFEE.communicationOption communicationOption;
    private String[] communicationFields;
    private final utils utility = new utils();

    public C_SFEE_communication() {
        this.savedValues = new ArrayList<>();
    }

    public TreeMap<Integer, sensor_actuator> getIo() {
        return io;
    }

    @FXML
    private ToggleGroup comProtocol;
    @FXML
    private TextField ip;
    @FXML
    private TextField port;
    @FXML
    private TextField slaveID;

    @FXML
    private ToggleGroup ioMap;

    @FXML
    private TableView<sensor_actuator> inputsTable;
    @FXML
    private TableColumn<sensor_actuator, String> inName;
    @FXML
    private TableColumn<sensor_actuator, String> inDataType;
    @FXML
    private TableColumn<sensor_actuator, Integer> inAddress;
    @FXML
    private TableColumn<sensor_actuator, Integer> inBit;
    @FXML
    private TableColumn<sensor_actuator, Boolean> inInvLogic;

    @FXML
    private TableView<sensor_actuator> outputsTable;
    @FXML
    private TableColumn<sensor_actuator, String> outName;
    @FXML
    private TableColumn<sensor_actuator, String> outDataType;
    @FXML
    private TableColumn<sensor_actuator, String> outAddress;
    @FXML
    private TableColumn<sensor_actuator, String> outBit;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        inName.setCellValueFactory(new PropertyValueFactory<>("name"));
        inDataType.setCellValueFactory(new PropertyValueFactory<>("dataType"));
        inAddress.setCellValueFactory(new PropertyValueFactory<>("addressType"));
        inBit.setCellValueFactory(new PropertyValueFactory<>("bit_offset"));
        inInvLogic.setCellValueFactory(new PropertyValueFactory<>("invLogic"));
        // Define cell height
        inputsTable.setFixedCellSize(30.0);

        outName.setCellValueFactory(new PropertyValueFactory<>("name"));
        outDataType.setCellValueFactory(new PropertyValueFactory<>("dataType"));
        outAddress.setCellValueFactory(new PropertyValueFactory<>("addressType"));
        outBit.setCellValueFactory(new PropertyValueFactory<>("bit_offset"));
        // Define cell height
        outputsTable.setFixedCellSize(30.0);

        if (!C_ShopFloor.getInstance().isLoadedConfig()) {
            if (savedValues.size() > 0) {
                // Load data from "new configuration"
                for (int i = 0; i < comProtocol.getToggles().size(); i++) {
                    if (((ToggleButton) comProtocol.getToggles().get(i)).getId().equals(savedValues.get(0)))
                        comProtocol.selectToggle(comProtocol.getToggles().get(i));
                }

                ip.setText((String) savedValues.get(1));
                port.setText((String) savedValues.get(2));
                slaveID.setText((String) savedValues.get(3));
                updateTables();
//            saveValues.clear();
            }
        } else {
            // Load data from "load configuration"
            for (int i = 0; i < comProtocol.getToggles().size(); i++) {
                if (((ToggleButton) comProtocol.getToggles().get(i)).getId().equalsIgnoreCase(communicationOption.name().toLowerCase()))
                    comProtocol.selectToggle(comProtocol.getToggles().get(i));
            }
            assert communicationFields != null;
            ip.setText(communicationFields[0]);
            port.setText(communicationFields[1]);
            slaveID.setText(communicationFields[2]);

            // For IOs, it is already loaded !
            updateTables();
        }

    }

    public void loadData(SFEE.communicationOption communicationOption, String[] communicationFields, TreeMap<Integer, sensor_actuator> IOs) {
        this.communicationOption = communicationOption;
        this.communicationFields = communicationFields;
        this.io = IOs;
    }


    @FXML
    void doubleClick() {
        sensor_actuator row = inputsTable.getSelectionModel().getSelectedItem();
        if (row == null)
            return;
        System.out.println(row.getName() + " " + row.getBit_offset());
        int idx = -1;
        for (Map.Entry<Integer, sensor_actuator> map : io.entrySet()) {
            if (map.getValue().equals(row))
                idx = map.getKey();
        }
        if (idx != -1)
            io.replace(idx, io.get(idx), io.get(idx).changeInvLogic(!io.get(idx).getInvLogic()));

        updateTables();
    }


    @FXML
    void importCSV(ActionEvent event) {
        FileChooser f_chooser = new FileChooser();

        // set title
        f_chooser.setTitle("Select File");

        // set initial File
//        f_chooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        f_chooser.setInitialDirectory(new File("C:\\Users\\danie\\Documents\\GitHub\\SC-sketch\\blocks\\sorting_station\\simulation"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        file = f_chooser.showOpenDialog(stage);
        if (file != null) {
            System.out.println(file.getPath());
            io = utility.getReader().readModbusTags(file.getPath(), 0, false);
            updateTables();
        }
    }

    @FXML
    void importManually(ActionEvent event) {

    }

    public SFEE.communicationOption getComOption() {
        return ((ToggleButton) comProtocol.getSelectedToggle()).getId().equalsIgnoreCase("modbus") ? SFEE.communicationOption.MODBUS : SFEE.communicationOption.OPC_UA;
    }


    private String errorMsg;

    public boolean validateMoveOn() {
        if (comProtocol.getSelectedToggle() == null ||
                ip.getText().isBlank() || port.getText().isBlank() || slaveID.getText().isBlank() ||
                io.size() == 0) {
            errorMsg = "Select all fields !";
            return false;
        }

        return true;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    private void updateTables() {
        ObservableList<sensor_actuator> list_in = FXCollections.observableArrayList(utility.getSearch().getSensorsOrActuators(io, true).values());
        inputsTable.setItems(list_in);

        ObservableList<sensor_actuator> list_out = FXCollections.observableArrayList(utility.getSearch().getSensorsOrActuators(io, false).values());
        outputsTable.setItems(list_out);

    }

    public void setSaveValues() {
        System.out.println("Saving COMMUNICATIONS");
        if (validateMoveOn()) {
            savedValues.add(0, ((ToggleButton) comProtocol.getSelectedToggle()).getId());
            savedValues.add(1, ip.getText());
            savedValues.add(2, port.getText());
            savedValues.add(3, slaveID.getText());
        }

    }


}
