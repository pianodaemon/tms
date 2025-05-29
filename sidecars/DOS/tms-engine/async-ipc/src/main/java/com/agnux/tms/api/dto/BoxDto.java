package com.agnux.tms.api.dto;

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
    private String numberPlate;
    private Date numberPlateExpiration;
    private int boxYear;
}
