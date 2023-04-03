package viewers.controllers.SFEE;

import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;

import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.sensor_actuator;
import utils.utils;

import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;

public class C_SFEE_communication /*extends Application*/ implements Initializable {

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

        inputsTable.setFixedCellSize(30.0);

        outName.setCellValueFactory(new PropertyValueFactory<>("name"));
        outDataType.setCellValueFactory(new PropertyValueFactory<>("dataType"));
        outAddress.setCellValueFactory(new PropertyValueFactory<>("addressType"));
        outBit.setCellValueFactory(new PropertyValueFactory<>("bit_offset"));

        outputsTable.setFixedCellSize(30.0);
    }


    public C_SFEE_communication() {
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

    private File file;
    private TreeMap<Integer, sensor_actuator> io;

    public TreeMap<Integer, sensor_actuator> getIo() {
        return io;
    }

    private final utils utility = new utils();

    @FXML
    void importCSV(ActionEvent event) {
        FileChooser f_chooser = new FileChooser();

        // set title
        f_chooser.setTitle("Select File");

        // set initial File
        f_chooser.setInitialDirectory(new File(System.getProperty("user.dir")));

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        file = f_chooser.showOpenDialog(stage);
        if (file != null)
            System.out.println(file.getPath());


        io = utility.getReader().readModbusTags(file.getPath(), 0, false);
        updateTables();
    }

    @FXML
    void importManually(ActionEvent event) {

    }


    private String errorMsg;

    public boolean validation_moveON() {
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
}
