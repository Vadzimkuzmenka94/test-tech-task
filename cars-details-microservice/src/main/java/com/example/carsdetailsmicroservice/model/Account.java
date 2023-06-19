package com.example.carsdetailsmicroservice.model;

import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Account {
    private Long id;
    private Driver driver;
    private Double redDollar;
    private Double greenDollar;
    private Double blueDollar;
}
