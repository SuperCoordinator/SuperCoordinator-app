package models.SFEx_particular;

//import monitor.stochasticTime.conveyor;

import models.base.SFEE;
import models.base.SFEI;
import models.base.SFEM;
import models.part_prodTime;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeMap;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class SFEM_production extends SFEM  {
        @XmlElement
    private TreeMap<Integer, SFEE> SFEEs;

    public SFEM_production() {
        super();
    }

    public SFEM_production(String name) {
        super(name, SFEM_type.PRODUCTION);
        this.SFEEs = new TreeMap<>((Comparator<Integer> & Serializable) Integer::compareTo);
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
