package utility.serialize;

import controllers.production.cSFEM_production;
import controllers.transport.cSFEM_transport;
import models.base.SFEE;
import models.base.SFEI;
import org.apache.commons.math3.util.Pair;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.util.ArrayList;
import java.util.Map;

public class serializer {


    public enum scenes {
        CMC_connection,
        CMC2_con_individual,
        sorting_station,
        SS_3CMC
    }

    public final scenes scene = scenes.SS_3CMC;
    private final String filePath = "blocks/" + scene + "/saves/" + scene;

    private serializable serializable = new serializable();

    public serializer() {
    }

    public ArrayList<cSFEM_production> getC_Production() {
        return serializable.getC_Production();
    }

    public ArrayList<cSFEM_transport> getC_Transport() {
        return serializable.getC_Transport();
    }


    public void saveXML() {
        try {

            JAXBContext context = JAXBContext.newInstance(utility.serialize.serializable.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(serializable, new File(filePath + ".xml"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadXML() {
        try {
            JAXBContext context = JAXBContext.newInstance(utility.serialize.serializable.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();

            serializable = (utility.serialize.serializable) unmarshaller.unmarshal(new FileReader(filePath + ".xml"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public Pair<SFEE, cSFEM_production> searchSFEEbyName(String name) {
        Pair<SFEE, cSFEM_production> sfee = null;
        for (cSFEM_production sfemController : serializable.getC_Production()) {
            for (Map.Entry<Integer, SFEE> currentSFEE : sfemController.getSfem().getSFEEs().entrySet()) {
                if (currentSFEE.getValue().getName().equals(name)) {
                    sfee = new Pair<>(currentSFEE.getValue(), sfemController);
                    break;
                }
            }
        }
        if (sfee == null)
            throw new RuntimeException("(" + serializer.class + ") SFEE not found");

        return sfee;
    }

    public SFEI searchSFEIbySFEE(SFEE sfee, String SFEI_name) {
        SFEI sfei = null;
        for (Map.Entry<Integer, SFEI> entry : sfee.getSFEIs().entrySet()) {
            if (entry.getValue().getName().equals(SFEI_name))
                sfei = entry.getValue();
        }
        if (sfei == null)
            throw new RuntimeException("(" + serializer.class + ") SFEI not found");

        return sfei;

    }
}
