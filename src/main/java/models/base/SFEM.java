package models.base;

public class SFEM {

    public enum SFEM_type {
        PRODUCTION,
        TRANSPORT
    }

    private final SFEM_type sfemType;
    private final String name;

    public SFEM(String name, SFEM_type sfemType) {
        this.sfemType = sfemType;
        this.name = name;
    }

    public SFEM_type getSfemType() {
        return sfemType;
    }

    public String getName() {
        return name;
    }
}
