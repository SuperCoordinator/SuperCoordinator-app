package viewers.controllers;

import controllers.production.cSFEM_production;
import controllers.transport.cSFEM_transport;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javafx.event.ActionEvent;
import models.base.SFEE;
import org.apache.commons.math3.util.Pair;
import utility.serialize.serializer;

import java.io.File;

public class C_Homepage {

    @FXML
    private Button loadConfig;
    @FXML
    private Button newConfig;

    public void initialize() {
        // clean memory
        C_ShopFloor.getInstance().getcSfemProductions().clear();

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
            f_chooser.setInitialDirectory(new File("C:\\Users\\danie\\Documents\\GitHub\\SC-sketch\\blocks\\SS_3CMC\\saves"));

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            File file = f_chooser.showOpenDialog(stage);
/*            if (file != null)
                System.out.println(file.getPath());
            else
                throw new RuntimeException("File is Null");*/
            if (file == null)
                throw new RuntimeException("File is Null");

            serializer.getInstance().loadXML();
            for (cSFEM_production production : serializer.getInstance().getC_Production()) {
                production.init_after_XML_loading();
            }

            for (cSFEM_transport transport : serializer.getInstance().getC_Transport()) {
                transport.init_after_XML_load();
            }

            // Set up the connections between SFEEs
            for (cSFEM_transport transport : serializer.getInstance().getC_Transport()) {
                Pair<Pair<String, String>, Pair<String, String>> names = transport.getPrevNext_SFEE_SFEI_names();

                Pair<SFEE, cSFEM_production> inSFEE = serializer.getInstance().searchSFEEbyName(names.getFirst().getFirst());
                Pair<SFEE, cSFEM_production> outSFEE = serializer.getInstance().searchSFEEbyName(names.getSecond().getFirst());

                transport.setupSFEETransportController(
                        inSFEE.getSecond().searchMBbySFEE(inSFEE.getFirst().getName()),
                        outSFEE.getSecond().searchMBbySFEE(outSFEE.getFirst().getName()),
                        serializer.getInstance().searchSFEIbySFEE(inSFEE.getFirst(), names.getFirst().getSecond()),
                        serializer.getInstance().searchSFEIbySFEE(outSFEE.getFirst(), names.getSecond().getSecond()));

            }

            // To be substituted by the SFEM controller viewer
            // The Mediator should be the new SFEM viewer

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ShopFloor.fxml"));
            C_ShopFloor.getInstance().loadData(serializer.getInstance().getC_Production(), serializer.getInstance().getC_Transport());
            C_ShopFloor.getInstance().setLoadedConfig(true);

            C_ShopFloor.getInstance().setCurrent_C_SFEM(C_ShopFloor.getInstance().getcSfemProductions().get(0));
            loader.setController(C_ShopFloor.getInstance());

            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(loader.load(), 1200, 900));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}