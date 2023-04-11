package viewers.controllers.SFEM;

import controllers.production.cSFEE_production;
import controllers.production.cSFEM_production;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import net.synedra.validatorfx.Validator;
import viewers.controllers.C_ShopFloor;
import viewers.controllers.SFEE.C_SFEEs;
import viewers.controllers.SFEI.C_SFEI_conveyor;
import viewers.controllers.SFEI.C_SFEI_machine;
import viewers.mediators.CM_SFEM;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class C_SFEM extends CM_SFEM implements Initializable {

    private String sfemName;
    private cSFEM_production cSFEMProduction;

    public C_SFEM() {
    }

    public C_SFEM(String sfemName) {
        this.sfemName = sfemName;
    }

    public C_SFEM(cSFEM_production cSFEMProduction) {
        this.cSFEMProduction = cSFEMProduction;
        this.sfemName = cSFEMProduction.getSfem().getName();
    }

    public String getSfemName() {
        return sfemName;
    }

    public cSFEM_production getcSFEMProduction() {
        return cSFEMProduction;
    }

    @FXML
    private TextField sfem_name;
    @FXML
    private Button submit_name_btn;
    @FXML
    private ListView<String> SFEMs_list;

    @FXML
    private TreeView<String> sfem_elements_tree;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        updateSFEMlist();
        updateElementsTreeView();
    }

    @FXML
    void buttonPressed(ActionEvent event) {
        try {
            Button temp = (Button) event.getSource();
            switch (temp.getId()) {
                case "new_sfee" -> {
                    // Get selected row -> C_SFEM instance

                    if (SFEMs_list.getSelectionModel().getSelectedIndices().size() == 1) {
                        C_SFEM cSfem = C_ShopFloor.getInstance().getCmSfems().get(SFEMs_list.getSelectionModel().getSelectedIndices().get(0));
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SFEE/SFEE.fxml"));
                        // create new C_SFEEs instance
                        C_SFEEs cSfees = new C_SFEEs();

                        loader.setController(cSfees);
                        cSfem.registerC_SFEEs(cSfees);
                        // open scene
                        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        stage.setScene(new Scene(loader.load()));
                        stage.show();
                    } else {
                        // ERROR
                        System.out.println("Select only 1 row!");
                    }


                }
            }
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
    void newSFEM(ActionEvent event) {

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
            C_ShopFloor.getInstance().getCmSfems().add(C_ShopFloor.getInstance().getCmSfems().size(), new C_SFEM(sfem_name.getText()));
            updateSFEMlist();
            sfem_name.clear();
            sfem_name.setVisible(false);
            submit_name_btn.setVisible(false);
        }
    }

    private void updateSFEMlist() {
        ArrayList<String> list = new ArrayList<>();
        for (C_SFEM cSfem : C_ShopFloor.getInstance().getCmSfems()) {
            System.out.println(C_SFEM.class + " " + cSfem.getSfemName());
            list.add(cSfem.getSfemName());
        }
        SFEMs_list.getItems().clear();
        SFEMs_list.getItems().addAll(list);
    }

    @FXML
    void updateElementsTreeView() {
        TreeItem<String> root = new TreeItem<>(" ");
        if (SFEMs_list.getSelectionModel().getSelectedIndices().size() == 1) {
            C_SFEM cSfem = C_ShopFloor.getInstance().getCmSfems().get(SFEMs_list.getSelectionModel().getSelectedIndices().get(0));

            if (cSfem.getSfeesControllers().size() == 0) {
                loadData(cSfem);
            }
            for (C_SFEEs cSfees : cSfem.getSfeesControllers()) {
                TreeItem<String> branch = new TreeItem<>(cSfees.getSFEE_name());
                if (cSfees.getItems() != null) {
                    for (C_SFEI_conveyor conveyor : cSfees.getItems().getSfeisController().getSfeiConveyors()) {
                        TreeItem<String> leaf = new TreeItem<>(conveyor.getSfeiConveyor().getName());
                        branch.getChildren().add(leaf);
                    }
                    for (C_SFEI_machine machine : cSfees.getItems().getSfeisController().getSfeiMachines()) {
                        TreeItem<String> leaf = new TreeItem<>(machine.getSfeiMachine().getName());
                        branch.getChildren().add(leaf);
                    }
                }
                root.getChildren().add(branch);
            }
            sfem_elements_tree.setRoot(root);
        }
    }

    private void loadData(C_SFEM cSfem) {
        try {
            System.out.println("Size: " + cSfem.getcSFEMProduction().getSfeeControllers().size());
            for (cSFEE_production cSFEEProduction : cSfem.getcSFEMProduction().getSfeeControllers()) {
                C_SFEEs cSfees = new C_SFEEs();
                cSfees.setcSFEEProduction(cSFEEProduction);

                cSfem.registerC_SFEEs(cSfees);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
