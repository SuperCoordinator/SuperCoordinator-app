package utils;

import controllers.production.cSFEM_production;
import controllers.transport.cSFEM_transport;
import models.base.SFEE;
import org.apache.commons.math3.util.Pair;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Map;

public class serializer {

    public enum scenes {
        CMC_connection,
        CMC2_con_individual
    }

    public final scenes scene = scenes.CMC2_con_individual;
    private final String prod_filePath = "blocks/" + scene + "/saves/SFEM_production.ser";
    private final String trans_filePath = "blocks/" + scene + "/saves/SFEM_transport.ser";
    private ArrayList<cSFEM_production> C_Production;
    private ArrayList<cSFEM_transport> C_Transport;

    public serializer() {
        C_Production = new ArrayList<>();
        C_Transport = new ArrayList<>();
    }

    public ArrayList<cSFEM_production> getC_Production() {
        return C_Production;
    }

    public void setC_Production(ArrayList<cSFEM_production> c_Production) {
        C_Production = c_Production;
    }

    public ArrayList<cSFEM_transport> getC_Transport() {
        return C_Transport;
    }

    public void setC_Transport(ArrayList<cSFEM_transport> c_Transport) {
        C_Transport = c_Transport;
    }


    public void serialize_prod() {

        try {
            // serialize object's state
            FileOutputStream fos = new FileOutputStream(prod_filePath);
            ObjectOutputStream outputStream = new ObjectOutputStream(fos);
            outputStream.writeObject(C_Production);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void serialize_trans() {

        try {
            // serialize object's state
            FileOutputStream fos = new FileOutputStream(trans_filePath);
            ObjectOutputStream outputStream = new ObjectOutputStream(fos);
            outputStream.writeObject(C_Transport);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void deserialize_prod() {

        try {
            FileInputStream fis = new FileInputStream(prod_filePath);
            ObjectInputStream inputStream = new ObjectInputStream(fis);
            C_Production = new ArrayList<>((ArrayList<cSFEM_production>) inputStream.readObject());
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public void deserialize_prod(String path) {

        try {
            FileInputStream fis = new FileInputStream(path);
            ObjectInputStream inputStream = new ObjectInputStream(fis);
            C_Production = new ArrayList<>((ArrayList<cSFEM_production>) inputStream.readObject());
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void deserialize_trans() {

        try {
            FileInputStream fis = new FileInputStream(trans_filePath);
            ObjectInputStream inputStream = new ObjectInputStream(fis);
            C_Transport = new ArrayList<>((ArrayList<cSFEM_transport>) inputStream.readObject());
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public Pair<SFEE, cSFEM_production> searchSFEEbyName(String name) {
        Pair<SFEE, cSFEM_production> sfee = null;
        for (cSFEM_production sfemController : C_Production) {
            for (Map.Entry<Integer, SFEE> currentSFEE : sfemController.getSfem().getSFEEs().entrySet()) {
                if (currentSFEE.getValue().getName().equals(name)) {
                    sfee = new Pair<>(currentSFEE.getValue(), sfemController);
                    break;
                }
            }
        }
        if (sfee == null)
            throw new RuntimeException("SFEE not found");

        return sfee;
    }
}
