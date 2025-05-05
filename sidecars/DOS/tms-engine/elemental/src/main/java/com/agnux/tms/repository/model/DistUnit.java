package com.agnux.tms.repository.model;

public enum DistUnit {
    KM(1.0),
    MI(1.60934);

    /**
     * The double value representing this distance unit.
     */
    protected double scalarValue;

    /**
     * Constructor for creating a distance unit with a specific scalar value.
     *
     * @param code The double associated with the distance unit.
     */
    DistUnit(final double code) {
        this.scalarValue = code;
    }

    /**
     * Retrieves the scalar value value associated with this distance unit.
     *
     * @return The double scalar value.
     */
    public double getCode() {
        return scalarValue;
    }
}
