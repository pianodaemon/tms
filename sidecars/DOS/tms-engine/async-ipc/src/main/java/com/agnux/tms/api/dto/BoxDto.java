package com.agnux.tms.api.dto;

import com.agnux.tms.reference.qualitative.BoxBrand;
import com.agnux.tms.reference.qualitative.BoxType;

import java.util.Date;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BoxDto {

    private UUID id;
    private String name;
    private BoxType boxType;
    private BoxBrand brand;
    private String numberSerial;
    private String numberPlate;
    private Date numberPlateExpiration;
    private int boxYear;
    private boolean lease;
}
