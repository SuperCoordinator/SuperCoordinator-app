package utils.serialize;

import controllers.production.cSFEM_production;
import controllers.transport.cSFEM_transport;
import models.base.SFEE;
import org.apache.commons.math3.util.Pair;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.*;
import java.util.ArrayList;
import java.util.Map;

public class serializer {

    public enum scenes {
        CMC_connection,
        CMC2_con_individual
    }

    public final scenes scene = scenes.CMC_connection;
    private final String prod_filePath = "blocks/" + scene + "/saves/tests/SFEM_production";
    private final String trans_filePath = "blocks/" + scene + "/saves/tests/SFEM_transport";

    private production production = new production();
    private transport transport = new transport();

    public serializer() {
    }


    public utils.serialize.production getProduction() {
        return production;
    }

    public utils.serialize.transport getTransport() {
        return transport;
    }

    public void serialize_prod() {

        try {
            // serialize object's state
            FileOutputStream fos = new FileOutputStream(prod_filePath + ".ser");
            ObjectOutputStream outputStream = new ObjectOutputStream(fos);
            outputStream.writeObject(production);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void serialize_trans() {

        try {
            // serialize object's state
            FileOutputStream fos = new FileOutputStream(trans_filePath + ".ser");
            ObjectOutputStream outputStream = new ObjectOutputStream(fos);
            outputStream.writeObject(transport);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void deserialize_prod() {

        try {
            FileInputStream fis = new FileInputStream(prod_filePath + ".ser");
            ObjectInputStream inputStream = new ObjectInputStream(fis);
            production = (utils.serialize.production) inputStream.readObject();
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void deserialize_prod(String path) {

        try {
            FileInputStream fis = new FileInputStream(path);
            ObjectInputStream inputStream = new ObjectInputStream(fis);
//            C_Production = new ArrayList<>((ArrayList<cSFEM_production>) inputStream.readObject());
            production = (utils.serialize.production) inputStream.readObject();
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void deserialize_trans() {

        try {
            FileInputStream fis = new FileInputStream(trans_filePath + ".ser");
            ObjectInputStream inputStream = new ObjectInputStream(fis);
//            C_Transport = new ArrayList<>((ArrayList<cSFEM_transport>) inputStream.readObject());
            transport = (utils.serialize.transport) inputStream.readObject();
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void saveXML_prod() {
        try {
            JAXBContext context = JAXBContext.newInstance(utils.serialize.production.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(production, new File(prod_filePath + ".xml"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveXML_trans() {
        try {
            JAXBContext context = JAXBContext.newInstance(utils.serialize.transport.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(transport, new File(trans_filePath + ".xml"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadXML_prod() {
        try {
            JAXBContext context = JAXBContext.newInstance(utils.serialize.production.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();

            production = (utils.serialize.production) unmarshaller.unmarshal(new FileReader(prod_filePath + ".xml"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadXML_trans() {
        try {
            JAXBContext context = JAXBContext.newInstance(utils.serialize.transport.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();

            transport = (utils.serialize.transport) unmarshaller.unmarshal(new FileReader(trans_filePath + ".xml"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public Pair<SFEE, cSFEM_production> searchSFEEbyName(String name) {
        Pair<SFEE, cSFEM_production> sfee = null;
        for (cSFEM_production sfemController : production.getC_Production()) {
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
