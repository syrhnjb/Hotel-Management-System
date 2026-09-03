package com.abhiram.hotel.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** The full set of food line items ordered for a single {@link Booking}. */
public final class FoodOrder {

    private final List<FoodOrderLine> lines = new ArrayList<>();

    public void addLine(FoodOrderLine line) {
        lines.add(line);
    }

    public List<FoodOrderLine> lines() {
        return Collections.unmodifiableList(lines);
    }

    public Money total() {
        Money total = Money.ZERO;
        for (FoodOrderLine line : lines) {
            total = total.add(line.lineTotal());
        }
        return total;
    }
}
