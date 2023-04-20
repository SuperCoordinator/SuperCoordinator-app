package viewers.controllers;

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

import viewers.controllers.SFEM.C_SFEM_transport;
import viewers.controllers.SFEM.C_SFEM_production;
import viewers.controllers.SFEM.C_SFEM_connection;

public class C_ShopFloor {

    private final ArrayList<C_SFEM_production> cSfemProductions = new ArrayList<>();
    private final ArrayList<C_SFEM_transport> cSfemTransports = new ArrayList<>();

    private String layout_name;

    private boolean loadedConfig = false;

    private C_SFEM_production current_C_SFEMProduction;

    private C_SFEM_transport current_C_SFEMTransport;

    public C_ShopFloor() {
    }

    public C_ShopFloor(String layout_name) {
        this.layout_name = layout_name;
    }

    public ArrayList<C_SFEM_production> getcSfemProductions() {
        return cSfemProductions;
    }

    public ArrayList<C_SFEM_transport> getcSfemTransports() {
        return cSfemTransports;
    }

    public void setLoadedConfig(boolean loadedConfig) {
        this.loadedConfig = loadedConfig;
    }

    public boolean isLoadedConfig() {
        return loadedConfig;
    }

    public void setCurrent_C_SFEM(C_SFEM_production current_C_SFEMProduction) {
        this.current_C_SFEMProduction = current_C_SFEMProduction;
    }

    public C_SFEM_production getCurrent_C_SFEM() {
        return current_C_SFEMProduction;
    }

    public C_SFEM_transport getCurrent_C_SFEMTransport() {
        return current_C_SFEMTransport;
    }

    public void setCurrent_C_SFEMTransport(C_SFEM_transport current_C_SFEMTransport) {
        this.current_C_SFEMTransport = current_C_SFEMTransport;
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
            loader.setController(C_ShopFloor.getInstance().isLoadedConfig() ? C_ShopFloor.getInstance().getCurrent_C_SFEM() : new C_SFEM_production());
            AnchorPane anchorPane = loader.load();
            pane.getChildren().setAll(anchorPane);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadData(ArrayList<cSFEM_production> cSFEM_Productions) {
        for (cSFEM_production cSFEMProduction : cSFEM_Productions) {
            C_SFEM_production cSfem = new C_SFEM_production(cSFEMProduction);
            C_ShopFloor.getInstance().getcSfemProductions().add(cSfem);
//            System.out.println(C_ShopFloor.class + " " + cSFEMProduction.getSfem().getName());
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
