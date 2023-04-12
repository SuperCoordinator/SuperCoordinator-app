package viewers.controllers;

import controllers.production.cSFEM_production;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javafx.event.ActionEvent;
import utils.serialize.serializer;
import viewers.controllers.SFEM.C_SFEM;

import java.io.File;
import java.io.IOException;

public class C_Homepage {

    @FXML
    private Button loadConfig;
    @FXML
    private Button newConfig;

    public void initialize() {
        // clean memory
        C_ShopFloor.getInstance().getCmSfems().clear();

    }

    public void handleNewConfig(ActionEvent event) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ShopFloor.fxml"));
            loader.setController(C_ShopFloor.getInstance());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
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
//            f_chooser.setInitialDirectory(new File(System.getProperty("user.dir")));
            f_chooser.setInitialDirectory(new File("C:\\Users\\danie\\Documents\\GitHub\\SC-sketch\\blocks\\sorting_station\\saves\\tests"));

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            File file = f_chooser.showOpenDialog(stage);
            if (file != null)
                System.out.println(file.getPath());
            else
                throw new RuntimeException("File is Null");

            serializer serializer = new serializer();

            serializer.loadXML_prod();
            for (cSFEM_production production : serializer.getProduction().getC_Production()) {
                production.init_after_XML_loading();
            }

            // To be substituted by the SFEM controller viewer
            // The Mediator should be the new SFEM viewer


            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ShopFloor.fxml"));
            C_ShopFloor.getInstance().loadData(serializer.getProduction());
            C_ShopFloor.getInstance().setLoadedConfig(true);

            C_ShopFloor.getInstance().setCurrent_C_SFEM(C_ShopFloor.getInstance().getCmSfems().get(0));
            loader.setController(C_ShopFloor.getInstance());

//            Parent root = FXMLLoader.load(getClass().getResource("/fxml/ShopFloor.fxml"));
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(loader.load(), 1200, 900));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}