package viewers.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import javafx.event.ActionEvent;

import java.io.IOException;

public class C_Homepage {

    @FXML
    private Button loadConfig;

    @FXML
    private Button newConfig;

    public void handleNewConfig(ActionEvent event) throws IOException {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/SFEM_layout.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 900));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleLoadConfig(ActionEvent event) {

    }

}