package com.agnux.tms.api.dto;


import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PatioDto {

    private UUID id;
    private String name;
    private double latitudeLocation;
    private double longitudeLocation;
}
