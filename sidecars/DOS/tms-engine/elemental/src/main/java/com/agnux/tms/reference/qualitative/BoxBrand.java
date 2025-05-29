package com.agnux.tms.reference.qualitative;

public enum BoxBrand {

    UTILITY_TRAILER,
    GREAT_DANE,
    WABASH,
    SCHMITZ_CARGOBULL,
    KOGEL,
    LAMBERET,
    KRONE,
    CIMC,
    FRUEHAUF,
    HYUNDAI_TRANSLEAD,
    FACCHINI,
    RANDON,
    MIYAZAKI,
    TOPRE,
    LECITRAILER,
    INDETRUCK;

    public String getDescription() {
        return name().replace("_", " ");
    }
}
