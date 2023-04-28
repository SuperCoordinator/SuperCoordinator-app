package viewers.controllers;

import controllers.production.cSFEM_production;
import controllers.transport.cSFEM_transport;
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

    public C_SFEM_production getCurrent_C_SFEMProduction() {
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
            loader.setController(C_ShopFloor.getInstance().isLoadedConfig() ? C_ShopFloor.getInstance().getCurrent_C_SFEMProduction() : new C_SFEM_production());
            AnchorPane anchorPane = loader.load();
            pane.getChildren().setAll(anchorPane);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadData(ArrayList<cSFEM_production> cSFEM_Productions, ArrayList<cSFEM_transport> cSFEM_Transports) {
        for (cSFEM_production cSFEMProduction : cSFEM_Productions) {
            C_SFEM_production cSfem = new C_SFEM_production(cSFEMProduction);
            C_ShopFloor.getInstance().getcSfemProductions().add(cSfem);

/*            if (!C_ShopFloor.getInstance().getcSfemProductions().contains(cSfem)) {
                C_ShopFloor.getInstance().getcSfemProductions().insert(cSfem);
            }*/
        }

        for (cSFEM_transport cSFEMTransport : cSFEM_Transports) {
            C_SFEM_transport cSfem = new C_SFEM_transport(cSFEMTransport);
            C_ShopFloor.getInstance().getcSfemTransports().add(cSfem);
/*            if (!C_ShopFloor.getInstance().getcSfemTransports().contains(cSfem)) {
                C_ShopFloor.getInstance().getcSfemTransports().insert(cSfem);
            }*/

        }

    }

    public void updateData(ArrayList<cSFEM_production> cProduction, ArrayList<cSFEM_transport> cTransport) {

        for (cSFEM_production cSFEMProduction : cProduction) {
            boolean exist = false;
            for (int j = 0; j < C_ShopFloor.getInstance().getcSfemProductions().size(); j++) {
                C_SFEM_production cSfemProduction = C_ShopFloor.getInstance().getcSfemProductions().get(j);
                if (cSfemProduction.getSfemName().equals(cSFEMProduction.getSfem().getName())) {
                    exist = true;
                    // update (Remove and insert in the same position)
                    C_ShopFloor.getInstance().getcSfemProductions().remove(j);
                    C_ShopFloor.getInstance().getcSfemProductions().add(j, new C_SFEM_production(cSFEMProduction));
                    break;
                }
            }
//            System.out.println(exist);
            if (!exist) {
                // Create new C_SFEM_Production
                C_ShopFloor.getInstance().getcSfemProductions().add(new C_SFEM_production(cSFEMProduction));
            }
        }

        for (cSFEM_transport cSFEMTransport : cTransport) {
            boolean exist = false;
            for (int j = 0; j < C_ShopFloor.getInstance().getcSfemTransports().size(); j++) {
                C_SFEM_transport cSfemTransport = C_ShopFloor.getInstance().getcSfemTransports().get(j);
                if (cSfemTransport.getSfemName().equals(cSFEMTransport.getSfem().getName())) {
                    exist = true;
                    // update (Remove and insert in the same position
                    C_ShopFloor.getInstance().getcSfemTransports().remove(j);
                    C_ShopFloor.getInstance().getcSfemTransports().add(j, new C_SFEM_transport(cSFEMTransport));
                    break;
                }
            }
//            System.out.println(exist);
            if (!exist) {
                // Create new C_SFEM_Transport
                C_ShopFloor.getInstance().getcSfemTransports().add(new C_SFEM_transport(cSFEMTransport));
            }
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
            // create cSFEM_production instances related to C_SFEM_production
            for(C_SFEM_production cSfemProduction : C_ShopFloor.getInstance().getcSfemProductions()){
                cSfemProduction.create_cSFEM_production();
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SFEM/SFEM_connection.fxml"));
            loader.setController(new C_SFEM_connection());
            AnchorPane anchorPane = loader.load();
            pane.getChildren().setAll(anchorPane);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }




}
