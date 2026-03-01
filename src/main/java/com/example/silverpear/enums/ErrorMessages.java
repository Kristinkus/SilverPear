package com.example.silverpear.enums;

public enum ErrorMessages {
    PRODUCT_NOT_FOUND("Product not found with id: "),
    COSMETICS_NOT_FOUND("Cosmetics not found with id: "),
    PERFUME_NOT_FOUND("Perfume not found with id: "),
    USER_NOT_FOUND("User not found with id: "),
    ORDER_NOT_FOUND("Order not found with id: ");

    private final String message;

    ErrorMessages(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public String withId(Long id) {
        return message + id;
    }
}