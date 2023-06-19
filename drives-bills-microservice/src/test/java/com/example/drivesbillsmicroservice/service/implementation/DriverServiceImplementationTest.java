package com.example.drivesbillsmicroservice.service.implementation;

import com.example.drivesbillsmicroservice.dto.driver.create.DriverCreateRequestDto;
import com.example.drivesbillsmicroservice.dto.driver.get.DriverGetResponseDto;
import com.example.drivesbillsmicroservice.dto.driver.update.DriverUpdateRequestDto;
import com.example.drivesbillsmicroservice.entity.Driver;
import com.example.drivesbillsmicroservice.enums.LicenseCategory;
import com.example.drivesbillsmicroservice.events.CarPurchaseEvent;
import com.example.drivesbillsmicroservice.events.DetailAddEvent;
import com.example.drivesbillsmicroservice.exceptions.driver.DriverAlreadyExistException;
import com.example.drivesbillsmicroservice.exceptions.driver.DriverNotFoundException;
import com.example.drivesbillsmicroservice.kafka.Producer;
import com.example.drivesbillsmicroservice.repository.DriverRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DriverServiceImplementationTest {
    @Mock
    private DriverRepository driverRepository;

    @Mock
    private Producer producer;

    @InjectMocks
    private DriverServiceImplementation driverService;

    @Captor
    private ArgumentCaptor<CarPurchaseEvent> carPurchaseEventCaptor;

    @Captor
    private ArgumentCaptor<DetailAddEvent> detailAddEventCaptor;

    @Test
    void registerDriver_CreatesDriver_WhenDriverDoesNotExist() {
        // Arrange
        DriverCreateRequestDto driverCreateRequestDto = createDriverCreateRequestDto();
        when(driverRepository.existsByPassport(driverCreateRequestDto.getPassport())).thenReturn(false);

        // Act
        driverService.registerDriver(driverCreateRequestDto);

        // Assert
        verify(driverRepository).save(any(Driver.class));
    }

    @Test
    void registerDriver_ThrowsDriverAlreadyExistException_WhenDriverAlreadyExists() {
        // Arrange
        DriverCreateRequestDto driverCreateRequestDto = createDriverCreateRequestDto();
        when(driverRepository.existsByPassport(driverCreateRequestDto.getPassport())).thenReturn(true);

        // Act & Assert
        assertThrows(DriverAlreadyExistException.class, () -> driverService.registerDriver(driverCreateRequestDto));
    }

    @Test
    void findDriverByPassport_ReturnsDriver_WhenDriverExists() {
        String passport = "Oleg";
        String firstName = "Oleg";
        String lastName = "PASSPORT1";

        Driver driver = createDriver(firstName, lastName, passport, LocalDate.now());
        when(driverRepository.findDriverByPassport(passport)).thenReturn(Optional.of(driver));

        // Act
        Optional<DriverGetResponseDto> result = driverService.findDriverByPassport(passport);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(driver.getFirstName(), result.get().getFirstName());
        assertEquals(driver.getLastName(), result.get().getLastName());
        // ... assert other properties as needed
    }

    @Test
    void findDriverByPassport_ThrowsDriverNotFoundException_WhenDriverDoesNotExist() {
        // Arrange
        String passport = "PASSPORT1";
        when(driverRepository.findDriverByPassport(passport)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(DriverNotFoundException.class, () -> driverService.findDriverByPassport(passport));
    }

    @Test
    void deleteDriver_DeletesDriver_WhenDriverExists() {
        // Arrange
        String passport = "Oleg";
        String firstName = "Oleg";
        String lastName = "PASSPORT1";
        Optional<Driver> driver = Optional.of(createDriver(firstName, lastName, passport, LocalDate.now()));
        when(driverRepository.findDriverByPassport(passport)).thenReturn(driver);

        // Act
        driverService.deleteDriver(passport);

        // Assert
        verify(driverRepository).deleteByPassport(passport);
    }

    @Test
    void deleteDriver_ThrowsDriverNotFoundException_WhenDriverDoesNotExist() {
        // Arrange
        String passport = "PASSPORT1";
        when(driverRepository.findDriverByPassport(passport)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(DriverNotFoundException.class, () -> driverService.deleteDriver(passport));
    }

    @Test
    void updateDriver_UpdatesDriver_WhenDriverExists() {
        // Arrange
        String passport = "Oleg";
        String firstName = "Oleg";
        String lastName = "PASSPORT1";
        DriverUpdateRequestDto updatedDriver = createUpdatedDriver();
        Driver existingDriver = createDriver(firstName, lastName, passport, LocalDate.now());
        when(driverRepository.findDriverByPassport(passport)).thenReturn(Optional.of(existingDriver));

        // Act
        driverService.updateDriver(passport, updatedDriver);

        // Assert
        verify(driverRepository).save(existingDriver);
        assertEquals(updatedDriver.getFirstName(), existingDriver.getFirstName());
        assertEquals(updatedDriver.getLastName(), existingDriver.getLastName());
        // ... assert other properties as needed
    }

    @Test
    void updateDriver_ThrowsDriverNotFoundException_WhenDriverDoesNotExist() {
        // Arrange
        String passport = "PASSPORT1";
        DriverUpdateRequestDto updatedDriver = createUpdatedDriver();
        when(driverRepository.findDriverByPassport(passport)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(DriverNotFoundException.class, () -> driverService.updateDriver(passport, updatedDriver));
    }

    @Test
    void getAllDrivers_ReturnsAllDrivers() {
        // Arrange
        int page = 0;
        int size = 10;
        String sortBy = "lastName";
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<Driver> driverPage = createDriverPage(pageable);
        when(driverRepository.findAll(pageable)).thenReturn(driverPage);

        // Act
        Page<DriverGetResponseDto> result = driverService.getAllDrivers(page, size, sortBy);

        // Assert
        assertEquals(driverPage.getTotalElements(), result.getTotalElements());
        assertEquals(driverPage.getTotalPages(), result.getTotalPages());
        // ... assert other properties as needed
    }

    @Test
    void searchDriversByFirstName_ReturnsMatchingDrivers() {
        // Arrange
        String firstName = "John";
        int page = 0;
        int size = 10;
        String sortBy = "lastName";
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<Driver> driverPage = createDriverPage(pageable);
        when(driverRepository.findByFirstNameContaining(firstName, pageable)).thenReturn(driverPage);

        // Act
        Page<DriverGetResponseDto> result = driverService.searchDriversByFirstName(firstName, page, size, sortBy);

        // Assert
        assertEquals(driverPage.getTotalElements(), result.getTotalElements());
        assertEquals(driverPage.getTotalPages(), result.getTotalPages());
        // ... assert other properties as needed
    }

    @Test
    void searchDriversByLastName_ReturnsMatchingDrivers() {
        // Arrange
        String lastName = "Doe";
        int page = 0;
        int size = 10;
        String sortBy = "firstName";
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<Driver> driverPage = createDriverPage(pageable);
        when(driverRepository.findByLastNameContaining(lastName, pageable)).thenReturn(driverPage);

        // Act
        Page<DriverGetResponseDto> result = driverService.searchDriversByLastName(lastName, page, size, sortBy);

        // Assert
        assertEquals(driverPage.getTotalElements(), result.getTotalElements());
        assertEquals(driverPage.getTotalPages(), result.getTotalPages());
        // ... assert other properties as needed
    }

    @Test
    void searchDriversByPassport_ReturnsMatchingDrivers() {
        // Arrange
        String passport = "PASSPORT1";
        int page = 0;
        int size = 10;
        String sortBy = "lastName";
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<Driver> driverPage = createDriverPage(pageable);
        when(driverRepository.findByPassportContaining(passport, pageable)).thenReturn(driverPage);

        // Act
        Page<DriverGetResponseDto> result = driverService.searchDriversByPassport(passport, page, size, sortBy);

        // Assert
        assertEquals(driverPage.getTotalElements(), result.getTotalElements());
        assertEquals(driverPage.getTotalPages(), result.getTotalPages());
        // ... assert other properties as needed
    }

    @Test
    void searchDriversByExperience_ReturnsMatchingDrivers() {
        // Arrange
        int experience = 5;
        int page = 0;
        int size = 10;
        String sortBy = "lastName";
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<Driver> driverPage = createDriverPage(pageable);
        when(driverRepository.findByExperience(experience, pageable)).thenReturn(driverPage);

        // Act
        Page<DriverGetResponseDto> result = driverService.searchDriversByExperience(experience, page, size, sortBy);

        // Assert
        assertEquals(driverPage.getTotalElements(), result.getTotalElements());
        assertEquals(driverPage.getTotalPages(), result.getTotalPages());
        // ... assert other properties as needed
    }

    @Test
    void sendCarPurchaseEvent_ReturnsMessageSentByProducer() throws JsonProcessingException {
        // Arrange
        CarPurchaseEvent carPurchaseEvent = createCarPurchaseEvent();
        String expectedMessage = "Message sent";
        when(producer.sendMessageCarPurchase(carPurchaseEvent)).thenReturn(expectedMessage);

        // Act
        String result = driverService.sendCarPurchaseEvent(carPurchaseEvent);

        // Assert
        assertEquals(expectedMessage, result);
    }

    @Test
    void sendSuccessfulPaymentEvent_ReturnsMessageSentByProducer() throws JsonProcessingException {
        // Arrange
        DetailAddEvent detailAddEvent = createDetailAddEvent();
        String expectedMessage = "Message sent";
        when(producer.sendMessageDetailAdd(detailAddEvent)).thenReturn(expectedMessage);

        // Act
        String result = driverService.sendSuccessfulPaymentEvent(detailAddEvent);

        // Assert
        assertEquals(expectedMessage, result);
    }

    private List<Driver> createDriversWithBirthday() {
        // Создаем и возвращаем список водителей с днем рождения для тестирования
        List<Driver> drivers = new ArrayList<>();

        // Добавляем водителей с днем рождения в список
        drivers.add(createDriver("John", "Doe", "1234567890", LocalDate.now()));
        drivers.add(createDriver("Jane", "Smith", "0987654321", LocalDate.now()));

        return drivers;
    }

    private DriverCreateRequestDto createDriverCreateRequestDto() {
        // Создаем и возвращаем объект DriverCreateRequestDto для тестирования
        DriverCreateRequestDto dto = new DriverCreateRequestDto();
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setPassport("1234567890");
        dto.setDateOfBirth(LocalDate.of(1990, 5, 10));
        // Задайте другие необходимые свойства объекта dto

        return dto;
    }

    private Driver createDriver(String firstName, String lastName, String passport, LocalDate dateOfBirth) {
        // Создаем и возвращаем объект Driver для тестирования с указанными свойствами
        Driver driver = new Driver();
        driver.setFirstName(firstName);
        driver.setLastName(lastName);
        driver.setPassport(passport);
        driver.setDateOfBirth(dateOfBirth);
        // Задайте другие необходимые свойства объекта driver

        return driver;
    }

    private DriverUpdateRequestDto createUpdatedDriver() {
        // Создаем и возвращаем объект DriverUpdateRequestDto для тестирования
        DriverUpdateRequestDto dto = new DriverUpdateRequestDto();
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setLicenseCategory(LicenseCategory.B);
        // Задайте другие необходимые свойства объекта dto

        return dto;
    }

    private Page<Driver> createDriverPage(Pageable pageable) {
        // Создаем и возвращаем объект Page<Driver> для тестирования с указанным pageable
        List<Driver> drivers = new ArrayList<>();
        // Создайте список водителей и добавьте их в drivers

        return new PageImpl<>(drivers, pageable, drivers.size());
    }

    private CarPurchaseEvent createCarPurchaseEvent() {
        // Создаем и возвращаем объект CarPurchaseEvent для тестирования
        CarPurchaseEvent event = new CarPurchaseEvent();
        event.setLicensePlate("0987654321A");
        event.setDriverId(1L);
        // Задайте другие необходимые свойства объекта event

        return event;
    }

    private DetailAddEvent createDetailAddEvent() {
        DetailAddEvent event = new DetailAddEvent();
        event.setSerialNumber("1234567890A");
        event.setLicensePlate("0987654321A");
        return event;
    }
}