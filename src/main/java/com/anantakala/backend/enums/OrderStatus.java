package com.anantakala.backend.enums;

public enum OrderStatus {

    PENDING,
    CONFIRMED,
    PROCESSING,
    SHIPPED,
    COMPLETED,
    CANCELLED
}

//These values exactly match the CHECK constraint in our V3 migration.
//Status	Meaning
//PENDING	Order placed, awaiting confirmation
//CONFIRMED	Order accepted
//PROCESSING	Order being prepared
//SHIPPED	Physical artwork dispatched
//COMPLETED	Order fulfilled
//CANCELLED	Order cancelled
//
//Later, we will introduce PaymentStatus separately.