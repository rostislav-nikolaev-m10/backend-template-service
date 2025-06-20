package com.m10.demo.component_tests.extension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import lombok.SneakyThrows;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import com.m10.demo.component_tests.authorization.KeyPairHolder;

import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import static com.m10.demo.component_tests.extension.Images.WIREMOCK_IMAGE;

public class DefaultWireMockTestContainerExtension implements BeforeAllCallback, BeforeEachCallback {

    public static final int WIREMOCK_PORT = 8080;

    private static final GenericContainer<?> wireMockContainer =
            new GenericContainer<>(
                    DockerImageName.parse(WIREMOCK_IMAGE)
            )
                    .withExposedPorts(WIREMOCK_PORT)
                    .withLabel("com.testcontainers.desktop.service", "component-tests-wiremock")
                    .withReuse(true);

    private static WireMock wireMockClient;
    private static boolean started = false;

    private static final KeyPairHolder keyPairHolder = generateKeyPair();
    private static final String defaultUrl =
            "/realms/default-test-realm/protocol/openid-connect/certs";
    private static final String jwksJson = generateJwksJson();

    @Override
    public void beforeAll(ExtensionContext context) {
        startContainer();
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        wireMockClient.resetMappings();
        mockJWKS();
    }

    private void mockJWKS() {
        wireMockClient.register(
                WireMock.get(
                                urlEqualTo(defaultUrl)
                        )
                        .willReturn(
                                okJson(jwksJson)
                        )
        );
    }

    public static GenericContainer<?> startContainer() {
        if (!started) {
            wireMockContainer.start();
            wireMockClient = new WireMock(wireMockContainer.getHost(), wireMockContainer.getMappedPort(WIREMOCK_PORT));
            started = true;
        }
        return wireMockContainer;
    }

    public static WireMock wireMockClient() {
        if (!started) {
            startContainer();
        }
        return wireMockClient;
    }

    public static RSAPrivateKey getPrivateKey() {
        return keyPairHolder.privateKey();
    }

    @SneakyThrows
    private static KeyPairHolder generateKeyPair() {
        var keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        return new KeyPairHolder(keyPairGenerator.generateKeyPair());
    }

    @SneakyThrows
    private static String generateJwksJson() {
        var jwk = new RSAKey.Builder(keyPairHolder.publicKey())
                .keyID("test-key-id")
                .algorithm(JWSAlgorithm.RS256)
                .build();
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(new JWKSet(jwk).toJSONObject());
    }

}
