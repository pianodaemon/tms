package com.agnux.haul.repository.model;

public enum PatioType {

    A(1),
    B(2);

    protected int code;

    PatioType(final int code) {
        this.code = code;
    }

    public double getCode() {
        return code;
    }
}
