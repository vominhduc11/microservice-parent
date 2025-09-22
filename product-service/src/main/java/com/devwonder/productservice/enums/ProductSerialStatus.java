package com.devwonder.productservice.enums;

public enum ProductSerialStatus {
    AVAILABLE("Available"),
    SOLD_TO_DEALER("Sold to Dealer"),
    SOLD_TO_CUSTOMER("Sold to Customer"),
    DAMAGED("Damaged");

    private final String displayName;

    ProductSerialStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}