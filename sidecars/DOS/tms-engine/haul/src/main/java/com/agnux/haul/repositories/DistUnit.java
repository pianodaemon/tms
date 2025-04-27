package com.agnux.haul.repositories;

public enum DistUnit {
    KM(1.0),
    MI(1.60934);

    /**
     * The integer code representing this distance unit.
     */
    protected double code;

    /**
     * Constructor for creating an error code with a specific integer value.
     *
     * @param code The integer code associated with the distance unit.
     */
    DistUnit(final double code) {
        this.code = code;
    }

    /**
     * Retrieves the integer code associated with this distance unit.
     *
     * @return The integer error code.
     */
    public double getCode() {
        return code;
    }
}
