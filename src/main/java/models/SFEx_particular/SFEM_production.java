package models.SFEx_particular;

//import monitor.stochasticTime.conveyor;

import models.base.SFEE;
import models.base.SFEI;
import models.base.SFEM;
import models.part_prodTime;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.TreeMap;

public class SFEM_production extends SFEM implements Externalizable {

    public static final long serialVersionUID = 1234L;

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(SFEEs);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.SFEEs = (TreeMap<Integer, SFEE>) in.readObject();

    }

    private TreeMap<Integer, SFEE> SFEEs;

    public SFEM_production(String name) {
        super(name, SFEM_type.PRODUCTION);
        this.SFEEs = new TreeMap<>();
    }

    public SFEM_production() {
        super();
    }

    public TreeMap<Integer, SFEE> getSFEEs() {
        return SFEEs;
    }

    public SFEE getSFEEbyIndex(int index) {
        return SFEEs.get(index);
    }

    public void addNewSFEE(SFEE newSFEE) {
        int index = SFEEs.size();
        SFEEs.put(index, newSFEE);
    }


}
