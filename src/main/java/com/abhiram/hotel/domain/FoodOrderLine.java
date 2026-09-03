package com.abhiram.hotel.domain;

import com.abhiram.hotel.exception.InvalidFoodOrderException;

/** A single line item within a {@link FoodOrder}: one menu item and its quantity. */
public final class FoodOrderLine {

    private static final int MAX_QUANTITY = 100;

    private final MenuItem menuItem;
    private final int quantity;

    public FoodOrderLine(MenuItem menuItem, int quantity) {
        if (menuItem == null) {
            throw new InvalidFoodOrderException("Menu item must not be null");
        }
        if (quantity <= 0) {
            throw new InvalidFoodOrderException("Quantity must be positive, got: " + quantity);
        }
        if (quantity > MAX_QUANTITY) {
            throw new InvalidFoodOrderException(
                    "Quantity " + quantity + " exceeds the maximum of " + MAX_QUANTITY + " per line item");
        }
        this.menuItem = menuItem;
        this.quantity = quantity;
    }

    public MenuItem menuItem() {
        return menuItem;
    }

    public int quantity() {
        return quantity;
    }

    public Money lineTotal() {
        return menuItem.unitPrice().multiply(quantity);
    }
}
