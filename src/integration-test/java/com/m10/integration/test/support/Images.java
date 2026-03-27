package com.m10.integration.test.support;

public final class Images {

    private Images() {
    }

    public static final String POSTGRES_IMAGE = "postgres:16";
    public static final String WIREMOCK_IMAGE = "holomekc/wiremock-gui:3.12.7-alpine";
    public static final String VAULT_IMAGE = "hashicorp/vault:1.13";
    public static final String KAFKA_IMAGE = "apache/kafka:4.0.0";
    public static final String KAFKA_UI_IMAGE = "kafbat/kafka-ui:7073c72";
    public static final String REDIS_IMAGE = "redis:6.2.14";

}
