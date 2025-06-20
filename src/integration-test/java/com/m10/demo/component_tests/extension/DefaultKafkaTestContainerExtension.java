package com.m10.demo.component_tests.extension;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


public class DefaultKafkaTestContainerExtension implements BeforeAllCallback, BeforeEachCallback {

    private static final KafkaContainer kafkaContainer =
            new CustomKafkaContainer(DockerImageName.parse(Images.KAFKA_IMAGE))
                    .withListener(() -> "change:29093")
                    .withReuse(true);
    private static List<String> topicsToCreate = List.of();
    private static boolean started = false;
    private static boolean topicsCreated = false;

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        if (!started) {
            kafkaContainer.start();
            System.setProperty("spring.kafka.bootstrap-servers", kafkaContainer.getBootstrapServers());
            started = true;
        }
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        resetKafkaTopicsOffsets();
    }

    public static KafkaContainer getKafkaContainer() {
        return kafkaContainer;
    }

    public static void setTopicsToCreate(List<String> topicsToCreate) {
        DefaultKafkaTestContainerExtension.topicsToCreate = topicsToCreate;
    }

    public static void createInitialTopics() {
        if (topicsCreated) {
            return;
        }
        try (AdminClient admin = AdminClient.create(Map.of(
                AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers()
        ))) {
            var currentTopics = admin.listTopics().names().get().stream()
                    .filter(t -> !t.startsWith("__"))
                    .collect(Collectors.toSet());
            List<NewTopic> topics = topicsToCreate.stream()
                    .filter(t -> !currentTopics.contains(t))
                    .map(name -> new NewTopic(name, 1, (short) 1))
                    .toList();
            admin.createTopics(topics).all().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to create Kafka topics", e);
        } finally {
            topicsCreated = true;
        }
    }

    private void resetKafkaTopicsOffsets() {
        try (AdminClient admin = AdminClient.create(Map.of(
                AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers()
        ))) {
            // Получаем список всех не-системных топиков
            Set<String> topics = admin.listTopics().names().get().stream()
                    .filter(t -> !t.startsWith("__"))
                    .collect(Collectors.toSet());

            if (!topics.isEmpty()) {
                // Получаем информацию о партициях
                Map<TopicPartition, Long> endOffsets = new HashMap<>();

                for (String topic : topics) {
                    // Получаем список партиций для топика
                    List<TopicPartitionInfo> partitions = admin.describeTopics(List.of(topic))
                            .allTopicNames()
                            .get()
                            .get(topic)
                            .partitions();

                    // Создаем группу потребителей для сброса офсетов
                    try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(
                            Map.of(
                                    ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers(),
                                    ConsumerConfig.GROUP_ID_CONFIG, "reset-group-" + UUID.randomUUID(),
                                    ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName(),
                                    ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName()
                            ))) {

                        // Получаем конечные офсеты для каждой партиции
                        List<TopicPartition> topicPartitions = partitions.stream()
                                .map(p -> new TopicPartition(topic, p.partition()))
                                .collect(Collectors.toList());

                        consumer.assign(topicPartitions);
                        Map<TopicPartition, Long> latestOffsets = consumer.endOffsets(topicPartitions);
                        endOffsets.putAll(latestOffsets);
                    }
                }

                // Сдвигаем офсеты в конец для всех групп потребителей
                admin.listConsumerGroups().all().get()
                        .forEach(group -> {
                            try {
                                admin.alterConsumerGroupOffsets(
                                        group.groupId(),
                                        endOffsets.entrySet().stream()
                                                .collect(Collectors.toMap(
                                                        Map.Entry::getKey,
                                                        e -> new OffsetAndMetadata(e.getValue())
                                                ))
                                ).all().get();
                            } catch (Exception e) {
                                // Игнорируем ошибки для несуществующих групп
                            }
                        });
            }
        } catch (Exception e) {
            throw new RuntimeException("Не удалось очистить топики Kafka", e);
        }
    }

    private void resetKafkaTopics() {
        try (AdminClient admin = AdminClient.create(Map.of(
                AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers()
        ))) {
            List<String> topicsToDelete = getNonInternalTopics(admin);
            if (!topicsToDelete.isEmpty()) {
                admin.deleteTopics(topicsToDelete).all().get();
                Awaitility.await()
                        .pollInterval(Duration.of(100, ChronoUnit.MILLIS))
                        .atMost(Duration.of(10, ChronoUnit.SECONDS))
                        .untilAsserted(() -> {
                            List<String> remainingTopics = getNonInternalTopics(admin);
                            assert remainingTopics.isEmpty() : "Не все топики были удалены";
                        });
            }
            createInitialTopics();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to reset Kafka topics", e);
        }
    }

    private List<String> getNonInternalTopics(AdminClient admin) throws ExecutionException, InterruptedException {
        return admin.listTopics().names().get().stream()
                .filter(t -> !t.startsWith("__")) // exclude internal Kafka topics
                .collect(Collectors.toList());
    }

}
