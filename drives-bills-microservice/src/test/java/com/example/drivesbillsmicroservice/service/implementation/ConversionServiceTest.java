package com.example.drivesbillsmicroservice.service.implementation;

import com.example.drivesbillsmicroservice.enums.Currency;
import com.example.drivesbillsmicroservice.service.CurrencyConversionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class ConversionServiceTest {

    @Autowired
    private CurrencyConversionService currencyConversionService;

    @Test
    public void testConvert() {
        Currency red = Currency.RED;
        Currency green = Currency.GREEN;
        Currency blue = Currency.BLUE;

        double amount1 = 100.0;
        double amount2 = 200.0;
        double delta = 0.001;

        // Test RED -> GREEN conversion
        assertEquals(amount1 * 2.5, currencyConversionService.convert(amount1, red, green), delta);
        assertEquals(amount2 * 2.5, currencyConversionService.convert(amount2, red, green), delta);

        // Test GREEN -> RED conversion
        assertEquals(amount1 / 2.5, currencyConversionService.convert(amount1, green, red), delta);
        assertEquals(amount2 / 2.5, currencyConversionService.convert(amount2, green, red), delta);

        // Test GREEN -> BLUE conversion
        assertEquals(amount1 / 0.6, currencyConversionService.convert(amount1, green, blue), delta);
        assertEquals(amount2 / 0.6, currencyConversionService.convert(amount2, green, blue), delta);

        // Test BLUE -> GREEN conversion
        assertEquals(amount1 * 0.6, currencyConversionService.convert(amount1, blue, green), delta);
        assertEquals(amount2 * 0.6, currencyConversionService.convert(amount2, blue, green), delta);

        // Test RED -> BLUE conversion
        assertEquals(amount1 * 2.5 / 0.6, currencyConversionService.convert(amount1, red, blue), delta);
        assertEquals(amount2 * 2.5 / 0.6, currencyConversionService.convert(amount2, red, blue), delta);

        // Test BLUE -> RED conversion
        assertEquals(amount1 * 0.6 / 2.5, currencyConversionService.convert(amount1, blue, red), delta);
        assertEquals(amount2 * 0.6 / 2.5, currencyConversionService.convert(amount2, blue, red), delta);

        // Test same currency conversion
        assertEquals(amount1, currencyConversionService.convert(amount1, red, red), delta);
        assertEquals(amount2, currencyConversionService.convert(amount2, green, green), delta);
        assertEquals(amount1, currencyConversionService.convert(amount1, blue, blue), delta);
    }
}