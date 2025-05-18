package com.agnux.tms.repository.model;

public enum VehicleColor {

    BLUE("#1F77B4"),
    ORANGE("#FF7F0E"),
    GREEN("#2CA02C"),
    RED("#D62728"),
    PURPLE("#9467BD"),
    BROWN("#8C564B"),
    PINK("#E377C2"),
    GRAY("#7F7F7F"),
    OLIVE("#BCBD22"),
    TEAL("#17BECF"),
    LIGHT_BLUE("#AEC7E8"),
    LIGHT_ORANGE("#FFBB78"),
    LIGHT_GREEN("#98DF8A"),
    LIGHT_RED("#FF9896"),
    LIGHT_PURPLE("#C5B0D5"),
    LIGHT_BROWN("#C49C94");

    private final String hexCode;

    VehicleColor(String hexCode) {
        this.hexCode = hexCode;
    }

    public String getHexCode() {
        return hexCode;
    }

    @Override
    public String toString() {
        return name() + " (" + hexCode + ")";
    }
}
