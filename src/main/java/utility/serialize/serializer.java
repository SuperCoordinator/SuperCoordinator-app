package utility.serialize;

import communication.database.sf_configuration;
import controllers.production.cSFEE_production;
import controllers.production.cSFEM_production;
import controllers.transport.cSFEM_transport;
import controllers.warehouse.cSFEM_warehouse;
import models.SFEx_particular.SFEM_transport;
import models.base.SFEE;
import models.base.SFEI;
import org.apache.commons.math3.util.Pair;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.util.ArrayList;
import java.util.Map;

public class serializer {

    /**
     * Singleton pattern
     */
    public serializer() {
    }

    public static serializer getInstance() {
        return serializer.serializerHolder.INSTANCE;
    }

    private static class serializerHolder {
        private static final serializer INSTANCE = new serializer();
    }

    public enum scenes {
        CMC_connection,
        CMC2_con_individual,
        sorting_station,
        SS_3CMC,
        MC_Staudinger,
        WH_SS
    }

    public final scenes scene = scenes.WH_SS;
    private final String filePath = "blocks/" + scene + "/saves/" + scene;

    private serializable serializable = new serializable();

    public void setC_Warehouse(cSFEM_warehouse cSFEMWarehouse) {
        serializable.setC_Warehouse(cSFEMWarehouse);
    }

    public cSFEM_warehouse getC_Warehouse() {
        return serializable.getC_Warehouse();
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
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }

    }

    public void loadXML() {
        try {
            JAXBContext context = JAXBContext.newInstance(utility.serialize.serializable.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            serializable = (utility.serialize.serializable) unmarshaller.unmarshal(new FileReader(filePath + ".xml"));
            createDB_Instance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void createDB_Instance() {
        sf_configuration.getInstance().insert(scene.name());
    }

    public void new_cSFEM_transport(ArrayList<Object> data, boolean isWH) {
        try {
            SFEM_transport sfemTransport = new SFEM_transport((String) data.get(0));
            cSFEM_transport sfemController = new cSFEM_transport(sfemTransport);

            sfemController.init_SFEE_transport((String) data.get(1));

            // Perform searches for SFEIs and SFEEs objects based on the elements name
            Pair<SFEE, SFEI> in;
            if (!isWH) {
                in = searchSFEE_SFEIbySFEI_name((String) data.get(7));
            } else {
                in = new Pair<>((SFEE) data.get(5), (SFEI) data.get(7));
            }
            Pair<SFEE, SFEI> out = searchSFEE_SFEIbySFEI_name((String) data.get(8));

            data.set(5, in.getFirst());
            data.set(6, out.getFirst());
            data.set(7, in.getSecond());
            data.set(8, out.getSecond());

            sfemController.init_cSFEETransport(new ArrayList<>(data.subList(2, 13)), new ArrayList<>(data.subList(13, data.size())));

            // Add if not exist or update if exists
            int i;
            for (i = 0; i < serializer.getInstance().getC_Transport().size(); i++) {
                if (serializer.getInstance().getC_Transport().removeIf(next -> next.getSfem().getName().equals(data.get(0)))) {
                    serializer.getInstance().getC_Transport().add(i, sfemController);
                    System.out.println("Updated C_Transport");
                    break;
                }
            }

            if (i == serializer.getInstance().getC_Transport().size() /*&& i > 0*/) {
                serializer.getInstance().getC_Transport().add(/*serializer.getInstance().getC_Transport().size() - 1,*/ sfemController);
                System.out.println("Created new C_Transport");
            } /*else {
                serializer.getInstance().getC_Transport().add(serializer.getInstance().getC_Transport().size(), sfemController);
            }*/
        } catch (Exception e) {
            throw new RuntimeException(e);
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
            throw new RuntimeException("(" + serializer.class + ") SFEE " + name + " not found");

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

    public Pair<SFEE, SFEI> searchSFEE_SFEIbySFEI_name(String name) {
        Pair<SFEE, SFEI> returnPair = null;
        System.out.println(serializer.getInstance().getC_Production());
        for (cSFEM_production cSFEMProduction : serializer.getInstance().getC_Production()) {
            for (cSFEE_production cSFEEProduction : cSFEMProduction.getSfeeControllers()) {
                for (Map.Entry<Integer, SFEI> entry : cSFEEProduction.getSFEE().getSFEIs().entrySet()) {
                    if (entry.getValue().getName().equals(name)) {
                        returnPair = new Pair<>(cSFEEProduction.getSFEE(), entry.getValue());
                    }
                }
            }
        }
        if (returnPair == null)
            throw new RuntimeException("SFEI " + name + " not found in any SFEM");

        return returnPair;

    }
}
