package com.example.delivery.order.entity;

public enum OrderStatus {
    REQUESTED, ACCEPTED, COOKING, DELIVERING, DELIVERED, COMPLETED, CANCELED;

    public boolean canChangeTo(OrderStatus nextStatus) {
        return switch (this) {
            case REQUESTED ->
                nextStatus == ACCEPTED || nextStatus == CANCELED;

            case ACCEPTED ->
                nextStatus == COOKING || nextStatus == CANCELED;

            case COOKING ->
                nextStatus == DELIVERING;

            case DELIVERING ->
                nextStatus == DELIVERED;

            case DELIVERED ->
                nextStatus == COMPLETED;

            case COMPLETED, CANCELED ->
                false;
        };
    }
}
