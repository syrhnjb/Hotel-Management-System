package com.abhiram.hotel.domain;

/** The hotel's fixed food menu. Replaces the original's fragile 1-4 integer item codes. */
public enum MenuItem {

    SANDWICH("Sandwich", Money.rupees(50)),
    PASTA("Pasta", Money.rupees(60)),
    NOODLES("Noodles", Money.rupees(70)),
    COKE("Coke", Money.rupees(30));

    private final String displayName;
    private final Money unitPrice;

    MenuItem(String displayName, Money unitPrice) {
        this.displayName = displayName;
        this.unitPrice = unitPrice;
    }

    public String displayName() {
        return displayName;
    }

    public Money unitPrice() {
        return unitPrice;
    }
}
