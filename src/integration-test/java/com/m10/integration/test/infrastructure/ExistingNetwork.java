package com.m10.integration.test.infrastructure;

import org.junit.rules.ExternalResource;
import org.testcontainers.containers.Network;

public class ExistingNetwork extends ExternalResource implements Network {
    private final String id;

    public ExistingNetwork(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void close() {
        // ничего не делать — мы не хотим удалять существующую сеть
    }

}
