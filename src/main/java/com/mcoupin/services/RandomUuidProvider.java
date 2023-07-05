package com.mcoupin.services;

import java.util.UUID;

public class RandomUuidProvider implements UuidProvider {
    @Override
    public UUID generate() {
        return UUID.randomUUID();
    }
}
