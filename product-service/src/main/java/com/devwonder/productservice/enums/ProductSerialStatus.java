package com.devwonder.productservice.enums;

public enum ProductSerialStatus {
    AVAILABLE("Available"),
    SOLD("Sold"),
    DAMAGED("Damaged");

    private final String displayName;

    ProductSerialStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}