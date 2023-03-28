package viewers.controllers.SFEE;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import javafx.scene.control.ToggleGroup;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.sensor_actuator;

import java.io.File;

public class C_SFEE_communication {

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
    private TableView<sensor_actuator> outputsTable;

    public C_SFEE_communication() {
    }

    private File file;

    @FXML
    void importCSV(ActionEvent event) {
        FileChooser f_chooser = new FileChooser();

        // set title
        f_chooser.setTitle("Select File");

        // set initial File
        f_chooser.setInitialDirectory(new File("c:\\"));

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        file = f_chooser.showOpenDialog(stage);
        if (file != null)
            System.out.println(file.getPath());

    }

    @FXML
    void importManually(ActionEvent event) {

    }



}
