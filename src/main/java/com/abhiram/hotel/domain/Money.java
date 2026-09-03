package com.abhiram.hotel.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Immutable monetary value in Indian Rupees.
 *
 * <p>Wrapping {@link BigDecimal} (instead of {@code double}/{@code float}) avoids the binary
 * floating-point rounding errors that make {@code double} unsuitable for financial arithmetic.
 */
public final class Money implements Comparable<Money> {

    public static final Money ZERO = new Money(BigDecimal.ZERO);

    private final BigDecimal amount;

    private Money(BigDecimal amount) {
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public static Money rupees(long amount) {
        return new Money(BigDecimal.valueOf(amount));
    }

    public static Money of(BigDecimal amount) {
        Objects.requireNonNull(amount, "amount must not be null");
        return new Money(amount);
    }

    public Money add(Money other) {
        return new Money(this.amount.add(other.amount));
    }

    public Money multiply(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("quantity must not be negative");
        }
        return new Money(this.amount.multiply(BigDecimal.valueOf(quantity)));
    }

    public BigDecimal asBigDecimal() {
        return amount;
    }

    @Override
    public int compareTo(Money other) {
        return this.amount.compareTo(other.amount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money)) return false;
        return amount.compareTo(((Money) o).amount) == 0;
    }

    @Override
    public int hashCode() {
        return amount.stripTrailingZeros().hashCode();
    }

    @Override
    public String toString() {
        return "\u20B9" + amount.toPlainString();
    }
}
