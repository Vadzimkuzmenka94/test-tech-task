package com.example.carsdetailsmicroservice.service.implementation;

import com.example.carsdetailsmicroservice.dto.detail.get.DetailGetResponseDto;
import com.example.carsdetailsmicroservice.dto.detail.update.DetailUpdateRequestDto;
import com.example.carsdetailsmicroservice.entity.Detail;
import com.example.carsdetailsmicroservice.exceptions.detail.DetailAlreadyExistException;
import com.example.carsdetailsmicroservice.exceptions.detail.DetailNotFoundException;
import com.example.carsdetailsmicroservice.kafka.Producer;
import com.example.carsdetailsmicroservice.repository.CarRepository;
import com.example.carsdetailsmicroservice.repository.DetailRepository;
import com.example.carsdetailsmicroservice.service.CarService;
import com.example.carsdetailsmicroservice.service.DetailService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DetailServiceImplementationTest {
    @Mock
    private DetailRepository detailRepository;
    @Mock
    private DetailService detailService;
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        detailService = new DetailServiceImplementation(detailRepository);
    }


    @Test
    void createDetail_CreatesDetail_WhenDetailDoesNotExist() {
        // Arrange
        Detail detail = createDetail("DETAIL1", BigDecimal.valueOf(100.00));

        // Mock the detailRepository.existsBySerialNumber() method
        when(detailRepository.existsBySerialNumber(detail.getSerialNumber())).thenReturn(false);

        // Mock the detailRepository.save() method
        when(detailRepository.save(any(Detail.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        detailService.createDetail(detail);

        // Assert
        verify(detailRepository, times(1)).save(detail);
    }

    @Test
    void createDetail_ThrowsDetailAlreadyExistException_WhenDetailExists() {
        // Arrange
        Detail detail = createDetail("DETAIL1", BigDecimal.valueOf(100.00));

        // Mock the detailRepository.existsBySerialNumber() method
        when(detailRepository.existsBySerialNumber(detail.getSerialNumber())).thenReturn(true);

        // Act & Assert
        assertThrows(DetailAlreadyExistException.class, () -> detailService.createDetail(detail));
    }

    @Test
    void getAllDetails_ReturnsAllDetails() {
        // Arrange
        List<Detail> detailList = createDetailList();

        // Mock the detailRepository.findAll() method
        when(detailRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(detailList));

        // Act
        Page<DetailGetResponseDto> result = detailService.getAllDetails(0, 10, "serialNumber");

        // Assert
        assertEquals(detailList.size(), result.getContent().size());
        // ... assert other properties as needed
    }

    @Test
    void findDetailBySerialNumber_ReturnsDetailDto_WhenDetailExists() {
        // Arrange
        String serialNumber = "DETAIL1";
        Detail detail = createDetail(serialNumber, BigDecimal.valueOf(100.00));
        Optional<Detail> optionalDetail = Optional.of(detail);

        // Mock the detailRepository.findBySerialNumber() method
        when(detailRepository.findBySerialNumber(serialNumber)).thenReturn(optionalDetail);

        // Act
        Optional<DetailGetResponseDto> result = detailService.findDetailBySerialNumber(serialNumber);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(detail.getSerialNumber(), result.get().getSerialNumber());
        assertEquals(detail.getPrice(), result.get().getPrice());
        // ... assert other properties as needed
    }

    @Test
    void findDetailBySerialNumber_ThrowsDetailNotFoundException_WhenDetailDoesNotExist() {
        // Arrange
        String serialNumber = "DETAIL1";

        // Mock the detailRepository.findBySerialNumber() method
        when(detailRepository.findBySerialNumber(serialNumber)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(DetailNotFoundException.class, () -> detailService.findDetailBySerialNumber(serialNumber));
    }

    @Test
    void searchDetailsBySerialNumber_ReturnsMatchingDetails() {
        // Arrange
        String serialNumber = "DETAIL";
        List<Detail> detailList = createDetailList();
        Pageable pageable = PageRequest.of(0, 10, Sort.by("serialNumber"));
        Page<Detail> expectedPage = new PageImpl<>(detailList, pageable, detailList.size());

        // Mock the detailRepository.findBySerialNumberContaining() method
        when(detailRepository.findBySerialNumberContaining(eq(serialNumber), any(Pageable.class)))
                .thenReturn(expectedPage);

        // Act
        Page<DetailGetResponseDto> result = detailService.searchDetailsBySerialNumber(serialNumber, 0, 10, "serialNumber");

        // Assert
        assertEquals(expectedPage.getTotalElements(), result.getTotalElements());
        assertEquals(expectedPage.getNumber(), result.getNumber());
        assertEquals(detailList.size(), result.getContent().size());
        // ... assert other properties as needed
    }

    @Test
    void searchDetailsByPrice_ReturnsMatchingDetails() {
        // Arrange
        BigDecimal price = BigDecimal.valueOf(100.00);
        List<Detail> detailList = createDetailList();
        Pageable pageable = PageRequest.of(0, 10, Sort.by("serialNumber"));
        Page<Detail> expectedPage = new PageImpl<>(detailList, pageable, detailList.size());

        // Mock the detailRepository.findByPrice() method
        when(detailRepository.findByPrice(eq(price), any(Pageable.class)))
                .thenReturn(expectedPage);

        // Act
        Page<DetailGetResponseDto> result = detailService.searchDetailsByPrice(price, 0, 10, "serialNumber");

        // Assert
        assertEquals(expectedPage.getTotalElements(), result.getTotalElements());
        assertEquals(expectedPage.getNumber(), result.getNumber());
        assertEquals(detailList.size(), result.getContent().size());
        // ... assert other properties as needed
    }

    @Test
    void deleteDetail_DeletesDetail_WhenDetailExists() {
        // Arrange
        String serialNumber = "DETAIL1";

        // Mock the detailRepository.existsBySerialNumber() method
        when(detailRepository.existsBySerialNumber(serialNumber)).thenReturn(true);

        // Act
        detailService.deleteDetail(serialNumber);

        // Assert
        verify(detailRepository, times(1)).deleteBySerialNumber(serialNumber);
    }

    @Test
    void deleteDetail_ThrowsDetailNotFoundException_WhenDetailDoesNotExist() {
        // Arrange
        String serialNumber = "DETAIL1";

        // Mock the detailRepository.existsBySerialNumber() method
        when(detailRepository.existsBySerialNumber(serialNumber)).thenReturn(false);

        // Act & Assert
        assertThrows(DetailNotFoundException.class, () -> detailService.deleteDetail(serialNumber));
    }

    @Test
    void changeDetail_UpdatesDetail_WhenDetailExists() {
        // Arrange
        String serialNumber = "DETAIL1";
        Detail existingDetail = createDetail(serialNumber, BigDecimal.valueOf(100.00));
        DetailUpdateRequestDto updatedDetail = createUpdatedDetail("UPDATED_DETAIL", BigDecimal.valueOf(200.00));

        // Mock the detailRepository.existsBySerialNumber() method
        when(detailRepository.existsBySerialNumber(serialNumber)).thenReturn(true);

        // Mock the detailRepository.findBySerialNumber() method
        when(detailRepository.findBySerialNumber(serialNumber)).thenReturn(Optional.of(existingDetail));

        // Mock the detailRepository.save() method
        when(detailRepository.save(any(Detail.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Detail result = detailService.changeDetail(serialNumber, updatedDetail);

        // Assert
        assertEquals(updatedDetail.getSerialNumber(), result.getSerialNumber());
        assertEquals(updatedDetail.getPrice(), result.getPrice());
        // ... assert other properties as needed
    }

    @Test
    void changeDetail_ThrowsDetailNotFoundException_WhenDetailDoesNotExist() {
        // Arrange
        String serialNumber = "DETAIL1";
        DetailUpdateRequestDto updatedDetail = createUpdatedDetail("UPDATED_DETAIL", BigDecimal.valueOf(200.00));

        // Mock the detailRepository.existsBySerialNumber() method
        when(detailRepository.existsBySerialNumber(serialNumber)).thenReturn(false);

        // Act & Assert
        assertThrows(DetailNotFoundException.class, () -> detailService.changeDetail(serialNumber, updatedDetail));
    }

    private Detail createDetail(String serialNumber, BigDecimal price) {
        Detail detail = new Detail();
        detail.setSerialNumber(serialNumber);
        detail.setPrice(price);
        // ... set other properties as needed
        return detail;
    }

    private List<Detail> createDetailList() {
        List<Detail> detailList = new ArrayList<>();
        detailList.add(createDetail("DETAIL1", BigDecimal.valueOf(100.00)));
        detailList.add(createDetail("DETAIL2", BigDecimal.valueOf(200.00)));
        // ... add more details as needed
        return detailList;
    }

    private DetailUpdateRequestDto createUpdatedDetail(String serialNumber, BigDecimal price) {
        DetailUpdateRequestDto detailDto = new DetailUpdateRequestDto();
        detailDto.setSerialNumber(serialNumber);
        detailDto.setPrice(price);
        // ... set other properties as needed
        return detailDto;
    }

}