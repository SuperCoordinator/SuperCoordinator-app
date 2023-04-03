package viewers.controllers.SFEE;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import viewers.controllers.C_SFEM_layout;
import viewers.mediators.CM_SFEE;

import java.net.URL;
import java.util.ResourceBundle;


public class C_SFEE2 extends CM_SFEE implements Initializable {

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        CM_SFEE.getInstance().registerC_SFEE_body_properties(new C_SFEE_properties());
        CM_SFEE.getInstance().registerC_SFEE_body_communication(new C_SFEE_communication());
        CM_SFEE.getInstance().registerC_SFEE_body_items(new C_SFEE_items());
        CM_SFEE.getInstance().registerC_SFEE_body_failure(new C_SFEE_failure());
        CM_SFEE.getInstance().registerC_SFEE_body_finish(new C_SFEE_finish());

        toggleComunication.setDisable(true);
        toggleItems.setDisable(true);
        toggleFailure.setDisable(true);
        toggleFinish.setDisable(true);
    }

    private enum Panes {
        PROPERTIES,
        COMMUNICATION,
        ITEMS,
        FAILURE,
        FINISH;

    }

    private Panes activePane;

    @FXML
    private AnchorPane SFEE_body;
    @FXML
    private ToggleButton toggleProperties;
    @FXML
    private ToggleButton toggleComunication;
    @FXML
    private ToggleButton toggleItems;
    @FXML
    private ToggleButton toggleFailure;

    @FXML
    private ToggleButton toggleFinish;
    @FXML
    private ToggleGroup bar;

    @FXML
    private ImageView error_icon;
    @FXML
    private Tooltip errorMsg;
    @FXML
    private Button next;

    public C_SFEE2() {
        super();
    }

    @FXML
    public void buttonPressed(ActionEvent event) {

        ToggleButton temp = (ToggleButton) event.getSource();

        try {
            switch (temp.getText()) {
                case "Properties" -> {
                    activePane = Panes.PROPERTIES;
                    next.setVisible(true);
                }
                case "Communication" -> {
                    activePane = Panes.COMMUNICATION;
                }
                case "Items" -> {
                    activePane = Panes.ITEMS;
                }
                case "Failure" -> {
                    activePane = Panes.FAILURE;
                }
                case "Finish" -> {
                    activePane = Panes.FINISH;
                }
/*                case "Failures" -> {
                    paneFailuresMode.setVisible(true);
                }
                case "Normal" -> {
                    paneFailuresMode.setVisible(false);
                }*/
            }

            setPane();

        } catch (
                Exception e) {
            e.printStackTrace();
        }

    }

    @FXML
    void goBack(ActionEvent event) {
        try {
//            Parent root = FXMLLoader.load(getClass().getResource("/fxml/SFEM_layout.fxml"));
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SFEM_layout.fxml"));
            loader.setController(new C_SFEM_layout(""));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void goNext(ActionEvent event) {
        ToggleButton selected = (ToggleButton) bar.getSelectedToggle();

        switch (selected.getText()) {
            case "Properties" -> {
                if (CM_SFEE.getInstance().getProperties().validation_moveON()) {
                    activePane = Panes.COMMUNICATION;
                    toggleComunication.setDisable(false);
                    toggleComunication.requestFocus();
                    bar.selectToggle(toggleComunication);
                } else {
                    error_icon.setVisible(true);
                    errorMsg.setText(CM_SFEE.getInstance().getProperties().getErrorMsg());
                    next.setTooltip(errorMsg);
                }
            }
            case "Communication" -> {
                if (CM_SFEE.getInstance().getCommunication().validation_moveON()) {
                    activePane = Panes.ITEMS;
                    toggleItems.setDisable(false);
                    toggleItems.requestFocus();
                    bar.selectToggle(toggleItems);
                } else {
                    error_icon.setVisible(true);
                    errorMsg.setText(CM_SFEE.getInstance().getCommunication().getErrorMsg());
                    next.setTooltip(errorMsg);
                }
            }
            case "Items" -> {
                if (CM_SFEE.getInstance().getItems().validation_moveON()) {
                    activePane = Panes.FAILURE;
                    toggleFailure.setDisable(true);
                    toggleFailure.requestFocus();
                    bar.selectToggle(toggleFailure);
                } else {
                    error_icon.setVisible(true);
                    errorMsg.setText(CM_SFEE.getInstance().getItems().getErrorMsg());
                    next.setTooltip(errorMsg);
                }
            }
            case "Failure" -> {
                activePane = Panes.FINISH;
                toggleFinish.requestFocus();
                bar.selectToggle(toggleFinish);
            }
            case "Finish" -> {
                // to do
            }

        }
        setPane();

    }

    private void setPane() {

        String name = String.valueOf(activePane);
        name = name.toLowerCase();
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SFEE_" + name + ".fxml"));
            switch (activePane) {
                case PROPERTIES -> {
                    CM_SFEE.getInstance().getProperties().activatePreviousValues();
                    loader.setController(CM_SFEE.getInstance().getProperties());
                }
                case COMMUNICATION -> {
                    loader.setController(CM_SFEE.getInstance().getCommunication());
                }
                case ITEMS -> {
                    CM_SFEE.getInstance().getItems().setIo(CM_SFEE.getInstance().getCommunication().getIo());
                    loader.setController(CM_SFEE.getInstance().getItems());
                }
                case FAILURE -> loader.setController(CM_SFEE.getInstance().getFailure());
                case FINISH -> loader.setController(CM_SFEE.getInstance().getFinish());
            }
            Pane pane = loader.load();
            SFEE_body.getChildren().setAll(pane);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
