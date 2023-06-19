package com.example.carsdetailsmicroservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Driver {
    private Long id;
    private String firstName;
    private String lastName;
    private String passport;
    private String licenseCategory;
    private LocalDate dateOfBirth;
    private int experience;
    private Account account;
}
