package viewers.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import javafx.event.ActionEvent;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import net.synedra.validatorfx.Validator;

public class C_SFEM_layout implements Initializable {

    @FXML
    private TextField sfem_name;
    @FXML
    private Button submit_name_btn;
    @FXML
    private ListView<String> listSFEMs;


    private final ArrayList<String> dummy = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dummy.add("SFEM0");
        dummy.add("SFEM1");
        dummy.add("SFEM2");
        listSFEMs.getItems().addAll(dummy);

    }

    @FXML
    void goBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/homepage.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void newSFEMname(ActionEvent event) {

        sfem_name.setVisible(true);
        submit_name_btn.setVisible(true);
    }

    @FXML
    void submitSFEM_name(ActionEvent event) {

        Validator validator = new Validator();

        validator.createCheck()
                .dependsOn("SFEM_name", sfem_name.textProperty())
                .withMethod(c -> {
                    String name = c.get("SFEM_name");
                    if (name.isBlank())
                        c.error("This field can not be empty");
                })
                .decorates(sfem_name)
                .immediate();

        if (validator.validate()) {

            dummy.add(sfem_name.getText());
            // ADD new SFEM
            listSFEMs.setItems(FXCollections.observableArrayList(dummy));
        }
    }

    @FXML
    void newSFEE(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/SFEE.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
