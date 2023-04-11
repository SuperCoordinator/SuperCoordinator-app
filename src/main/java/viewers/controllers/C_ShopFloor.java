package viewers.controllers;

import controllers.production.cSFEE_production;
import controllers.production.cSFEM_production;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.event.ActionEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.ArrayList;

import utils.serialize.production;
import viewers.controllers.SFEE.C_SFEE_communication;
import viewers.controllers.SFEE.C_SFEE_properties;
import viewers.controllers.SFEE.C_SFEEs;
import viewers.controllers.SFEM.C_SFEM;
import viewers.controllers.SFEM.C_SFEM_connection;

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

    public void loadData(production production) {
        for (cSFEM_production cSFEMProduction : production.getC_Production()) {
            C_SFEM cSfem = new C_SFEM(cSFEMProduction);
/*            for (cSFEE_production cSFEEProduction : cSFEMProduction.getSfeeControllers()) {
                C_SFEEs cSfees = new C_SFEEs();
                cSfees.setcSFEEProduction(cSFEEProduction);
                cSfees.getSFEE_name().setText(cSFEEProduction.getSFEE_name());
                cSfem.registerC_SFEEs(cSfees);
            }*/
            C_ShopFloor.getInstance().getCmSfems().add(cSfem);
            System.out.println(C_ShopFloor.class + " " + cSFEMProduction.getSfem().getName());
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
