package viewers.controllers.SFEE;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import models.sensor_actuator;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;


public class C_SFEE {

    private enum Panes {
        PROPERTIES,
        COMMUNICATION,
        FAILURE,
        FINISH;

    }

    private Panes activePane;

    @FXML
    private ToggleButton importCSV;
    @FXML
    private ToggleButton manually;

    @FXML
    private TableView<sensor_actuator> inputsTable;

    @FXML
    private TableView<sensor_actuator> outputsTable;

    @FXML
    private Pane paneFailuresMode;

    @FXML
    private Pane SFEE_body_properties;
    @FXML
    private AnchorPane SFEE_body_communication;
    @FXML
    private Pane SFEE_body_failure;
    @FXML
    private Pane SFEE_body_finish;
    @FXML
    private ToggleButton toggleProperties;
    @FXML
    private ToggleButton toggleComunication;

    @FXML
    private ToggleButton toggleFailure;

    @FXML
    private ToggleButton toggleFinish;
    @FXML
    private ToggleGroup bar;

    @FXML
    private Button next;

    @FXML
    public void buttonPressed(ActionEvent event) {

        ToggleButton temp = (ToggleButton) event.getSource();

        try {
            switch (temp.getText()) {
                case "Properties" -> {
                    activePane = Panes.PROPERTIES;
                    next.setVisible(true);
                }
                case "Communication" -> {
                    activePane = Panes.COMMUNICATION;
                }
                case "Failure" -> {
                    activePane = Panes.FAILURE;
                }
                case "Finish" -> {
                    activePane = Panes.FINISH;
                }
                case "Failures" -> {
                    paneFailuresMode.setVisible(true);
                }
                case "Normal" -> {
                    paneFailuresMode.setVisible(false);
                }
            }

            setPane();

        } catch (
                Exception e) {
            e.printStackTrace();
        }

    }

    @FXML
    void goBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/SFEM_layout.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void goNext(ActionEvent event) {
        ToggleButton selected = (ToggleButton) bar.getSelectedToggle();
        switch (selected.getText()) {
            case "Properties" -> {
                activePane = Panes.COMMUNICATION;
                toggleComunication.requestFocus();
                bar.selectToggle(toggleComunication);
            }
            case "Communication" -> {
                activePane = Panes.FAILURE;
                toggleFailure.requestFocus();
                bar.selectToggle(toggleFailure);
            }
            case "Failure" -> {
                activePane = Panes.FINISH;
                toggleFinish.requestFocus();
                bar.selectToggle(toggleFinish);
            }
            case "Finish" -> {
                // to do
            }

        }
        setPane();

    }

    private void setPane() {
        SFEE_body_properties.setVisible(activePane.equals(Panes.PROPERTIES));
        SFEE_body_communication.setVisible(activePane.equals(Panes.COMMUNICATION));
        SFEE_body_failure.setVisible(activePane.equals(Panes.FAILURE));
        SFEE_body_finish.setVisible(activePane.equals(Panes.FINISH));


    }
}
