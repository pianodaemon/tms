package com.agnux.tms.reference.qualitative;

public enum BoxType {

    UNKNOWN_A,
    UNKNOWN_B;

    public String getDescription() {
        return name().replace("_", " ");
    }
}
