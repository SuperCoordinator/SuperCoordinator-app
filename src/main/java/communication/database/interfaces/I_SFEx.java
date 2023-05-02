package communication.database.interfaces;

import java.util.List;

public interface I_SFEx {

    int insert(String sfex_name, String fk);

    void delete(String sfex_name, String fk);

    void update(String old_sfex_name, String new_sfex_name, String fk);

    List<String> getAll_SFEx();

    List<String> getAll_SFExFrom(String fk);

}
