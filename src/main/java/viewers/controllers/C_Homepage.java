package viewers.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javafx.event.ActionEvent;
import utils.serialize.serializer;

import java.io.File;
import java.io.IOException;

public class C_Homepage {

    @FXML
    private Button loadConfig;
    @FXML
    private Button newConfig;

    public void handleNewConfig(ActionEvent event) throws IOException {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SFEM_layout.fxml"));
            loader.setController(new C_SFEM_layout(""));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(loader.load(), 1200, 900));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleLoadConfig(ActionEvent event) {
        try {

            FileChooser f_chooser = new FileChooser();

            // set title
            f_chooser.setTitle("Select Production File");

            // set initial File
            f_chooser.setInitialDirectory(new File(System.getProperty("user.dir")));

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            File file = f_chooser.showOpenDialog(stage);
            if (file != null)
                System.out.println(file.getPath());
            else
                throw new RuntimeException("File is Null");

            serializer serializer = new serializer();

            serializer.deserialize_prod(file.getPath());

            // To be substituted by the SFEM controller viewer
            // The Mediator should be the new SFEM viewer
            C_SFEM_layout sfemLayout = new C_SFEM_layout(serializer.getProduction().getC_Production().get(0).getSfem().getName());

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SFEM_layout.fxml"));
            loader.setController(sfemLayout);

//            Parent root = FXMLLoader.load(getClass().getResource("/fxml/SFEM_layout.fxml"));
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(loader.load(), 1200, 900));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}