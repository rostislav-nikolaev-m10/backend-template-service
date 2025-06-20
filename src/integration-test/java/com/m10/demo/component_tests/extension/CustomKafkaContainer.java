package com.m10.demo.component_tests.extension;

import com.github.dockerjava.api.command.InspectContainerResponse;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.DockerImageName;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

public class CustomKafkaContainer extends KafkaContainer {

    public CustomKafkaContainer(final DockerImageName dockerImageName) {
        super(dockerImageName);
    }

    @Override
    protected void containerIsStarting(InspectContainerResponse containerInfo) {
        super.containerIsStarting(containerInfo);
        var content = copyFileFromContainer("/testcontainers_start.sh", (stream) -> new String(stream.readAllBytes(), StandardCharsets.UTF_8));
        var lines = content.split("\\R");
        var modifiedContent = Arrays.stream(lines).map((line) -> {
            if (line.contains("KAFKA_ADVERTISED_LISTENERS")) {
                return line.replace("change:29093", getContainerInfo().getNetworkSettings().getIpAddress() + ":29093");
            } else {
                return line;
            }
        }).collect(Collectors.joining("\n"));
        copyFileToContainer(Transferable.of(modifiedContent, 0777), "/testcontainers_start.sh");
    }

}
