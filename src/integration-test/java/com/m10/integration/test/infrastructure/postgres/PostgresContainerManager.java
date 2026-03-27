package com.m10.integration.test.infrastructure.postgres;

import java.io.IOException;
import java.util.List;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.FrameConsumerResultCallback;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.output.ToStringConsumer;
import org.testcontainers.utility.DockerImageName;

import com.m10.integration.test.infrastructure.Docker;


public class PostgresContainerManager {

    public static final String BEAN_NAME = PostgresContainerManager.class.getName();

    private PostgreSQLContainer<?> container;

    private boolean snapshotCreated = false;

    synchronized public PostgreSQLContainer<?> getContainer(String imageName) {
        if (container != null) {
            return container;
        }
        container = createContainer(imageName);
        container.start();
        return container;
    }

    synchronized public boolean createSnapshot() {
        if (snapshotCreated) {
            return true;
        }
        if (container == null) {
            return false;
        }
        try (
            var callback = new FrameConsumerResultCallback()
        ) {
            var dockerClient = DockerClientFactory.instance().client();
            var response = dockerClient.execCreateCmd(container.getContainerId())
                .withAttachStdout(true)
                .withEnv(List.of("PGPASSWORD=\"" + container.getPassword() + "\""))
                .withCmd(
                    "pg_dump",
                    "-U",
                    container.getUsername(),
                    "--inserts",
                    "--clean",
                    "--file=test_dump.sql",
                    container.getDatabaseName()
                )
                .exec();
            var stdoutConsumer = new ToStringConsumer();
            var stderrConsumer = new ToStringConsumer();
            callback.addConsumer(OutputFrame.OutputType.STDOUT, stdoutConsumer);
            callback.addConsumer(OutputFrame.OutputType.STDERR, stderrConsumer);
            dockerClient.execStartCmd(response.getId())
                .exec(callback)
                .awaitCompletion();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        snapshotCreated = true;
        return true;
    }

    synchronized public boolean restoreSnapshot() {
        if (container == null || !snapshotCreated) {
            return false;
        }
        try (
            var callback = new FrameConsumerResultCallback()
        ) {
            var dockerClient = DockerClientFactory.instance().client();
            var response = dockerClient.execCreateCmd(container.getContainerId())
                .withAttachStdout(true)
                .withEnv(List.of("PGPASSWORD=\"" + container.getPassword() + "\""))
                .withCmd("psql", "--file=test_dump.sql", container.getDatabaseName(), container.getUsername())
                .exec();
            var stdoutConsumer = new ToStringConsumer();
            var stderrConsumer = new ToStringConsumer();
            callback.addConsumer(OutputFrame.OutputType.STDOUT, stdoutConsumer);
            callback.addConsumer(OutputFrame.OutputType.STDERR, stderrConsumer);
            dockerClient.execStartCmd(response.getId())
                .exec(callback)
                .awaitCompletion();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    private PostgreSQLContainer<?> createContainer(String imageName) {
        return new PostgreSQLContainer<>(DockerImageName.parse(imageName))
            .withDatabaseName("test")
            .withUsername("test")
            .withExposedPorts(5432)
            .withPassword("test")
            .withLabel("com.testcontainers.desktop.service", "component-tests-postgres")
            .withNetwork(Docker.network)
            .withReuse(true);
    }

}
