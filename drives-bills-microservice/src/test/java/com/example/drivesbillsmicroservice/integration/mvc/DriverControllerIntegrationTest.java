package com.example.drivesbillsmicroservice.integration.mvc;

import com.example.drivesbillsmicroservice.controller.DriverController;
import com.example.drivesbillsmicroservice.dto.driver.create.DriverCreateRequestDto;
import com.example.drivesbillsmicroservice.dto.driver.update.DriverUpdateRequestDto;
import com.example.drivesbillsmicroservice.entity.Driver;
import com.example.drivesbillsmicroservice.repository.DriverRepository;
import com.example.drivesbillsmicroservice.service.DriverService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DriverController.class)
public class DriverControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DriverService driverService;
    @MockBean
    DriverRepository driverRepository;

    @Test
    public void testRegisterDriver() throws Exception {
        DriverCreateRequestDto driverCreateRequestDto = new DriverCreateRequestDto();
        driverCreateRequestDto.setPassport("QWERTY1234");
        driverCreateRequestDto.setLastName("Oleg");
        driverCreateRequestDto.setFirstName("Ivanov");
        mockMvc.perform(post("/drivers/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(driverCreateRequestDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Driver created"));
    }

/*    @Test
    public void testUpdateDriver() throws Exception {
        // Arrange
        String passport = "QWERTY1234";

        DriverUpdateRequestDto updatedDriver = new DriverUpdateRequestDto();
        updatedDriver.setLastName("NewLastName");
        updatedDriver.setFirstName("NewFirstName");

        Driver existingDriver = new Driver();
        existingDriver.setPassport(passport);
        existingDriver.setLastName("Oleg");
        existingDriver.setFirstName("Ivanov");

        when(driverRepository.findDriverByPassport(passport)).thenReturn(Optional.of(existingDriver));
        when(driverRepository.save(existingDriver)).thenReturn(existingDriver); // Добавленная строка

        // Act & Assert
        mockMvc.perform(patch("/drivers/{passport}", passport)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(updatedDriver)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Driver updated"));

        verify(driverRepository, times(1)).save(existingDriver);
    }

    @Test
    public void testDeleteDriverByPassport() throws Exception {
        // Arrange
        String passport = "QWERTY1234";

        Driver driver = new Driver();
        driver.setPassport(passport);
        driver.setLastName("Oleg");
        driver.setFirstName("Ivanov");

        when(driverRepository.findDriverByPassport(passport)).thenReturn(Optional.of(driver));

        // Act & Assert
        mockMvc.perform(delete("/drivers/{passport}", passport))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Driver deleted"));

        verify(driverRepository, times(1)).deleteByPassport(passport);
    }*/

}