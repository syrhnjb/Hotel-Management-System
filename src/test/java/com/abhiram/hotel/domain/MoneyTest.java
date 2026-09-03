package com.abhiram.hotel.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MoneyTest {

    @Test
    void addSumsTwoAmounts() {
        assertEquals(Money.rupees(150), Money.rupees(100).add(Money.rupees(50)));
    }

    @Test
    void multiplyScalesByQuantity() {
        assertEquals(Money.rupees(180), Money.rupees(60).multiply(3));
    }

    @Test
    void multiplyByNegativeQuantityThrows() {
        assertThrows(IllegalArgumentException.class, () -> Money.rupees(60).multiply(-1));
    }

    @Test
    void roundsToTwoDecimalPlaces() {
        Money money = Money.of(new BigDecimal("10.005"));
        assertEquals(new BigDecimal("10.01"), money.asBigDecimal());
    }

    @Test
    void zeroIsTheAdditiveIdentity() {
        assertEquals(Money.rupees(42), Money.ZERO.add(Money.rupees(42)));
    }
}
