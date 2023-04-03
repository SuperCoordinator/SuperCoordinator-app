package viewers.controllers.SFEE;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import models.sensor_actuator;
import viewers.controllers.SFEI.C_SFEI;

import java.util.TreeMap;

public class C_SFEE_items {

    private C_SFEI sfeisController;
    private TreeMap<Integer, sensor_actuator> io = new TreeMap<>();

    public C_SFEE_items() {
    }

    public void setIo(TreeMap<Integer, sensor_actuator> io) {
        this.io = io;
        this.sfeisController = new C_SFEI(io);
    }

    public C_SFEI getSfeisController() {
        return sfeisController;
    }

    @FXML
    private Pane newSFEI_Pane;

    @FXML
    private Text title;
    @FXML
    private TreeView<?> treeView_SFEIs;

    @FXML
    void buttonPressed(ActionEvent event) {
        Button temp = (Button) event.getSource();

        try {
            switch (temp.getId()) {
                case "add_sfei" -> {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SFEI.fxml"));
                    loader.setController(sfeisController);
                    AnchorPane pane = loader.load();
                    newSFEI_Pane.getChildren().setAll(pane);
                    title.setText("New Shop Floor Educational Item");

                }
                case "delete_sfei" -> {

                }
                case "edit_sfei" -> {

                }
            }

//            setPane();

        } catch (
                Exception e) {
            e.printStackTrace();
        }
    }

    private void setPane() {

       /* String name = String.valueOf(activePane);
        name = name.toLowerCase();
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SFEE_" + name + ".fxml"));
            switch (activePane) {
                case PROPERTIES -> loader.setController(CM_SFEE.getInstance().getProperties());
                case COMMUNICATION -> loader.setController(CM_SFEE.getInstance().getCommunication());
                case ITEMS -> loader.setController(CM_SFEE.getInstance().getItems());
                case FAILURE -> loader.setController(CM_SFEE.getInstance().getFailure());
                case FINISH -> loader.setController(CM_SFEE.getInstance().getFinish());
            }
            Pane pane = loader.load();
            SFEE_body.getChildren().setAll(pane);

        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

}
