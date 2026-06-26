package com.m10.testkit.core;

import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.Network;


public final class Docker {

    private Docker() {
    }

    public static final Network network;
    public static final String NETWORK_NAME = "business_profile_testcontainers_run_network";

    static {
        var dockerClient = DockerClientFactory.instance().client();

        // Поиск уже существующей сети
        String existingNetworkId = dockerClient.listNetworksCmd()
            .withNameFilter(NETWORK_NAME)
            .exec()
            .stream()
            .findFirst()
            .map(com.github.dockerjava.api.model.Network::getId)
            .orElse(null);
        network =
            existingNetworkId != null
                ? new ExistingNetwork(existingNetworkId)
                : Network.builder()
                .createNetworkCmdModifier(cmd -> cmd.withName(NETWORK_NAME))
                .build();
    }

}
