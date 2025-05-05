package com.agnux.tms.repository.model;

public enum VolUnit {
    LT(1.0),
    GAL(3.78541);

    /**
     * The double value representing this volume unit.
     */
    protected double scalarValue;

    /**
     * Constructor for creating a volume unit with a specific scalar value.
     *
     * @param code The double associated with the volume unit.
     */
    VolUnit(final double code) {
        this.scalarValue = code;
    }

    /**
     * Retrieves the scalar value value associated with this volume unit.
     *
     * @return The double scalar value.
     */
    public double getCode() {
        return scalarValue;
    }
}
