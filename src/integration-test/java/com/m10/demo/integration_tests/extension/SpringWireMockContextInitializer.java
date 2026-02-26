package com.m10.demo.integration_tests.extension;

public class SpringWireMockContextInitializer {
    private SpringWireMockContextInitializer() {
    }

    //    @Override
//    public void initialize(ConfigurableApplicationContext ctx) {
//        var wiremockContainer = DefaultWireMockTestContainerExtension.startContainer();
//        var env = ctx.getEnvironment();
//        var host = wiremockContainer.getHost();
//        var port = wiremockContainer.getMappedPort(DefaultWireMockTestContainerExtension.WIREMOCK_PORT);
//        var wireMockBaseUrl = "http://" + host + ":" + port;
//        var defaultUrl =
//                "/realms/default-test-realm/protocol/openid-connect/certs";
//        var customerCareUrl =
//                "/realms/customer-care-test-realm/protocol/openid-connect/certs";
//        env.getPropertySources().addFirst(
//                new MapPropertySource(
//                        "wireMockContainerProperties",
//                        Map.of(
//                                "spring.security.oauth2.resourceserver.default.issuer-uri", wireMockBaseUrl + defaultUrl,
//                                "spring.security.oauth2.resourceserver.customer-care.issuer-uri", wireMockBaseUrl + customerCareUrl,
//                                "wiremock.server.host", host,
//                                "wiremock.server.port", port
//                        )
//                )
//        );
//    }

}
