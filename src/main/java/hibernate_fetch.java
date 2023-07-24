import communication.Address;
import communication.Employee;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import javax.persistence.TypedQuery;
import java.util.Iterator;
import java.util.List;

public class hibernate_fetch {
    public static void main(String[] args) {

        StandardServiceRegistry ssr = new StandardServiceRegistryBuilder().configure("hibernate.cfg.xml").build();
        Metadata meta = new MetadataSources(ssr).getMetadataBuilder().build();

        SessionFactory factory = meta.getSessionFactoryBuilder().build();
        Session session = factory.openSession();

        TypedQuery query = session.createQuery("from Employee e");
        List<Employee> list = query.getResultList();

        for (Employee emp : list) {
            System.out.println(emp.getEmployeeId() + " " + emp.getName() + " " + emp.getEmail());
            Address address = emp.getAddress();
            System.out.println(address.getAddressLine1() + " " + address.getCity() + " " +
                    address.getState() + " " + address.getCountry() + " " + address.getPincode());
        }

        session.close();
        System.out.println("success");
    }
}
