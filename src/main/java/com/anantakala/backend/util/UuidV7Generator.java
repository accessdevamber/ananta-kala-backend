package com.anantakala.backend.util;

import com.github.f4b6a3.uuid.UuidCreator;

import java.util.UUID;

public final class UuidV7Generator {

    private UuidV7Generator() {
        // Utility class: prevent instantiation
    }

    public static UUID generate() {
        return UuidCreator.getTimeOrderedEpoch();//UuidCreator.getTimeOrderedEpoch() generates a UUIDv7 value.
    }
}
