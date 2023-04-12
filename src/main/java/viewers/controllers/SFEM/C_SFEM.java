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
import javafx.scene.input.KeyCode;
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

    public void setSfemName(String sfemName) {
        this.sfemName = sfemName;
    }

    @FXML
    private TextField sfem_name;
    @FXML
    private Button submit_name_btn;
    @FXML
    private ListView<String> SFEMs_list;
    @FXML
    private TreeView<String> sfem_elements_tree;


    @FXML
    private Button new_sfem;
    @FXML
    private Button edit_sfemName;
    @FXML
    private Button delete_sfem;
    @FXML
    private Button refresh_sfem;


    @FXML
    private Button new_sfee;
    @FXML
    private Button edit_sfee;
    @FXML
    private Button delete_sfee;
    @FXML
    private Button refresh_sfee;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        updateSFEMlist();
        updateElementsTreeView();

        SFEMs_list.setPlaceholder(new Label("No Content"));

        // Install Tooltips over buttons

        Tooltip.install(new_sfem, new Tooltip("Add new SFEM"));
        Tooltip.install(edit_sfemName, new Tooltip("Change selected SFEM name"));
        Tooltip.install(delete_sfem, new Tooltip("Delete selected SFEM"));
        Tooltip.install(refresh_sfem, new Tooltip("Refresh List"));

        Tooltip.install(new_sfee, new Tooltip("Add new SFEE"));
        Tooltip.install(edit_sfee, new Tooltip("Edit selected SFEE"));
        Tooltip.install(delete_sfee, new Tooltip("Delete selected SFEE"));
        Tooltip.install(refresh_sfee, new Tooltip("Refresh List"));

    }

    private String oldSFEM_name = "";

    @FXML
    void buttonPressed(ActionEvent event) {
        try {
            Button temp = (Button) event.getSource();
            switch (temp.getId()) {

                case "new_sfem" -> {
                    sfem_name.setVisible(true);
                    submit_name_btn.setVisible(true);

                    sfem_name.setOnKeyPressed(keyEvent -> {
                        if (keyEvent.getCode().equals(KeyCode.ENTER))
                            newSFEM(new ActionEvent());
                    });
                }
                case "edit_sfemName" -> {
                    oldSFEM_name = SFEMs_list.getSelectionModel().getSelectedItem();
                    sfem_name.setText(SFEMs_list.getSelectionModel().getSelectedItem());
                    sfem_name.setVisible(true);
                    submit_name_btn.setVisible(true);

                    sfem_name.setOnKeyPressed(keyEvent -> {
                        if (keyEvent.getCode().equals(KeyCode.ENTER))
                            C_ShopFloor.getInstance().getCmSfems().forEach(cSfem -> {
                                if (cSfem.getSfemName().equals(oldSFEM_name)) {
                                    cSfem.setSfemName(sfem_name.getText());
                                    updateSFEMlist();
                                    sfem_name.clear();
                                    sfem_name.setVisible(false);
                                    submit_name_btn.setVisible(false);
                                }
                            });
                    });
                }
                case "delete_sfem" -> {
                    if (C_ShopFloor.getInstance().getCmSfems().removeIf(cSfem -> cSfem.getSfemName().equals(SFEMs_list.getSelectionModel().getSelectedItem())))
                        updateSFEMlist();
                }
                case "refresh_sfem" -> {
                    updateSFEMlist();

                }
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
                case "edit_sfee" -> {
                    if (SFEMs_list.getSelectionModel().getSelectedIndices().size() == 1) {
                        C_SFEM cSfem = C_ShopFloor.getInstance().getCmSfems().get(SFEMs_list.getSelectionModel().getSelectedIndices().get(0));
                        String sfeeName = sfem_elements_tree.getSelectionModel().getSelectedItem().getValue();
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SFEE/SFEE.fxml"));
                        // load C_SFEE instance
                        C_SFEEs cSfees = null;
                        for (C_SFEEs C : cSfem.getSfeesControllers()) {
                            if (C.getSFEE_name().equalsIgnoreCase(sfeeName)) {
                                cSfees = C;
                                break;
                            }
                        }
                        if (cSfees == null)
                            throw new RuntimeException("ERROR, selection " + sfeeName + "is not a SFEE!");

                        loader.setController(cSfees);
                        // open scene
                        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        stage.setScene(new Scene(loader.load()));
                        stage.show();
                    } else {
                        // ERROR
                        System.out.println("Select only 1 row!");
                    }
                }
                case "delete_sfee" -> {
                    if (SFEMs_list.getSelectionModel().getSelectedIndices().size() == 1) {
                        C_SFEM cSfem = C_ShopFloor.getInstance().getCmSfems().get(SFEMs_list.getSelectionModel().getSelectedIndices().get(0));
                        String sfeeName = sfem_elements_tree.getSelectionModel().getSelectedItem().getValue();

                        // load C_SFEE instance
                        C_SFEEs cSfees = null;
                        cSfem.getSfeesControllers().removeIf(C -> C.getSFEE_name().equalsIgnoreCase(sfeeName));
                        updateElementsTreeView();

                    } else {
                        // ERROR
                        System.out.println("Select only 1 row!");
                    }
                }
                case "refresh_sfee" -> {
                    updateElementsTreeView();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @FXML
    void newSFEM(ActionEvent event) {

        Validator validator = new Validator();

        validator.createCheck()
                .dependsOn("SFEM_name", sfem_name.textProperty())
                .withMethod(c -> {
                    String name = c.get("SFEM_name");
                    if (name == null || name.isEmpty())
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
//            System.out.println(C_SFEM.class + " " + cSfem.getSfemName());
            list.add(cSfem.getSfemName());
        }
        SFEMs_list.getItems().clear();
        SFEMs_list.getItems().addAll(list);
    }

    @FXML
    void updateElementsTreeView() {
        sfem_elements_tree.setRoot(null);
        try {
            // Swap between C_SFEM when select one of them in the List
            if (SFEMs_list.getSelectionModel().getSelectedIndices().size() > 0) {
                C_SFEM active_C_SFEM = null;
                for (C_SFEM cSfem : C_ShopFloor.getInstance().getCmSfems()) {
                    if (cSfem.getSfemName().equalsIgnoreCase(SFEMs_list.getSelectionModel().getSelectedItem()))
                        active_C_SFEM = cSfem;
                }

                if (active_C_SFEM == null)
                    throw new RuntimeException(C_SFEM.class + " active_C_SFEM is NULL");

                C_ShopFloor.getInstance().setCurrent_C_SFEM(active_C_SFEM);
            }

            TreeItem<String> root = new TreeItem<>(SFEMs_list.getSelectionModel().getSelectedItem());
            if (SFEMs_list.getSelectionModel().getSelectedIndices().size() == 1) {
                C_SFEM cSfem = C_ShopFloor.getInstance().getCmSfems().get(SFEMs_list.getSelectionModel().getSelectedIndices().get(0));

                if (C_ShopFloor.getInstance().isLoadedConfig()) {
                    loadData(cSfem);
                }

                for (C_SFEEs cSfees : cSfem.getSfeesControllers()) {
                    TreeItem<String> branch = new TreeItem<>(cSfees.getSFEE_name());
                    if (cSfees.getItems() != null && cSfees.getItems().getSfeisController() != null) {

                        if (cSfees.getItems().getSfeisController().getSfeiConveyors() != null) {
                            for (C_SFEI_conveyor conveyor : cSfees.getItems().getSfeisController().getSfeiConveyors()) {
                                TreeItem<String> leaf = new TreeItem<>(conveyor.getSfeiConveyor().getName());
                                branch.getChildren().add(leaf);

                            }
                        }

                        if (cSfees.getItems().getSfeisController().getSfeiMachines() != null) {
                            for (C_SFEI_machine machine : cSfees.getItems().getSfeisController().getSfeiMachines()) {
                                TreeItem<String> leaf = new TreeItem<>(machine.getSfeiMachine().getName());
                                branch.getChildren().add(leaf);
                            }
                        }
                    }
                    root.getChildren().add(branch);
                }
                sfem_elements_tree.setRoot(root);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final ArrayList<String> alreadyLoadedC_SFEM = new ArrayList<>();

    private void loadData(C_SFEM cSfem) {
        try {
            if (!alreadyLoadedC_SFEM.contains(cSfem.getSfemName())) {
                System.out.println("Size: " + cSfem.getcSFEMProduction().getSfeeControllers().size());
                for (cSFEE_production cSFEEProduction : cSfem.getcSFEMProduction().getSfeeControllers()) {

                    C_SFEEs cSfees = new C_SFEEs();
                    cSfees.setcSFEEProduction(cSFEEProduction);
                    cSfem.registerC_SFEEs(cSfees);
                }
                alreadyLoadedC_SFEM.add(cSfem.getSfemName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
