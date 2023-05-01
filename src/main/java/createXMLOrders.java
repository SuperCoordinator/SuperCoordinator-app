import models.inboundOrder;
import utility.utils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;

public class createXMLOrders {
    public static void main(String[] args) {

        try {

            for (int i = 0; i < 5; i++) {
                inboundOrder inboundOrder = new inboundOrder(i,
                        utils.getInstance().getRandom().nextInt(1, 11),
                        utils.getInstance().getRandom().nextInt(1, 11),
                        utils.getInstance().getRandom().nextInt(1, 11));

                JAXBContext context = JAXBContext.newInstance(inboundOrder.class);
                Marshaller marshaller = context.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                marshaller.marshal(inboundOrder, new File("src/main/resources/inboundOrders/order" + i + ".xml"));
            }


        } catch (JAXBException  e) {
            throw new RuntimeException(e);
        }

    }
}
