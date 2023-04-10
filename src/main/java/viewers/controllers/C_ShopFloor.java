package viewers.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;

import javafx.event.ActionEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import models.SFEx_particular.SFEI_conveyor;
import models.base.SFEE;
import net.synedra.validatorfx.Validator;
import viewers.controllers.SFEE.C_SFEEs;
import viewers.controllers.SFEI.C_SFEI_conveyor;
import viewers.controllers.SFEI.C_SFEI_machine;
import viewers.controllers.SFEM.C_SFEM;
import viewers.controllers.SFEM.C_SFEM_connection;
import viewers.mediators.CM_SFEM;

public class C_ShopFloor {

    private final ArrayList<C_SFEM> cmSfems = new ArrayList<>();

    private String layout_name;

    public C_ShopFloor() {
    }

    public C_ShopFloor(String layout_name) {
        this.layout_name = layout_name;
    }

    public ArrayList<C_SFEM> getCmSfems() {
        return cmSfems;
    }

    /**
     * Everything below here is in support of Singleton pattern
     */
    public static C_ShopFloor getInstance() {
        return C_ShopFloor.C_ShopFloorHolder.INSTANCE;
    }

    private static class C_ShopFloorHolder {
        private static final C_ShopFloor INSTANCE = new C_ShopFloor();
    }

    /**
     * Until here !
     */

    @FXML
    private Pane pane;

    public void initialize() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SFEM/SFEM_elements.fxml"));
            loader.setController(new C_SFEM());
            AnchorPane anchorPane = loader.load();
            pane.getChildren().setAll(anchorPane);

        } catch (Exception e) {
            e.printStackTrace();
        }
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
    void goNext(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SFEM/SFEM_connection.fxml"));
            loader.setController(new C_SFEM_connection());
            AnchorPane anchorPane = loader.load();
            pane.getChildren().setAll(anchorPane);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
