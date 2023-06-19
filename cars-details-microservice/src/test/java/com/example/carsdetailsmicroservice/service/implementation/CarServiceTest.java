package com.example.carsdetailsmicroservice.service.implementation;

import com.example.carsdetailsmicroservice.dto.car.create.CarCreateRequestDto;
import com.example.carsdetailsmicroservice.dto.car.get.CarGetResponseDto;
import com.example.carsdetailsmicroservice.dto.car.update.CarUpdateRequestDto;
import com.example.carsdetailsmicroservice.entity.Car;
import com.example.carsdetailsmicroservice.entity.Detail;
import com.example.carsdetailsmicroservice.exceptions.car.CarAlreadyExistException;
import com.example.carsdetailsmicroservice.exceptions.car.CarNotFoundException;
import com.example.carsdetailsmicroservice.kafka.Producer;
import com.example.carsdetailsmicroservice.mapper.CarMapper;
import com.example.carsdetailsmicroservice.repository.CarRepository;
import com.example.carsdetailsmicroservice.repository.DetailRepository;
import com.example.carsdetailsmicroservice.service.CarService;
import com.example.carsdetailsmicroservice.service.DetailService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CarServiceTest {
    @Mock
    private CarRepository carRepository;

    @Mock
    private DetailService detailService;
    @Mock
    private EntityManager entityManager;
    @Mock
    DetailRepository detailRepository;
    @Mock
    private Producer producer;

    private CarService carService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        carService = new CarServiceImplementation(carRepository,
                entityManager,
                detailService,
                detailRepository,
                producer);
    }

    @Test
    void registerCar_SuccessfulRegistration() {
        // Arrange
        CarCreateRequestDto carRequestDto = createCarRequestDto();
        Car car = CarMapper.INSTANCE.toCar(carRequestDto);

        when(carRepository.existsByVin(carRequestDto.getVin())).thenReturn(false);
        when(carRepository.existsByLicensePlate(carRequestDto.getLicensePlate())).thenReturn(false);

        // Create an ArgumentCaptor for the Car argument
        ArgumentCaptor<Car> carCaptor = ArgumentCaptor.forClass(Car.class);

        // Act
        carService.registerCar(carRequestDto);

        // Assert
        verify(detailService, times(carRequestDto.getDetails().size())).createDetail(any(Detail.class));
        verify(carRepository, times(1)).save(carCaptor.capture());

        // Get the captured Car object
        Car capturedCar = carCaptor.getValue();

        // Assert the properties of the captured Car object
        assertEquals(car.getVin(), capturedCar.getVin());
        assertEquals(car.getLicensePlate(), capturedCar.getLicensePlate());
        // ... assert other properties as needed
    }

    @Test
    void registerCar_CarAlreadyExists_ThrowsCarAlreadyExistException() {
        // Arrange
        CarCreateRequestDto carRequestDto = createCarRequestDto();

        when(carRepository.existsByVin(carRequestDto.getVin())).thenReturn(true);

        // Act & Assert
        assertThrows(CarAlreadyExistException.class, () -> carService.registerCar(carRequestDto));
        verify(carRepository, never()).save(any(Car.class));
    }

    private CarCreateRequestDto createCarRequestDto() {
        CarCreateRequestDto carRequestDto = new CarCreateRequestDto();
        carRequestDto.setVin("VIN123");
        carRequestDto.setLicensePlate("ABC123");
        carRequestDto.setDetails(createDetails());
        return carRequestDto;
    }

    private Set<Detail> createDetails() {
        Set<Detail> details = new HashSet<>();
        Detail detail1 = new Detail();
        detail1.setId(1L);
        detail1.setSerialNumber("DETAIL1");
        details.add(detail1);
        Detail detail2 = new Detail();
        detail2.setId(2L);
        detail2.setSerialNumber("DETAIL1");
        details.add(detail2);
        return details;
    }

    @Test
    void getAllCars_ReturnsCarPage() {
        // Arrange
        int page = 0;
        int size = 10;
        String sortBy = "vin";

        // Create a list of Cars
        List<Car> cars = new ArrayList<>();
        cars.add(createCar("VIN1", "ABC123"));
        cars.add(createCar("VIN2", "DEF456"));

        // Create a Pageable object
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));

        // Create a Page<Car> object
        Page<Car> carPage = new PageImpl<>(cars, pageable, cars.size());

        // Mock the carRepository.findAll() method
        when(carRepository.findAll(pageable)).thenReturn(carPage);

        // Act
        Page<CarGetResponseDto> result = carService.getAllCars(page, size, sortBy);

        // Assert
        assertEquals(cars.size(), result.getTotalElements());
        assertEquals(cars.size(), result.getContent().size());
        // ... assert other properties or transformations as needed
    }

    private Car createCar(String vin, String licensePlate) {
        Car car = new Car();
        car.setVin(vin);
        car.setLicensePlate(licensePlate);
        return car;
    }

    @Test
    void searchCarsByManufacturer_ReturnsMatchingCarsPage() {
        // Arrange
        String manufacturer = "Toyota";
        int page = 0;
        int size = 10;
        String sortBy = "vin";

        // Create a list of Cars matching the manufacturer
        List<Car> matchingCars = new ArrayList<>();
        matchingCars.add(createCar1("VIN1", "ABC123", "Toyota"));
        matchingCars.add(createCar("VIN2", "DEF456", "Toyota"));

        // Create a Pageable object
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));

        // Create a Page<Car> object with the matching cars
        Page<Car> matchingCarsPage = new PageImpl<>(matchingCars, pageable, matchingCars.size());

        // Mock the carRepository.findByManufacturerContaining() method
        when(carRepository.findByManufacturerContaining(manufacturer, pageable)).thenReturn(matchingCarsPage);

        // Act
        Page<CarGetResponseDto> result = carService.searchCarsByManufacturer(manufacturer, page, size, sortBy);

        // Assert
        assertEquals(matchingCars.size(), result.getTotalElements());
        assertEquals(matchingCars.size(), result.getContent().size());
        // ... assert other properties or transformations as needed
    }

    private Car createCar1(String vin, String licensePlate, String manufacturer) {
        Car car = new Car();
        car.setVin(vin);
        car.setLicensePlate(licensePlate);
        car.setManufacturer(manufacturer);
        // ... set other properties as needed
        return car;
    }

    @Test
    void findCarByLicensePlate_ReturnsCarDto_WhenCarExists() {
        // Arrange
        String licensePlate = "ABC123";
        Car car = createCar("VIN1", licensePlate, "Toyota");
        Optional<Car> optionalCar = Optional.of(car);

        // Mock the carRepository.findCarByLicensePlate() method
        when(carRepository.findCarByLicensePlate(licensePlate)).thenReturn(optionalCar);

        // Act
        Optional<CarGetResponseDto> result = carService.findCarByLicensePlate(licensePlate);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(car.getVin(), result.get().getVin());
        assertEquals(car.getLicensePlate(), result.get().getLicensePlate());
        // ... assert other properties as needed
    }

    @Test
    void findCarByLicensePlate_ThrowsCarNotFoundException_WhenCarDoesNotExist() {
        // Arrange
        String licensePlate = "ABC123";

        // Mock the carRepository.findCarByLicensePlate() method
        when(carRepository.findCarByLicensePlate(licensePlate)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CarNotFoundException.class, () -> carService.findCarByLicensePlate(licensePlate));
    }

    @Test
    void deleteCar_DeletesCar_WhenCarExists() {
        // Arrange
        String vin = "VIN1";

        // Mock the carRepository.existsByVin() method
        when(carRepository.existsByVin(vin)).thenReturn(true);

        // Act
        carService.deleteCar(vin);

        // Assert
        verify(carRepository, times(1)).deleteByVin(vin);
    }

    @Test
    void deleteCar_ThrowsCarNotFoundException_WhenCarDoesNotExist() {
        // Arrange
        String vin = "VIN1";

        // Mock the carRepository.existsByVin() method
        when(carRepository.existsByVin(vin)).thenReturn(false);

        // Act & Assert
        assertThrows(CarNotFoundException.class, () -> carService.deleteCar(vin));
    }

    @Test
    void updateCar_UpdatesCar_WhenCarExists() {
        // Arrange
        String vin = "VIN1";
        CarUpdateRequestDto updatedCar = createUpdatedCar("DEF456", "Ford", "Mustang", 2022);

        Car existingCar = createCar(vin, "ABC123", "Toyota");

        // Mock the carRepository.existsByVin() method
        when(carRepository.existsByVin(vin)).thenReturn(true);

        // Mock the carRepository.findByVin() method
        when(carRepository.findByVin(vin)).thenReturn(Optional.of(existingCar));

        // Mock the carRepository.save() method
        when(carRepository.save(any(Car.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Car result = carService.updateCar(vin, updatedCar);

        // Assert
        assertEquals(updatedCar.getLicensePlate(), result.getLicensePlate());
        assertEquals(updatedCar.getManufacturer(), result.getManufacturer());
        assertEquals(updatedCar.getModel(), result.getModel());
        assertEquals(updatedCar.getYear(), result.getYear());
        // ... assert other properties as needed
    }

    @Test
    void updateCar_ThrowsCarNotFoundException_WhenCarDoesNotExist() {
        // Arrange
        String vin = "VIN1";
        CarUpdateRequestDto updatedCar = createUpdatedCar("DEF456", "Ford", "Mustang", 2022);

        // Mock the carRepository.existsByVin() method
        when(carRepository.existsByVin(vin)).thenReturn(false);

        // Act & Assert
        assertThrows(CarNotFoundException.class, () -> carService.updateCar(vin, updatedCar));
    }

    private Car createCar(String vin, String licensePlate, String manufacturer) {
        Car car = new Car();
        car.setVin(vin);
        car.setLicensePlate(licensePlate);
        car.setManufacturer(manufacturer);
        // ... set other properties as needed
        return car;
    }

    private CarUpdateRequestDto createUpdatedCar(String licensePlate, String manufacturer, String model, int year) {
        CarUpdateRequestDto updatedCar = new CarUpdateRequestDto();
        updatedCar.setLicensePlate(licensePlate);
        updatedCar.setManufacturer(manufacturer);
        updatedCar.setModel(model);
        updatedCar.setYear(year);
        // ... set other properties as needed
        return updatedCar;
    }

}