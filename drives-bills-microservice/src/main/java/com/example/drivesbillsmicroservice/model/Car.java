package com.example.drivesbillsmicroservice.model;
import com.example.drivesbillsmicroservice.entity.Driver;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Car {
    private String vin;
    private String licensePlate;
    private String manufacturer;
    private String model;
    private Integer year;
    private Set<Detail> details = new HashSet<>();
    private Driver driver;
}
