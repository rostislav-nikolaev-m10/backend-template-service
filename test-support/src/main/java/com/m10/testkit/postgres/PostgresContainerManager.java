package com.m10.testkit.postgres;

import java.io.IOException;
import java.util.List;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.FrameConsumerResultCallback;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.output.ToStringConsumer;
import org.testcontainers.utility.DockerImageName;

import com.m10.testkit.core.Docker;


public class PostgresContainerManager {

    public static final String BEAN_NAME = PostgresContainerManager.class.getName();

    private final PostgresContainerDefinition containerDefinition;

    private PostgreSQLContainer<?> container;

    private boolean snapshotCreated = false;

    public PostgresContainerManager(PostgresContainerDefinition containerDefinition) {
        this.containerDefinition = containerDefinition;
    }

    synchronized public PostgreSQLContainer<?> getContainer() {
        if (container != null) {
            return container;
        }
        container = createContainer(containerDefinition.containerName());
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
        // -Fc — custom-формат, восстанавливается через pg_restore (см. restoreSnapshot)
        execInContainer(
            "pg_dump",
            "-U", container.getUsername(),
            "-Fc",
            "--file=test_dump.pgdump",
            container.getDatabaseName()
        );
        snapshotCreated = true;
        return true;
    }

    synchronized public boolean restoreSnapshot() {
        if (container == null || !snapshotCreated) {
            return false;
        }
        // --clean --if-exists — снести объекты из дампа перед восстановлением (повторяемость между тестами)
        execInContainer(
            "pg_restore",
            "--clean", "--if-exists",
            "-U", container.getUsername(),
            "-d", container.getDatabaseName(),
            "test_dump.pgdump"
        );
        return true;
    }

    /**
     * Выполняет команду в контейнере и падает с исключением, если её exit code != 0.
     * Без этого ошибки pg_dump/pg_restore проходили незаметно: awaitCompletion()
     * не проверяет код возврата, а stderr раньше даже не был приаттачен.
     */
    private void execInContainer(String... cmd) {
        try (var dockerClient = DockerClientFactory.instance().client()) {
            try (var callback = new FrameConsumerResultCallback()) {
                var execId = dockerClient.execCreateCmd(container.getContainerId())
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .withEnv(List.of("PGPASSWORD=" + container.getPassword()))
                    .withCmd(cmd)
                    .exec()
                    .getId();
                var stdout = new ToStringConsumer();
                var stderr = new ToStringConsumer();
                callback.addConsumer(OutputFrame.OutputType.STDOUT, stdout);
                callback.addConsumer(OutputFrame.OutputType.STDERR, stderr);
                dockerClient.execStartCmd(execId)
                    .exec(callback)
                    .awaitCompletion();

                Long exitCode = dockerClient.inspectExecCmd(execId).exec().getExitCodeLong();
                if (exitCode == null || exitCode != 0) {
                    throw new IllegalStateException(
                        cmd[0] + " failed in container (exit=" + exitCode + "):\n" + stderr.toUtf8String()
                    );
                }
            } catch (IOException e) {
                throw new RuntimeException(cmd[0] + " execution failed", e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(cmd[0] + " execution was interrupted", e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
