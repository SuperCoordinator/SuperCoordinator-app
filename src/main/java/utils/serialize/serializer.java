package utils.serialize;

import controllers.production.cSFEM_production;
import models.base.SFEE;
import org.apache.commons.math3.util.Pair;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.util.Map;

public class serializer {

    public enum scenes {
        CMC_connection,
        CMC2_con_individual,
        sorting_station,
        SS_3CMC
    }

    public final scenes scene = scenes.SS_3CMC;
    private final String prod_filePath = "blocks/" + scene + "/saves/SFEM_production";
    private final String trans_filePath = "blocks/" + scene + "/saves/SFEM_transport";

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
