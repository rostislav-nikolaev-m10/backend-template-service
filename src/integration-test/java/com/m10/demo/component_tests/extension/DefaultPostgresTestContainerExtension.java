package com.m10.demo.component_tests.extension;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.FrameConsumerResultCallback;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.output.ToStringConsumer;

import java.io.IOException;
import java.util.List;

public class DefaultPostgresTestContainerExtension implements BeforeAllCallback, AfterAllCallback, AfterEachCallback {

    private static final PostgreSQLContainer<?> postgresContainer =
            new PostgreSQLContainer<>(Images.POSTGRES_IMAGE)
                    .withDatabaseName("test")
                    .withUsername("test")
                    .withExposedPorts(5432)
                    .withPassword("test")
                    .withLabel("com.testcontainers.desktop.service", "component-tests-postgres")
                    .withReuse(true);

    private static boolean started = false;
    private static final String SNAPSHOT_FILE = "target/test-snapshot.dump";
    private static boolean snapshotCreated = false;

    @Override
    public void beforeAll(ExtensionContext context) {
        startContainer();
    }

    @Override
    public void afterAll(ExtensionContext context) {
        // Don't stop container if reuse=true; otherwise, you can stop here
        // postgresContainer.stop();
    }

    public void afterEach(ExtensionContext context) {
        if (snapshotCreated) {
            restoreSnapshot();
        }
    }

    public static PostgreSQLContainer<?> startContainer() {
        if (!started) {
            postgresContainer.start();
            started = true;
        }
        return postgresContainer;
    }

    public static void createSnapshot() {
        if (!snapshotCreated) {
            try (
                    var callback = new FrameConsumerResultCallback()
            ) {
                var dockerClient = DockerClientFactory.instance().client();
                var response = dockerClient.execCreateCmd(postgresContainer.getContainerId())
                        .withAttachStdout(true)
                        .withEnv(List.of("PGPASSWORD=\"" + postgresContainer.getPassword() + "\""))
                        .withCmd("pg_dump", "-U", postgresContainer.getUsername(), "--inserts", "--clean", "--file=test_dump.sql", postgresContainer.getDatabaseName())
                        .exec();
                var stdoutConsumer = new ToStringConsumer();
                var stderrConsumer = new ToStringConsumer();
                callback.addConsumer(OutputFrame.OutputType.STDOUT, stdoutConsumer);
                callback.addConsumer(OutputFrame.OutputType.STDERR, stderrConsumer);
                dockerClient.execStartCmd(response.getId())
                        .exec(callback)
                        .awaitCompletion();
            } catch (IOException | InterruptedException e ) {
                throw new RuntimeException(e);
            }
            snapshotCreated = true;
        }
    }

    private static void restoreSnapshot() {
        try (
                var callback = new FrameConsumerResultCallback()
        ) {
            var dockerClient = DockerClientFactory.instance().client();
            var response = dockerClient.execCreateCmd(postgresContainer.getContainerId())
                    .withAttachStdout(true)
                    .withEnv(List.of("PGPASSWORD=\"" + postgresContainer.getPassword() + "\""))
                    .withCmd("psql", "--file=test_dump.sql", postgresContainer.getDatabaseName(), postgresContainer.getUsername())
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
    }

}
